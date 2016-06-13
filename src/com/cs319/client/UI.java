package com.cs319.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import com.cs319.server.Connection;
import com.cs319.server.othello.Game;
import com.cs319.server.othello.GameBoard;
import com.cs319.server.othello.Message;
import com.cs319.server.othello.Status;
import com.cs319.server.packets.Packet;
import com.cs319.server.packets.Request;

public class UI extends JPanel {
	private static final long serialVersionUID = 1L;
	private static UI ui;
	public static final int WIDTH = 862, HEIGHT = 602;
	private static Connection connection;
	private static final Object waitObject = new Object();
	private static final Image blackImage = loadImage("black piece.png");
	private static final Image whiteImage = loadImage("white piece.png");
	private static FontMetrics metric;

	private static JTextArea chat;

	public static Image getBlackImage() {
		return blackImage;
	}

	public static Connection getConnection() {
		return connection;
	}

	public static Image getWhiteImage() {
		return whiteImage;
	}

	//reads an image from a file and returns it
	private static BufferedImage loadImage(String fileName) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(fileName));
		} catch (IOException e) {
			System.err.println("Could not load image: " + fileName);
		}
		return img;
	}

	//entry point for client side
	public static void main(String[] args) throws UnknownHostException, IOException {
		JFrame frame = new JFrame("Othello");
		ui = new UI();
		frame.setContentPane(ui);
		frame.setResizable(false);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.requestFocus();
	}

	//splits strings so that they do not go past the width of the text/chat area
	private static String[] splitMessage(String message) {
		char[] chars = message.toCharArray();
		StringBuffer buffer = new StringBuffer();
		List<String> strings = new ArrayList<>();
		for (int i = 0; i < chars.length; i++) {
			if (metric.stringWidth(buffer.toString()) < chat.getWidth() - 10) {
				buffer.append(chars[i]);
				continue;
			}
			if (strings.size() == 0) {
				strings.add(buffer.toString() + "-");
			}
			buffer = new StringBuffer();
			buffer.append(chars[i]);
		}
		if (buffer.length() > 0) {
			strings.add(buffer.toString());
		}
		return strings.toArray(new String[strings.size()]);
	}
	private JLabel turnLabel;

	private JLabel whiteScore;

	private JLabel blackScore;

	private BoardComponent boardComponent = new BoardComponent();

	private JLabel colorID;

	private byte color = 0; // 0 = no color, then Game.PLAYER_BLACK, Game.PLAYER_WHITE

	private JScrollPane scrollPane;

	//this thread receives the packets and interprets them in a switch statement
	private final Thread connectionHandler = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				int count = 0;
				while (connection.hasPacket()) {
					count++;
					Packet packet = connection.getPacket();
					switch (packet.getIdentifierByte()) {
					case 1: // Message
						for (String string : splitMessage(((Message) packet).getMessage())) {
							chat.append(string + "\n");
						}
						chat.setCaretPosition(chat.getText().length());
						break;
					case 2: // Move (Shouldn't occur on client)
					case 5: // Request (Shouldn't occur on client.)
						System.err.println("Shouldn't be getting packet with type: " + packet.getIdentifierByte());
						break;
					case 3: // GameBoard
						GameBoard newBoard = (GameBoard) packet;
						for (int x = 0; x < 8; x++) {
							for (int y = 0; y < 8; y++) {
								boardComponent.setPieceState(x, y, newBoard.get(x, y));
								if (newBoard.validMove(x, y, newBoard.getCurrentPlayer())
										&& newBoard.getCurrentPlayer() == color) {
									boardComponent.setPieceState(x, y, (byte) (newBoard.getCurrentPlayer() << 2));
								}
							}
						}
						blackScore.setText("Black: " + newBoard.getPieceCountForPlayer(Game.PLAYER_BLACK));
						whiteScore.setText("White: " + newBoard.getPieceCountForPlayer(Game.PLAYER_WHITE));
						break;
					case 4: // Status
						Status status = (Status) packet;
						switch (status.getStatus()) {
						case Status.BLACK_TURN:
							if (color == Game.PLAYER_BLACK) {
								turnLabel.setText("It's your turn, Black.");
							} else {
								turnLabel.setText("It's Black's turn.");
							}
							break;
						case Status.BLACK_VICTORY:
							if (color == Game.PLAYER_BLACK) {
								turnLabel.setText("Game over, you won!");
								chat.append("Game over, you won!\n");
							} else {
								turnLabel.setText("Game over, Black won.");
								chat.append("Game over, Black won.\n");
							}
							break;
						case Status.WHITE_TURN:
							if (color == Game.PLAYER_WHITE) {
								turnLabel.setText("It's your turn, White.");
							} else {
								turnLabel.setText("It's White's turn.");
							}
							break;
						case Status.WHITE_VICTORY:
							if (color == Game.PLAYER_WHITE) {
								turnLabel.setText("Game over, you won!");
								chat.append("Game over, you won!\n");
							} else {
								turnLabel.setText("Game over, White won.");
								chat.append("Game over, White won.\n");
							}
							break;
						case Status.DRAW:
							turnLabel.setText("Game over, Draw.");
							break;
						case Status.YOURE_BLACK:
							color = Game.PLAYER_BLACK;
							colorID.setText("You're Black");
							break;
						case Status.YOURE_WHITE:
							color = Game.PLAYER_WHITE;
							colorID.setText("You're White");
							break;
						case Status.NEW_GAME:
							chat.setText("Found new game.\n");
							break;
						case Status.GAME_CLOSED:
							for (int x = 0; x < 8; x++) {
								for (int y = 0; y < 8; y++) {
									boardComponent.setPieceState(x, y, (byte) 0);
								}
							}
							turnLabel.setText("Game ended, player left");
							blackScore.setText("Black: 0");
							whiteScore.setText("White: 0");
							colorID.setText("");
							break;
						}
						break;
					default:
						throw new RuntimeException("No case for type: " + packet.getIdentifierByte());
					}
				}
				if (count != 0) {
					ui.repaint();
				}
				synchronized (waitObject) {
					waitObject.notifyAll();
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
		}

	});

	public UI() throws UnknownHostException, IOException {
		chat = new JTextArea();
		chat.setEditable(false);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setLayout(new BorderLayout());
		add(setupStatusBar(), BorderLayout.SOUTH);
		add(setupSideBoxes(), BorderLayout.EAST);
		add(boardComponent, BorderLayout.CENTER);
		try {
			connection = new Connection(new Socket("localhost", 52413), waitObject);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(null, "Error: Failed to connect to the server.");
			System.exit(1);
		}
		connectionHandler.start();
	}

	@Override
	public void paint(Graphics g) {
		metric = g.getFontMetrics();
		Graphics2D graphics = (Graphics2D) g;
		//for pretty edges
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g);
	}

	//swing construction helper method
	private JPanel setupSideBoxes() {
		int sideWidth = 260;
		JPanel chatPanel = new JPanel();
		chatPanel.setBackground(Color.ORANGE);
		chatPanel.setMaximumSize(new Dimension(260, 900));
		chatPanel.setLayout(new BorderLayout());
		JTextField textField = new JTextField();
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.sendPacket(new Message(textField.getText()));
				textField.setText("");
			}
		});
		chatPanel.add(textField, BorderLayout.SOUTH);
		scrollPane = new JScrollPane(chat);
		chatPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		menuPanel.add(Box.createRigidArea(new Dimension(sideWidth, 10)));

		JButton findPartner = new JButton("Find Partner");
		findPartner.setAlignmentX(Component.CENTER_ALIGNMENT);
		findPartner.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.sendPacket(new Request(Request.REQUEST_GAME_SEARCH));
			}
		});
		menuPanel.add(findPartner);
		menuPanel.add(Box.createRigidArea(new Dimension(sideWidth, 10)));

		JButton leaveGame = new JButton("Leave Game");
		leaveGame.setAlignmentX(Component.CENTER_ALIGNMENT);
		leaveGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connection.sendPacket(new Request(Request.REQUEST_LEAVE_GAME));
			}
		});
		menuPanel.add(leaveGame);
		menuPanel.add(Box.createRigidArea(new Dimension(sideWidth, 10)));

		JPanel sidePanel = new JPanel();
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		sidePanel.add(menuPanel);
		sidePanel.add(chatPanel);
		sidePanel.setMaximumSize(sidePanel.getPreferredSize());
		return sidePanel;
	}

	//swing construction helper methode
	private JPanel setupStatusBar() {
		JPanel bar = new JPanel();
		bar.setLayout(new GridLayout(1, 3));

		turnLabel = new JLabel("");
		bar.add(turnLabel);
		JPanel countPanel = new JPanel();
		countPanel.setLayout(new GridLayout(1, 2));
		whiteScore = new JLabel("White: 2");
		countPanel.add(whiteScore);

		blackScore = new JLabel("Black: 2");
		countPanel.add(blackScore);
		bar.add(countPanel);

		colorID = new JLabel(""); // Not known until join game
		bar.add(colorID);

		return bar;
	}
}
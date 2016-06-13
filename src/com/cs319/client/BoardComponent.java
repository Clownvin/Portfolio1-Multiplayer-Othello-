package com.cs319.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.cs319.server.othello.Move;

public class BoardComponent extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final PieceComponent[][] pieces = new PieceComponent[8][8];

	public BoardComponent() {
		setLayout(new GridLayout(8, 8));
		for (int y = 0; y < 8; y++) {
			for (int x = 0; x < 8; x++) {
				pieces[y][x] = new PieceComponent(); // Automatically starts with state 0 ( nothing in slot )
				add(pieces[y][x]);
			}
		}
		setMinimumSize(new Dimension(508, 508)); // Adding 8 for the gaps between
		setBackground(new Color(34, 177, 76));
		setForeground(new Color((int) (34 * .75), (int) (177 * .75), (int) (76 * .75)));
		//sends a packet to the connection with the click coordinates upon a mouse click 
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				int clickX = e.getX() / (getWidth() / 8);
				int clickY = e.getY() / (getHeight() / 8);
				if (clickX < 8 && clickX >= 0 && clickY < 8 && clickY >= 0) {
					UI.getConnection().sendPacket(new Move(clickX, clickY));
				}
			}
		});
	}

	//draws lines on game board
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(getForeground());
		for (int x = 0; x < getWidth(); x += getWidth() / 8) {
			g.drawLine(x, 0, x, getHeight());
			g.drawLine(x - 1, 0, x - 1, getHeight());
			g.drawLine(x + 1, 0, x + 1, getHeight());
		}
		for (int y = 0; y < getHeight(); y += getHeight() / 8) {
			g.drawLine(0, y, getWidth(), y);
			g.drawLine(0, y - 1, getWidth(), y - 1);
			g.drawLine(0, y + 1, getWidth(), y + 1);
		}
	}

	public void setPieceState(int x, int y, byte state) {
		pieces[y][x].setState(state);
		pieces[y][x].handleFlip();
	}
}
package com.cs319.server.othello;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.cs319.server.Connection;

public final class Game {
	//Player states
	public static final byte PLAYER_WHITE = 1;
	public static final byte PLAYER_BLACK = 2;
	//Absolute static final messages in packet objects
	private static final Message NOT_YOUR_TURN_PACKET = new Message("It's not your turn yet!");
	private static final Message NOT_VALID_MOVE_PACKET = new Message("That's not a valid move!");
	private static final Message NEW_GAME_PACKET = new Message("Starting new game.");
	private static final Message GAME_CLOSED_PACKET = new Message("Game has closed because a player left.");
	//Both connections (players)
	private final Connection player1, player2;
	
	private volatile boolean playerLeft = false;
	//Player 1's color (Decided at newGame)
	private byte p1Color;
	//Current player (color)
	private byte currentPlayer;

	private GameBoard gameBoard = new GameBoard();
	private Timer timer = new Timer(5000, new ActionListener() { // Lol @ using swing timer to restart games.

		@Override
		public void actionPerformed(ActionEvent arg0) {
			timer.stop();
			if (!finished()) {
				newGame();
				player1.sendPacket(NEW_GAME_PACKET);
				player2.sendPacket(NEW_GAME_PACKET);
			}
		}

	});

	public Game(final Connection player1, final Connection player2) {
		this.player1 = player1;
		this.player2 = player2;
		player1.setStatus(Connection.IN_GAME);
		player2.setStatus(Connection.IN_GAME);
		player1.sendPacket(new Status(Status.NEW_GAME));
		player2.sendPacket(new Status(Status.NEW_GAME));
		newGame();
	}

	public void end() {
		player1.setStatus(Connection.IDLE);
		player2.setStatus(Connection.IDLE);
		player1.sendPacket(GAME_CLOSED_PACKET);
		player2.sendPacket(GAME_CLOSED_PACKET);
		player1.sendPacket(new Status(Status.GAME_CLOSED));
		player2.sendPacket(new Status(Status.GAME_CLOSED));
	}

	public boolean finished() {
		return playerLeft;
	}

	private Connection getCurrentPlayer() {
		if (currentPlayer == p1Color) {
			return player1;
		}
		return player2;
	}

	public void handleMove(final Move move, final Connection connection) {
		if (gameBoard.gameOver()) {
			connection.sendPacket(new Message("Game is already over."));
		}
		Connection cPlayer = getCurrentPlayer();
		if (!cPlayer.equals(connection)) {
			connection.sendPacket(NOT_YOUR_TURN_PACKET);
			return;
		}
		if (gameBoard.place(move.getMoveX(), move.getMoveY(), currentPlayer)) {
			if (gameBoard.gameOver()) {
				Status endGameStatus;
				if (gameBoard.getWinner() == PLAYER_BLACK) {
					endGameStatus = new Status(Status.BLACK_VICTORY);
				} else if (gameBoard.getWinner() == PLAYER_WHITE) {
					endGameStatus = new Status(Status.WHITE_VICTORY);
				} else {
					endGameStatus = new Status(Status.DRAW);
				}
				player1.sendPacket(endGameStatus);
				player2.sendPacket(endGameStatus);
				timer.start();
			} else if (gameBoard.playerHasMove(PLAYER_WHITE)
					&& (currentPlayer == PLAYER_BLACK || !gameBoard.playerHasMove(PLAYER_BLACK))) { // Now white turn
				currentPlayer = PLAYER_WHITE;
				player1.sendPacket(new Status(Status.WHITE_TURN));
				player2.sendPacket(new Status(Status.WHITE_TURN));
			} else { // Now Black turn
				currentPlayer = PLAYER_BLACK;
				player1.sendPacket(new Status(Status.BLACK_TURN));
				player2.sendPacket(new Status(Status.BLACK_TURN));
			}
			gameBoard.setCurrentPlayer(currentPlayer);
			player1.sendPacket(gameBoard);
			player2.sendPacket(gameBoard);
		} else {
			cPlayer.sendPacket(NOT_VALID_MOVE_PACKET);
		}
	}

	public boolean isPlayer(final Connection connection) {
		return connection.equals(player1) || connection.equals(player2);
	}

	public void newGame() {
		gameBoard.reset();
		if (Math.random() > 0.5) {
			p1Color = PLAYER_BLACK;
			player1.sendPacket(new Message("You are player Black, and will move first."));
			player1.sendPacket(new Status(Status.YOURE_BLACK));
			player2.sendPacket(new Message("You are player White, and will move second."));
			player2.sendPacket(new Status(Status.YOURE_WHITE));
		} else {
			p1Color = PLAYER_WHITE;
			player1.sendPacket(new Message("You are player White, and will move second."));
			player1.sendPacket(new Status(Status.YOURE_WHITE));
			player2.sendPacket(new Message("You are player Black, and will move first."));
			player2.sendPacket(new Status(Status.YOURE_BLACK));
		}
		player1.sendPacket(gameBoard);
		player2.sendPacket(gameBoard);
		currentPlayer = PLAYER_BLACK;
	}

	public boolean playerLeft() {
		return playerLeft;
	}

	public void sendMessage(Message message, Connection connection) {
		String player;
		if (connection.equals(player1)) {
			if (p1Color == PLAYER_WHITE) {
				player = "White: ";
			} else {
				player = "Black: ";
			}
		} else {
			if (p1Color == PLAYER_WHITE) {
				player = "Black: ";
			} else {
				player = "White: ";
			}
		}
		message.setMessage(player + message.getMessage());
		player1.sendPacket(message);
		player2.sendPacket(message);
	}

	public void setPlayerLeft(boolean value) {
		playerLeft = value;
		if (playerLeft) {
			GameMaster.clearFinished();
		}
	}
}

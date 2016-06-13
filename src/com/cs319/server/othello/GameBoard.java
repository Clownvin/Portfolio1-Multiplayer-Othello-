package com.cs319.server.othello;

import com.cs319.server.packets.Packet;

public final class GameBoard implements Packet {

	//2D array of bytes representing board. Default configuration.
	private static final byte[][] DEFAULT_GAMEBOARD = new byte[][] { { 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, Game.PLAYER_BLACK, Game.PLAYER_WHITE, 0, 0, 0 },
			{ 0, 0, 0, Game.PLAYER_WHITE, Game.PLAYER_BLACK, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 0, 0 }, };

	//Same as above
	private byte[][] gameboard = new byte[8][8];
	private byte currentPlayer = Game.PLAYER_BLACK;
	
	public GameBoard() {
		reset();
	}

	public GameBoard(final byte[] bytes) {
		int index = 1;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				gameboard[i][j] = bytes[index++];
			}
		}
		currentPlayer = bytes[index++];
	}

	private GameBoard(final byte[][] gameboard) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				this.gameboard[x][y] = gameboard[x][y];
			}
		}
	}

	@Override
	public Object clone() {
		return new GameBoard(gameboard);
	}

	public boolean gameOver() {
		return !playerHasMove(Game.PLAYER_BLACK) && !playerHasMove(Game.PLAYER_WHITE);
	}

	public byte get(int x, int y) {
		if (x > 7 || y > 7 || x < 0 || y < 0)
			throw new IllegalArgumentException("x and y must be within the doman of all real numbers [0, 7]");
		return gameboard[x][y];
	}

	public byte getCurrentPlayer() {
		return currentPlayer;
	}

	public byte getIdentifierByte() {
		return 3;
	}

	public int getPieceCountForPlayer(byte player) {
		int pieces = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (gameboard[x][y] == player) {
					pieces++;
				}
			}
		}
		return pieces;
	}

	public byte getWinner() {
		int blackCount = 0, whiteCount = 0;
		for (byte[] row : gameboard)
			for (byte b : row) {
				if (b == Game.PLAYER_BLACK)
					blackCount++;
				else if (b == Game.PLAYER_WHITE)
					whiteCount++;
			}
		if (whiteCount == blackCount)
			return 0;
		return whiteCount > blackCount ? Game.PLAYER_WHITE : Game.PLAYER_BLACK;
	}

	public boolean place(int x, int y, byte player) {
		if (x > 7 || y > 7 || x < 0 || y < 0)
			throw new IllegalArgumentException("x and y must be within the doman of all real numbers [0, 7]");
		if (gameboard[x][y] != 0)
			return false;
		boolean valid = false;
		for (int x2 = x + 1, y2 = y + 1; x2 < 8 && y2 < 8; x2++, y2++) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x + 1) {
				for (; x2 > x && y2 > y; x2--, y2--) {
					gameboard[x2][y2] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x2][y2] == player && x2 == x + 1)
				break;
		}
		for (int x2 = x + 1; x2 < 8; x2++) {
			if (gameboard[x2][y] == 0)
				break;
			if (gameboard[x2][y] == player && x2 != x + 1) {
				for (; x2 > x; x2--) {
					gameboard[x2][y] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x2][y] == player && x2 == x + 1)
				break;
		}
		for (int x2 = x + 1, y2 = y - 1; x2 < 8 && y2 > -1; x2++, y2--) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x + 1) {
				for (; x2 > x && y2 < y; x2--, y2++) {
					gameboard[x2][y2] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x2][y2] == player && x2 == x + 1)
				break;
		}
		for (int y2 = y - 1; y2 > -1; y2--) {
			if (gameboard[x][y2] == 0)
				break;
			if (gameboard[x][y2] == player && y2 != y - 1) {
				for (; y2 < y; y2++) {
					gameboard[x][y2] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x][y2] == player && y2 == y - 1)
				break;
		}
		for (int y2 = y + 1; y2 < 8; y2++) {
			if (gameboard[x][y2] == 0)
				break;
			if (gameboard[x][y2] == player && y2 != y + 1) {
				for (; y2 > y; y2--) {
					gameboard[x][y2] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x][y2] == player && y2 == y + 1)
				break;
		}
		for (int x2 = x - 1; x2 > -1; x2--) {
			if (gameboard[x2][y] == 0)
				break;
			if (gameboard[x2][y] == player && x2 != x - 1) {
				for (; x2 < x; x2++) {
					gameboard[x2][y] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x2][y] == player && x2 == x - 1)
				break;
		}
		for (int x2 = x - 1, y2 = y + 1; x2 > -1 && y2 < 8; x2--, y2++) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x - 1) {
				for (; x2 < x && y2 > y; x2++, y2--) {
					gameboard[x2][y2] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x2][y2] == player && x2 == x - 1)
				break;
		}
		for (int x2 = x - 1, y2 = y - 1; x2 > -1 && y2 > -1; x2--, y2--) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x - 1) {
				for (; x2 < x && y2 < y; x2++, y2++) {
					gameboard[x2][y2] = player;
				}
				valid = true;
				break;
			} else if (gameboard[x2][y2] == player && x2 == x - 1)
				break;
		}
		if (valid) {
			gameboard[x][y] = player;
		}
		return valid;
	}

	public boolean playerHasMove(byte player) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (validMove(x, y, player)) {
					return true;
				}
			}
		}
		return false;
	}

	public void reset() {
		currentPlayer = Game.PLAYER_BLACK;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				gameboard[x][y] = DEFAULT_GAMEBOARD[x][y];
			}
		}
	}

	public void setCurrentPlayer(byte currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	public byte[] toBytes() {
		byte[] bytes = new byte[66];
		int index = 0;
		bytes[index++] = getIdentifierByte();
		for (int x = 0; x < 8; x++) {
			for (byte b : gameboard[x]) {
				bytes[index++] = b;
			}
		}
		bytes[index++] = currentPlayer;
		return bytes;
	}

	@Override
	public String toString() {
		return "Board";
	}

	public boolean validMove(int x, int y, byte player) {
		if (x > 7 || y > 7 || x < 0 || y < 0)
			throw new IllegalArgumentException("x and y must be within the doman of all real numbers [0, 7]");
		if (gameboard[x][y] != 0)
			return false;
		for (int x2 = x + 1, y2 = y + 1; x2 < 8 && y2 < 8; x2++, y2++) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x + 1)
				return true;
			else if (gameboard[x2][y2] == player && x2 == x + 1)
				break;
		}
		for (int x2 = x + 1; x2 < 8; x2++) {
			if (gameboard[x2][y] == 0)
				break;
			if (gameboard[x2][y] == player && x2 != x + 1)
				return true;
			else if (gameboard[x2][y] == player && x2 == x + 1)
				break;
		}
		for (int x2 = x + 1, y2 = y - 1; x2 < 8 && y2 > -1; x2++, y2--) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x + 1)
				return true;
			else if (gameboard[x2][y2] == player && x2 == x + 1)
				break;
		}
		for (int y2 = y - 1; y2 > -1; y2--) {
			if (gameboard[x][y2] == 0)
				break;
			if (gameboard[x][y2] == player && y2 != y - 1)
				return true;
			else if (gameboard[x][y2] == player && y2 == y - 1)
				break;
		}
		for (int y2 = y + 1; y2 < 8; y2++) {
			if (gameboard[x][y2] == 0)
				break;
			if (gameboard[x][y2] == player && y2 != y + 1)
				return true;
			else if (gameboard[x][y2] == player && y2 == y + 1)
				break;
		}
		for (int x2 = x - 1; x2 > -1; x2--) {
			if (gameboard[x2][y] == 0)
				break;
			if (gameboard[x2][y] == player && x2 != x - 1)
				return true;
			else if (gameboard[x2][y] == player && x2 == x - 1)
				break;
		}
		for (int x2 = x - 1, y2 = y + 1; x2 > -1 && y2 < 8; x2--, y2++) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x - 1)
				return true;
			else if (gameboard[x2][y2] == player && x2 == x - 1)
				break;
		}
		for (int x2 = x - 1, y2 = y - 1; x2 > -1 && y2 > -1; x2--, y2--) {
			if (gameboard[x2][y2] == 0)
				break;
			if (gameboard[x2][y2] == player && x2 != x - 1)
				return true;
			else if (gameboard[x2][y2] == player && x2 == x - 1)
				break;
		}
		return false;
	}
}
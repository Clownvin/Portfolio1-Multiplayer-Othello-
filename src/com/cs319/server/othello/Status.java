package com.cs319.server.othello;

import com.cs319.server.packets.Packet;

public class Status implements Packet {
	//Statuses
	public static final byte WHITE_TURN = Game.PLAYER_WHITE;
	public static final byte BLACK_TURN = Game.PLAYER_BLACK;
	public static final byte WHITE_VICTORY = 3;
	public static final byte BLACK_VICTORY = 4;
	public static final byte DRAW = 5;
	public static final byte YOURE_BLACK = 6;
	public static final byte YOURE_WHITE = 7;
	public static final byte NEW_GAME = 8;
	public static final byte GAME_CLOSED = 9;

	private final byte status;

	public Status(final byte status) {
		this.status = status;
	}

	public Status(final byte[] bytes) {
		this.status = bytes[1];
	}

	public byte getIdentifierByte() {
		return 4;
	}

	public byte getStatus() {
		return status;
	}

	public byte[] toBytes() {
		byte[] bytes = { getIdentifierByte(), status };
		return bytes;
	}

	@Override
	public String toString() {
		return "Status: " + status;
	}

}

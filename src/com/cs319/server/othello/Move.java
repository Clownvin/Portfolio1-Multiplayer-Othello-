package com.cs319.server.othello;

import com.cs319.server.packets.Packet;

public class Move implements Packet {

	private final int moveY;
	private final int moveX;

	public Move(final byte[] buffer) {
		int index = 1;
		//Get move x and move y out of buffer using bitwise operations
		moveX = (buffer[index++] << 24) | (buffer[index++] << 16) | (buffer[index++] << 8) | (buffer[index++]);
		moveY = (buffer[index++] << 24) | (buffer[index++] << 16) | (buffer[index++] << 8) | (buffer[index++]);
	}

	public Move(final int moveX, final int moveY) {
		this.moveX = moveX;
		this.moveY = moveY;
	}

	public byte getIdentifierByte() {
		return 2;
	}

	public int getMoveX() {
		return moveX;
	}

	public int getMoveY() {
		return moveY;
	}

	public byte[] toBytes() {
		byte[] buffer = new byte[9]; // 2 x int + 1 byte identifier;
		int index = 0;
		buffer[index++] = getIdentifierByte();
		buffer[index++] = (byte) (moveX >> 24);
		buffer[index++] = (byte) ((moveX >> 16) & 0xFF);
		buffer[index++] = (byte) ((moveX >> 8) & 0xFF);
		buffer[index++] = (byte) (moveX & 0xFF);
		buffer[index++] = (byte) (moveY >> 24);
		buffer[index++] = (byte) ((moveY >> 16) & 0xFF);
		buffer[index++] = (byte) ((moveY >> 8) & 0xFF);
		buffer[index++] = (byte) (moveY & 0xFF);
		return buffer;
	}

	@Override
	public String toString() {
		return "Move: (" + moveX + ", " + moveY + ")";
	}
}

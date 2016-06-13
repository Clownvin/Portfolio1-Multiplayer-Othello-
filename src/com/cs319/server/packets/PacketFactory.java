package com.cs319.server.packets;

import com.cs319.server.othello.GameBoard;
import com.cs319.server.othello.Message;
import com.cs319.server.othello.Move;
import com.cs319.server.othello.Status;

public final class PacketFactory {
	public static Packet buildPacket(final byte[] packetBytes) {
		if (packetBytes.length < 1) {
			throw new IllegalArgumentException("Packet bytes length less than 1.");
		}
		switch (packetBytes[0]) { // Switch on first byte, which should be identifier byte
		case 1:
			return new Message(packetBytes);
		case 2:
			return new Move(packetBytes);
		case 3:
			return new GameBoard(packetBytes);
		case 4:
			return new Status(packetBytes);
		case 5:
			return new Request(packetBytes);
		default:
			throw new RuntimeException("No case for packet identifier: " + packetBytes[0]);
		}
	}

	private PacketFactory() {
		// To prevent instantiation.
	}
}

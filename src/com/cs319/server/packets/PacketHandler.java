package com.cs319.server.packets;

import com.cs319.server.Connection;
import com.cs319.server.othello.Game;
import com.cs319.server.othello.GameMaster;
import com.cs319.server.othello.Message;
import com.cs319.server.othello.Move;

public final class PacketHandler {
	public static void handlePacket(final Packet packet, final Connection connection) {
		switch (packet.getIdentifierByte()) {
		case 1: // Message
			if (!GameMaster.inGame(connection)) {
				return;
			}
			GameMaster.getGame(connection).sendMessage((Message) packet, connection);
			break;
		case 2: // Move
			if (!GameMaster.inGame(connection)) {
				return;
			}
			GameMaster.getGame(connection).handleMove((Move) packet, connection);
			break;
		case 3: // GameBoard (Shouldn't occur on server.)
		case 4: // Status (Shouldn't occur on server.)
			System.err.println("Shouldn't be getting packet with type: " + packet.getIdentifierByte());
			break;
		case 5: // Request
			switch (((Request) packet).getRequest()) {
			case Request.REQUEST_GAME_SEARCH:
				if (connection.getStatus() == Connection.IN_GAME) {
					connection.sendPacket(new Message("You're already in a game! Leave your current one first."));
					break;
				} else if (connection.getStatus() == Connection.IDLE) {
					connection.setStatus(Connection.SEARCHING);
					connection.sendPacket(new Message("Searching for a game..."));
				} else if (connection.getStatus() == Connection.SEARCHING) {
					connection.sendPacket(new Message("You're already searching for a game."));
				}
				break;
			case Request.REQUEST_LEAVE_GAME:
				Game game = GameMaster.getGame(connection);
				if (game == null && connection.getStatus() == Connection.IN_GAME) {
					connection.sendPacket(new Message("You're not currently in a game."));
				} else if (game != null) {
					game.setPlayerLeft(true);
					connection.setStatus(Connection.IDLE);
					connection.sendPacket(new Message(
							"You've successfully left your current game. Use the search button to find a new one."));
				} else if (connection.getStatus() == Connection.IDLE) {
					connection.sendPacket(new Message("You're not in a game."));
				} else if (connection.getStatus() == Connection.SEARCHING) {
					connection.setStatus(Connection.IDLE);
					connection.sendPacket(new Message("You've stopped searching for a game."));
				}
				break;
			default:
				System.err.println("No case for request: " + ((Request) packet).getRequest());
			}
			break;
		default:
			System.err.println("No case for type: " + packet.getIdentifierByte());
		}
	}
}

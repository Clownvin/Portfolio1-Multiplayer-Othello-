package com.cs319.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cs319.server.othello.Game;
import com.cs319.server.othello.GameMaster;
import com.cs319.server.packets.PacketHandler;

public final class ConnectionManager {
	//Set of connections
	private static volatile Set<Connection> connections = new HashSet<>();
	//Wait object that connections will get and wait on
	private static final Object waitObject = new Object();

	public static void addConnection(final Connection connection) {
		if (connections.contains(connection)) { // Already have exact same connection in set
			connection.disconnect();
		}
		System.out.println("Connection joined: " + connection);
		connections.add(connection);
	}

	public static Object getWaitObject() {
		return waitObject;
	}

	public static void process() {
		List<Connection> toRemove = new ArrayList<>(); // List of connections to remove (ones that are 
		Connection lookingFor = null; // Connection looking
		for (Connection connection : connections) {
			if (connection.isDisconnected()) {
				toRemove.add(connection); // Add disconnected to removal list
				continue;
			}
			while (connection.hasPacket()) { // Loop through and take all packets
				PacketHandler.handlePacket(connection.getPacket(), connection);
			}
			//If connection looking for partner and looking for is null (no other person yet)
			if (lookingFor == null && connection.getStatus() == Connection.SEARCHING) {
				lookingFor = connection;
			//Else partner is lookingFor already
			} else if (connection.getStatus() == Connection.SEARCHING) {
				GameMaster.setupGame(lookingFor, connection);
				lookingFor = null;
			}
		}
		synchronized (waitObject) {
			waitObject.notifyAll(); // wake up connections so they can grab packets for next tick
		}
		//Remove connections from removalList and make sure any games they're in get closed.
		for (Connection connection : toRemove) {
			if (connections.remove(connection)) {
				Game currentGame = GameMaster.getGame(connection);
				if (currentGame != null) {
					currentGame.setPlayerLeft(true);
				}
				System.out.println("Connection disconnected: " + connection);
			}
		}
	}
}

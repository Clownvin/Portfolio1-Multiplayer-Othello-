package com.cs319.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.cs319.server.util.ClosedIDSystem;

public final class ConnectionAcceptor implements Runnable {
	//Singleton to ensure that only one instance exists per program
	private static final ConnectionAcceptor SINGLETON = new ConnectionAcceptor();
	//Port running on
	private static int port;

	public static void startAcceptor(final int newPort) {
		port = newPort;
		new Thread(SINGLETON).start();
	}

	private ConnectionAcceptor() {
		// To prevent instantiation.
	}

	@Override
	public void run() {
		try {
			ServerSocket acceptorSocket = new ServerSocket(port);
			while (Main.isRunning()) {
				Socket socket = acceptorSocket.accept();
				if (ClosedIDSystem.hasTag()) {
					ConnectionManager.addConnection(new Connection(socket, ConnectionManager.getWaitObject()));
				} else { // No tags, so close connection
					socket.close();
				}
			}
			acceptorSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}

package com.cs319.server;

public final class Main {

	private static volatile boolean running = true;

	public static boolean isRunning() {
		return running;
	}

	public static void main(String[] args) {
		System.out.println("Starting server...");
		ConnectionAcceptor.startAcceptor(52413);
		while (isRunning()) {
			ConnectionManager.process(); // Start new tick
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// Don't care if get interrupted.
			}
		}
	}
}

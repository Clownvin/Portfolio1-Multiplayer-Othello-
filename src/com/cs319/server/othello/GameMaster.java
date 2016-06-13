package com.cs319.server.othello;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.cs319.server.Connection;

public final class GameMaster {
	//Set of all games
	private static final Set<Game> GAMES = new HashSet<>();

	public static void clearFinished() {
		List<Game> toRemove = new ArrayList<>();
		for (Game game : GAMES) {
			if (game.playerLeft()) { // remove based on player leaving.
				toRemove.add(game);
				game.end();
			}
		}
		GAMES.removeAll(toRemove);
	}

	//Get the game a connection is currently in
	public static Game getGame(final Connection connection) {
		for (Game game : GAMES) {
			if (game.isPlayer(connection)) {
				return game;
			}
		}
		return null;
	}

	public static boolean inGame(final Connection connection) {
		for (Game game : GAMES) {
			if (game.isPlayer(connection)) {
				return true;
			}
		}
		return false;
	}

	public static void setupGame(final Connection connection1, final Connection connection2) {
		if (inGame(connection1) || inGame(connection2)) {
			System.out.println("Tried to match two players when at least one is already in a game..");
			return;
		}
		GAMES.add(new Game(connection1, connection2));
	}
}

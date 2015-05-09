package net.zomis.server.model;

public interface GameFactory {
	Game newGame(Server server, int id);
}

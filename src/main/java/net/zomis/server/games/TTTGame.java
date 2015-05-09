package net.zomis.server.games;

import net.zomis.server.model.Command;
import net.zomis.server.model.Game;
import net.zomis.server.model.Server;
import net.zomis.tttultimate.TTPlayer;
import net.zomis.tttultimate.games.TTController;
import net.zomis.tttultimate.games.TTControllers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TTTGame extends Game {
	private static final Logger logger = LogManager.getLogger(TTTGame.class);
	
	private TTController game;
	
	public TTTGame(Server server, final int id) {
		super(server, id);
		game = TTControllers.ultimateTTT();
		game.setOnMoveListener((playedAt) -> send("MOVE " + id + " " + playedAt.getGlobalX() + " " + playedAt.getGlobalY()));
	}

	@Override
	protected boolean makeMove(Command command, int player) {
		TTPlayer expectedPlayer = TTPlayer.getPlayerByIndex(player);
		logger.info("Expected player " + expectedPlayer + " actual is " + game.getCurrentPlayer());
		if (!game.getCurrentPlayer().equals(expectedPlayer))
			return false;
		
		int x = command.getParameterInt(2);
		int y = command.getParameterInt(3);
		logger.info("Play at " + x + "; " + y);
		boolean result = game.play(x, y);
		
		if (game.isGameOver()) {
			this.endGame();
			logger.info("Replay is " + game.saveHistory());
		}
		
		return result;
	}

	@Override
	protected void updateStatus() {
		
	}

	@Override
	protected void onStart() {
		
	}
	
}

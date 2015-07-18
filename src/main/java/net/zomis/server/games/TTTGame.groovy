package net.zomis.server.games

import net.zomis.server.model.*
import net.zomis.tttultimate.TTPlayer
import net.zomis.tttultimate.games.TTController
import net.zomis.tttultimate.games.TTControllers

import org.apache.log4j.LogManager
import org.apache.log4j.Logger

public class TTTGame extends Game<TTPlayer> {

    private static final Logger logger = LogManager.getLogger(TTTGame.class);

    private final TTController game;

    public TTTGame(Server server, final int id) {
        super(server, id);
        game = TTControllers.ultimateTTT();
        game.setOnMoveListener({playedAt -> send(new GameMoveXY(gameId: id,
            x: playedAt.getGlobalX(), y: playedAt.getGlobalY()))});
    }

    public TTController getGame() {
        return game;
    }

    @Override
    public boolean handleMove(GameMove move, PlayerInGame<TTPlayer> player) {
        GameMoveXY xyMove = (GameMoveXY) move;
        TTPlayer expectedPlayer = player.getData();
        logger.info("Expected player " + expectedPlayer + " actual is " + game.getCurrentPlayer());
        if (!game.getCurrentPlayer().equals(expectedPlayer)) {
            return false;
        }

        int x = xyMove.getX();
        int y = xyMove.getY();
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
    protected TTPlayer createPlayerData(int idx) {
        switch (idx) {
            case 0:
                return TTPlayer.X;
            case 1:
                return TTPlayer.O;
            default:
                throw new IllegalArgumentException("TTTGame only supports two players. Unknown index " + idx);
        }
    }

    @Override
    public boolean playerCanMove(PlayerInGame<TTPlayer> p) {
        return game.getCurrentPlayer() == p.getData();
    }

    @Override
    protected void onStart() {

    }

}

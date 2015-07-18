package net.zomis.server.games.uttt

import net.zomis.server.games.GameMoveXY
import net.zomis.server.games.TTTGame
import net.zomis.server.model.AI
import net.zomis.server.model.GameMove
import net.zomis.server.model.PlayerInGame
import net.zomis.tttultimate.TTBase
import net.zomis.tttultimate.TTPlayer
import net.zomis.tttultimate.players.TTAI

class TTAIAdapter implements AI<TTPlayer> {

    TTAI ai

    TTAIAdapter(TTAI ai) {
        this.ai = ai
    }

    @Override
    GameMove play(PlayerInGame<TTPlayer> player) {
        TTTGame game = player.game as TTTGame
        TTBase move = ai.play(game.getGame())
        return new GameMoveXY(x: move.globalX, y: move.globalY)
    }

}

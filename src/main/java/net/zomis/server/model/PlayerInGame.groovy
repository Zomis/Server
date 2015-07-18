package net.zomis.server.model

import net.zomis.server.clients.ClientIO

class PlayerInGame<T> {

    private final Game<T> game
    private final T data
    private final int index
    ClientIO client
    AI<T> ai

    PlayerInGame(Game<T> game, T data, ClientIO client, int index) {
        this.game = game
        this.data = data
        this.client = client
        this.index = index
    }

    boolean hasTurn() {
        game.playerCanMove(this)
    }

    Game<T> getGame() {
        game
    }

    int getIndex() {
        index
    }

    T getData() {
        data
    }

}

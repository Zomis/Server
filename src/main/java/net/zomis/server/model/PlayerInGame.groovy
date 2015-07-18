package net.zomis.server.model

import net.zomis.server.clients.ClientIO

class PlayerInGame<T> {

    private final Game<T> game
    private final T data
    ClientIO client
    int index
    AI ai

    PlayerInGame(Game<T> game, T data, ClientIO client) {
        this.game = game
        this.data = data
        this.client = client
    }

    boolean hasTurn() {
        game.playerCanMove(this)
    }

    T getData() {
        data
    }

}

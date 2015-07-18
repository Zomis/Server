package net.zomis.server.model

interface AI<T> {
    GameMove play(PlayerInGame<T> game)
}

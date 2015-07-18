package net.zomis.server.messages.outgoing

import groovy.transform.ToString
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

@ToString
@FourChar(value = 'NEWG', outgoingStr = { "$gameId $playerIndex" })
class NewGameMessage implements Message {
    int gameId
    int playerIndex

    NewGameMessage(int gameId, int playerIndex) {
        this.playerIndex = playerIndex
        this.gameId = gameId
    }
}

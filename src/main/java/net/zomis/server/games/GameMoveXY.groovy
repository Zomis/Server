package net.zomis.server.games

import groovy.transform.ToString
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message
import net.zomis.server.model.GameMove

@ToString(includeSuper = true)
@FourChar(value = 'MOVE',
    incomingStr = {
        mess -> new GameMoveXY(
            gameId: mess[1] as int,
            x: mess[2] as int,
            y: mess[3] as int,
            type: mess.length > 4 ? mess[4] : '')
    },
    outgoingStr = {
        type && !type.isEmpty() ? "$gameId $x $y $type" : "$gameId $x $y"
    }
)
class GameMoveXY extends GameMove implements Message {

    int x
    int y
    String type

}

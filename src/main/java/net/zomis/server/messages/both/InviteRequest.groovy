package net.zomis.server.messages.both

import groovy.transform.ToString
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

@ToString
@FourChar(value = 'INVT', incomingStr = {mess -> new InviteRequest(gameType: mess[1], who: mess[2], parameters: mess.length > 3 ? mess[3] : '') },
        outgoingStr = { "$inviteId $gameType $who" })
class InviteRequest implements Message {

    String gameType
    String who
    String parameters
    int inviteId

}

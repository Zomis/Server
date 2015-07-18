package net.zomis.server.messages.both

import groovy.transform.ToString
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

@FourChar(value = 'INVR', incomingStr = {mess -> new InviteResponse(inviteId: mess[1] as int, accepted: mess[2] == '1') },
    outgoingStr = { "$inviteId $responder ${accepted ? '1' : '0'}" })
@ToString
class InviteResponse implements Message {

    boolean accepted
    int inviteId
    String responder

}

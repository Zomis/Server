package net.zomis.server.messages.outgoing

import groovy.transform.Immutable
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

@FourChar(value = 'ERRR', outgoingStr = { message })
@Immutable
class ServerErrorMessage implements Message {

    String message

}

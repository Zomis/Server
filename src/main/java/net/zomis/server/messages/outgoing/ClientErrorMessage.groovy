package net.zomis.server.messages.outgoing

import groovy.transform.Immutable
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

@FourChar(value = 'FAIL', outgoingStr = { message })
@Immutable
class ClientErrorMessage implements Message {

    String message

}

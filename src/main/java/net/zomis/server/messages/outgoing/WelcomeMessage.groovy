package net.zomis.server.messages.outgoing

import groovy.transform.Immutable
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message

@FourChar(value = 'WELC', outgoingStr = { clientName })
@Immutable
class WelcomeMessage implements Message {

    String clientName

}

package net.zomis.server.messages

import groovy.transform.Immutable
import groovy.transform.ToString

@ToString
@FourChar(value = 'ERRR', outgoingStr = { "$message" })
@groovy.transform.TupleConstructor
public class ServerErrorMessage implements Message {

    String message

}

package net.zomis.server.messages

import groovy.transform.ToString

@ToString
@FourChar(value = 'CHAT', incomingStr = {mess -> new ChatMessage(Integer.parseInt(mess[1]),
        String.join(' ', Arrays.copyOfRange(mess, 2, mess.length))) },
        outgoingStr = { "$chatId $message" })
public class ChatMessage implements Message {

    int chatId
    String message

    public ChatMessage(int chatId, String message) {
        this.chatId = chatId
        this.message = message
    }

}

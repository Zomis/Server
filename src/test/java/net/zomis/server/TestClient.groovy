package net.zomis.server

import net.zomis.server.clients.ClientIO
import net.zomis.server.messages.Message
import net.zomis.server.messtransform.MessageTransformer
import net.zomis.server.model.Server

import java.util.function.Predicate

class TestClient {

    private static final Message NULL_MESSAGE = new Message() { }
    private boolean closed
    Queue<String> stringMessages = new ArrayDeque<>()
    Queue<Message> messages = new ArrayDeque<>()
    MessageTransformer transformer
    ClientIO io

    TestClient(Server server, ClientIO io) {
        this.transformer = server.getTransformer()
        this.io = io
    }

    void handleString(String data) {
        assert !closed
        stringMessages.add(data)
        messages.add(NULL_MESSAGE)
    }

    void handleMessage(Message data) {
        assert !closed
        transformer.transform(data, null, {stringMessages.add(it)})
        messages.add(data)
    }

    void expect(Message message) {
        assert false : 'Not implemented'
    }

    void expect(Class<? extends Message> clazz) {
        Message message = takeMessage()
        assert message : "Expected message of type $clazz but no message found"
        assert clazz.isAssignableFrom(message.class)
    }

    void awaitUntil(Predicate<Message> predicate) {
        assert false : 'Not implemented'
    }

    void expect(Predicate<Message> message) {
        assert false : 'Not implemented'
    }

    void expect(String message) {
        assert takeString() == message
    }

    private Message takeMessage() {
        assert messages.size() == stringMessages.size()
        def str = stringMessages.poll()
        Message mess = messages.poll()
        assert mess != NULL_MESSAGE : 'Received a string, not a message. Received string is ' + str
        mess
    }

    private String takeString() {
        assert messages.size() == stringMessages.size()
        messages.poll()
        stringMessages.poll()
    }

    void assertNoMessages() {
        assert messages.isEmpty()
        assert stringMessages.isEmpty()
    }

    @Deprecated
    void sentToServer(String message) {
        io.sentToServer(message)
    }

    void sentToServer(Message message) {
        io.sentToServer(message)
    }

}

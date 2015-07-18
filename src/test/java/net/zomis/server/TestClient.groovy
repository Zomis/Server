package net.zomis.server

import net.zomis.server.clients.ClientIO
import net.zomis.server.messages.Message
import net.zomis.server.messtransform.MessageTransformer
import net.zomis.server.model.Server

import java.util.function.Predicate

class TestClient extends ClientIO {

    private static final Message NULL_MESSAGE = new Message() { }
    private boolean closed
    ClientIO deleg
    Queue<String> stringMessages = new ArrayDeque<>()
    Queue<Message> messages = new ArrayDeque<>()
    MessageTransformer transformer

    TestClient(Server server) {
        super(server)
        this.transformer = server.getTransformer()
    }

    @Override
    protected void onSend(String data) {
        assert !closed
        stringMessages.add(data)
        messages.add(NULL_MESSAGE)
    }

    @Override
    protected void onSend(Message data) {
        assert !closed
        transformer.transform(data, null, {stringMessages.add(it)})
        messages.add(data)
    }

    @Override
    void close() {
        closed = true
    }

    void expect(Message message) {
        assert false : 'Not implemented'
    }

    void expect(Class<? extends Message> message) {
        assert false : 'Not implemented'
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
        stringMessages.poll()
        messages.poll()
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
}

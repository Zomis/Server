package net.zomis.server.clients

import net.zomis.server.messages.Message
import net.zomis.server.messtransform.MessageTransformer
import net.zomis.server.model.Server
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import java.util.function.Predicate

class TestClient {

    private static final Message NULL_MESSAGE = new Message() { }
    private static final Logger logger = LogManager.getLogger(TestClient)
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
        logger.debug("TestClient $io received string $data")
        stringMessages.add(data)
        messages.add(NULL_MESSAGE)
    }

    void handleMessage(Message data) {
        assert !closed
        logger.debug("TestClient $io received message $data")
        transformer.transform(data, null, {stringMessages.add(it)})
        messages.add(data)
    }

    void expect(Message message) {
        assert false : 'Not implemented'
    }

    void expect(Class<? extends Message> clazz) {
        Message message = takeMessage()
        assert message != NULL_MESSAGE : 'Received a string, not a message object'
        assert message : "Expected message of type $clazz but no message found"
        assert clazz.isAssignableFrom(message.class)
    }

    Message awaitUntil(Class<? extends Message> clazz) {
        while (true) {
            Message message = takeMessage()
            assert message : "No more messages while waiting for $clazz"
            if (clazz.isAssignableFrom(message.class)) {
                return message
            }
        }
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
        assert !messages.isEmpty() : 'There are no more messages to take'
        stringMessages.poll()
        messages.poll()
    }

    private String takeString() {
        assert messages.size() == stringMessages.size()
        assert !stringMessages.isEmpty() : 'There are no more messages to take'
        messages.poll()
        stringMessages.poll()
    }

    void assertNoMessages() {
        assert messages.isEmpty()
        assert stringMessages.isEmpty()
    }

    void sentToServer(String message) {
        io.sentToServer(message)
    }

    void sentToServer(Message message) {
        io.sentToServer(message)
    }

    String getName() {
        io.name
    }

    void expectStr(Predicate<String> predicate) {
        def str = takeString()
        assert predicate.test(str) : "Predicate did not match string $str"
    }

    void awaitUntilStr(Predicate<String> predicate) {
        while (true) {
            def str = takeString()
            if (predicate.test(str)) {
                return
            }
        }
    }

}

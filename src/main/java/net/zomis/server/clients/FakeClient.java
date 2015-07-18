package net.zomis.server.clients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.zomis.server.messages.Message;
import net.zomis.server.messtransform.MessageTransformer;
import net.zomis.server.model.Server;

public class FakeClient extends ClientIO {

    @Deprecated
    private final List<Consumer<String>> stringConsumers = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<Message>> messageConsumers = Collections.synchronizedList(new ArrayList<>());

    public FakeClient(Server server) {
		super(server);
	}

    public void addConsumerString(Consumer<String> consumer) {
        stringConsumers.add(consumer);
    }

    public void addConsumer(Consumer<Message> consumer) {
        messageConsumers.add(consumer);
    }

    @Override
    @Deprecated
	public void onSend(String message) {
        stringConsumers.forEach(consumer -> consumer.accept(message));
	}

	@Override
    protected void onSend(Message data) {
        messageConsumers.forEach(consumer -> consumer.accept(data));
    }

    @Override
	public void close() {
		
	}
	
    @Override
    public String toString() {
        return "FakeClient-" + this.getName();
    }
}

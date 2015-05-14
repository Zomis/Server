package net.zomis.server.clients;

import java.util.function.Consumer;

import net.zomis.server.messages.Message;
import net.zomis.server.model.Server;

public class FakeClient extends ClientIO {

	private final Consumer<String> consumer;
	
	public FakeClient(Server server, Consumer<String> consumer) {
		super(server);
		this.consumer = consumer;
	}

	@Override
	public void onSend(String message) {
		consumer.accept(message);
	}

	@Override
    protected void onSend(Message data) {
        throw new UnsupportedOperationException("not supported yet");
    }

    @Override
	public void close() {
		
	}
	
}

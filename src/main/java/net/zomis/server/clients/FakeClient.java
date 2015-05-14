package net.zomis.server.clients;

import java.util.function.Consumer;

import net.zomis.server.messages.Message;
import net.zomis.server.messtransform.MessageTransformer;
import net.zomis.server.model.Server;

public class FakeClient extends ClientIO {

	private final Consumer<String> consumer;
    private final MessageTransformer transformer;

    public FakeClient(Server server, Consumer<String> consumer) {
		super(server);
		this.consumer = consumer;
        this.transformer = server.getTransformer();
	}

	@Override
	public void onSend(String message) {
		consumer.accept(message);
	}

	@Override
    protected void onSend(Message data) {
        transformer.transform(data, null, this::onSend);
    }

    @Override
	public void close() {
		
	}
	
}

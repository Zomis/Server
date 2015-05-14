package net.zomis.server.clients;

import net.zomis.server.messages.Message;
import net.zomis.server.messtransform.MessageTransformer;
import net.zomis.server.model.Server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

public class ClientWebSocket extends ClientIO {
	private static final Logger logger = LogManager.getLogger(ClientWebSocket.class);
	
	private final WebSocket conn;
    private final MessageTransformer transformer;
	
	public ClientWebSocket(Server server, WebSocket conn) {
		super(server);
		this.conn = conn;
        transformer = server.getTransformer();
	}
	
	@Override
    @Deprecated
	public void onSend(String message) {
		logger.info("Send to " + conn + ": " + message);
		conn.send(message);
	}

	@Override
    protected void onSend(Message data) {
        transformer.transform(data, bytes -> conn.send(bytes), str -> conn.send(str));
    }

    @Override
	public void close() {
		logger.info("Manual close " + this);
		conn.close();
	}

    public void transformAndHandle(byte[] data) {
        transformer.read(null, data, this::sentToServer);
    }
}

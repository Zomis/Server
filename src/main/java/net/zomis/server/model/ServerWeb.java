package net.zomis.server.model;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.zomis.server.clients.Base64Utils;
import net.zomis.server.clients.ClientIO;
import net.zomis.server.clients.ClientWebSocket;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class ServerWeb implements ConnectionHandler {
	private static final Logger logger = LogManager.getLogger(ServerWeb.class);
	
	private final InnerServer websocketServer;
	
	public ServerWeb(Server server, int port) {
		this.websocketServer = new InnerServer(server, port);
	}

	private static class InnerServer extends WebSocketServer {
		
		public InnerServer(Server server, int port) {
			super(new InetSocketAddress(port));
			this.webClients = new ConcurrentHashMap<>();
			this.server = server;
		}
		
		private final Map<WebSocket, ClientWebSocket> webClients;
		private final Server server;
		
		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			logger.info("Connection opened: " + conn);
			ClientWebSocket io = new ClientWebSocket(server, conn);
			webClients.put(conn, io);
			server.newClient(io);
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			logger.info("Connection closed: " + conn + " code " + code + " reason " + reason + " remote " + remote);
			ClientIO io = webClients.remove(conn);
			if (io == null) {
				logger.error("Closing unknown ClientIO");
				return;
			}
			io.close();
			server.onDisconnected(io);
		}

        @Override
        public void onMessage(WebSocket conn, ByteBuffer message) {
            // TODO: Use onMessage(WebSocket, ByteBuffer) ?
            super.onMessage(conn, message);
        }

        @Override
        public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
            // TODO: Use onWebsocketMessageFragment?
            super.onWebsocketMessageFragment(conn, frame);
        }

        @Override
		public void onMessage(WebSocket conn, String message) {
			logger.info("Connection message from: " + conn + ": " + message);
			ClientWebSocket io = webClients.get(conn);
			if (io == null) {
				logger.error("Message was recieved from unknown ClientIO");
				return;
			}
			io.transformAndHandle(Base64Utils.fromBase64(message));
		}

		@Override
		public void onError(WebSocket conn, Exception ex) {
			logger.warn("Connection error: " + conn, ex);
		}
		
	}

	@Override
	public void start() {
		websocketServer.start();
	}

	@Override
	public void shutdown() throws Exception {
		websocketServer.stop();
	}
	
}

package net.zomis.server.clients;

import net.zomis.server.messages.Message;
import net.zomis.server.model.Command;
import net.zomis.server.model.Server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public abstract class ClientIO {

	private static final Logger logger = LogManager.getLogger(ClientIO.class);
	
	private String name = "";
	private final Server server;
	
	public ClientIO(Server server) {
		this.server = server;
	}
	
	public boolean login(String username, String password) {
		if (username == null || username.contains(" ") || username.isEmpty())
			return false;
		
		this.name = username;
		return true;
	}
	
	/**
	 * Send a message to this client
	 * 
	 * @param data Message to send
	 */
    @Deprecated
    public final void sendToClient(String data) {
        logger.debug("Send to " + this.name + ": " + data);
        onSend(data);
    }

    public final void sendToClient(Message data) {
        logger.debug("Send to " + this.name + ": " + data);
        onSend(data);
    }

    @Deprecated
    protected abstract void onSend(String data);

    protected abstract void onSend(Message data);

    public String getName() {
		return name;
	}
	
    @Deprecated
	public void sentToServer(String message) {
		logger.debug("Incoming message from " + this.name + ": " + message);
		server.handleMessage(this, message);
	}
	
	/**
	 * Disconnect this client
	 */
	public abstract void close();
	
	public Command parseMessage(String input) {
		return new Command(this, input);
	}

	public boolean isLoggedIn() {
		return name.length() > 0;
	}

	public String getStatus() {
		return isLoggedIn() ? "online" : "offline";
	}
	
    public void sentToServer(Message mess) {
        logger.info("Sent to server: " + mess);
        server.handleMessage(this, mess);
    }

    protected void disconnected() {
        server.onDisconnected(this);
    }
}

package net.zomis.server.clients;

import java.io.*;
import java.net.Socket;

import net.zomis.server.messages.Message;
import net.zomis.server.messtransform.MessageTransformer;
import net.zomis.server.model.Server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ClientSocketHandler extends ClientIO implements Runnable {
	private static final Logger logger = LogManager.getLogger(ClientSocketHandler.class);
	
	private Socket socket;
	private final InputStream in;
	private final OutputStream out;
    private final PrintWriter outWriter;
    private MessageTransformer transformer;
	
	public ClientSocketHandler(Server server, Socket socket) throws IOException {
		super(server);
		this.socket = socket;
		
		in = socket.getInputStream();
		out = socket.getOutputStream();
        outWriter = new PrintWriter(out);

        transformer = server.getTransformer();
	}
	
	@Override
    @Deprecated
	public void onSend(String message) {
        sendString(message);
	}

    @Override
    protected void onSend(Message data) {
        transformer.transform(data, this::sendBytes, this::sendString);
    }

    private synchronized void sendString(String data) {
        logger.info("Send string: " + data);
        outWriter.print(data);
        outWriter.print('\n');
        outWriter.flush();
    }

    private synchronized void sendBytes(byte[] bytes) throws IOException {
        logger.info("Send bytes: " + bytes.length);
        out.write(bytes);
        out.flush();
    }

    @Override
	public void run() {
        logger.info("Started thread for " + this);
        try {
            transformer.read(in, null, mess -> this.sentToServer(mess), mess -> this.sentToServer(mess));
        } catch (Exception ex) {
            logger.error("Error in " + this, ex);
        }
        disconnected();
	}

	@Override
	public void close() {
		try {
			socket.close();
		}
		catch (IOException e) {
			logger.warn("Error closing", e);
		}
        disconnected();
	}
}

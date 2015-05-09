package net.zomis.server.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.zomis.server.clients.ClientSocketHandler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ServerSock implements ConnectionHandler {
	private static final Logger logger = LogManager.getLogger(ServerSock.class);
	
	private final AtomicInteger activeConnections = new AtomicInteger(0);
	private final AtomicInteger threadCounter = new AtomicInteger(0);
	private final ExecutorService executor;
	private final Server	server;
	private final Thread thread;
	private final ServerSocket serverSocket;
	
	public ServerSock(Server server, int port) throws IOException {
		this.server = server;
		this.executor = Executors.newCachedThreadPool(r -> new Thread(r, "Conn-" + threadCounter.getAndIncrement()));
		this.serverSocket = new ServerSocket(port);
		this.thread = new Thread(this::run);
	}

	private void run() {
		try {
			int maxConnections = 0;
			
			while (activeConnections.incrementAndGet() < maxConnections || maxConnections == 0) {
				logger.info("Waiting for client nr: " + activeConnections.get() + "...");
				Socket client = serverSocket.accept();
				ClientSocketHandler clientHandler = new ClientSocketHandler(this.server, client);
				if (thread.isInterrupted()) {
					logger.info("ServerSocket thread interrupted, shutting down.");
					break;
				}
				this.server.newClient(clientHandler);
				executor.submit(clientHandler);
			}
		}
		catch (Exception e) {
			logger.error("Error in ServerSocket", e);
		}
	}


	@Override
	public void start() {
		thread.start();
	}


	@Override
	public void shutdown() throws Exception {
		thread.interrupt();
	}

}

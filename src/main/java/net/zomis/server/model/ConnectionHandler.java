package net.zomis.server.model;

public interface ConnectionHandler {

	void start();
	void shutdown() throws Exception;
	
}

package net.zomis.server.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.zomis.server.clients.ClientIO;

import net.zomis.server.messages.ChatMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ChatArea {
	private static final Logger logger = LogManager.getLogger(ChatArea.class);
	
	// By using an id here, it could be used in a database table with Hibernate
	
	private final String name;
	private final Set<ClientIO> clients;
	private final int id;
	
	public ChatArea(int id, String name) {
		this.id = id;
		this.name = name;
		this.clients = Collections.synchronizedSet(new HashSet<>());
	}
	
	public void broadcast(String message) {
        ChatMessage chatMessage = new ChatMessage(id, message);
		logger.info(this + " broadcast: " + chatMessage);
		clients.forEach(cl -> cl.sendToClient(chatMessage));
	}
	
	public void add(ClientIO client) {
		clients.add(client);
	}
	
	public boolean remove(ClientIO client) {
		return clients.remove(client);
	}
	
	@Override
	public String toString() {
		return "ChatArea:" + id + name;
	}
	
}

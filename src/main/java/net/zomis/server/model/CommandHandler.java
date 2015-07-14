package net.zomis.server.model;

import net.zomis.server.clients.ClientIO;
import net.zomis.server.messages.Message;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class CommandHandler {
	
	private final Map<String, Consumer<Command>> commands;
    private final Map<Class<? extends Message>, MessageHandler<? extends Message>> commands2 = new ConcurrentHashMap<>();

	public CommandHandler() {
		this.commands = new ConcurrentHashMap<>();
	}

    @Deprecated
    public void addHandler(String command, Consumer<Command> handler) {
        commands.put(command, handler);
    }

    public <T extends Message> void addHandler(Class<T> command, MessageHandler<T> handler) {
        commands2.put(command, handler);
    }

	public boolean handle(Command command) {
		Consumer<Command> handler = commands.get(command.getCommand());
		if (handler == null)
			System.out.println("Invalid command: " + command);
		else handler.accept(command);
		return handler != null;
	}
	
	
    public <E extends Message> void handle(E message, ClientIO client) {
        Objects.requireNonNull(message, "Cannot handle a null message from " + client);
        @SuppressWarnings("unchecked")
        MessageHandler<E> messagePerform = (MessageHandler<E>) this.commands2.get(message.getClass());
        if (messagePerform == null) {
            throw new NullPointerException("No handler for message " + message + " of class " + message.getClass());
        }
        messagePerform.handle(message, client);
    }

	
}

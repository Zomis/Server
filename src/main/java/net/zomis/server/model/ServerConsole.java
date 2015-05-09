package net.zomis.server.model;

import java.util.Scanner;
import java.util.function.Consumer;

import net.zomis.server.clients.ClientIO;

import org.apache.log4j.LogManager;

public class ServerConsole extends ClientIO implements Runnable {

	public ServerConsole(Server server) {
		super(server);
	}
	
	private final CommandHandler commands = new CommandHandler();
	
	public void addHandler(String command, Consumer<Command> handler) {
		commands.addHandler(command, handler);
	}
	
	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		
		while (!Thread.interrupted()) {
			String input = scanner.nextLine();
			LogManager.getLogger(getClass()).info("Console input: " + input);
			Command cmd = new Command(this, input);
			boolean handled = commands.handle(cmd);
			if (!handled)
				System.out.println("CONSOLE Invalid command: " + cmd);
		}
		LogManager.getLogger(getClass()).info("Console stopped");
		scanner.close();
	}

	@Override
	public void onSend(String message) {
		System.out.println(message);
	}

	@Override
	public void sentToServer(String message) {
		commands.handle(this.parseMessage(message));
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

}

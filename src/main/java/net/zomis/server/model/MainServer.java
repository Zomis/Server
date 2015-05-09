package net.zomis.server.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MainServer {
	private static Logger logger = LogManager.getLogger(MainServer.class);
	
	public void start() {
		try {
			logger.info("Starting Server...");
			Server server = new Server();
			
			server.addConnections(new ServerSock(server, 7282));
			server.addConnections(new ServerWeb(server, 7283));
			
			logger.info("Starting Console...");
			ServerConsole console = new ServerConsole(server);
			new Thread(console, "Console-Thread").start();
			console.addHandler("threads", cmd -> showAllStackTraces(server, System.out::println));
			logger.info("Started");
		}
		catch (Exception e) {
			logger.error("Initializing Error", e);
		}
	}
	
	private void showAllStackTraces(Server server, Consumer<String> output) {
		Map<String, String> threadName_userName = new HashMap<String, String>();
		
//		for (ClientIO user : server.getClients()) {
//			threadName_userName.put(user.getThreadName(), user.getName());
//			output.accept(user.getName() + (user.isOnline() ? " is online" : "") + " has thread " + user.getThreadName());
//		}
		
		output.accept("All stack traces:");
		Map<Thread, StackTraceElement[]> allTraces = Thread.getAllStackTraces();
		for (Thread thread : allTraces.keySet()) {
			output.accept(thread.getName() + " belongs to " + threadName_userName.get(thread.getName()));
			this.stackTrace(thread, output);
		}
	}
	
	private void stackTrace(Thread thread, Consumer<String> output) {
		StackTraceElement[] stackTrace = thread.getStackTrace();
		output.accept("Stack trace for thread " + thread.getId() + ": " + thread.getName());
		for (StackTraceElement trace : stackTrace) {
			output.accept(trace.toString());
		}
		output.accept("");
	}
	
}
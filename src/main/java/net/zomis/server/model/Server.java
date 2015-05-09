package net.zomis.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import net.zomis.server.clients.ClientIO;
import net.zomis.server.games.BattleshipGame;
import net.zomis.server.games.TTTGame;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Server {
	private static final Logger	logger	= LogManager.getLogger(Server.class);
	
	// Counters for various things
	private final AtomicInteger roomCounter = new AtomicInteger(0);
	private final AtomicInteger inviteId = new AtomicInteger(0);
	private final AtomicInteger gameId = new AtomicInteger(0);
	
	private final CommandHandler incomingHandler;
	
	private final Set<ClientIO> clients = Collections.synchronizedSet(new HashSet<>());
	private final Map<Integer, ChatArea> chats = new ConcurrentHashMap<>();
	private final Map<Integer, Game> games = new ConcurrentHashMap<>();
	private final Map<Integer, GameInvite> invites = new ConcurrentHashMap<>();
	private final Map<String, GameFactory> gameFactories = new ConcurrentHashMap<>();

	private final Set<ConnectionHandler> handlers = Collections.synchronizedSet(new HashSet<>());

	public Server() {
		this.incomingHandler = new CommandHandler();
		this.newChatRoom("Main");
		
		Server server = this;
		CommandHandler incomings = server.getIncomingHandler();
		incomings.addHandler("CHAT", cmd -> server.incomingChatMessage(cmd));
		incomings.addHandler("USER", cmd -> server.loginRequest(cmd));
		
		incomings.addHandler("INVT", cmd -> server.inviteRequest(cmd));
		incomings.addHandler("INVY", cmd -> server.inviteRequest(cmd));
		incomings.addHandler("INVN", cmd -> server.inviteRequest(cmd));
		incomings.addHandler("MOVE", cmd -> server.incomingGameCommand(cmd));

        server.addGameFactory("UTTT", (serv, id) -> new TTTGame(serv, id));
        server.addGameFactory("Battleship", (serv, id) -> new BattleshipGame(serv, id));

	}
	
	@Deprecated
	private ChatArea getMainChat() {
		return chats.get(0);
	}
	
	public ChatArea newChatRoom(String name) {
		int id = roomCounter.getAndIncrement();
		ChatArea room = new ChatArea(roomCounter.getAndIncrement(), name);
		chats.put(id, room);
		return room;
	}
	
	// x Web Sockets
	// x Regular Sockets
	// x Step 1. Make clients chat with each other
	// x Step 2. Make a client be able to invite another client to a game (create the game here already? For parameterized CWars2 for example)

	// TODO: LOW PRIO saving game state
	// TODO: LOW PRIO replays
	// TODO: MEDIUM PRIO different kinds of communication, client informs if it wants JSON, MFE-style with spaces, or other variants
	// TODO: MEDIUM PRIO server lobby, see all available players and activity in the different game types (chat rooms?)
	// TODO: MEDIUM PRIO game lobby, see online/available players, games running, and available players/AIs (MFE lobby)
	// TODO: Automatic Unit tests

	// TODO: HIGH PRIO game chats (MFE in-game chat)
	// TODO: HIGH PRIO Server controls games
	// TODO: LOW PRIO Hibernate, database (HSQLDB? MySQL? Postgres?)

	// TODO: Step 3. Two clients start a TTT ultimate game and play and have in-game chat
	// TODO: Step 4. ZonesAndCards - send and receive information about cards played, cards dealt, resource changes...
	
	public Collection<ClientIO> getClients() {
		return new ArrayList<>(clients);
	}
	
	public CommandHandler getIncomingHandler() {
		return incomingHandler;
	}

	public void handleMessage(ClientIO client, String message) {
		Objects.requireNonNull(client, "Cannot handle message from a null client");
		if (!incomingHandler.handle(client.parseMessage(message)))
			logger.warn("Unhandled Message: " + message + " from " + client);
	}

	public void newClient(ClientIO cl) {
		logger.info("New client: " + cl);
		clients.add(cl);
		getMainChat().add(cl);
	}
	
	public void onDisconnected(ClientIO client) {
		clients.remove(client);
		getMainChat().remove(client);
	}

	public void loginRequest(Command cmd) {
		ClientIO sender = cmd.getSender();
		boolean usernameTaken = clients.stream().anyMatch(cl -> cl.getName().equals(cmd.getParameter(2)));
		// Perhaps use `Map<String, ClientIO>` for client names?
		boolean allowed = !usernameTaken && sender.login(cmd.getParameter(2), cmd.getParameter(3));
		logger.info("Login request: " + sender + " -- " + cmd);
		if (!allowed) {
			sender.sendToClient("FAIL " + usernameTaken); // TODO: Add proper protocol/messages for login denied
			sender.close();
		}
		else {
			sender.sendToClient("WELC " + sender.getName());
			broadcast("STUS " + sender.getName() + " online");
			clients.stream()
				.filter(cl -> cl.isLoggedIn())
				.filter(cl -> !cl.getName().equals(sender.getName())) // send information about the clients who were already connected
				.forEach(cl -> sender.sendToClient("STUS " + cl.getName() + " " + cl.getStatus()));
		}
	}

	void broadcast(String data) {
		clients.forEach(cl -> cl.sendToClient(data));
	}

	public void incomingChatMessage(Command cmd) {
		ChatArea room = this.chats.get(cmd.getParameterInt(1));
		if (room == null) {
			cmd.getSender().sendToClient("INVALID CHAT ROOM");
			return;
		}
		room.broadcast(cmd.getFullCommand(2));
	}
	
	public void addGameFactory(String gameType, GameFactory factory) {
		this.gameFactories.put(gameType, factory);
	}

	public boolean inviteRequest(Command cmd) {
		final GameInvite invite;
		switch (cmd.getCommand()) {
			case "INVT":
				String target = cmd.getParameter(2);
				Game game = createGame(cmd.getParameter(1));
				if (game == null) {
					cmd.getSender().sendToClient("FAIL Game creation failed");
					return false;
				}
				invite = new GameInvite(this, inviteId.getAndIncrement(), cmd, game);
				this.invites.put(invite.getId(), invite);
				
				Stream<ClientIO> targetStream = clients.stream().filter(cl -> cl.getName().equals(target));
				Optional<ClientIO> result = targetStream.findFirst();
				if (result.isPresent()) {
					invite.sendInvite(result.get());
				}
				else cmd.getSender().sendToClient("FAIL No such user");
				return result.isPresent();
			case "INVY":
				invite = invites.get(cmd.getParameterInt(1));
				if (invite == null) {
					cmd.getSender().sendToClient("FAIL Invalid invite id");
					return false;
				}
				return invite.inviteAccept(cmd.getSender());
			case "INVN":
				invite = invites.get(cmd.getParameterInt(1));
				if (invite == null) {
					cmd.getSender().sendToClient("FAIL Invalid invite id");
					return false;
				}
				return invite.inviteDecline(cmd.getSender());
			default:
				throw new AssertionError("Invalid command: " + cmd);
		}
	}
	
	public void incomingGameCommand(Command cmd) {
		Game game = games.get(cmd.getParameterInt(1));
		if (game != null) {
			if (!game.handleMove(cmd))
				cmd.getSender().sendToClient("FAIL Invalid move");
		}
		else cmd.getSender().sendToClient("FAIL Invalid gameid");
	}

	private Game createGame(String parameter) {
		GameFactory suppl = gameFactories.get(parameter);
		if (suppl == null) {
			throw new IllegalArgumentException("No such game factory: " + parameter);
		}
		Game game = suppl.newGame(this, gameId.getAndIncrement());
		this.games.put(game.getId(), game);
		return suppl != null ? game : null;
	}
	
	public Map<Integer, ChatArea> getChats() {
		return new HashMap<>(chats);
	}
	
	public Map<Integer, Game> getGames() {
		return new HashMap<>(games);
	}
	
	public Map<Integer, GameInvite> getInvites() {
		return new HashMap<>(invites);
	}

	public void addConnections(ConnectionHandler handler) {
		handler.start();
		this.handlers.add(handler);
	}
	
}

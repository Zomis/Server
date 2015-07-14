package net.zomis.server.model

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

import net.zomis.server.messages.ChatMessage;
import net.zomis.server.messages.LoginMessage;
import net.zomis.server.messages.Message;
import net.zomis.server.messages.ServerErrorMessage;
import net.zomis.server.messtransform.FourCharTransform;
import net.zomis.server.messtransform.MessageTransformer;
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
    private final FourCharTransform transformer = new FourCharTransform();

    public Server() {
        this.incomingHandler = new CommandHandler();
        this.newChatRoom("Main");

        Server server = this;

        transformer.registerClass(LoginMessage.class);
        transformer.registerClass(ChatMessage.class);
        transformer.registerClass(ServerErrorMessage.class);

        CommandHandler incomings = server.getIncomingHandler();
        incomings.addHandler(LoginMessage.class, server.&loginRequest);
        incomings.addHandler(ChatMessage.class, server.&incomingChatMessage);

        incomings.addHandler("INVT", {cmd -> server.inviteRequest(cmd)});
        incomings.addHandler("INVY", {cmd -> server.inviteRequest(cmd)});
        incomings.addHandler("INVN", {cmd -> server.inviteRequest(cmd)});
        incomings.addHandler("MOVE", {cmd -> server.incomingGameCommand(cmd)});

        server.addGameFactory("UTTT", {serv, id -> new TTTGame(serv, id)});
        server.addGameFactory("Battleship", {serv, id -> new BattleshipGame(serv, id)});

    }

    @Deprecated
    private ChatArea getMainChat() {
        return chats.get(0);
    }

    public MessageTransformer getTransformer() {
        return transformer;
    }

    public ChatArea newChatRoom(String name) {
        int id = roomCounter.getAndIncrement();
        ChatArea room = new ChatArea(roomCounter.getAndIncrement(), name);
        chats.put(id, room);
        return room;
    }

    public Collection<ClientIO> getClients() {
        return new ArrayList<>(clients);
    }

    public CommandHandler getIncomingHandler() {
        return incomingHandler;
    }

    @Deprecated
    public void handleMessage(ClientIO client, String message) {
        Objects.requireNonNull(client, "Cannot handle message " + message + " from a null client");
        Objects.requireNonNull(message, "Cannot handle null message from " + client);
        if (!incomingHandler.handle(client.parseMessage(message))) {
            handleMessage(client, transformer.stringToMessage(message));
        }
    }

    public void handleMessage(ClientIO client, Message message) {
        Objects.requireNonNull(client, "Cannot handle message " + message + " from a null client");
        Objects.requireNonNull(message, "Cannot handle null message from " + client);
        incomingHandler.handle(message, client);
    }

    public void newClient(ClientIO cl) {
        logger.info("New client: " + cl);
        clients.add(cl);
        getMainChat().add(cl);
    }

    public void onDisconnected(ClientIO client) {
        clients.remove(client);
        broadcast("USER ${client.name} offline");
        getMainChat().remove(client);
    }

    public void loginRequest(LoginMessage cmd, ClientIO sender) {
        logger.info("Login Request! " + cmd);
        boolean usernameTaken = clients.stream().anyMatch({ cl -> cl.getName().equals(cmd.getUsername()) });
        // Perhaps use `Map<String, ClientIO>` for client names?
        boolean allowed = !usernameTaken && sender.login(cmd.getUsername(), cmd.getPassword());
        logger.info("Login request: " + sender + " -- " + cmd);
        if (!allowed) {
            sender.sendToClient("FAIL " + usernameTaken); // TODO: Add proper protocol/messages for login denied
            sender.close();
        }
        else {
            sender.sendToClient("WELC " + sender.getName());
            broadcast("STUS " + sender.getName() + " online");
            clients.stream()
                    .filter({cl -> cl.isLoggedIn()})
                    .filter({cl -> !cl.getName().equals(sender.getName())}) // send information about the clients who were already connected
                    .forEach({cl -> sender.sendToClient("STUS " + cl.getName() + " " + cl.getStatus())});
        }
    }

    void broadcast(String data) {
        clients.forEach({cl -> cl.sendToClient(data)});
    }

    public void incomingChatMessage(ChatMessage cmd, ClientIO sender) {
        ChatArea room = this.chats.get(cmd.getChatId());
        if (room == null) {
            sender.sendToClient(new ServerErrorMessage("Invalid chat id: " + cmd.getChatId()));
            return;
        }
        room.broadcast(sender.getName() + ": " + cmd.getMessage());
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

                Stream<ClientIO> targetStream = clients.stream().filter({cl -> cl.getName().equals(target)});
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

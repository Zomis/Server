package net.zomis.server.model;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import net.zomis.server.clients.ClientIO;

import net.zomis.server.messages.Message;
import net.zomis.server.messages.outgoing.NewGameMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Game<T> {
	private static final Logger logger = LogManager.getLogger(Game.class);

	private final List<ClientIO> players;
    private final List<PlayerInGame<T>> gamePlayers = Collections.synchronizedList(new ArrayList<>());
	private final int id;

	private Instant active;
	private GameState state;
//	private final Set<ClientIO> observers;
//	private final ChatArea chat;
	
	public Game(Server server, int id) {
		this.id = id;
		this.players = Collections.synchronizedList(new ArrayList<>());
		this.state = GameState.NOT_STARTED;
		this.active = Instant.now();
//		this.chat = server.newChatRoom(this.toString());
	}

    protected abstract boolean handleMove(GameMove move, PlayerInGame<T> player);

    public boolean handleMove(GameMove move, ClientIO sender) {
        int index = players.indexOf(sender);
        if (index < 0 || index >= gamePlayers.size()) {
            throw new IllegalArgumentException("Invalid sender: " + sender + " with index " + index +
                    " game players contains: " + gamePlayers);
        }
        PlayerInGame<T> playerInGame = gamePlayers.get(index);
        Objects.requireNonNull(playerInGame, "No player found for " + index);
        this.active = Instant.now();
        return handleMove(move, playerInGame);
    }

    public int aiPlayLoop() {
        int aiMoves = 0;
        boolean played = true;
        while (played) {
            logger.info("Play loop");
            played = false;
            for (PlayerInGame<T> pig : gamePlayers) {
                AI<T> ai = pig.getAi();
                if (pig.hasTurn() && ai != null) {
                    logger.info("AI is " + ai + " and " + pig + " has turn");
                    GameMove move = ai.play(pig);
                    boolean moveMade = handleMove(move, pig);
                    if (moveMade) {
                        aiMoves++;
                    }
                    played = played || moveMade;
                }
            }
        }
        return aiMoves;
    }

	protected abstract void updateStatus();

	public void endGame() {
		if (state == GameState.ENDED) {
			throw new IllegalStateException("Game can only be ended once");
		}
		logger.info("Game Ended: " + this + " with players " + players);
		this.send("GEND " + this.id);
		this.active = Instant.now();
		this.state = GameState.ENDED;
	}

	public boolean isGameOver() {
		return state == GameState.ENDED;
	}
	
	public void start(List<ClientIO> players) {
		if (state != GameState.NOT_STARTED) {
			throw new IllegalStateException("Game can only be started once");
		}
		this.players.addAll(players);
        ListIterator<ClientIO> playerIterator = players.listIterator();
        while (playerIterator.hasNext()) {
            int idx = playerIterator.nextIndex();
            ClientIO player = playerIterator.next();
            T data = createPlayerData(idx);
            PlayerInGame<T> playerInGame = new PlayerInGame<>(this, data, player, idx);
            gamePlayers.add(playerInGame);
        }
		gamePlayers.forEach(pl -> pl.getClient().sendToClient(new NewGameMessage(this.id, pl.getIndex())));
		this.onStart();
		this.active = Instant.now();
		this.state = GameState.RUNNING;
	}

	protected abstract void onStart();
    protected abstract T createPlayerData(int idx);

    @Deprecated
    public void send(String data) {
        players.forEach(pl -> pl.sendToClient(data));
    }

    public void send(Message data) {
        players.forEach(pl -> pl.sendToClient(data));
    }

    public int getId() {
		return id;
	}
	
	public Duration getLastActive() {
		return Duration.between(active, Instant.now());
	}
	
	public GameState getState() {
		return state;
	}

    public abstract boolean playerCanMove(PlayerInGame<T> p);

    public PlayerInGame<T> getPlayingPlayer(int index) {
        return gamePlayers.get(index);
    }
}

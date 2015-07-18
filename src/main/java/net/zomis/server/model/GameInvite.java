package net.zomis.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.zomis.server.clients.ClientIO;

import net.zomis.server.messages.both.InviteRequest;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class GameInvite {
	private static final Logger logger = LogManager.getLogger(GameInvite.class);
	
	private final int	id;
	private final ClientIO	host;
	private final Game	game;
	private final List<ClientIO> invited;
	private final List<ClientIO> players;

	private final String details;
    private final String gameType;

    public GameInvite(Server server, int id, InviteRequest request, ClientIO sender, Game game) {
		this.id = id;
		this.host = sender;
        this.gameType = request.getGameType();
		this.details = request.getParameters();
		this.game = game;
		this.invited = Collections.synchronizedList(new ArrayList<>());
		this.players = Collections.synchronizedList(new ArrayList<>());
		players.add(host);
	}

	public int getId() {
		return id;
	}
	
	public void sendInvite(ClientIO to) {
        InviteRequest message = new InviteRequest();
        message.setGameType(gameType);
        message.setInviteId(id);
        message.setParameters(details);
        message.setWho(host.getName());
        this.invited.add(to);
        to.sendToClient(message);
	}

	public boolean inviteAccept(ClientIO who) {
		logger.info(this + " Invite Accept: " + who + " contains? " + invited.contains(who));
		if (!invited.remove(who)) {
			return false;
		}
		players.add(who);
		
		if (players.size() == 2) {
			return start();
		}
		return true;
	}

	public boolean inviteDecline(ClientIO who) {
		logger.info(this + " Invite Decline: " + who);
		return invited.remove(who);
	}
	
	public boolean start() {
		logger.info(this + " Game Start! " + players);
		game.start(players);
		return true;
	}
	
}
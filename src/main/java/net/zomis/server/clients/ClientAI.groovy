package net.zomis.server.clients

import net.zomis.server.messages.LoginMessage
import net.zomis.server.messages.Message
import net.zomis.server.messages.both.InviteRequest
import net.zomis.server.messages.both.InviteResponse
import net.zomis.server.model.AI
import net.zomis.server.model.Server
import org.apache.log4j.LogManager
import org.apache.log4j.Logger

import java.util.function.Consumer

class ClientAI implements Consumer<Message> {

    private static final Logger logger = LogManager.getLogger(ClientAI)

    String gameType
    AI<?> ai
    ClientIO io

    ClientAI(ClientIO io) {
        this.io = io
    }

    @Override
    void accept(Message data) {
        logger.info("AI ${io.name} received $data")
        if (data instanceof InviteRequest) {
            def invite = data as InviteRequest
            boolean accept = (invite.gameType == this.gameType)
            io.sentToServer(new InviteResponse(inviteId: invite.inviteId, accepted: accept))
        }
    }

/*    static ClientAI createAI(Server server, String gameType, String name, AI<?> ai) {
        ClientAI client = new ClientAI(server)
        client.gameType = gameType
        client.ai = ai
        server.newClient(client)
        client.sentToServer(new LoginMessage(username: "#AI_${gameType}_${name}", password: "AI", client: "AI"))
        client
    }*/

}

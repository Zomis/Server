package net.zomis.server

import net.zomis.server.clients.FakeClient
import net.zomis.server.messages.LoginMessage
import net.zomis.server.model.AI
import net.zomis.server.model.Game
import net.zomis.server.model.Server
import org.apache.log4j.PropertyConfigurator

class TestServer {

    private Server server

    TestServer() {
        this.server = new Server()
    }

    static TestServer create() {
        PropertyConfigurator.configure(Server.class.getResourceAsStream("log4j.properties"));
        new TestServer()
    }

    Game getGame(int id) {
        server.games.get(id)
    }

    TestClient createClient() {
        def fakeClient = new FakeClient(server)
        def client = new TestClient(server, fakeClient)
        fakeClient.addConsumerString(client.&handleString)
        fakeClient.addConsumer(client.&handleMessage)
        server.newClient(fakeClient)
        client
    }

}

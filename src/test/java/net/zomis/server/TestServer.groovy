package net.zomis.server

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
        def client = new TestClient(server)
        server.newClient(client)
        client
    }

}

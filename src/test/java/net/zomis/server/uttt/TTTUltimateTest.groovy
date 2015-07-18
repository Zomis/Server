package net.zomis.server.uttt

import net.zomis.server.TestServer
import net.zomis.server.model.Game
import net.zomis.server.model.GameState
import org.junit.Test

class TTTUltimateTest {

    @Test
    public void testTTTGame() {
        TestServer server = TestServer.create()

        def client1 = server.createClient()
        client1.sentToServer("USER xxx TestOne password")
        client1.expect("WELC TestOne")

        def client2 = server.createClient()
        client2.sentToServer("USER xxx TestTwo password")
        client2.expect("WELC TestTwo")

        client1.expect("STUS TestOne online")
        client1.expect("STUS TestTwo online")
        client2.expect("STUS TestTwo online")
        client2.expect("STUS TestOne online")

        client1.sentToServer("INVT UTTT TestTwo")
        client2.expect("INVT 0 UTTT TestOne")

        Game game = server.getGame(0)
        assert game
        assert game.getState() == GameState.NOT_STARTED

        client2.sentToServer("INVR 0 1")
        client2.expect("NEWG 0 1")
        client1.expect("NEWG 0 0")
        assert game.getState() == GameState.RUNNING

        client1.sentToServer("MOVE 0 4 2");
        client1.expect("MOVE 0 4 2")
        client2.expect("MOVE 0 4 2")

        client1.assertNoMessages()
        client2.assertNoMessages()
    }

}
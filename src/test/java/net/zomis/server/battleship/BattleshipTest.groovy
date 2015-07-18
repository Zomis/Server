package net.zomis.server.battleship

import net.zomis.server.TestServer
import net.zomis.server.messages.outgoing.NewGameMessage
import org.junit.Test

class BattleshipTest {

    @Test
    void sinkShipTest() {
        TestServer server = TestServer.create()
        def clientA = server.createClient()
        clientA.sentToServer("USER Test A AAA")

        def clientB = server.createClient()
        clientB.sentToServer("USER Test B BBB")

        clientA.sentToServer("INVT Battleship ${clientB.name}")
        clientB.sentToServer("INVR 0 1")
        clientA.awaitUntil NewGameMessage
        clientA.expectStr({it.startsWith('CONF')})
        clientA.sentToServer("SHIP 0 Air_Carrier 5 1 0 0 Battleship 4 1 0 1 Submarine 3 1 0 2 Submarine 3 1 0 3 Patrol 2 1 0 4")
        clientB.sentToServer("SHIP 0 Air_Carrier 5 1 0 0 Battleship 4 1 0 1 Submarine 3 1 0 2 Submarine 3 1 0 3 Patrol 2 1 0 4")

        clientA.sentToServer("MOVE 0 0 0")
        clientA.sentToServer("MOVE 0 1 0")
        clientA.sentToServer("MOVE 0 2 0")
        clientA.sentToServer("MOVE 0 3 0")
        clientA.sentToServer("MOVE 0 4 0")
        clientA.sentToServer("MOVE 0 5 0")
        clientA.awaitUntilStr({it == 'MOVE 0 SINK 0 Air_Carrier 5 1 0 0'})
        clientA.expect('MOVE 0 5 0 0 MISS')
        clientA.expect('MOVE 0 TURN 1')
    }

}

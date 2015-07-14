package net.zomis.server.uttt;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.Queue;

import net.zomis.server.clients.FakeClient;
import net.zomis.server.model.Game;
import net.zomis.server.model.GameState;
import net.zomis.server.model.Server;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

public class TTTUltimateTest {

	@Test
	public void das() {
		PropertyConfigurator.configure(Server.class.getResourceAsStream("log4j.properties"));
		
		assertEquals(0, 0);
		Server server = new Server();
		Queue<String> listOne = new ArrayDeque<>();
		Queue<String> listTwo = new ArrayDeque<>();
		FakeClient client1 = new FakeClient(server, listOne::add);
		server.newClient(client1);
		
		client1.sentToServer("USER xxx TestOne password");
		assertEquals("WELC TestOne", listOne.poll());
		
		FakeClient client2 = new FakeClient(server, listTwo::add);
		server.newClient(client2);
		
		client2.sentToServer("USER xxx TestTwo password");
		
		assertEquals("WELC TestTwo", listTwo.poll());
		assertEquals("STUS TestOne online", listOne.poll());
		assertEquals("STUS TestTwo online", listOne.poll());
		assertEquals("STUS TestTwo online", listTwo.poll());
		assertEquals("STUS TestOne online", listTwo.poll());
		
		client1.sentToServer("INVT UTTT TestTwo");
		assertEquals("INVT 0 UTTT TestOne", listTwo.poll());
		
		Game game = server.getGames().get(0);
		assertNotNull(game);
		assertEquals(GameState.NOT_STARTED, game.getState());
		
		client2.sentToServer("INVR 0 1");
        assertEquals("NEWG 0 1", listTwo.poll());
		assertEquals("NEWG 0 0", listOne.poll());
		assertEquals(GameState.RUNNING, game.getState());
		
		client1.sentToServer("MOVE 0 4 2");
		assertEquals("MOVE 0 4 2", listOne.poll());
		assertEquals("MOVE 0 4 2", listTwo.poll());
		
		assertTrue(listOne.isEmpty());
		assertTrue(listTwo.isEmpty());
	}
	
	
}

package net.zomis.server.games.battleship

import net.zomis.server.games.Battleship
import net.zomis.server.messages.FourChar
import net.zomis.server.messages.Message
import net.zomis.server.model.GameMove

@FourChar(value = 'SHIP',
    incomingStr = {String[] mess ->
        List<Battleship> ships = []
        for (int i = 1; i < mess.length - 2; i += 5) {
            String name = mess[i + 1];
            if (name.equals("")) {
//            this.send("ERRR", "Player " + player + " has not the right amount of shipModels");
//            this.endGame();
                return false;
            }
            int width = mess[i + 2] as int
            int height = mess[i + 3] as int
            int x = mess[i + 4] as int
            int y = mess[i + 5] as int
            Battleship ship = new Battleship(name, width, height, x, y);
            ships << ship
        }
        new ShipPlacementMove(ships: ships)
    }
)
class ShipPlacementMove extends GameMove implements Message {

    List<Battleship> ships

}

package net.zomis.server.games;

import net.zomis.server.model.Command;
import net.zomis.server.model.Game;
import net.zomis.server.model.Server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BattleshipGame extends Game {

    private final int mapWidth = 10;
    private final int mapHeight = 10;
    private boolean shipsChosen[] = new boolean[2];
    private int currentPlayer;
    private List<Battleship> shipModels = new ArrayList<>();
    private List<List<Battleship>> playerShips = new ArrayList<>();

    public BattleshipGame(Server server, int id) {
        super(server, id);
        this.playerShips.add(new ArrayList<>());
        this.playerShips.add(new ArrayList<>());
        this.shipModels.add(Battleship.AIR_CARRIER);
        this.shipModels.add(Battleship.BATTLESHIP);
        this.shipModels.add(Battleship.SUBMARINE);
        this.shipModels.add(Battleship.SUBMARINE);
        this.shipModels.add(Battleship.PATROL_BOAT);
    }

    @Override
    protected boolean makeMove(Command command, int player) {
        if (command.getParameter(2).equals("SHIP")) {
            if (shipsChosen[player]) {
                this.send("ERRR", "Player " + player + " has already placed the ships. Disqualified");
                this.endGame();
            } else {
                shipsChosen[player] = true;
                placeShips(player, command);
                if (shipsChosen[0] && shipsChosen[1]) {
                    this.send("MOVE", "TURN " + currentPlayer);
                } else {
                    this.send("MOVE", "OK");
                }
            }
            return true;
        }
        if (command.getParameter(2).equals("PLAY")) {
            if (!shipsChosen[0] || !shipsChosen[1]) {
                this.send("ERRR", "Player " + player + " is making a move before opponent have placed ships. Disqualified");
            }
            if (currentPlayer != player) {
                this.send("ERRR", "Player " + player + " is not in turn to play. Disqualified");
                this.endGame();
            } else {
                boolean hit = makeMove(player, command.getParameterInt(3), command.getParameterInt(4));
                this.send(command.getFullCommand() + " " + player + " " + (hit ? "HIT" : "MISS"));
                if (!hit) {
                    currentPlayer = 1 - player;
                    this.send("MOVE", "TURN " + currentPlayer);
                }
            }
            return true;
        }
        return false;
    }

    private boolean placeShips(int player, Command command) {
        List<Battleship> yourShips = this.playerShips.get(player);
        Iterator<Battleship> it = shipModels.iterator();
        int i = 2;
        while (it.hasNext()) {
            Battleship ship = it.next();
            String name = command.getParameter(i + 1);
            if (name.equals("")) {
                this.send("ERRR", "Player " + player + " has not the right amount of shipModels");
                this.endGame();
                return false;
            }
            int width = command.getParameterInt(i + 2);
            int height = command.getParameterInt(i + 3);
            int x = command.getParameterInt(i + 4);
            int y = command.getParameterInt(i + 5);
            boolean correctUnflipped = ship.getWidth() == width && ship.getHeight() == height;
            boolean correctFlipped = ship.getWidth() == height && ship.getHeight() == width;
            System.out.printf("Received name %s, size %d x %d. Pos %d, %d.%n", name, width, height, x, y);
            System.out.println("flipped status: " + correctFlipped + ", " + correctUnflipped);
            Battleship yourShip = ship.atPos(correctFlipped, x, y);
            if (!correctFlipped && !correctUnflipped) {
                this.send("ERRR", "Player " + player + " sent incorrect dimensions of ship: Expected " + ship + " but found " + yourShip);
                this.endGame();
                return false;
            }

            if (!yourShip.inRange(mapWidth, mapHeight)) {
                this.send("ERRR", "Ship " + yourShip + " does not fit inside range: " + mapWidth + "x" + mapHeight);
                this.endGame();
                return false;
            }

            Optional<Battleship> collision = yourShip.collidesWith(yourShips);
            if (collision.isPresent()) {
                this.send("ERRR", "Ship " + yourShip + " collides with " + collision.get());
                this.endGame();
                return false;
            }

            yourShips.add(yourShip);
            i += 5;
        }
        return true;
    }

    private boolean makeMove(int player, int x, int y) {
        int opponent = 1 - player;
        List<Battleship> ships = playerShips.get(opponent);
        Iterator<Battleship> it = ships.iterator();
        boolean hit = false;
        while (it.hasNext()) {
            Battleship ship = it.next();
            hit = hit | ship.sink(x, y);
            if (!ship.isAlive()) {
                it.remove();
            }
        }
        if (ships.isEmpty()) {
            // All ships gone, player wins!
            this.send("GEND", "Player " + player + " wins!");
            this.endGame();
        }
        return hit;
    }

    private void send(String type, String message) {
        send(type + " " + getId() + " " + message);
    }

    @Override
    protected void updateStatus() {

    }

    @Override
    protected void onStart() {
        String message = String.join(" ", shipModels.stream().map(ship -> ship.getModelString()).collect(Collectors.toList()));
        send("CONF", mapWidth + " " + mapHeight + " " + message);
    }
}

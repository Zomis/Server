package net.zomis.server.games;

import java.util.List;

/**
 * Created by Simon on 5/9/2015.
 */
public class Battleship {

    private final String name;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final boolean[][] sunk;
    private int healthLeft;

    public static final Battleship AIR_CARRIER = new Battleship("Air_Carrier", 5, 1);
    public static final Battleship BATTLESHIP = new Battleship("Battleship", 4, 1);
    public static final Battleship SUBMARINE = new Battleship("Submarine", 3, 1);
    public static final Battleship PATROL_BOAT = new Battleship("Patrol", 2, 1);

    public Battleship(String name, int width, int height) {
        this(name, width, height, -1, -1);
    }

    public Battleship(String name, int width, int height, int x, int y) {
        this.name = name;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.sunk = new boolean[height][width];
        this.healthLeft = width * height;
    }

    public boolean isModel() {
        return x < 0 || y < 0;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public Battleship atPos(boolean flipped, int x, int y) {
        return new Battleship(this.name, flipped ? width : height, flipped ? height : width,
                x, y);
    }

    public boolean inRange(int mapWidth, int mapHeight) {
        return x >= 0 && y >= 0 && x + width < mapWidth && y + height < mapHeight;
    }

    public String getModelString() {
        return name + " " + width + " " + height;
    }

    public boolean sink(int bombX, int bombY) {
        int localX = bombX - x;
        int localY = bombY - y;
        boolean withinShip = localX >= 0 && localX < width && localY >= 0 && localY < height;
        if (withinShip) {
            if (this.sunk[localY][localX]) {
                throw new IllegalArgumentException("Already bombed there");
            }
            this.sunk[localY][localX] = true;
            this.healthLeft--;
        }
        return withinShip;
    }

    public boolean isAlive() {
        return healthLeft > 0;
    }

    public boolean collidesWith(List<Battleship> ships) {
        return ships.stream().anyMatch(ship -> this.collidesWith(ship));
    }

    private boolean collidesWith(Battleship ship) {
        if (ship.x > x + width)
            return false;
        if (ship.y > y + height)
            return false;
        if (ship.x + ship.width < x)
            return false;
        if (ship.y + ship.height < y)
            return false;
        return true;
    }

}

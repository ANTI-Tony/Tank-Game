package Tanks;

import processing.core.PImage;
import java.util.ArrayList;

import static Tanks.App.tanks;

/**
 * Represents a tank in the game.
 */
public class Tank {
    private int x;
    private int y;
    private final int r;
    private final int g;
    private final int b;
    private int health = 100;
    private int fuel;
    private int power;
    private int parachutes;
    private int scoreGained = 0;
    private float turretAngle;
    public static float PI = (float) Math.PI;
    static float turretAngleSpeed = (float) Math.PI / 2f; // Rotate 3 radians per second
    static int moveSpeed = 60; // Move 60 pixels per second
    static int powerChangeRate = 36; // Power changes by 36 units per second
    private final String playerName;
    public boolean isCurrentTurn = false;
    private boolean isSelectingTeleport = false;
    boolean isUsingParachute = false;
    ArrayList<Projectile> projectiles = new ArrayList<>();

    /**
     * Constructs a new Tank object.
     *
     * @param name the name of the player controlling the tank
     * @param x    the x-coordinate of the tank's position
     * @param y    the y-coordinate of the tank's position
     * @param r    the red component of the tank's color
     * @param g    the green component of the tank's color
     * @param b    the blue component of the tank's color
     */
    public Tank(String name, int x, int y, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.fuel = 250; // Initial fuel is 250
        this.power = 50; // Initial power is 50
        this.parachutes = App.INITIAL_PARACHUTES; // Initial number of parachutes
        this.turretAngle = -PI/2; // Initial turret angle is -90 degrees (pointing vertically upwards)
        this.playerName = name;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    /**
     * Moves the tank to the left.
     */
    public static void moveLeft() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        int newX = currentTank.getX() - moveSpeed / App.FPS;
        int newY = App.terrainHeightArray[newX];
        int fuelConsumed = currentTank.getX() - newX; // Calculate the fuel consumed
        if (currentTank.getFuel() >= fuelConsumed) { // If there is enough fuel
            currentTank.setX(newX);
            currentTank.setY(newY);
            currentTank.setFuel(currentTank.getFuel() - fuelConsumed); // Reduce the fuel
        }
    }

    /**
     * Moves the tank to the right.
     */
    public static void moveRight() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        int newX = currentTank.getX() + moveSpeed / App.FPS;
        int newY = App.terrainHeightArray[newX];
        int fuelConsumed = newX - currentTank.getX(); // Calculate the fuel consumed
        if (currentTank.getFuel() >= fuelConsumed) { // If there is enough fuel
            currentTank.setX(newX);
            currentTank.setY(newY);
            currentTank.setFuel(currentTank.getFuel() - fuelConsumed); // Reduce the fuel
        }
    }

    /**
     * Moves the tank's turret to the left.
     */
    public static void turretMoveLeft() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        currentTank.setTurretAngle(constrain(currentTank.getTurretAngle() + turretAngleSpeed / App.FPS, -PI, 0));
    }

    /**
     * Moves the tank's turret to the right.
     */
    public static void turretMoveRight() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        currentTank.setTurretAngle(constrain(currentTank.getTurretAngle() - turretAngleSpeed / App.FPS, -PI, 0));
    }

    private static float constrain(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Increases the tank's power level.
     */
    public static void increasePower() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        int newPower = currentTank.getPower() + powerChangeRate / App.FPS;
        newPower = Math.min(newPower, currentTank.getHealth()); // Ensure power does not exceed health
        currentTank.setPower(newPower);
    }

    /**
     * Decreases the tank's power level.
     */
    public static void decreasePower() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        int newPower = currentTank.getPower() - powerChangeRate / App.FPS;
        newPower = Math.max(newPower, 0); // Ensure power does not go below 0
        currentTank.setPower(newPower);
    }

    /**
     * Fires a projectile from the tank.
     */
    public static void fireProjectile() {
        // If the current player is dead, switch to the next player
        while (tanks.get(App.currentPlayerIndex).getHealth() <= 0) {
            App.currentPlayerIndex = (App.currentPlayerIndex + 1) % tanks.size();
        }

        // Get the current player's tank based on currentPlayerIndex
        Tank currentTank = tanks.get(App.currentPlayerIndex);

        // Ensure the power level does not exceed the tank's health
        int power = Math.min(currentTank.getPower(), currentTank.getHealth());

        Projectile projectile = new Projectile(currentTank.getX(), currentTank.getY(), currentTank.getTurretAngle(), power, currentTank.getR(), currentTank.getG(), currentTank.getB(), currentTank.getPlayerName());
        currentTank.projectiles.add(projectile);

        Tank nextTank = tanks.get((Tanks.App.currentPlayerIndex + 1) % tanks.size());
        nextTank.setIsCurrentTurn(true); // Set the flag for the next player to true
    }

    /**
     * Uses a repair kit to restore the tank's health.
     */
    public static void useRepairKit() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        if (currentTank.getScoreGained() >= 20) {
            currentTank.setHealth(Math.min(currentTank.getHealth() + 20, 100));
            currentTank.addScore(-20); // Deduct the spent 20 points
        }
    }

    /**
     * Adds fuel to the tank.
     */
    public static void addFuel() {
        Tank currentTank = tanks.get(App.currentPlayerIndex);
        if (currentTank.getScoreGained() >= 10) {
            currentTank.setFuel(currentTank.getFuel() + 200);
            currentTank.addScore(-10); // Deduct the spent 10 points
        }
    }

    public int getScoreGained() {
        return scoreGained;
    }

    /**
     * Adds the specified score to the tank's score.
     *
     * @param score the score to add
     */
    public void addScore(int score) {
        scoreGained += score;
    }

    public boolean isSelectingTeleport() {
        return isSelectingTeleport;
    }

    public void setSelectingTeleport(boolean selectingTeleport) {
        isSelectingTeleport = selectingTeleport;
    }

    /**
     * Slows down the tank's fall when using a parachute.
     */
    public void slowFall() {
        if (isUsingParachute && y < App.terrainHeightArray[x]) {
            y += 30 / App.FPS; // Slowly descend 60 pixels per second
        } else {
            isUsingParachute = false;
        }
    }

    /**
     * Makes the tank fall quickly when not using a parachute.
     */
    public void fastFall() {
        if (y < App.terrainHeightArray[x]) {
            int fallDistance = Math.min(120 / App.FPS, App.terrainHeightArray[x] - y); // Quickly descend 120 pixels per second
            y += fallDistance;
            health -= fallDistance;

            // Add the damage value to the score of the player who fired the projectile that caused the terrain destruction
            Tank firingTank = App.tanks.get(App.currentPlayerIndex);
            firingTank.addScore(fallDistance);
        }
    }

    // getter setter
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getFuel() {
        return fuel;
    }

    public void setFuel(int fuel) {
        this.fuel = fuel;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getParachutes() {
        return parachutes;
    }

    public void setParachutes(int parachutes) {
        this.parachutes = parachutes;
    }

    public float getTurretAngle() {
        return turretAngle;
    }

    public void setTurretAngle(float angle) {
        this.turretAngle = angle;
    }

    public void setIsCurrentTurn(boolean isCurrentTurn) {
        this.isCurrentTurn = isCurrentTurn;
    }

    public String getPlayerName() {
        return playerName;
    }

    /**
     * Draws the tank on the screen.
     *
     * @param app the App object representing the game
     */
    public void draw(App app) {
        if (isUsingParachute) {
            PImage parachuteImage = app.loadImage("Tanks/parachute.png");
            app.image(parachuteImage, x - 15 - 15/2, y - 50, 50, 50);
            slowFall();
        } else {
            fastFall();
        }

        // Draw the tank image here
        app.stroke(255); // Black outline
        app.strokeWeight(0); // Outline thickness
        app.fill(r, g, b); // Fill the tank color
        app.rect(x + 3 - 15/2, y - 4, 15, 4);
        app.rect(x - 15/2, y, 20, 4); // Draw the main body rectangle of the tank
        // Draw the barrel
        app.fill(0);
        app.pushMatrix();
        app.translate(x+10-15/2, y+2);
        app.rotate(turretAngle);
        app.rect(0, -2, 15, 4);
        app.popMatrix();

        if (isSelectingTeleport) {
            app.fill(255, 255, 0, 128);
            app.ellipse(app.mouseX, app.mouseY, 30, 30);
        }
    }

    public void setUsingParachute(boolean usingParachute) {
        isUsingParachute = usingParachute;
    }
}
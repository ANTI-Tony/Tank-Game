package Tanks;

import processing.core.PVector;
import processing.core.PApplet;

/**
 * Represents a projectile fired by a tank.
 */
public class Projectile {
    float x;
    float y;  // position of the shell
    private float vx, vy;  // The speed of the cannonball
    int r;
    int g;
    int b;  // The color of the shell (same as the tank it was fired from)
    float power;  // Initial strength of the shell
    public boolean exploded = false;
    public float explodeX;
    public float explodeY;
    public float explosionRadius = 30;
    public int explodeStartTime;
    public static final float GRAVITY = 3.6f/App.FPS;
    private final PVector wind;
    String name;

    /**
     * Constructs a new Projectile object.
     *
     * @param x     the x-coordinate of the projectile's initial position
     * @param y     the y-coordinate of the projectile's initial position
     * @param angle the angle at which the projectile is fired
     * @param power the power of the projectile
     * @param r     the red component of the projectile's color
     * @param g     the green component of the projectile's color
     * @param b     the blue component of the projectile's color
     * @param name  the name of the player who fired the projectile
     */
    public Projectile(float x, float y, float angle, float power, int r, int g, int b, String name) {
        this.x = x;
        this.y = y;
        this.power = power;
        this.r = r;
        this.g = g;
        this.b = b;
        this.name = name;
        this.wind = new PVector(App.wind, 0);

        // The initial velocity is calculated based on the launch Angle and fire power
        float speed = map(power, 0, 100, 1, 9); // Mapping fire to speed ranges [1, 9]
        vx = speed * cos(angle);
        vy = speed * sin(angle);
    }

    /**
     * Updates the position and state of the projectile.
     */
    public void update() {
        // Update projectile position
        x += vx;
        y += vy;
        vy += GRAVITY; // Apply gravity acceleration

        // Apply wind force
        vx += wind.x * 0.002f; // Acceleration approximately equal to wind * 0.002


        // Check if the projectile hits the terrain
        if (x >= 0 && x < App.terrainHeightArray.length) {
            int terrainHeight = App.terrainHeightArray[(int)x];
            if (y >= terrainHeight) {
                explode(x, terrainHeight);
            }
        }
    }

    /**
     * Explodes the projectile at the specified position.
     *
     * @param x the x-coordinate of the explosion position
     * @param y the y-coordinate of the explosion position
     */
    public void explode(float x, float y) {
        if (!exploded) {
            exploded = true;
            explodeX = x;
            explodeY = y;
            explodeStartTime = App.instance.millis();

            // Explosion radius
            float explosionRadius = 30;

            // Destroy terrain
            for (int col = (int)(x - explosionRadius); col <= (int)(x + explosionRadius); col++) {
                if (col >= 0 && col < App.terrainHeightArray.length) {
                    for (int row = (int)(y - explosionRadius); row <= (int)(y + explosionRadius); row++) {
                        if (row >= 0 && row < App.HEIGHT) {
                            float distance = dist(col, row, x, y);
                            if (distance <= explosionRadius) {
                                App.terrain[row][col] = 0;
                                if (row > App.terrainHeightArray[col]) {
                                    App.terrainHeightArray[col] = row;
                                }
                            }
                        }
                    }
                }
            }

            // Make the destroyed terrain "fall down"
            for (int col = (int) (x - explosionRadius); col <= (int) (x + explosionRadius); col++) {
                if (col >= 0 && col < App.terrainHeightArray.length) {
                    for (int row = App.terrainHeightArray[col]; row < App.HEIGHT; row++) {
                        if (App.terrain[row][col] == 0) {
                            App.terrain[row][col] = 1;
                        } else {
                            break;
                        }
                    }
                }
            }

            for (Tank tank : App.tanks) {
                float distance = PApplet.dist(x, y, tank.getX(), tank.getY());
                if (distance <= explosionRadius) {
                    float damage = PApplet.map(distance, 0, explosionRadius, 60, 0);
                    tank.setHealth(tank.getHealth() - (int) damage);

                    // Add the damage value as a score to the tank that fired the projectile
                    if (!tank.getPlayerName().equals(name)) {
                        App.tanks.get(name.charAt(0)-'A').addScore((int) damage);
                        App.playerScores[name.charAt(0)-'A'] += (int) damage; // 50
                    }

                    if (tank.getY() < App.terrainHeightArray[tank.getX()]) {
                        if (tank.getParachutes() > 0) {
                            tank.setParachutes(tank.getParachutes() - 1);
                            tank.setUsingParachute(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws the projectile on the screen.
     *
     * @param app the App object representing the game
     */
    public void draw(App app) {
        // Draw the projectile
        app.fill(r, g, b);
        // Projectile radius
        float radius = 5;
        app.ellipse(x, y, radius * 2, radius * 2);
    }

    // Helper methods
    private float cos(float angle) {
        return (float)Math.cos(angle);
    }

    private float sin(float angle) {
        return (float)Math.sin(angle);
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private float map(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }
}
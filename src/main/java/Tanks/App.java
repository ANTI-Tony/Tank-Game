package Tanks;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import java.util.*;
import ddf.minim.*;

/**
 * The main class of the Tanks game.
 * Extends the PApplet class from the Processing library.
 */
public class App extends PApplet {

    public static int WIDTH = 864;
    public static int HEIGHT = 640;
    public static final int INITIAL_PARACHUTES = 3;
    public static final int FPS = 30;
    public static int currentPlayerIndex = 0;
    public String configPath;
    public static Random random = new Random();

    // Feel free to add any additional methods or attributes you want. Please put classes in different files.

    Map<Integer, String> layoutFiles = new HashMap<>();
    Map<Integer, int[]> foregroundColors = new HashMap<>();
    Map<Integer, PImage> backgroundImages = new HashMap<>();
    Map<Integer, PImage> treeImages = new HashMap<>();
    HashMap<String, int[]> playerColorsMap = new HashMap<>();
    public static int[] playerScores = new int[4];
    public static App instance;
    public boolean gameEnded = false;

    static ArrayList<int[]> trees;
    static ArrayList<Tank> tanks;
    static int[][] terrain;
    static int[] terrainHeightArray;
    int terrainWidth;
    int terrainHeight;
    int levelNumber = 1;
    PImage windLeftImage;
    PImage windRightImage;
    Minim minim;
    AudioSample fireSound;
    AudioSample explodeSound;
    AudioSample buySound;
    AudioSample gameOverSound;
    public AudioSample fixSound;
    AudioSample teleportSound;
    private boolean testMode = false;
    public static int wind;

    /**
     * Constructor for the App class.
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Sets up the game by loading resources, initializing elements, and setting up the layout.
     */
    @Override
    public void setup() {
        frameRate(FPS);
        instance = this;
        minim = new Minim(this);
        fireSound = minim.loadSample("Tanks/fire.wav", 512);
        explodeSound = minim.loadSample("Tanks/explode.wav", 512);
        buySound = minim.loadSample("Tanks/buy.wav", 512);
        gameOverSound = minim.loadSample("Tanks/gameOver.wav", 512);
        teleportSound = minim.loadSample("Tanks/teleport.wav", 512);
        fixSound = minim.loadSample("Tanks/fix.wav", 512);
        windLeftImage = loadImage("Tanks/wind-1.png");
        windRightImage = loadImage("Tanks/wind.png");
        JSONObject jsonObject = loadJSONObject("config.json");
        JSONArray levels = jsonObject.getJSONArray("levels");

        for (int i = 0; i < levels.size(); i++) {
            JSONObject object = levels.getJSONObject(i);

            if (object.hasKey("layout")) {
                layoutFiles.put(i + 1, object.getString("layout"));
            }

            if (object.hasKey("foreground-colour")) {
                String[] colorParts = object.getString("foreground-colour").split(",");
                int[] color = new int[3];
                color[0] = Integer.parseInt(colorParts[0]);
                color[1] = Integer.parseInt(colorParts[1]);
                color[2] = Integer.parseInt(colorParts[2]);
                foregroundColors.put(i + 1, color);
            }

            PImage backgroundImage;
            if (object.hasKey("background")) {
                String backgroundFileName = object.getString("background");
                backgroundImage = loadImage("Tanks/" + backgroundFileName);
                backgroundImages.put(i + 1, backgroundImage);
            }

            PImage treeImage;
            if (object.hasKey("trees")) {
                String treeFileName = object.getString("trees");
                treeImage = loadImage("Tanks/" + treeFileName);
                treeImages.put(i + 1, treeImage);
            } else {
                treeImages.put(i + 1, null);
            }
        }

        JSONObject playerColors = jsonObject.getJSONObject("player_colours");
        Set<String> keys = playerColors.keys();
        for (String key : keys) {
            String[] colorParts = playerColors.getString(key).split(",");
            if (colorParts.length == 3) {
                int[] color = new int[]{Integer.parseInt(colorParts[0]), Integer.parseInt(colorParts[1]), Integer.parseInt(colorParts[2])};
                playerColorsMap.put(key, color);
            } else if (colorParts[0].equals("random")) {
                int[] color = new int[]{(int) random(256), (int) random(256), (int) random(256)};
                playerColorsMap.put(key, color);
            }
        }
        setupLayout();
    }

    /**
     * Sets up the layout of the game level.
     */
    public void setupLayout() {
        String[] lines = loadStrings(layoutFiles.get(levelNumber));
        terrainHeight = lines.length;
        int maxWidth = 0;
        for (String line : lines) {
            if (line.length() > maxWidth) {
                maxWidth = line.length();
            }
        }
        terrainWidth = maxWidth;
        trees = new ArrayList<>();
        tanks = new ArrayList<>();

        terrain = new int[640][864];
        terrainHeightArray = new int[896];
        for (int row = 0; row < terrainHeight; row++) {
            String line = lines[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                switch (c) {
                    case 'X':
                        for(int count = 0; count < 32; count++) {
                            terrainHeightArray[32 * (col) + count] = 32 * row;
                        }
                        break;
                    case 'T':
                        trees.add(new int[]{col * 32, row * 32});
                        break;
                    default:
                        if (playerColorsMap.containsKey(String.valueOf(c))) {
                            int[] color = playerColorsMap.get(String.valueOf(c));
                            tanks.add(new Tank(String.valueOf(c), col * 32, row * 32, color[0], color[1], color[2]));
                        }
                        break;
                }
            }
        }
        tanks.sort(new Comparator<Tank>() {
            @Override
            public int compare(Tank tank1, Tank tank2) {
                return tank1.getPlayerName().compareTo(tank2.getPlayerName());
            }
        });
        smoothTerrain();
        for (Tank tank : tanks) {
            tank.setParachutes(INITIAL_PARACHUTES);
        }
        initializeWind();
        currentPlayerIndex = 0;
    }

    /**
     * Smooths the terrain by averaging the height values.
     */
    public static void smoothTerrain() {
        for (int iteration = 0; iteration < 2; iteration++) {
            for (int col = 0; col < 864; col++) {
                int sum = 0;

                for (int dx = 0; dx < 32; dx++) {
                    int x = col + dx;
                    sum += terrainHeightArray[x];
                }
                terrainHeightArray[col] = sum/32;
            }
        }

        for (int col = 0; col < 864; col++) {
            int height = terrainHeightArray[col];
            for (int row = 0; row < terrain.length; row++) {
                if (row > height) {
                    terrain[row][col] = 1;
                }
            }
        }

        for (int i = 0; i < trees.size(); i++) {
            int[] tree = trees.get(i);
            int x = tree[0];
            int y = terrainHeightArray[x];
            tree[1] = y;
            trees.set(i, tree);
        }

        for (Tank tank : tanks) {
            int x = tank.getX();
            int y = terrainHeightArray[x];
            tank.setY(y);
        }
    }

    /**
     * Handles key press events.
     *
     * @param event The KeyEvent object.
     */
    @Override
    public void keyPressed(KeyEvent event){
        char key = event.getKey();

        if (keyCode == UP) {
            Tank.turretMoveRight();
        }
        if (keyCode == DOWN) {
            Tank.turretMoveLeft();
        }
        if (keyCode == LEFT) {
            Tank.moveLeft();
        }
        if (keyCode == RIGHT) {
            Tank.moveRight();
        }
        switch(key){
            case 'w':
            case 'W':
                Tank.increasePower();
                break;
            case 's':
            case 'S':
                Tank.decreasePower();
                break;
            case 'r':
            case 'R':
                Tank.useRepairKit();
                fixSound.trigger();
                break;
            case 'f':
            case 'F':
                Tank.addFuel();
                buySound.trigger();
                break;
            case 't':
            case 'T':
                Tank currentTank = tanks.get(currentPlayerIndex);
                if (currentTank.getParachutes() > 0) {
                    currentTank.setSelectingTeleport(true);
                }
                break;
            case ' ':
                Tank.fireProjectile();
                updateWind();
                fireSound.trigger();
                while (tanks.get((currentPlayerIndex + 1) % tanks.size()).getHealth() <= 0) {
                    currentPlayerIndex = (currentPlayerIndex + 1) % tanks.size();
                }
                currentPlayerIndex = (currentPlayerIndex + 1) % tanks.size();
                break;
            default:
                break;
        }

        if (gameEnded && key == 'r') {
            gameEnded = false;
            levelNumber = 1;
            gameOverSound.trigger();
            setup();
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
    @Override
    public void keyReleased(){

    }

    /**
     * Handles mouse press events.
     *
     * @param e The MouseEvent object.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        Tank currentTank = tanks.get(currentPlayerIndex);

        if (currentTank.isSelectingTeleport()) {
            teleportSound.trigger();
            int newX = mouseX;
            int newY = App.terrainHeightArray[newX];

            currentTank.setX(newX);
            currentTank.setY(newY);
            currentTank.setParachutes(currentTank.getParachutes() - 1);
            currentTank.setSelectingTeleport(false);
        }
    }

    /**
     * Handles mouse release events.
     *
     * @param e The MouseEvent object.
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Draw all elements in the game by current frame.
     */
    @Override
    public void draw() {
        if (testMode) {
            for (Tank tank : tanks) {
                for (Projectile projectile : tank.projectiles) {
                    projectile.update();
                }
            }
        } else {
            // draw background
            if (backgroundImages.containsKey(levelNumber))
                background(backgroundImages.get(levelNumber));
            int[] foregroundColor = foregroundColors.get(levelNumber);

            // draw terrian
            fill(255);
            int rows = terrain.length;
            int cols = terrain[0].length;
            loadPixels();
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    int loc = col + row * WIDTH;
                    if (terrain[row][col] == 1) {
                        if (foregroundColor != null)
                            pixels[loc] = color(foregroundColor[0], foregroundColor[1], foregroundColor[2]);
                    }
                }
            }
            updatePixels();

            // Recalculate the terrain height
            for (int col = 0; col < 864; col++) {
                int height = App.terrainHeightArray[col];
                for (int row = 0; row < terrain.length; row++) {
                    if (row > height) {
                        terrain[row][col] = 1;
                    } else {
                        terrain[row][col] = 0;
                    }
                }
            }

            // draw trees
            if (treeImages.containsKey(levelNumber)) {
                PImage treeImage = treeImages.get(levelNumber);
                if (treeImage != null) {
                    for (int[] tree : trees) {
                        image(treeImage, tree[0] - 16, terrainHeightArray[tree[0]] - 25, 30, 30);
                    }
                }
            }

            // draw tanks
            for (Tank tank : tanks) {
                if (tank.getHealth() > 0) {
                    rectMode(PApplet.CORNER);
                    tank.draw(this);
                    rectMode(PApplet.CENTER);
                }
            }

            // draw explosion effect
            for (Tank tank : tanks) {
                for (int i = tank.projectiles.size() - 1; i >= 0; i--) {
                    Projectile projectile = tank.projectiles.get(i);
                    if (projectile.exploded) {
                        float elapsedTime = (millis() - projectile.explodeStartTime) / 1000f;
                        if (elapsedTime <= 0.2f) {
                            float explosionRadius = projectile.explosionRadius;
                            // Calculate the radius of the red outer ring based on the elapsed time
                            float redRadius = map(elapsedTime, 0, 0.2f, 0, explosionRadius);
                            fill(255, 0, 0);
                            ellipse(projectile.explodeX, projectile.explodeY, redRadius * 2, redRadius * 2);

                            // Calculate the radius of the orange middle circle based on the elapsed time
                            float orangeRadius = map(elapsedTime, 0, 0.2f, 0, explosionRadius * 0.5f);
                            fill(255, 165, 0);
                            ellipse(projectile.explodeX, projectile.explodeY, orangeRadius * 2, orangeRadius * 2);

                            // Calculate the radius of the yellow inner circle based on the elapsed time
                            float yellowRadius = map(elapsedTime, 0, 0.2f, 0, explosionRadius * 0.2f);
                            fill(255, 255, 0);
                            ellipse(projectile.explodeX, projectile.explodeY, yellowRadius * 2, yellowRadius * 2);
                        } else {
                            tank.projectiles.remove(i);
                            explodeSound.trigger();
                        }
                    }
                }
            }

            // wind
            textSize(10);
            fill(0);
            // wind image
            if (wind > 0) {
                image(windRightImage, WIDTH - 100, 3, 50, 50);
            } else {
                image(windLeftImage, WIDTH - 100, 3, 50, 50);
            }

            // wind value
            fill(0);
            textSize(16);
            text(wind, WIDTH - 45, 35);

            // fuel
            PImage fuelImage = loadImage("Tanks/fuel.png");
            textSize(10);
            fill(0);
            image(fuelImage, 150, 5, 25, 25);

            // scoreboard
            textSize(15);
            fill(0);
            text("Scores", 720, 65);

            noFill();
            strokeWeight(4);
            stroke(0);
            rect(785, 110, 140, tanks.size() * 20);
            rect(785, 60, 140, 20);

            // sort by key alphabetically
            for (char i = 0; i < playerScores.length; i++) {
                int score = 0;
                int r = 0;
                int g = 0;
                int b = 0;
                for (Tank tank : tanks) {
                    if (tank.getPlayerName().charAt(0) == (i+'A')) {
                        score = playerScores[i];
                        r = tank.getR();
                        g = tank.getG();
                        b = tank.getB();
                    }
                }
                fill(r,g,b);
                text("Player " + (char) (i+'A'), 720, 85 + i * 20);
                fill(0);
                text(score, 835, 85 + i * 20);
            }

            // health bar
            for (int i = 0; i < tanks.size(); i++) {
                Tank currentTank = tanks.get(currentPlayerIndex % tanks.size());
                fill(255);
                strokeWeight(2);
                rectMode(CORNER);
                rect(440, 5, 150, 20);
                fill(currentTank.getR(), currentTank.getG(), currentTank.getB());
                float healthBarWidth = (float) (currentTank.getHealth() * 1.5);
                rect(440, 5, healthBarWidth, 20);
            }

            // power bar
            for (int i = 0; i < tanks.size(); i++) {
                Tank currentTank = tanks.get(currentPlayerIndex % tanks.size());
                noFill();
                strokeWeight(4);
                stroke(128);
                float powerWidth = (float) (currentTank.getPower() * 1.5);
                rect(440, 5, powerWidth, 20);
            }

            //red line on power bar
            for (int i = 0; i < tanks.size(); i++) {
                Tank currentTank = tanks.get(currentPlayerIndex % tanks.size());
                float powerWidth = (float) (currentTank.getPower() * 1.5);
                stroke(255, 0, 0);
                strokeWeight(1);
                line(440 + powerWidth, 2, 440 + powerWidth, 29);
            }

            // turn display
            if (!tanks.isEmpty()) {
                Tank currentTank = tanks.get(currentPlayerIndex % tanks.size());
                fill(0);
                text("Player " + currentTank.getPlayerName() + "'s turn", 20, 20);

                // parachute
                PImage ParachutesImage = loadImage("Tanks/parachute.png");
                textSize(15);
                fill(0);
                image(ParachutesImage, 150, 35, 25, 25);
                text(currentTank.getParachutes(), 183, 55);

                // health、power, fuel
                fill(0);
                text("Health: ", 385, 20);
                text(currentTank.getHealth(), 600, 20);
                text("Power: " + currentTank.getPower(), 385, 40);
                text(currentTank.getFuel(), 180, 25);

                // tp image
                PImage teleportImage = loadImage("Tanks/teleport.png");
                textSize(15);
                fill(0);
                image(teleportImage, 230, 5, 25, 25);
                text(currentTank.getParachutes(), 262, 25);

                // arrows
                stroke(0);
                strokeWeight(3);
                currentTank = tanks.get(currentPlayerIndex);
                line(currentTank.getX() + 3, currentTank.getY() - 80, currentTank.getX() + 3, currentTank.getY() - 40);
                line(currentTank.getX() + 3, currentTank.getY() - 40, currentTank.getX() -7, currentTank.getY() - 50);
                line(currentTank.getX() + 3, currentTank.getY() - 40, currentTank.getX() + 13, currentTank.getY() - 50);
            }

            for (Tank tank : tanks) {
                for (Projectile projectile : tank.projectiles) {
                    projectile.update();
                    projectile.draw(this);
                }
            }

            int aliveTanks = 0;
            for (Tank tank : tanks) {
                if (tank.getHealth() > 0) {
                    aliveTanks++;
                }
            }

            // If there is only one tank left, proceed to the next level
            if (aliveTanks == 1) {
                levelNumber++;
                // if the last level, game end
                if (levelNumber > layoutFiles.size()) {
                    gameEnded = true;
                } else {
                    setupLayout();
                }
            }

            if (gameEnded) {
                fill(255, 255, 255, 200);
                // find winner
                int maxScore = 0;
                int winnerIndex = 0;
                for (int i = 0; i < tanks.size(); i++) {
                    int score = tanks.get(i).getScoreGained();
                    if (score > maxScore) {
                        maxScore = score;
                        winnerIndex = i;
                    }
                }
                // display winner
                fill(tanks.get(winnerIndex).getR(), tanks.get(winnerIndex).getG(), tanks.get(winnerIndex).getB(), 50);
                rectMode(CENTER);
                rect((float) WIDTH / 2, (float) HEIGHT / 2, 400, 300);
                fill(255);
                textAlign(BASELINE);
                textSize(32);
                text("Player " + (char)('A' + winnerIndex) + " wins!", 330, 250);

                ArrayList<Tank> sortedTanks = new ArrayList<>(tanks);
                sortedTanks.sort(Comparator.comparingInt(Tank::getScoreGained).reversed());
                textSize(24);
                for (int i = 0; i < sortedTanks.size(); i++) {
                    Tank tank = sortedTanks.get(i);
                    fill(tank.getR(), tank.getG(), tank.getB());
                    text("Player " + (char)('A' + tanks.indexOf(tank)) + ": " + tank.getScoreGained(),
                            (float) (WIDTH / 2) - 75, (float) HEIGHT / 2 + i * 40 - 30);
                }
                delay(700);
            }
        }
    }

    /**
     * Stops the Minim audio library and closes the sketch.
     */
    @Override
    public void stop() {
        minim.stop();
        super.stop();
    }

    /**
     * Initializes the wind value randomly between -35 and 35.
     */
    public static void initializeWind() {
        // generate random number from -35 to 35
        wind = App.random.nextInt(71) - 35;
    }

    /**
     * Updates the wind value based on the current wind speed.
     */
    public static void updateWind() {
        // update wind value
        if (wind <= 35 && wind >= -35) {
            int change = App.random.nextInt(11) - 5; // random -5 to 5
            wind += change;

            // Determines if the updated value is out of range
            if (wind > 35) {
                wind = 35;
            } else if (wind < -35) {
                wind = -35;
            }
        } else {
            wind = 0;
        }
    }

    /**
     * The main method that runs the Tanks application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        PApplet.main("Tanks.App");
    }
}
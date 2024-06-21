package Tanks;


import org.junit.jupiter.api.Test;
import static Tanks.App.tanks;
import static org.junit.jupiter.api.Assertions.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.event.KeyEvent;


class AppTest {
    @Test
    void testSetup() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        assertNotNull(app.minim);
        assertNotNull(app.fireSound);
        assertNotNull(app.explodeSound);
        assertNotNull(app.buySound);
        assertNotNull(app.gameOverSound);
        assertNotNull(app.teleportSound);
        assertNotNull(app.fixSound);
        assertNotNull(app.windLeftImage);
        assertNotNull(app.windRightImage);
        assertNotNull(app.layoutFiles);
        assertNotNull(app.foregroundColors);
        assertNotNull(app.backgroundImages);
        assertNotNull(app.treeImages);
    }

    @Test
    void testSetupLayout() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);
        app.setupLayout();

        assertNotNull(app.tanks);
        assertFalse(app.tanks.isEmpty());
        assertNotNull(app.trees);
        assertNotNull(app.terrain);
        assertNotNull(app.terrainHeightArray);
    }

    @Test
    void testSmoothTerrain() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);
        app.setupLayout();
        int[] originalHeightArray = app.terrainHeightArray.clone();
        App.smoothTerrain();
        assertNotEquals(originalHeightArray, app.terrainHeightArray);
    }

    @Test
    void testKeyPressed() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);
        Tank currentTank = tanks.get(App.currentPlayerIndex);

        // Test 'w' key
        app.keyPressed(new KeyEvent(app, System.currentTimeMillis(), KeyEvent.PRESS, 0, 'w', 'w'));
        assertEquals(10, currentTank.getPower());

        // Test 's' key
        app.keyPressed(new KeyEvent(app, 0, 0, 0, 's', 's'));
        assertEquals(5, currentTank.getPower());

        // Test UP arrow key
        float initialTurretAngle = currentTank.getTurretAngle();
        app.keyPressed(new KeyEvent(app, 0, 0, 0, (char) PConstants.UP, PConstants.UP));
        assertNotEquals(initialTurretAngle, currentTank.getTurretAngle());

        // Test DOWN arrow key
        initialTurretAngle = currentTank.getTurretAngle();
        app.keyPressed(new KeyEvent(app, 0, 0, 0, (char) PConstants.DOWN, PConstants.DOWN));
        assertNotEquals(initialTurretAngle, currentTank.getTurretAngle());

        // Test LEFT arrow key
        int initialX = currentTank.getX();
        app.keyPressed(new KeyEvent(app, 0, 0, 0, (char) PConstants.LEFT, PConstants.LEFT));
        assertNotEquals(initialX, currentTank.getX());

        // Test RIGHT arrow key
        initialX = currentTank.getX();
        app.keyPressed(new KeyEvent(app, System.currentTimeMillis(), KeyEvent.PRESS, 0, '\0', app.RIGHT));
        assertNotEquals(initialX, currentTank.getX());

        // Test 'r' key
        int initialHealth = currentTank.getHealth();
        app.keyPressed(new KeyEvent(app, 0, 0, 0, 'r', 'r'));
        assertNotEquals(initialHealth, currentTank.getHealth());

        // Test 'f' key
        int initialFuel = currentTank.getFuel();
        app.keyPressed(new KeyEvent(app, 0, 0, 0, 'f', 'f'));
        assertNotEquals(initialFuel, currentTank.getFuel());

        // Test 't' key
        app.keyPressed(new KeyEvent(app, 0, 0, 0, 't', 't'));
        assertTrue(currentTank.isSelectingTeleport());

        // Test SPACE key
        int initialProjectileSize = currentTank.projectiles.size();
        app.keyPressed(new KeyEvent(app, 0, 0, 0, ' ', ' '));
        assertNotEquals(initialProjectileSize, currentTank.projectiles.size());
    }

    @Test
    void testMousePressed() {

        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);


        Tank tank = tanks.get(App.currentPlayerIndex);
        tank.setSelectingTeleport(true);
        app.mouseX = 100;
        app.mouseY = 200;
        app.mousePressed(null);
        assertEquals(100, tank.getX());
        assertEquals(200, tank.getY());
        assertEquals(App.INITIAL_PARACHUTES - 1, tank.getParachutes());
        assertFalse(tank.isSelectingTeleport());
    }

    @Test
    void testDraw() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);
        app.setupLayout();

        // Test game over state
        app.gameEnded = true;
        app.draw();
        app.gameEnded = false;

        // Test background images, foreground colors, and tree images
        for (int i = 1; i <= app.layoutFiles.size(); i++) {
            app.levelNumber = i;
            app.draw();
        }

        if (!app.tanks.isEmpty()) { // 检查坦克列表是否为空
            Tank tank = app.tanks.get(0);
            Projectile projectile = new Projectile(tank.getX(), tank.getY(), 0, 10, tank.getR(), tank.getG(), tank.getB(), tank.getPlayerName());
            projectile.exploded = true;
            projectile.explodeStartTime = app.millis();
            tank.projectiles.add(projectile);
            app.draw();
        }

        // Test wind direction and speed display
        App.wind = 10;
        app.draw();
        App.wind = -10;
        app.draw();
    }

    @Test
    void testInitializeWind() {

        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        assertTrue(App.wind >= -35 && App.wind <= 35);
    }

    @Test
    void testUpdateWind() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        // Test when wind is within the valid range (-35 to 35)
        App.wind = 20;
        App.updateWind();
        assertTrue(App.wind >= -35 && App.wind <= 35);

        // Test when wind exceeds the upper limit (35)
        App.wind = 35;
        App.updateWind();
        assertTrue(App.wind >= -35 && App.wind <= 35);

        // Test when wind exceeds the lower limit (-35)
        App.wind = -35;
        App.updateWind();
        assertTrue(App.wind >= -35 && App.wind <= 35);

        // Test when wind is outside the valid range
        App.wind = 40;
        App.updateWind();
        assertEquals(0, App.wind);

        App.wind = -40;
        App.updateWind();
        assertEquals(0, App.wind);
    }

    @Test
    void testGameEnded() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        assertFalse(app.gameEnded);
    }

    @Test
    void testNextLevel() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);
        app.setupLayout();
        for (int i = 1; i < app.tanks.size(); i++) {
            app.tanks.get(i).setHealth(0);
        }
        app.draw();
        assertEquals(2, app.levelNumber);
    }

    @Test
    void testStop() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        // 检查音频是否已加载
        assertNotNull(app.fireSound);
        assertNotNull(app.explodeSound);
        assertNotNull(app.buySound);
        assertNotNull(app.gameOverSound);
        assertNotNull(app.teleportSound);
        assertNotNull(app.fixSound);
        // 调用 stop() 方法
        assertDoesNotThrow(() -> app.stop());
    }
}
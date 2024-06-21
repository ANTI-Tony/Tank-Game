package Tanks;

import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileTest {
    @Test
    void testConstructor() {

        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);


        Projectile projectile = new Projectile(100, 200, (float)Math.PI/4, 50, 255, 0, 0, "A");
        assertEquals(100, projectile.x);
        assertEquals(200, projectile.y);
        assertEquals(50, projectile.power);
        assertEquals(255, projectile.r);
        assertEquals(0, projectile.g);
        assertEquals(0, projectile.b);
    }

    @Test
    void testUpdate() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        Projectile projectile = new Projectile(100, 200, (float)Math.PI/4, 50, 255, 0, 0, "A");
        float initialX = projectile.x;
        float initialY = projectile.y;
        projectile.update();
        assertNotEquals(initialX, projectile.x);
        assertNotEquals(initialY, projectile.y);
    }

    @Test
    void testFireProjectile() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        Tank currentTank = app.tanks.get(App.currentPlayerIndex);
        int initialProjectileCount = currentTank.projectiles.size();

        Tank.fireProjectile();

        assertEquals(initialProjectileCount + 1, currentTank.projectiles.size());
        Projectile lastProjectile = currentTank.projectiles.get(currentTank.projectiles.size() - 1);
        assertEquals(currentTank.getX(), lastProjectile.x);
        assertEquals(currentTank.getY(), lastProjectile.y);
        assertEquals(currentTank.getR(), lastProjectile.r);
        assertEquals(currentTank.getG(), lastProjectile.g);
        assertEquals(currentTank.getB(), lastProjectile.b);
        assertEquals(currentTank.getPlayerName(), lastProjectile.name);
    }

    @Test
    void testExplode() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        Tank tank = app.tanks.get(0);
        int initialHealth = tank.getHealth();

        Projectile projectile = new Projectile(tank.getX(), tank.getY(), (float)Math.PI/4, 50, 255, 0, 0, "A");
        projectile.explode(tank.getX(), tank.getY());

        assertTrue(projectile.exploded);
        assertNotEquals(initialHealth, tank.getHealth());
    }

    @Test
    void testDraw() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        Projectile projectile = new Projectile(100, 200, (float)Math.PI/4, 50, 255, 0, 0, "A");

        // 记录初始的填充颜色
        int initialFill = app.g.fillColor;

        projectile.draw(app);

        // 检查填充颜色是否与炮弹的颜色一致
        assertEquals(app.color(255, 0, 0), app.g.fillColor);

        // 恢复初始的填充颜色
        app.fill(initialFill);
    }

    @Test
    void testExplodeOutOfBounds() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        Projectile projectile = new Projectile(-100, -100, (float)Math.PI/4, 50, 255, 0, 0, "A");
        projectile.explode(-100, -100);

        assertFalse(projectile.exploded);
    }

    @Test
    void testExplodeNoTankHit() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(100);

        Projectile projectile = new Projectile(0, 0, (float)Math.PI/4, 50, 255, 0, 0, "A");
        projectile.explode(0, 0);

        assertTrue(projectile.exploded);
        for (Tank tank : app.tanks) {
            assertEquals(100, tank.getHealth());
        }
    }

}
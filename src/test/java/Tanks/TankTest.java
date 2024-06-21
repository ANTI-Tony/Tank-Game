package Tanks;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class TankTest {
    @Test
    void testConstructor() {
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        assertEquals(100, tank.getX());
        assertEquals(200, tank.getY());
        assertEquals(255, tank.getR());
        assertEquals(0, tank.getG());
        assertEquals(0, tank.getB());
        assertEquals(100, tank.getHealth());
        assertEquals(250, tank.getFuel());
        assertEquals(50, tank.getPower());
        assertEquals(App.INITIAL_PARACHUTES, tank.getParachutes());
    }

    @Test
    void testMoveLeft() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        int initialFuel = tank.getFuel();
        Tank.moveLeft();
        assertEquals(100 - Tank.moveSpeed / App.FPS, tank.getX());
        assertEquals(initialFuel - (100 - tank.getX()), tank.getFuel());
    }

    @Test
    void testMoveRight() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        int initialFuel = tank.getFuel();
        Tank.moveRight();
        assertEquals(100 + Tank.moveSpeed / App.FPS, tank.getX());
        assertEquals(initialFuel - (tank.getX() - 100), tank.getFuel());
    }

    @Test
    void testTurretMoveLeft() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        float initialAngle = tank.getTurretAngle();
        Tank.turretMoveLeft();
        assertEquals(initialAngle + Tank.turretAngleSpeed / App.FPS, tank.getTurretAngle(), 0.001);

        // 测试炮塔向左移动
        Tank.turretMoveLeft();
        assertTrue(tank.getTurretAngle() > initialAngle);
    }

    @Test
    void testTurretMoveRight() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        float initialAngle = tank.getTurretAngle();
        Tank.turretMoveRight();
        assertEquals(initialAngle - Tank.turretAngleSpeed / App.FPS, tank.getTurretAngle(), 0.001);

        // 测试炮塔向右移动
        Tank.turretMoveRight();
        assertTrue(tank.getTurretAngle() < initialAngle);
    }

    @Test
    void testIncreasePower() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        int initialPower = tank.getPower();
        Tank.increasePower();
        assertEquals(initialPower + Tank.powerChangeRate / App.FPS, tank.getPower());
    }

    @Test
    void testDecreasePower() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        int initialPower = tank.getPower();
        Tank.decreasePower();
        assertEquals(initialPower - Tank.powerChangeRate / App.FPS, tank.getPower());
    }

    @Test
    void testFireProjectile() {
        App.tanks = new ArrayList<>();
        Tank tank1 = new Tank("A", 100, 200, 255, 0, 0);
        Tank tank2 = new Tank("B", 300, 200, 0, 255, 0);
        App.tanks.add(tank1);
        App.tanks.add(tank2);
        App.currentPlayerIndex = 0;

        assertEquals(1, tank1.projectiles.size());
        assertEquals(1, App.currentPlayerIndex);
    }

    @Test
    void testUseRepairKit() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        tank.setHealth(50);
        tank.addScore(20);

        Tank.useRepairKit();

        assertEquals(70, tank.getHealth());
        assertEquals(0, tank.getScoreGained());
    }

    @Test
    void testAddFuel() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        tank.setFuel(100);

        // 测试分数不足的情况
        tank.addScore(5);
        Tank.addFuel();
        assertEquals(100, tank.getFuel());
        assertEquals(5, tank.getScoreGained());

        // 测试分数足够的情况
        tank.addScore(10);
        Tank.addFuel();
        assertEquals(300, tank.getFuel());
        assertEquals(5, tank.getScoreGained());
    }

    @Test
    void testSlowFall() {
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        tank.setUsingParachute(true);
        tank.setY(150);
        App.terrainHeightArray[100] = 100;

        tank.slowFall();

        assertEquals(150 + 30 / App.FPS, tank.getY());
    }

    @Test
    void testFastFall() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        tank.setY(150);
        App.terrainHeightArray[100] = 100;

        tank.fastFall();

        assertEquals(100, tank.getY());
        assertTrue(tank.getHealth() < 100);

        // 添加新的测试用例
        tank.setY(80);
        int initialHealth = tank.getHealth();

        tank.fastFall();

        assertEquals(80, tank.getY()); // 坦克的y坐标不应该改变
        assertEquals(initialHealth, tank.getHealth()); // 坦克的生命值也不应该改变
    }

    @Test
    void testTeleport() {
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        tank.setParachutes(1);
        tank.setSelectingTeleport(true);

        App app = new App();
        app.noLoop();
        app.setup();
        app.setupLayout();
        app.mouseX = 300;
        app.mouseY = 150;
        app.mousePressed(null);

        assertEquals(300, tank.getX());
        assertEquals(150, tank.getY());
        assertEquals(0, tank.getParachutes());
        assertFalse(tank.isSelectingTeleport());
    }

    @Test
    void testDraw() {
        App app = new App();
        app.noLoop();
        app.setup();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);

        // 测试使用降落伞的情况
        tank.setUsingParachute(true);
        tank.draw(app);
        assertTrue(tank.getY() > 200); // 坦克应该缓慢下降

        // 测试不使用降落伞的情况
        tank.setUsingParachute(false);
        tank.draw(app);
        assertTrue(tank.getY() <= 200); // 坦克应该快速下降

        // 测试选择传送位置的情况
        tank.setSelectingTeleport(true);
        tank.draw(app);
        // 可以添加更多的断言来验证传送位置的绘制
    }

    @Test
    void testIsSelectingTeleport() {
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);

        assertFalse(tank.isSelectingTeleport());

        tank.setSelectingTeleport(true);
        assertTrue(tank.isSelectingTeleport());
    }

    @Test
    void testSetUsingParachute() {
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);

        assertFalse(tank.isUsingParachute);

        tank.setUsingParachute(true);
        assertTrue(tank.isUsingParachute);
    }

    @Test
    void testDrawWithoutParachute() {
        App app = new App();
        app.noLoop();
        app.setup();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        tank.setUsingParachute(false);
        tank.draw(app);
        assertTrue(tank.getY() <= 200); // 坦克应该快速下降
    }

    @Test
    void testFireProjectileWithDeadTank() {
        App.tanks = new ArrayList<>();
        Tank tank1 = new Tank("A", 100, 200, 255, 0, 0);
        tank1.setHealth(0); // 设置坦克已死亡
        App.tanks.add(tank1);
        Tank tank2 = new Tank("B", 300, 200, 0, 255, 0);
        App.tanks.add(tank2);
        App.currentPlayerIndex = 0;

        Tank.fireProjectile();

        assertEquals(0, tank1.projectiles.size()); // 死亡的坦克不应发射炮弹
        assertEquals(1, App.currentPlayerIndex); // 应该切换到下一个玩家
    }

    @Test
    void testMoveLeftWithInsufficientFuel() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        tank.setFuel(10); // 设置燃料不足
        App.tanks.add(tank);
        int initialX = tank.getX();
        int initialFuel = tank.getFuel();

        Tank.moveLeft();

        assertEquals(initialX, tank.getX()); // x坐标不应改变
        assertEquals(initialFuel, tank.getFuel()); // 燃料不应减少
    }

    @Test
    void testMoveRightWithInsufficientFuel() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        tank.setFuel(10); // 设置燃料不足
        App.tanks.add(tank);
        int initialX = tank.getX();
        int initialFuel = tank.getFuel();

        Tank.moveRight();

        assertEquals(initialX, tank.getX()); // x坐标不应改变
        assertEquals(initialFuel, tank.getFuel()); // 燃料不应减少
    }

    @Test
    void testUseRepairKitWithInsufficientScore() {
        App.tanks = new ArrayList<>();
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        App.tanks.add(tank);
        tank.setHealth(50);
        tank.addScore(10); // 设置分数不足

        Tank.useRepairKit();

        assertEquals(50, tank.getHealth()); // 生命值不应改变
        assertEquals(10, tank.getScoreGained()); // 分数不应减少
    }

    @Test
    void testSlowFallWhenNotUsingParachute() {
        Tank tank = new Tank("A", 100, 200, 255, 0, 0);
        tank.setUsingParachute(false);
        int initialY = tank.getY();

        tank.slowFall();

        assertEquals(initialY, tank.getY()); // y坐标不应改变
    }
}
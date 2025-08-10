package Tanks;


import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class TankTest {

    private Tank tank;
    private float initialX = 100.0f;
    private float initialY = 50.0f;
    private int[] color = new int[]{255, 0, 0};
    private float[] terrainHeights = new float[864]; 
    //private float turretAngle = 0;

    @Test
    void setUp() {
        tank = new Tank(new App(), "tank1", initialX, initialY, color, terrainHeights);
    }

    @Test
    void testRotateTurretRight() {
        float initialAngle = tank. getTurretAngle();
        tank.rotateTurretRight(1.0f);
        assertTrue(tank.getTurretAngle() < initialAngle, "Turret angle should decrease when rotated right");
    }

    @Test
    void testRotateTurretLeft() {
        float initialAngle = tank. getTurretAngle();
        tank.rotateTurretLeft(1.0f);
        assertTrue(tank. getTurretAngle() > initialAngle, "Turret angle should increase when rotated left");
    }

    @Test
    void testFire() {
        int initialSize = tank.getProjectiles().size();
        tank.fire(0); // Assuming no wind
        assertEquals(initialSize + 1, tank.getProjectiles().size(), "Projectile should be added");
    }

    @Test
    void testIsAlive() {
        assertTrue(tank.isAlive, "Tank should be alive initially");
        tank.setHealth(0); 
        assertFalse(tank.alive(), "Tank should not be alive if health is 0");
    }

    @Test
    void testIsFlying() {
        tank.setY(300); 
        assertFalse(tank.isFlying(terrainHeights), "Tank should not be flying if it is not on the air");
    }
}
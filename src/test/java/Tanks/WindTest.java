package Tanks;


import processing.core.PApplet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WindTest {

    private Wind wind;

    @Test
    void setUp() {
        wind = new Wind();
    }

    @Test
    void testInitializeWind() {
        int windSpeed = wind.getWindSpeed();
        assertTrue(windSpeed >= 0 && windSpeed <= 35, "Wind speed should be between 0 and 35");
    }

    @Test
    void testUpdateWind() {
        int initialWindSpeed = wind.getWindSpeed();
        wind.updateWind();
        int updatedWindSpeed = wind.getWindSpeed();
        assertTrue(updatedWindSpeed >= 0 && updatedWindSpeed <= 35, "Wind speed should be between 0 and 35 after update");
        assertNotEquals(initialWindSpeed, updatedWindSpeed, "Wind speed should change after update");
    }

    @Test
    void testIsWindToTheLeft() {
        wind = new Wind() {
            { wind.setWindSpeed(-19); }
        };
        assertTrue(wind.isWindToTheLeft(), "Wind should be to the left if negative");
        assertFalse(wind.isWindToTheRight(), "Wind should not be to the right if negative");
    }

    @Test
    void testIsWindToTheRight() {
        wind = new Wind() {
            { wind.setWindSpeed(13); }
        };
        assertTrue(wind.isWindToTheRight(), "Wind should be to the right when wind speed is positive");
        assertFalse(wind.isWindToTheLeft(), "Wind should not be to the left when wind speed is positive");
    }

    @Test
    void testGetWindForce() {
        wind = new Wind() {
            { wind.setWindSpeed(20); }
        };
        assertEquals(0.02f, wind.getWindForce(), 0.001f, "Wind force should be correctly calculated from wind speed");
    }
}

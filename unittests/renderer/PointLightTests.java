package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for PointLight class functional methods.
 *
 * @author Tohar Chamami
 */
class PointLightTests {

    /**
     * Default constructor for the test class
     */
    public PointLightTests() {
    }

    /**
     * Test method for {@link lighting.PointLight} functional capabilities
     */
    @Test
    void testPointLightMethods() {
        Color lightColor = new Color(500, 300, 200);
        Point lightPosition = new Point(0, 0, 0);

        PointLight light = new PointLight(lightColor, lightPosition)
                .setKc(1).setKl(1).setKq(1);

        Point p = new Point(0, 0, 2);

        // ================== Equivalence Partitions Tests ==================

        // EP01: Test getIntensity with attenuation factors
        // Distance d = 2. Formula: Intensity / (kC + kL*d + kQ*d^2) = Color / (1 + 1*2 + 1*4) = Color / 7
        Color expectedColor = lightColor.reduce(7);
        assertEquals(expectedColor, light.getIntensity(p),
                "PointLight getIntensity() calculation with attenuation factors is wrong");

        // EP02: Test getL direction vector from light to point
        Vector expectedL = new Vector(0, 0, 1);
        assertEquals(expectedL, light.getL(p),
                "PointLight getL() wrong direction vector");
    }

    @Test
    public void testGetL() {
        Point p = new Point(1, 2, 3);
        //  BAV - Point is exactly at the light's position for PointLight
        PointLight pl = new PointLight(new primitives.Color(255, 255, 255), p);
        assertNull(pl.getL(p),
                "PointLight getL() should return null when point equals light position");
    }
}
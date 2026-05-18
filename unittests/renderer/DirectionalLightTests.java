package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for DirectionalLight class functional methods.
 *
 * @author Tohar Chamami
 */
class DirectionalLightTests {

    /**
     * Default constructor for the test class
     */
    public DirectionalLightTests() {
    }

    /**
     * Test method for {@link lighting.DirectionalLight} functional capabilities
     */
    @Test
    void testDirectionalLightMethods() {
        Color lightColor = new Color(100, 100, 100);
        Vector direction = new Vector(0, 0, -1);
        DirectionalLight light = new DirectionalLight(lightColor, direction);
        Point p = new Point(1, 2, 3);

        // ================== Equivalence Partitions Tests ==================

        // EP01: Test getIntensity returns the constant intensity everywhere
        assertEquals(lightColor, light.getIntensity(p),
                "DirectionalLight getIntensity() wrong color intensity");

        // EP02: Test getL returns the constant normalized direction vector
        assertEquals(direction.normalize(), light.getL(p),
                "DirectionalLight getL() wrong direction vector");
    }
}
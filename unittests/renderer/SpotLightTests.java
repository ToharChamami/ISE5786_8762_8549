package renderer;

import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for SpotLight class functional methods.
 *
 * @author Tohar Chamami
 */
class SpotLightTests {

    /**
     * Default constructor for the test class
     */
    public SpotLightTests() {
    }

    /**
     * Test method for {@link lighting.SpotLight} functional capabilities
     */
    @Test
    void testSpotLightMethods() {
        Color lightColor = new Color(500, 300, 200);
        Point lightPosition = new Point(0, 0, 0);
        Vector lightDirection = new Vector(0, 0, 1); // Pointing straight up Z axis

        // SpotLight setup with factors: kC=1, kL=1, kQ=1
        SpotLight light = new SpotLight(lightColor, lightPosition, lightDirection)
                .setKc(1).setKl(1).setKq(1);

        // ================== Equivalence Partitions Tests ==================

        // EP01: Test getIntensity when point is directly in the center of the beam
        Point pCenter = new Point(0, 0, 2);
        Color expectedColorCenter = lightColor.reduce(7);
        assertEquals(expectedColorCenter, light.getIntensity(pCenter),
                "SpotLight getIntensity() at beam center is wrong");

        // EP02: Test getIntensity when point is at an angle (attenuation due to direction)
        Point pAngle = new Point(2, 0, 2);
        double distance = Math.sqrt(8);
        double attenuation = (1 + 1 * distance + 1 * 8);
        double cosFactor = 1 / Math.sqrt(2); // cos(45 deg)
        Color expectedColorAngle = lightColor.scale(cosFactor).scale(1d / attenuation);

        assertTrue(Math.abs(expectedColorAngle.getColor().getRed() - light.getIntensity(pAngle).getColor().getRed()) <= 2,
                "SpotLight getIntensity() at an angle is wrong");

        // EP03: Test getL direction vector from light to point
        Vector expectedL = new Vector(2, 0, 2).normalize();
        assertEquals(expectedL, light.getL(pAngle),
                "SpotLight getL() wrong direction vector");

        // ==================== Boundary Values Tests ====================

        // BVA01: Test getIntensity when point is behind the spotlight (cos <= 0)
        Point pBehind = new Point(0, 0, -2);
        assertEquals(Color.BLACK, light.getIntensity(pBehind),
                "SpotLight getIntensity() should be Black for points behind the light source");
    }
}
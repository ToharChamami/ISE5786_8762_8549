package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link primitives.Ray}.
 */
public class RayTests {

    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link primitives.Ray#Ray(Point, Vector)}.
     */
    @Test
    void testRay() {
        Point point = new Point(1, 2, 3);
        Vector vector = new Vector(0, 3, 4);
        assertDoesNotThrow(() -> new Ray(point, vector), "ERROR:Failed constructing a valid vector");
        Ray result = new Ray(point, vector);
        assertEquals(1, result.direction().length(), DELTA, "ERROR:Ray ctor must normolize the direction vector");
    }
}

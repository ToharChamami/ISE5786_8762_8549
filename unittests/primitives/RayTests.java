package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link primitives.Ray}.
 */
public class RayTests {

    /**
     * Basic default constructor
     */
    public RayTests() {
    }

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

        // ============ Equivalence Partitions Tests ==============
        //EP01: ensure the ctor is building the ray
        assertDoesNotThrow(() -> new Ray(point, vector), "ERROR:Failed constructing a valid vector");

        //EP02: ensure the normal length is 1.0
        Ray result = new Ray(point, vector);
        assertEquals(1, result.direction().length(), DELTA, "ERROR:Ray ctor must normolize the direction vector");
    }

    /**
     * Test method for {@link primitives.Ray#getPoint(double)}.
     */
    @Test
    void testGetPoint() {
        Ray ray = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));

        // ============ EP Tests ==============

        // EP01: t > 0
        assertEquals(new Point(3, 2, 3), ray.getPoint(2), "Wrong point for t > 0");

        // EP02: t < 0
        assertEquals(new Point(0, 2, 3), ray.getPoint(-1), "Wrong point for t < 0");

        // =============== BV Tests ==================

        // BV01: t = 0
        assertEquals(new Point(1, 2, 3), ray.getPoint(0), "t=0 should return p0");
    }
}

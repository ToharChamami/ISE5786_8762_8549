package primitives;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    /**
     * Tests the findClosestPoint method.
     */
    @Test
    void testFindClosestPoint() {
        Ray ray = new Ray(new Point(0, 0, 10), new Vector(1, 10, -100));

        Point p1 = new Point(1, 1, -100);
        Point p2 = new Point(-1, 1, -99);
        Point p3 = new Point(0, 2, -10); // זו הנקודה הקרובה ביותר (מרחק ריבועי קטן)

        // ============ Equivalence Partitions Tests ==============
        // EP01 the midlle point is the closest
        assertEquals(p3, ray.findClosestPoint(List.of(p1, p3, p2)),
                "Closest point should be the middle one");

        // =============== Boundary Values Tests ==================
        // BV01
        assertNull(ray.findClosestPoint(null), "Null list should return null");

        // BV02
        assertEquals(p3, ray.findClosestPoint(List.of(p3, p1, p2)),
                "Closest point should be the first one");

        // BV03
        assertEquals(p3, ray.findClosestPoint(List.of(p1, p2, p3)),
                "Closest point should be the last one");
    }

}

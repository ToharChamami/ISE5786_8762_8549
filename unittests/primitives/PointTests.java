package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTests {

    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link primitives.Point#subtract(primitives.Point)}.
     */
    @Test
    void testSubtract() {
        Point p1 = new Point(1, 2, 3);

        // Equivalence Partitions Tests
        // EP01: Simple subtraction of two points
        Point p2 = new Point(2, 4, 6);
        assertEquals(new Vector(1, 2, 3), p2.subtract(p1), "ERROR: Point subtract(Point) result is wrong");

        // Boundary Values Tests
        // BV01: Subtraction of a point from itself (should throw exception for zero vector)
        assertThrows(IllegalArgumentException.class, () -> p1.subtract(p1),
                "ERROR: Point subtract(itself) does not throw an exception");
    }

    //* Test method for {@link primitives.Point#add(primitives.Point)}.

    @Test
    void testAdd() {
        // EP01: Simple addition of a vector to a point

        Point p1 = new Point(1, 2, 3);
        Vector v1 = new Vector(2, 4, 6);
        assertEquals(new Vector(3, 6, 9), p1.add(v1), "ERROR: Point add(Vector) result is wrong");

        // BV01: Addition of opposite vector resulting in origin (Point.ZERO)
        Vector vOpposite = new Vector(-1, -2, -3);
        assertEquals(Point.ZERO, p1.add(vOpposite), "ERROR: Point add(Vector) to origin result is wrong");

    }

    // * Test method for {@link primitives.Point#distanceSquared(primitives.Point)}.
    @Test
    void testDistanceSquared() {
// EP01: Simple distanceSquared of a point and a point
        Point p1 = new Point(4, 6, 8);
        Point p2 = new Point(4, 6, 2);
        assertEquals(36, p1.distanceSquared(p2), DELTA, "ERROR: Point distanceSquared(Point) to origin result is wrong");

        // BV01: distanceSquared of point with itself
        assertEquals(0, p1.distanceSquared(p1), DELTA, "ERROR: Point distanceSquared(itself) to origin result is wrong");

    }

    // * Test method for {@link primitives.Point#dintance(primitives.Point)}.
    @Test
    void testdistance() {
        // EP01: Simple distanceSquared of a point and a point
        Point p1 = new Point(4, 6, 8);
        Point p2 = new Point(2, 2, 2);
        assertEquals(6, p1.distance(p2), DELTA, "ERROR: Point distance(Point) to origin result is wrong");

        // BV01: distance of point with itself
        assertEquals(0, p1.distance(p1), DELTA, "ERROR: Point distance(itself) to origin result is wrong");

    }

}



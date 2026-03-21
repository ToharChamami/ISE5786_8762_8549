package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointTests {

    private static final double DELTA = 0.000001;

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

    @Test
    void testAdd() {
        Point p1 = new Point(1, 2, 3);
        Vector v1 = new Vector(2, 4, 6);
        assertEquals(new Vector(3, 6, 9), p1.add(v1), "ERROR: Point add(Vector) result is wrong");

//        vector
//        // Boundary Values Tests
//        assertThrows(IllegalArgumentException.class,()->;

    }
}

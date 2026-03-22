package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link primitives.Vector}.
 */
public class VectorTests {

    /**
     * Default constructor
     */
    public VectorTests() {
    }

    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link primitives.Point#subtract(primitives.Point)}.
     */
    @Test
    void testSubtract() {
        // Equivalence Partitions Tests
        // EP01: Simple subtraction of two points
        Point p1 = new Point(1, 2, 3);
        Point p2 = new Point(2, 4, 6);
        assertEquals(new Vector(1, 2, 3), p2.subtract(p1), "ERROR: Point subtract(Point) result is wrong");

        // Boundary Values Tests
        // BV01: Subtraction of a point from itself (should throw exception for zero vector)
        assertThrows(IllegalArgumentException.class, () -> p1.subtract(p1),
                "ERROR: Point subtract(itself) does not throw an exception");
    }

    /**
     * Test method for {@link primitives.Vector#Vector(double, double, double)}.
     */
    void testVector() {

        // EP01: Simple constractor of a vector
        assertDoesNotThrow(() -> new Vector(1, 2, 3), "ERROR:Failed constructing a valid vector");

        //BV01:
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0), "ERROR:constctor zero has been build ");
        //BV02
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO), "Constructed a zero vector from Double3.ZERO");
    }

    /**
     * Test method for {@link primitives.Vector#add(primitives.Vector)}.
     */
    @Test
    void testAdd() {
        Point p1 = new Point(1, 2, 3);
        Vector v1 = new Vector(1, 2, 3);
        //EP01
        assertEquals(new Vector(2, 4, 6), p1.add(v1), "ERROR:Vector add() result is wrong");
        //BV01
        // We use subtract and add logic; if v1 + v2 = 0, it should throw exception in Vector
        Vector v2 = new Vector(-1, -2, -3);
        assertThrows(IllegalArgumentException.class, () -> p1.add(v2), "ERROR: VECTOR Add() result doesnt throw exception");

    }

    /**
     * Test method for {@link primitives.Vector#scale(double)}.
     */
    @Test
    void testScale() {
        Vector v1 = new Vector(1, 2, 3);
        //ep01
        assertEquals(new Vector(2, 4, 6), v1.scale(2), "ERROR:The scale origin result is wrong");
        //BV01
        assertThrows(IllegalArgumentException.class, () -> v1.scale(0), "ERROR:Scale cannot be zero");
    }

    /**
     * Test method for {@link primitives.Vector#dotProduct(primitives.Vector)}.
     */
    @Test
    void testDotProduct() {
        Vector v1 = new Vector(1, 2, 3);
        Vector v2 = new Vector(0, 3, -2); // Orthogonal to v1 if it was (1,2,3)? No, let's pick better values.
        Vector vOrthogonal = new Vector(0, 3, -2); // 1*0 + 2*3 + 3*(-2) = 0

        // EP01: Simple dot product calculation
        assertEquals(14, v1.dotProduct(v1), DELTA, "dotProduct() wrong value");

        // BV01: Dot product of orthogonal vectors (should be zero)
        assertEquals(0, v1.dotProduct(vOrthogonal), DELTA, "dotProduct() for orthogonal vectors is not zero");
    }

    /**
     * Test method for {@link primitives.Vector#crossProduct(primitives.Vector)}.
     */
    @Test
    void testCrossProduct() {
        Vector v2 = new Vector(0, 3, -2);
        Vector v1 = new Vector(1, 2, 3);
        Vector result = v1.crossProduct(v2);

        // EP01: Check length of cross product result
        assertEquals(v1.length() * v2.length(), result.length(), DELTA,
                "crossProduct() result length is wrong");

        // EP02: Check orthogonality of result to operands
        assertEquals(0, result.dotProduct(v1), DELTA, "crossProduct() result not orthogonal to v1");
        assertEquals(0, result.dotProduct(v2), DELTA, "crossProduct() result not orthogonal to v2");

        // BV01: Cross product of parallel vectors (should throw exception for zero vector)
        assertThrows(IllegalArgumentException.class, () -> v1.crossProduct(v1.scale(2)),
                "crossProduct() for parallel vectors does not throw exception");
    }

    /**
     * Test method for {@link primitives.Vector#length()}.
     */
    @Test
    void testLength() {

        // EP01: Simple length calculation using Pythagoras triple
        assertEquals(5, new Vector(0, 3, 4).length(), DELTA, "ERROR: length() wrong value");
    }

    /**
     * Test method for {@link primitives.Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // EP01: Simple squared length calculation
        assertEquals(25, new Vector(0, 3, 4).lengthSquared(), DELTA, "ERROR: lengthSquared() wrong value");
    }

    /**
     * Test method for {@link primitives.Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        Vector v = new Vector(0, 3, 4);
        Vector n = v.normalize();

        // EP01: Check if normalized vector length is 1
        assertEquals(1, n.length(), DELTA, "normalized vector length is not 1");

        // EP02: Check if normalized vector is parallel to original
        assertThrows(IllegalArgumentException.class, () -> v.crossProduct(n),
                "normalized vector is not parallel to original");

        // EP03: Check if they are in the same direction
        assertTrue(v.dotProduct(n) > 0, "normalized vector has opposite direction");
    }
}


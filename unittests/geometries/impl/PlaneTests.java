package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link geometries.impl.Plane}.
 */
class PlaneTests {

    /**
     * Default constructor
     */
    PlaneTests() {
    }

    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link geometries.impl.Plane#Plane(primitives.Point, primitives.Point, primitives.Point)}.
     */
    @Test
    void testConstructor() {

        // =============== Boundary Values Tests ==================
        // BV01: Correct plane with three non-collinear points
        assertDoesNotThrow(() -> new Plane(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0)),
                "Failed constructing a correct plane");

        // BV02: Two points are co-located (identical)
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(new Point(1, 1, 0), new Point(1, 1, 1), new Point(1, 1, 1)),
                "Constructed a plane with identical points");

        // BV03: Three points are co-located (P1 == P2 == P3)
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(new Point(1, 1, 1), new Point(1, 1, 1), new Point(1, 1, 1)),
                "Constructed a plane with 3 identical points");
        // BV04: All three points are on the same line (collinear)
        assertThrows(IllegalArgumentException.class,
                () -> new Plane(new Point(1, 1, 1), new Point(2, 2, 2), new Point(3, 3, 3)),
                "Constructed a plane with collinear points");
    }

    /**
     * Test method for {@link geometries.impl.Plane#Plane(Point, Vector)}.
     */
    @Test
    void testConstructorPointVector() {

        // ============ Equivalence Partitions Tests ==============
        // EP01: Ensure the normal is normalized (unit vector)
        Point p = new Point(1, 2, 3);
        Vector v = new Vector(0, 0, 5);
        Plane plane = new Plane(p, v);

        assertEquals(1, plane.getNormal(p).length(), DELTA,
                "Plane constructor with vector should normalize the normal");
    }

    /**
     * Test method for {@link geometries.impl.Plane#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {
        Point p1 = new Point(0, 0, 1);
        Plane plane = new Plane(p1, new Point(1, 0, 0), new Point(0, 1, 0));

        // ============ Equivalence Partitions Tests ==============
        // EP01: Normal at a generic point on the plane
        Vector result = plane.getNormal(new Point(0.5, 0.5, 0));

        //EP02: ensure the normal length is 1
        assertEquals(1, result.length(), DELTA, "Plane normal is not a unit vector");

        // =============== Boundary Values Tests ==================
        // BV01: Normal at the reference point (q) of the plane
        assertDoesNotThrow(() -> plane.getNormal(p1), "getNormal() at reference point failed");
        assertEquals(result, plane.getNormal(p1), "Normal at reference point is different");
    }
}
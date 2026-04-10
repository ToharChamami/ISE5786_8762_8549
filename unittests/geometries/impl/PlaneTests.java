package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertDoesNotThrow(() -> new Plane(new Point(0, 0, 1), new Point(1, 0, 0), new Point(0, 1, 0)), "Failed constructing a correct plane");

        // BV02: Two points are co-located (identical)
        assertThrows(IllegalArgumentException.class, () -> new Plane(new Point(1, 1, 0), new Point(1, 1, 1), new Point(1, 1, 1)), "Constructed a plane with identical points");

        // BV03: Three points are co-located (P1 == P2 == P3)
        assertThrows(IllegalArgumentException.class, () -> new Plane(new Point(1, 1, 1), new Point(1, 1, 1), new Point(1, 1, 1)), "Constructed a plane with 3 identical points");
        // BV04: All three points are on the same line (collinear)
        assertThrows(IllegalArgumentException.class, () -> new Plane(new Point(1, 1, 1), new Point(2, 2, 2), new Point(3, 3, 3)), "Constructed a plane with collinear points");
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

        assertEquals(1, plane.getNormal(p).length(), DELTA, "Plane constructor with vector should normalize the normal");
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

    /**
     * Test method for {@link geometries.impl.Plane#findIntersections(Ray)}.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============

        // EP01: Ray intersects the plane (1 point)
        Plane plane = new Plane(new Point(0, 0, 1), new Vector(0, 0, 1));
        var result1 = plane.findIntersections(new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)));
        assertNotNull(result1);
        assertEquals(1, result1.size());

        // EP02: Ray does not intersect the plane (0 points)
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 0, 0))));

        // =============== Boundary Values Tests ==================

        // Ray is parallel to the plane (all tests 0 points)

        // BV11: Ray is included in the plane
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 1), new Vector(1, 1, 0))));

        // BV12: Ray is parallel and outside the plane
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 0), new Vector(1, 1, 0))));

        // Ray is orthogonal to the plane

        // BV21: Ray starts before the plane (1 point)
        var result2 = plane.findIntersections(new Ray(new Point(0, 0, 0), new Vector(0, 0, 1)));
        assertNotNull(result2);
        assertEquals(1, result2.size());

        // BV22: Ray starts in the plane (0 points)
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 1), new Vector(0, 0, 1))));

        // BV23: Ray starts after the plane (0 points)
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 2), new Vector(0, 0, 1))));

        // Ray is neither parallel nor orthogonal, but starts in the plane

        // BV31: Ray starts in the plane (0 points)
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 1), new Vector(1, 1, 1))));

        //Special cases

        // BV41: Ray starts at the plane’s reference point Q (0 points)
        assertNull(plane.findIntersections(new Ray(new Point(0, 0, 1), new Vector(1, 0, 1))));
    }

}
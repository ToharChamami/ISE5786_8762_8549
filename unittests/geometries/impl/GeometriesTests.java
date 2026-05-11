package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the Geometries class.
 */
class GeometriesTests {

    /**
     * Basic default constructor for documentation tools
     */
    public GeometriesTests() {
    }

    /**
     * Test method for findIntersections in Geometries (Composite).
     */
    @Test
    void testFindIntersections() {
        Geometries geometries = new Geometries(
                new Sphere(new Point(1, 0, 0), 1d),
                new Plane(new Point(0, 0, 1), new Vector(0, 0, 1)),
                new Triangle(new Point(1, 0, 0), new Point(0, 1, 0), new Point(0, 0, 1))
        );

        // ============ Equivalence Partitions Tests ==============

        // EP01: Some geometries are intersected (but not all)
        Ray raySome = new Ray(new Point(0.5, 0.5, 0.5), new Vector(0, 0, 1));
        assertEquals(2, geometries.calcIntersections(raySome).size(), "Some geometries should be intersected");

        // =============== Boundary Values Tests ==================

        // BVA01: Empty collection
        Geometries emptyGeo = new Geometries();
        assertNull(emptyGeo.calcIntersections(new Ray(new Point(1, 1, 1), new Vector(1, 0, 0))), "Empty collection should return null");

        // BVA02: No geometry is intersected
        Ray rayNone = new Ray(new Point(10, 10, 10), new Vector(1, 1, 1));
        assertNull(geometries.calcIntersections(rayNone), "No geometry intersected should return null");

        // BVA03: Only one geometry is intersected
        Ray rayOne = new Ray(new Point(0, 0, 2), new Vector(0, 0, -1));
        assertEquals(1, geometries.calcIntersections(rayOne).size(), "Only one geometry should be intersected");

        // BVA04: All geometries are intersected
        Ray rayAll = new Ray(new Point(0.2, 0.2, -1), new Vector(0, 0, 1));
        // Sphere (2) + Plane (1) + Triangle (1) = 4 points total
        assertEquals(4, geometries.calcIntersections(rayAll).size(), "All geometries should be intersected");
    }
}
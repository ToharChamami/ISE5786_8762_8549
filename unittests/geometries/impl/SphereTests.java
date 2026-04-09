package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link geometries.impl.Sphere}.
 */
public class SphereTests {

    /**
     * Basic default constructor
     */
    public SphereTests() {
    }

    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link geometries.impl.Sphere#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {

        Point center = new Point(1, 1, 1);
        Sphere sphere = new Sphere(center, 1);
        Point pointOnSurface = new Point(1, 1, 2);
        Vector result = sphere.getNormal(pointOnSurface);

        // ============ Equivalence Partitions Tests ==============
        //EP01 ensure the method does not throw exception
        assertDoesNotThrow(() -> sphere.getNormal(pointOnSurface), "getNormal() threw unexpected exception");
        //EP02 ensure the normal is a unit vector
        assertEquals(1, result.length(), DELTA, "Sphere normal is not a unit vector");
        //EP03 ensure the normal direction is correct
        Vector expected = pointOnSurface.subtract(center);
        assertEquals(expected, result, "normal direction isn`t correct");

    }

    @test
    void testIntersect() {
        // ============ Equivalence Partitions Tests ==============

        Sphere sphere = new Sphere(new Point(1, 0, 0), 1);

        //EP01 Ray outside the sphepre( 0 points)
        assertNull(sphere.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(1, 1, 0))), "Ray out of the sphere ");

        //EP02 Ray across the sphere ( 2 points)
        List<Point> result = sphere.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(3, 1, 0)));
        assertNotNull(result, "Ray should have instructions");
        assertEquals(2, result.size(), "wrong number of points");

        //EP03 Ray start inside the sphere (one point )
        result = sphere.findIntersections(new Ray(new Point(1, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result, "Ray should have instruction");
        assertEquals(1, result.size(), "wrong number of points");

        // EP04 Ray start after the sphere (0 points)
        assertNull(sphere.findIntersections(new Ray(new Point(3, 0, 0), new Vector(1, 0, 0))), "Ray out of the sphere ");

        // =============== Boundary Values Tests ==================
        //BVA1.1 : Ray starts at sphere and goes inside (1 point)
        assertEquals(1, sphere.findIntersections(
                new Ray(new Point(2, 0, 0), new Vector(-1, 0, 0))).size(), "wrong number of points");

        // BV1.2: Ray starts at sphere and goes outside ( 0 points)
        assertNull(sphere.findIntersections(
                new Ray(new Point(2, 0, 0), new Vector(1, 0, 0))));

        // Ray goes through the center
        //BVA2.1 Ray start before the sphere (2 point)
        result = sphere.findIntersections(new Ray(new Point(-1, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result, "Ray should have instructions");
        assertEquals(2, result.size(), "wrong number of points");

        //BVA2.2 Ray starts at sphere and goes inside (1 point)
        result = sphere.findIntersections(new Ray(new Point(2, 0, 0), new Vector(-1, 0, 0)));
        assertNotNull(result, "Ray should have instruction");
        assertEquals(1, result.size(), "wrong number of points");

        //BVA2.3 Ray start inside (1 point)
        assertEquals(1, sphere.findIntersections(
                new Ray(new Point(-1, 0, 0), new Vector(1, 0, 0))).size(), "wrong number of points");

        //BVA2.4 Ray start at the center (1 point)
        result = sphere.findIntersections(new Ray(new Point(1, 0, 0), new Vector(1, 0, 0)));
        assertNotNull(result, "Ray should have instruction");
        assertEquals(1, result.size(), "wrong number of points");

        //BVA 2.5 Ray start at the sphere and goes outside (0 point)
        assertNull(sphere.findIntersections(new Ray(new Point(1, 1, 0), new Vector(0, 1, 0))), "Ray should have instructions");
    }
}

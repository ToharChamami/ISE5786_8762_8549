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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link geometries.impl.Triangle}.
 */
public class TriangleTests {

    /**
     * Basic default constructor
     */
    public TriangleTests() {
    }

    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link geometries.impl.Triangle#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormal() {

        Point p1 = new Point(0, 0, 0);
        Point p2 = new Point(1, 0, 0);
        Point p3 = new Point(0, 1, 0);
        Triangle triangle = new Triangle(p1, p2, p3);

        // A point clearly inside the triangle boundaries
        Point pointInside = new Point(0.2, 0.2, 0);

        // ============ Equivalence Partitions Tests ==============
        //EP01: checks the method does not throw exception
        assertDoesNotThrow(() -> triangle.getNormal(pointInside), "getNormal() threw unexpected exception");

        Vector result = triangle.getNormal(pointInside);

        //EP02: checks the result length is 1
        assertEquals(1, result.length(), DELTA, "Triangle normal is not a unit vector");

        //EP03: checks if the normal is orthogonal
        Vector edge1 = p2.subtract(p1);
        Vector edge2 = p3.subtract(p2);
        assertEquals(0, result.dotProduct(edge1), DELTA, "Normal is not orthogonal to edge1");
        assertEquals(0, result.dotProduct(edge2), DELTA, "Normal is not orthogonal to edge2");

        //EP04: checks the normal direction is up or down
        assertTrue(result.equals(new Vector(0, 0, 1)) || result.equals(new Vector(0, 0, -1)),
                "Normal direction is incorrect for a triangle on XY plane");

    }

    /**
     * geometries.impl.Triangle#getNormal(primitives.Point)
     * Test method for {@link geometries.impl.Triangle#findIntersections(primitives.Ray)}.
     */
    @Test
    void testFindIntersections() {
        Triangle triangle = new Triangle(
                new Point(1, 0, 0),
                new Point(0, 1, 0),
                new Point(0, 0, 1));
        // ============ Equivalence Partitions Tests ==============

        // EP01: Inside triangle (1 point)
        Ray ray1 = new Ray(new Point(0.5, 0.5, 0.5), new Vector(-1, -1, -1));
        List<Point> result1 = triangle.findIntersections(ray1);
        assertNotNull(result1, "Ray should intersect inside the triangle");
        assertEquals(1, result1.size(), "Should be 1 intersection point");

        // EP02: Outside against edge (0 points)
        Ray ray2 = new Ray(new Point(1, 1, 1), new Vector(0, 0, -1));
        assertNull(triangle.findIntersections(ray2), "Ray should be outside against edge");

        // EP03: Outside against vertex (0 points)
        Ray ray3 = new Ray(new Point(2, 2, 2), new Vector(-1, -1, -1));
        assertNull(triangle.findIntersections(ray3), "Ray should be outside against vertex");

        // =============== Boundary Values Tests ==================

        // BV01: On edge (0 points)
        Ray ray4 = new Ray(new Point(0.5, 0.5, 0), new Vector(0, 0, 1));
        assertNull(triangle.findIntersections(ray4), "Ray on edge should return null");

        // BV02: In vertex (0 points)
        Ray ray5 = new Ray(new Point(1, 0, 0), new Vector(0, 1, 0));
        assertNull(triangle.findIntersections(ray5), "Ray in vertex should return null");

        // BV03: On edge continuation (0 points)
        Ray ray6 = new Ray(new Point(2, 0, 0), new Vector(0, 0, 1));
        assertNull(triangle.findIntersections(ray6), "Ray on edge continuation should return null");
    }

}









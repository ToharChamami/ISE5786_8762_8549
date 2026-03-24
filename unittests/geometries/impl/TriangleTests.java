package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
}








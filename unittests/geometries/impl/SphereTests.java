package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

}

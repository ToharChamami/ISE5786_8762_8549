package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link geometries.impl.Tube}.
 */
public class TubeTests {

    /**
     * Basic default constructor
     */
    public TubeTests() {
    }

    /**
     * Delta value for accuracy when comparing double values
     */
    private static final double DELTA = 1e-6;

    /**
     * Test method for {@link geometries.impl.Tube#getNormal(primitives.Point)}.
     */
    @Test
    void testGetNormalTube() {
        Ray axisRay = new Ray(new Point(0, 0, 0), new Vector(0, 0, 1));
        Tube tube = new Tube(1, axisRay);

        Point pointInFrontOfAxis = new Point(1, 0, 2);
        Point pointBehindAxis = new Point(1, 0, -2);
        Point pointAtAxisHead = new Point(1, 0, 0);

        // ============ Equivalence Partitions Tests ==============
        //EP01: case the point is in front of the axis,on the surface
        assertDoesNotThrow(() -> tube.getNormal(pointInFrontOfAxis), "ERROR:getNormal() threw unexpected exception");
        Vector result1 = tube.getNormal(pointInFrontOfAxis);
        assertEquals(1, result1.length(), DELTA, "ERROR: Tube normal is not a unit vector");
        Vector expected1 = new Vector(1, 0, 0);
        assertEquals(expected1, result1, "normal direction isn`t correct");

        //EP02: case the point is behind the axis,on the surface
        assertDoesNotThrow(() -> tube.getNormal(pointBehindAxis), "ERROR:getNormal() threw unexpected exception");
        Vector result2 = tube.getNormal(pointBehindAxis);
        assertEquals(1, result2.length(), DELTA, "ERROR: Tube normal is not a unit vector");
        Vector expected2 = new Vector(1, 0, 0);
        assertEquals(expected2, result2, "normal direction isn`t correct");

        // =============== Boundary Values Tests ==================
        //BV01: the point is at Axis head, on the surface
        assertDoesNotThrow(() -> tube.getNormal(pointAtAxisHead), "ERROR:getNormal() threw unexpected exception");
        Vector result3 = tube.getNormal(pointAtAxisHead);
        assertEquals(1, result3.length(), DELTA, "ERROR: Tube normal is not a unit vector");
        assertEquals(expected2, result3, "normal direction isn`t correct");
    }
}




package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SphereTests {
    private static final double DELTA = 1e-6;

    @Test
    void testGetNormal() {

        Point center = new Point(1, 1, 1);
        Sphere sphere = new Sphere(center, 1);

        Point pointOnSurface = new Point(1, 1, 2);
        Vector result = sphere.getNormal(pointOnSurface);
        //EP01
        assertDoesNotThrow(() -> sphere.getNormal(pointOnSurface), "getNormal() threw unexpected exception");
        //ep02
        assertEquals(1, result.length(), DELTA, "Sphere normal is not a unit vector");
        //EP03
        Vector expected = pointOnSurface.subtract(center);
        assertEquals(expected, result, "normal direction isn`t correct");

    }

}

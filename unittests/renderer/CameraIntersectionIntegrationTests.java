package renderer;

import geometries.api.Intersectable;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for Camera ray construction and geometric intersections.
 */
class CameraIntersectionIntegrationTests {

    /**
     * Default constructor for the integration tests class.
     */
    CameraIntersectionIntegrationTests() {
    }

    /**
     * Resolution X for integration tests
     */
    private static final int NX = 3;

    /**
     * Resolution Y for integration tests
     */
    private static final int NY = 3;

    /**
     * Private helper method to perform Act and Assert phases.
     *
     * @param camera   The camera to construct rays from
     * @param body     The geometric body to intersect
     * @param expected Expected total number of intersections
     * @param testName Name of the test for error reporting
     */
    private void assertIntersectionsCount(Camera camera, Intersectable body, int expected, String testName) {
        int count = 0;
        for (int i = 0; i < NY; ++i) {
            for (int j = 0; j < NX; ++j) {
                Ray ray = camera.constructRay(j, i);
                List<Point> intersections = body.findIntersections(ray);
                if (intersections != null) count += intersections.size();
            }
        }
        assertEquals(expected, count, "Wrong total number of intersections for " + testName);
    }

    /**
     * Integration tests for Camera and Sphere.
     */
    @Test
    void testCameraRaySphereIntegration() {
        Camera camera1 = Camera.getBuilder()
                .setLocation(Point.ZERO)
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpDistance(1).setVpSize(3, 3).setResolution(3, 3).build();

        // ============ Equivalence Partitions Tests ==============
        // EP01: Small sphere in front of camera (2 points)
        assertIntersectionsCount(camera1, new Sphere(new Point(0, 0, -3), 1), 2, "Sphere EP01");

        // EP02: Sphere encompasses all rays (18 points)
        Camera camera2 = Camera.getBuilder()
                .setLocation(new Point(0, 0, 0.5))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpDistance(1).setVpSize(3, 3).setResolution(3, 3).build();
        assertIntersectionsCount(camera2, new Sphere(new Point(0, 0, -2.5), 2.5), 18, "Sphere EP02");

        // EP03: Medium sphere (10 points)
        assertIntersectionsCount(camera2, new Sphere(new Point(0, 0, -2), 2), 10, "Sphere EP03");

        // =============== Boundary Values Tests ==================
        // BV01: Camera inside the sphere (9 points)
        assertIntersectionsCount(camera2, new Sphere(new Point(0, 0, -1), 4), 9, "Sphere BV01");

        // BV02: Sphere behind the camera (0 points)
        assertIntersectionsCount(camera1, new Sphere(new Point(0, 0, 1), 0.5), 0, "Sphere BV02");
    }

    /**
     * Integration tests for Camera and Plane.
     */
    @Test
    void testCameraRayPlaneIntegration() {
        Camera camera = Camera.getBuilder()
                .setLocation(Point.ZERO)
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpDistance(1).setVpSize(3, 3).setResolution(3, 3).build();

        // ============ Equivalence Partitions Tests ==============
        // EP01: Plane orthogonal to camera (9 points)
        assertIntersectionsCount(camera, new Plane(new Point(0, 0, -5), new Vector(0, 0, 1)), 9, "Plane EP01");

        // EP02: Tilted plane intersecting all rays (9 points)
        assertIntersectionsCount(camera, new Plane(new Point(0, 0, -5), new Vector(0, 1, 2)), 9, "Plane EP02");

        // =============== Boundary Values Tests ==================
        // BV01: Tilted plane, some rays miss (6 points)
        assertIntersectionsCount(camera, new Plane(new Point(0, 0, -5), new Vector(0, 5, 1)), 6, "Plane BV01");
    }

    /**
     * Integration tests for Camera and Triangle.
     */
    @Test
    void testCameraRayTriangleIntegration() {
        Camera camera = Camera.getBuilder()
                .setLocation(Point.ZERO)
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpDistance(1).setVpSize(3, 3).setResolution(3, 3).build();

        // ============ Equivalence Partitions Tests ==============
        // EP01: Small triangle (1 point)
        assertIntersectionsCount(camera,
                new Triangle(new Point(0, 1, -2), new Point(1, -1, -2), new Point(-1, -1, -2)), 1, "Triangle EP01");

        // EP02: Large tall triangle (2 points)
        assertIntersectionsCount(camera,
                new Triangle(new Point(0, 20, -2), new Point(1, -1, -2), new Point(-1, -1, -2)), 2, "Triangle EP02");
    }
}
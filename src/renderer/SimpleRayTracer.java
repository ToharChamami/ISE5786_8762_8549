package renderer;

import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import scene.Scene;

/**
 * A simple implementation of RayTracerBase.
 * Traces a ray and calculates the color of the closest intersection point.
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Constructor
     *
     * @param scene the scene to be rendered
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    @Override
    public Color traceRay(Ray ray) {
        // Find all intersection points between the ray and the scene's geometries
        List<Point> intersections = _scene.geometries.findIntersections(ray);

        // If no intersections exist, return the background color
        if (intersections == null) {
            return _scene.background;
        }

        // Find the closest point to the ray's origin
        Point closestPoint = ray.findClosestPoint(intersections);

        // Calculate and return the color at the closest intersection point
        return calcColor(closestPoint);
    }

    /**
     * Calculates the color at a specific intersection point.
     * Currently, returns only the ambient light intensity.
     *
     * @param point the intersection point
     * @return the calculated color
     */
    private Color calcColor(Point point) {
        return _scene.ambientLight.getIntensity();
    }
}
package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;

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
        var intersections = _scene.geometries.calcIntersections(ray);

        if (intersections == null) {
            return _scene.background;
        }
        
        Intersection closestIntersection = ray.findClosestIntersection(intersections);

        return calcColor(closestIntersection);
    }

    /**
     * Calculates the color at a specific intersection point.
     * Currently, returns only the ambient light intensity.
     *
     * @param intersection the intersection point
     * @return the calculated color
     */
    private Color calcColor(Intersection intersection) {
        // Color = emission + (ambient * kA)
        return _scene.ambientLight.getIntensity()
                .scale(intersection.material.kA)
                .add(intersection.geometry.getEmission());
    }
}
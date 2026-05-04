package renderer;

import primitives.Color;
import primitives.Ray;
import scene.Scene;

/**
 * Abstract base class for ray tracers.
 */
abstract class RayTracerBase {
    /**
     * The scene to trace rays in
     */
    protected final Scene _scene;

    /**
     * Constructor
     *
     * @param scene the scene
     */
    public RayTracerBase(Scene scene) {
        _scene = scene;
    }

    /**
     * Traces a ray and calculates the color of the point it hits.
     *
     * @param ray the ray to trace
     * @return the color at the intersection point
     */
    public abstract Color traceRay(Ray ray);
}
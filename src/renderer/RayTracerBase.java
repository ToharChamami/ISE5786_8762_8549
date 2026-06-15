package renderer;

import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

/**
 * Abstract base class for ray tracers.
 */
public abstract class RayTracerBase {
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

    /**
     * Pre-processes the intersection data, calculating the normal, view vector, and their dot product.
     *
     * @param intersection the intersection to process
     * @param rayDirection the direction of the camera ray
     * @return true if the dot product vNormal is not zero, false otherwise
     */
    protected boolean preprocessIntersection(Intersection intersection, Vector rayDirection) {
        intersection.normal = intersection.geometry.getNormal(intersection.point);
        intersection.v = rayDirection;
        intersection.vNormal = alignZero(intersection.normal.dotProduct(intersection.v));

        return intersection.vNormal != 0;
    }

    /**
     * Pre-processes the light source data for a specific intersection.
     *
     * @param intersection the intersection data
     * @param lightSource  the light source
     * @return true if the light and the camera are on the same side of the surface
     */
    protected boolean preprocessLightSource(Intersection intersection, LightSource lightSource) {
        intersection.light = lightSource;
        intersection.l = lightSource.getL(intersection.point);
        intersection.lNormal = alignZero(intersection.normal.dotProduct(intersection.l));

        return intersection.lNormal * intersection.vNormal > 0;
    }
}
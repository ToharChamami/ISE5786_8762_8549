package renderer;

import java.util.List;
import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

/**
 * A simple implementation of RayTracerBase.
 * Traces a ray and calculates the color of the closest intersection point.
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Fixed for the size of the ray initial shift for shadow rays
     */
    private static final double DELTA = 0.1;

    /**
     * Maximum recursion level for transparency/reflection
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Minimum attenuation coefficient for recursion stopping
     */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /**
     * Initial attenuation coefficient for recursion
     */
    private static final Double3 INITIAL_K = Double3.ONE;

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
        return calcColor(closestIntersection, ray);
    }

    /**
     * Calculates the total color of an intersection point.
     * Acts as the entry point for the recursive calculation.
     *
     * @param intersection the intersection point data
     * @param ray          the ray that caused the intersection
     * @return the final calculated color
     */
    private Color calcColor(Intersection intersection, Ray ray) {
        return calcColor(intersection, ray, MAX_CALC_COLOR_LEVEL, INITIAL_K);
    }

    /**
     * Recursive function to calculate color at an intersection point,
     * combining local effects and global effects (reflection/refraction).
     *
     * @param intersection the intersection point data
     * @param ray          the ray that caused the intersection
     * @param level        the current recursion level
     * @param k            the current attenuation factor
     * @return the calculated color at the intersection point
     */
    private Color calcColor(Intersection intersection, Ray ray, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, ray);
        return color;
    }

    /**
     * Calculates the local effects (Diffuse and Specular) of all light sources,
     * starting with the geometry's emission color as the base.
     *
     * @param intersection the intersection data container
     * @return the total color from emission and local light sources
     */
    private Color calcLocalEffects(Intersection intersection, Ray ray) {
        preprocessIntersection(intersection, ray.direction());

        Color color = intersection.geometry.getEmission();

        for (LightSource lightSource : _scene.lights) {
            if (unshaded(intersection, lightSource)) {
                intersection.l = lightSource.getL(intersection.point);
                intersection.lNormal = alignZero(intersection.normal.dotProduct(intersection.l));

                color = color.add(
                        lightSource.getIntensity(intersection.point)
                                .scale(calcDiffuse(intersection).add(calcSpecular(intersection)))
                );
            }
        }
        return color;
    }

    /**
     * Calculates the Diffuse reflection component.
     *
     * @param intersection the intersection data
     * @return the calculated diffuse factor
     */
    private Double3 calcDiffuse(Intersection intersection) {
        // kD * |lNormal|
        return intersection.material.kD.scale(Math.abs(intersection.lNormal));
    }

    /**
     * Calculates the Specular reflection component.
     *
     * @param intersection the intersection data
     * @return the calculated specular factor
     */
    private Double3 calcSpecular(Intersection intersection) {
        if (alignZero(intersection.lNormal) == 0) {
            return Double3.ZERO;
        }

        Vector r = intersection.l.subtract(intersection.normal.scale(2 * intersection.lNormal));

        if (r.equals(Vector.ZERO)) {
            return Double3.ZERO;
        }

        r = r.normalize();

        double minusVR = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0 ? Double3.ZERO
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }

    /**
     * Checks if the intersection point is unshaded by other geometries.
     *
     * @param intersection The intersection point to check.
     * @param light        The light source to check against.
     * @return true if the point is unshaded, false otherwise.
     */
    private boolean unshaded(Intersection intersection, LightSource light) {
        Vector lightDirection = light.getL(intersection.point).scale(-1);

        Vector delta = intersection.normal.scale(intersection.normal.dotProduct(lightDirection) > 0 ? DELTA : -DELTA);
        Point point = intersection.point.add(delta);

        Ray lightRay = new Ray(point, lightDirection.scale(-1));

        List<Point> intersections = _scene.geometries.findIntersections(lightRay);

        if (intersections == null || intersections.isEmpty()) {
            return true;
        }

        double lightDistance = light.getDistance(intersection.point);

        for (Point p : intersections) {
            if (p.distance(intersection.point) < lightDistance) {
                return false;
            }
        }
        return true;
    }

    /**
     * Constructs the reflected ray from an intersection point.
     * The starting point is offset along the normal to prevent self-intersection.
     * * @param p the intersection point
     *
     * @param vector the incoming ray direction
     * @param normal the surface normal
     * @return the reflected ray
     */
    private Ray constructReflectedRay(Point p, Vector vector, Vector normal) {
        Vector r = vector.subtract(normal.scale(2 * vector.dotProduct(normal)));
        double delta = vector.dotProduct(normal) > 0 ? 0.1 : -0.1;
        Point pOffset = p.add(normal.scale(delta));

        return new Ray(pOffset, r);
    }

    /**
     * Constructs the refracted (transparent) ray from an intersection point.
     * The starting point is offset along the normal to prevent self-intersection.
     * * @param p the intersection point
     *
     * @param vector the incoming ray direction
     * @param normal the surface normal
     * @return the refracted ray
     */
    private Ray constructRefractedRay(Point p, Vector vector, Vector normal) {
        double delta = vector.dotProduct(normal) > 0 ? 0.1 : -0.1;
        Point pOffset = p.add(normal.scale(-delta));
        return new Ray(pOffset, vector);
    }
}
package renderer;

import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
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
        Intersection closest = findClosestIntersection(ray);
        return closest == null ? _scene.background : calcColor(closest, ray);
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
        return preprocessIntersection(intersection, ray.direction()) ?
                _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K))
                : Color.BLACK;
    }

    /**
     * Recursive function to calculate color at an intersection point,
     * combining local effects and global effects (reflection/refraction).
     *
     * @param intersection the intersection point data
     * @param level        the current recursion level
     * @param k            the current attenuation factor
     * @return the calculated color at the intersection point
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection, k);
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    /**
     * Calculates the local effects (Diffuse and Specular) of all light sources,
     * considering partial shadow (transparency).
     *
     * @param intersection the intersection data container
     * @param k            the accumulated attenuation factor
     * @return the total color from emission and local light sources
     */
    private Color calcLocalEffects(Intersection intersection, Double3 k) {
        Color color = intersection.geometry.getEmission();

        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
                Double3 ktr = transparency(intersection);
                if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                    Color lightIntensity = lightSource.getIntensity(intersection.point).scale(ktr);
                    color = color.add(
                            lightIntensity.scale(calcDiffuse(intersection).add(calcSpecular(intersection)))
                    );
                }
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
        Vector r = intersection.l.subtract(intersection.normal.scale(2 * intersection.lNormal));
        double minusVR = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0 ? Double3.ZERO
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }

    /**
     * Checks if the intersection point is unshaded by other geometries.
     * Temporary fix: partially transparent geometries do not cast full shadows.
     * Only opaque geometries (kT is less MIN_CALC_COLOR_K) will cast a shadow.
     *
     * @param intersection The intersection point to check.
     * @return true if the point is unshaded, false otherwise.
     */
    @SuppressWarnings("unused")
    private boolean unshaded(Intersection intersection) {
        Vector lightDirection = intersection.l.scale(-1);

        // Create a shadow ray, offset by DELTA to avoid self-intersection
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);

        var intersections = _scene.geometries.calcIntersections(lightRay);
        if (intersections == null) {
            return true;
        }

        double lightDistance = intersection.light.getDistance(intersection.point);

        for (Intersection geo : intersections) {
            // Check if the intersecting geometry is closer than the light source
            if (alignZero(geo.point.distance(intersection.point) - lightDistance) <= 0) {
                // If the geometry is opaque (kT is smaller than the minimum threshold) -> light is blocked
                if (geo.material.kT.isLowerThan(MIN_CALC_COLOR_K)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Constructs the reflected ray from an intersection point.
     * The starting point is offset along the normal to prevent self-intersection.
     *
     * @param intersection the intersection cache
     * @return the reflected ray
     */
    private Ray constructReflectedRay(Intersection intersection) {
        Vector r = intersection.v.subtract(intersection.normal.scale(2 * intersection.vNormal));

        return new Ray(intersection.point, r, intersection.normal);
    }

    /**
     * Constructs the refracted (transparent) ray from an intersection point.
     * The starting point is offset along the normal to prevent self-intersection.
     *
     * @param intersection the intersection cache
     * @return the refracted ray
     */
    private Ray constructRefractedRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Calculates the transparency factor of the path between an intersection point and a light source.
     * Accumulates the transparency (kT) of all geometries blocking the light.
     *
     * @param intersection the intersection point to evaluate
     * @return a Double3 representing the attenuation factor due to partial transparency
     */
    private Double3 transparency(Intersection intersection) {
        Vector lightDirection = intersection.l.scale(-1);

        // Create a shadow ray, offset by DELTA to avoid self-intersection
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);

        var intersections = _scene.geometries.calcIntersections(lightRay);

        // If there are no intersections, the light is not blocked at all
        if (intersections == null) {
            return Double3.ONE;
        }

        Double3 ktr = Double3.ONE;
        double lightDistance = intersection.light.getDistance(intersection.point);

        for (Intersection geo : intersections) {
            // Check if the intersecting geometry is closer than the light source
            if (alignZero(geo.point.distance(intersection.point) - lightDistance) <= 0) {
                // Accumulate the transparency factor
                ktr = ktr.product(geo.material.kT);

                // If the transparency is negligible, stop calculating and return fully shaded
                if (ktr.isLowerThan(MIN_CALC_COLOR_K)) {
                    return Double3.ZERO;
                }
            }
        }
        return ktr;
    }

    /**
     * Calculates and returns the closest intersection point to the start of the ray.
     *
     * @param ray the ray to check for intersections
     * @return the closest Intersection object, or null if there are no intersections
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }

    /**
     * Calculates the color of a secondary ray (reflection or refraction)
     * by finding its closest intersection and recursively calculating its color.
     *
     * @param ray   the secondary ray (reflected or refracted)
     * @param level the current recursion level
     * @param k     the accumulated attenuation factor
     * @param kx    the specific attenuation factor for this effect (kR or kT)
     * @return the calculated color scaled by the attenuation factor (kx)
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkr = kx.product(k);
        // Check if the reflection effect is significant enough to continue
        if (kkr.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;

        Intersection closest = findClosestIntersection(ray);
        // If there is no intersection, return the background color scaled by kx
        if (closest == null) {
            return _scene.background.scale(kx);
        }

        // Recursively calculate the color of the intersection and scale it by kx
        return preprocessIntersection(closest, ray.direction())
                ? calcColor(closest, level - 1, k).scale(kx) : Color.BLACK;
    }

    /**
     * Calculates the global effects (reflection and refraction/transparency)
     * by spawning secondary rays and aggregating their colors.
     *
     * @param intersection the current intersection point
     * @param level        the current recursion level
     * @param k            the accumulated attenuation factor
     * @return the total color contribution from global effects
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        // --- Reflection Calculation ---
        Ray reflectedRay = constructReflectedRay(intersection);
        Ray refractedRay = constructRefractedRay(intersection);
        return calcGlobalEffect(reflectedRay, level, k, intersection.material.kR)
                .add(calcGlobalEffect(refractedRay, level, k, intersection.material.kT));
    }

}
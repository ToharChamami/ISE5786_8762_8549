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
     * Calculates the total color of an intersection point by combining ambient light
     * and local effects (emission, diffuse, specular) according to the Phong model.
     *
     * @param intersection the intersection point data
     * @param ray          the ray that caused the intersection
     * @return the final calculated color
     */
    private Color calcColor(Intersection intersection, Ray ray) {
        Color color = _scene.ambientLight.getIntensity().scale(intersection.material.kA);
        if (!preprocessIntersection(intersection, ray.direction())) return color;
        return color.add(calcLocalEffects(intersection));
    }

    /**
     * Calculates the local effects (Diffuse and Specular) of all light sources,
     * starting with the geometry's emission color as the base.
     *
     * @param intersection the intersection data container
     * @return the total color from emission and local light sources
     */
    private Color calcLocalEffects(Intersection intersection) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights) {
            if (preprocessLightSource(intersection, lightSource)) {
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
        // r = l - normal * 2 * (l * normal)
        Vector r = intersection.l.subtract(intersection.normal.scale(2 * intersection.lNormal)).normalize();

        double minusVR = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0 ? Double3.ZERO
                // kS * (-v * r)^nShininess
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }
}
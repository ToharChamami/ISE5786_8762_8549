package renderer;

import java.util.ArrayList;
import java.util.List;
import lighting.LightSource;
import lighting.PointLight;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.sampling.Offset2D;
import renderer.sampling.Sampler;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;
import static primitives.Vector.AXIS_X;

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

    // --- MINI-PROJECT 1 CONFIGURATION FIELDS ---
    private boolean softShadows = false; // Toggle: false = hard shadows (default), true = soft shadows
    private TargetShape shadowTargetShape = TargetShape.CIRCLE;
    private Sampler shadowSampler = null;

    /**
     * The sampling pattern strategy used for generating shadow ray distributions.
     * Defaults to {@link SamplingPattern#REGULAR_GRID}.
     */
    private SamplingPattern shadowPattern = SamplingPattern.REGULAR_GRID;

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
     * Calculates the shadow attenuation factor (shading coefficient) for an intersection point.
     * Supports both infinitesimal point lights (returns 0.0 or 1.0) and area lights with a
     * dimensional radius to produce soft shadows (returns a fraction between 0.0 and 1.0).
     *
     * @param intersection The intersection point data structure containing geometric and light data.
     * @return The shadow attenuation factor coefficient from 0.0 (full shadow) to 1.0 (fully lit).
     */
    @SuppressWarnings("unused")
    private double unshaded(Intersection intersection) {
        if (this.softShadows
                && this.shadowSampler != null
                && this.shadowSampler.getGridSize() > 1
                && intersection.light instanceof PointLight pointLight
                && pointLight.getRadius() > 0) {
            return calcSoftShadowFactor(intersection, pointLight);
        }
        Vector lightDirection = intersection.l.scale(-1);
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);
        var intersections = _scene.geometries.calcIntersections(lightRay);
        if (intersections == null) {
            return 1.0;
        }
        double lightDistance = intersection.light.getDistance(intersection.point);
        for (Intersection geo : intersections) {
            if (alignZero(geo.point.distance(intersection.point) - lightDistance) <= 0) {
                if (geo.material.kT.isLowerThan(MIN_CALC_COLOR_K)) {
                    return 0.0;
                }
            }
        }
        return 1.0;
    }

    /**
     * Helper method to calculate the soft shadow illumination coefficient using a distributed beam of rays.
     * Constructs a local orthonormal coordinate system grid on the light disk surface.
     *
     * @param intersection The core intersection data of the shaded geometry point.
     * @param pointLight   The point light source object casted to access its dimensional radius.
     * @return The fraction of unblocked rays reaching the light surface (between 0.0 and 1.0).
     */
    private double calcSoftShadowFactor(Intersection intersection, PointLight pointLight) {
        renderer.sampling.Sampler sampler = new renderer.sampling.Sampler(9);

        double radius = pointLight.getRadius();
        Point lightPosition = pointLight.getPosition();
        Vector l = intersection.l.normalize();
        Vector helperAxis = (Math.abs(l.dotProduct(AXIS_X)) > 0.9) ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
        Vector u = l.crossProduct(helperAxis).normalize();
        Vector v = l.crossProduct(u).normalize();
        List<Offset2D> offsets = this.shadowSampler.getSamplePoints(this.shadowTargetShape);

        int unshadedRaysCount = 0;
        int totalRays = offsets.size();
        for (renderer.sampling.Offset2D offset : offsets) {
            double deltaX = offset.getX() * radius;
            double deltaY = offset.getY() * radius;

            Point samplePoint = lightPosition;
            if (deltaX != 0) {
                samplePoint = samplePoint.add(u.scale(deltaX));
            }
            if (deltaY != 0) {
                samplePoint = samplePoint.add(v.scale(deltaY));
            }
            Vector shadowRayDir = samplePoint.subtract(intersection.point);
            Ray shadowRay = new Ray(intersection.point, shadowRayDir, intersection.normal);
            var intersections = _scene.geometries.calcIntersections(shadowRay);
            if (intersections == null) {
                unshadedRaysCount++;
                continue;
            }
            double currentLightDistance = samplePoint.distance(intersection.point);
            boolean isRayBlocked = false;
            for (Intersection geo : intersections) {
                if (alignZero(geo.point.distance(intersection.point) - currentLightDistance) <= 0) {
                    if (geo.material.kT.isLowerThan(MIN_CALC_COLOR_K)) {
                        isRayBlocked = true;
                        break;
                    }
                }
            }
            if (!isRayBlocked) {
                unshadedRaysCount++;
            }
        }
        return (double) unshadedRaysCount / totalRays;
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
        Ray lightRay = new Ray(intersection.point, lightDirection, intersection.normal);

        var intersections = _scene.geometries.calcIntersections(lightRay);
        if (intersections == null) {
            return Double3.ONE;
        }
        Double3 ktr = Double3.ONE;
        double lightDistance = intersection.light.getDistance(intersection.point);

        for (Intersection geo : intersections) {
            if (alignZero(geo.point.distance(intersection.point) - lightDistance) <= 0) {
                ktr = ktr.product(geo.material.kT);
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
        if (kkr.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        Intersection closest = findClosestIntersection(ray);
        if (closest == null) {
            return _scene.background.scale(kx);
        }
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

    /**
     * Generates a beam of shadow rays from the given intersection point toward
     * the surface of a dimensional area light source (such as a PointLight with a radius).
     * This method constructs a local coordinate system orthogonal to the main light direction vector.
     *
     * @param intersection The intersection point data structure containing geometric and light data.
     * @param light        the light source being evaluated
     * @param l            the main normalized direction vector from the light center to the point
     * @param sampler      the Sampler system providing pre-calculated 2D offset points
     * @return a list of generated shadow rays directed at the sampled points on the light source surface
     */
    private List<Ray> generateShadowBeam(Intersection intersection, PointLight light, Vector l, Sampler sampler) {
        List<Ray> beamRays = new ArrayList<>();
        double radius = light.getRadius();
        Point lightPosition = light.getPosition();
        Vector xAxis = new Vector(1, 0, 0);
        Vector helperAxis = (Math.abs(l.dotProduct(xAxis)) > 0.9) ? new Vector(0, 1, 0) : xAxis;

        Vector u = l.crossProduct(helperAxis).normalize();
        Vector v = l.crossProduct(u).normalize();

        List<Offset2D> offsets = sampler.getSamplePoints(TargetShape.CIRCLE);
        for (Offset2D offset : offsets) {
            double deltaX = offset.getX() * radius;
            double deltaY = offset.getY() * radius;

            Point samplePoint = lightPosition;
            if (deltaX != 0) {
                samplePoint = samplePoint.add(u.scale(deltaX));
            }
            if (deltaY != 0) {
                samplePoint = samplePoint.add(v.scale(deltaY));
            }
            Vector rayDirection = samplePoint.subtract(intersection.point);
            beamRays.add(new Ray(intersection.point, rayDirection, intersection.normal));
        }
        return beamRays;
    }

    /**
     * Toggles the soft shadows feature on or off.
     *
     * @param softShadows true to enable soft shadows, false for classic hard shadows
     * @return the SimpleRayTracer object itself for builder pattern chaining
     */
    public SimpleRayTracer setSoftShadows(boolean softShadows) {
        this.softShadows = softShadows;
        // If enabled but no sampler was built yet, initialize with a default 9x9 grid
        if (softShadows && this.shadowSampler == null) {
            this.shadowSampler = new renderer.sampling.Sampler(9);
        }
        return this;
    }

    /**
     * Sets the sampling grid density matrix size (e.g., 9 for a 9x9 grid).
     * Automatically instantiates the sampling engine once to guarantee high performance.
     *
     * @param gridSize number of rows and columns for the sample matrix
     * @return the SimpleRayTracer object itself for builder pattern chaining
     */
    public SimpleRayTracer setShadowGridSize(int gridSize) {
        this.shadowSampler = new renderer.sampling.Sampler(gridSize);
        return this;
    }

    /**
     * Configures the geometric shape configuration of the area light boundary surface.
     *
     * @param shape the target boundary shape (e.g., CIRCLE, SQUARE)
     * @return the SimpleRayTracer object itself for builder pattern chaining
     */
    public SimpleRayTracer setShadowTargetShape(TargetShape shape) {
        this.shadowTargetShape = shape;
        return this;
    }

    /**
     * Configures the mathematical distribution pattern template for the shadow beam rays.
     * This setting determines how sample points are scattered across the light source area
     * (e.g., a uniform structured grid versus a randomized stratified jittered distribution)
     * This method supports fluent builder pattern chaining.
     *
     * @param pattern the desired sampling distribution pattern strategy
     * @return the SimpleRayTracer object itself for builder pattern chaining
     */
    public SimpleRayTracer setShadowSamplingPattern(SamplingPattern pattern) {
        this.shadowPattern = pattern;
        return this;
    }
}


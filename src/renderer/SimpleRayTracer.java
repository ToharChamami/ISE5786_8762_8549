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
     * Maximum recursion level for transparency/reflection.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;

    /**
     * Minimum attenuation coefficient for recursion stopping.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;

    /**
     * Initial attenuation coefficient for recursion.
     */
    private static final Double3 INITIAL_K = Double3.ONE;

    // --- MINI-PROJECT 1 CONFIGURATION FIELDS ---
    private boolean softShadows = false; // Toggle: false = hard shadows (default), true = soft shadows
    private TargetShape shadowTargetShape = TargetShape.CIRCLE;
    private Sampler shadowSampler = new Sampler(1);
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
                // קריאה לפונקציית ה-unshaded שבודקת צל רך או קשיח
                double shadowFactor = unshaded(intersection);

                if (shadowFactor > MIN_CALC_COLOR_K) {
                    // שילוב השקיפות הרגילה יחד עם פקטור הצל שהתקבל
                    Double3 ktr = transparency(intersection).scale(shadowFactor);

                    if (ktr.product(k).isGreaterThan(MIN_CALC_COLOR_K)) {
                        Color lightIntensity = lightSource.getIntensity(intersection.point).scale(ktr);
                        color = color.add(
                                lightIntensity.scale(calcDiffuse(intersection).add(calcSpecular(intersection)))
                        );
                    }
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
     */
    private double calcSoftShadowFactor(Intersection intersection, PointLight pointLight) {
        double radius = pointLight.getRadius();
        Point lightPosition = pointLight.getPosition();

        Vector toLight = lightPosition.subtract(intersection.point).normalize();

        Vector helperAxis = (Math.abs(toLight.dotProduct(AXIS_X)) > 0.9)
                ? new Vector(0, 1, 0)
                : new Vector(1, 0, 0);
        Vector u = toLight.crossProduct(helperAxis).normalize();
        Vector v = toLight.crossProduct(u).normalize();

        List<Offset2D> offsets = this.shadowSampler.getSamplePoints(this.shadowTargetShape);
        int unshadedRaysCount = 0;
        int totalRays = offsets.size();

        for (Offset2D offset : offsets) {
            double deltaX = offset.getX() * 2 * radius;
            double deltaY = offset.getY() * 2 * radius;

            Point samplePoint = lightPosition;
            if (!primitives.Util.isZero(deltaX))
                samplePoint = samplePoint.add(u.scale(deltaX));
            if (!primitives.Util.isZero(deltaY))
                samplePoint = samplePoint.add(v.scale(deltaY));

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
            if (!isRayBlocked) unshadedRaysCount++;
        }
        return totalRays == 0 ? 1.0 : (double) unshadedRaysCount / totalRays;
    }

    /**
     * Constructs the reflected ray from an intersection point.
     */
    private Ray constructReflectedRay(Intersection intersection) {
        Vector r = intersection.v.subtract(intersection.normal.scale(2 * intersection.vNormal));
        return new Ray(intersection.point, r, intersection.normal);
    }

    /**
     * Constructs the refracted (transparent) ray from an intersection point.
     */
    private Ray constructRefractedRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Calculates the transparency factor of the path between an intersection point and a light source.
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
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }

    /**
     * Calculates the color of a secondary ray (reflection or refraction).
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
     * Calculates the global effects (reflection and refraction/transparency).
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        Ray reflectedRay = constructReflectedRay(intersection);
        Ray refractedRay = constructRefractedRay(intersection);
        return calcGlobalEffect(reflectedRay, level, k, intersection.material.kR)
                .add(calcGlobalEffect(refractedRay, level, k, intersection.material.kT));
    }

    /**
     * Generates a beam of shadow rays from the given intersection point toward the surface of a dimensional area light source.
     */
    private List<Ray> generateShadowBeam(Intersection intersection, PointLight light, Vector l, Sampler sampler) {
        List<Ray> beamRays = new ArrayList<>();
        double radius = light.getRadius();
        Point lightPosition = light.getPosition();
        Vector xAxis = new Vector(1, 0, 0);
        Vector helperAxis = (Math.abs(l.dotProduct(xAxis)) > 0.9) ? new Vector(0, 1, 0) : xAxis;

        Vector u = l.crossProduct(helperAxis).normalize();
        Vector v = l.crossProduct(u).normalize();

        List<Offset2D> offsets = sampler.getSamplePoints(this.shadowTargetShape);
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
     */
    public SimpleRayTracer setSoftShadows(boolean softShadows) {
        this.softShadows = softShadows;
        if (softShadows && this.shadowSampler == null) {
            this.shadowSampler = new Sampler(9);
        }
        return this;
    }

    /**
     * Sets the shadow target shape.
     */
    public SimpleRayTracer setShadowTargetShape(TargetShape shape) {
        this.shadowTargetShape = shape;
        return this;
    }

    /**
     * Configures the mathematical distribution pattern template for the shadow beam rays.
     */
    public SimpleRayTracer setShadowSamplingPattern(SamplingPattern pattern) {
        this.shadowPattern = pattern;
        return this;
    }

    /**
     * Sets the number of samples (grid size dimension) for the soft shadows beam.
     * For example, passing 9 will configure a 9x9 grid (81 samples).
     */
    public SimpleRayTracer setShadowSamples(int gridSize) {
        this.shadowSampler = new Sampler(gridSize);
        return this;
    }
}
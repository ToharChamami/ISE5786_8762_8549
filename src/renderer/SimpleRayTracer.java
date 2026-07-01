package renderer;

import java.util.List;
import lighting.LightSource;
import lighting.PointLight;
import primitives.Color;
import primitives.Double3;
import primitives.Ray;
import primitives.Vector;
import renderer.sampling.Sampler;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

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
    // אלו השדות שחסרים לך - תוודאי שהם מופיעים בחלק העליון של המחלקה:
    private boolean softShadows = false;
    private TargetShape shadowTargetShape = TargetShape.CIRCLE;
    private SamplingPattern shadowPattern = SamplingPattern.REGULAR_GRID;
    private Sampler shadowSampler = new Sampler(1);

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

                // האורות כבר עודכנו מראש, פשוט קוראים לחישוב!
                Double3 ktr = transparency(intersection, lightSource);

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
     * Constructs the reflected ray from an intersection point.
     *
     * @param intersection the intersection data
     * @return the reflected ray
     */
    private Ray constructReflectedRay(Intersection intersection) {
        Vector r = intersection.v.subtract(intersection.normal.scale(2 * intersection.vNormal));
        return new Ray(intersection.point, r, intersection.normal);
    }

    /**
     * Constructs the refracted (transparent) ray from an intersection point.
     *
     * @param intersection the intersection data
     * @return the refracted ray
     */
    private Ray constructRefractedRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.normal);
    }

    /**
     * Calculates the transparency/shadow factor of the path between an intersection point and a light source.
     * Evaluates a beam of rays for soft shadows and averages their contribution.
     *
     * @param intersection the intersection data
     * @param lightSource  the light source to check
     * @return the average transparency attenuation factor (Double3)
     */
    private Double3 transparency(Intersection intersection, LightSource lightSource) {
        // 1. קבלת אלומת הוקטורים (קרניים) ממקור האור אל הנקודה
        List<Vector> lightDirectionBeam = lightSource.getLBeam(intersection.point);
        
        Double3 totalKtr = Double3.ZERO;
        double lightDistance = lightSource.getDistance(intersection.point);

        // 2. מעבר על כל קרן באלומה
        for (Vector beamL : lightDirectionBeam) {
            // הוקטור שהתקבל הוא מהאור לנקודה, נהפוך אותו כדי שייצא מהנקודה אל האור
            Vector lightDirection = beamL.scale(-1);
            Ray shadowRay = new Ray(intersection.point, lightDirection, intersection.normal);

            var shadowIntersections = _scene.geometries.calcIntersections(shadowRay);
            Double3 ktr = Double3.ONE;

            if (shadowIntersections != null) {
                for (Intersection geo : shadowIntersections) {
                    // מוודאים שהחוסם נמצא בין הנקודה למקור האור ולא מאחוריו
                    if (alignZero(geo.point.distance(intersection.point) - lightDistance) <= 0) {
                        ktr = ktr.product(geo.material.kT);
                        // עצירת חישוב מוקדמת אם הקרן נחסמה לחלוטין (פגעה באובייקט אטום)
                        if (ktr.isLowerThan(MIN_CALC_COLOR_K)) {
                            ktr = Double3.ZERO;
                            break;
                        }
                    }
                }
            }
            // הוספת התרומה של הקרן הנוכחית לסך הכל
            totalKtr = totalKtr.add(ktr);
        }

        // 3. חישוב הממוצע (חלוקת הסכום במספר הקרניים)
        return totalKtr.scale(1.0 / lightDirectionBeam.size());
    }

    /**
     * Calculates and returns the closest intersection point to the start of the ray.
     *
     * @param ray the ray to trace
     * @return the closest intersection point, or null if none
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }

    /**
     * Calculates the color of a secondary ray (reflection or refraction).
     *
     * @param ray   the secondary ray
     * @param level the current recursion level
     * @param k     the accumulated attenuation factor
     * @param kx    the reflection or refraction coefficient
     * @return the calculated color for the secondary ray
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
     *
     * @param intersection the intersection data
     * @param level        the current recursion level
     * @param k            the accumulated attenuation factor
     * @return the combined color from global effects
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        Ray reflectedRay = constructReflectedRay(intersection);
        Ray refractedRay = constructRefractedRay(intersection);
        return calcGlobalEffect(reflectedRay, level, k, intersection.material.kR)
                .add(calcGlobalEffect(refractedRay, level, k, intersection.material.kT));
    }

    // פונקציית עזר שדוחפת את ההגדרות לכל האורות בסצנה (פועלת פעם אחת)
    private void updateLightsWithShadows() {
        if (_scene != null && _scene.lights != null) {
            for (LightSource light : _scene.lights) {
                if (light instanceof PointLight pointLight) {
                    pointLight.setSampler(this.shadowSampler)
                            .setShadowTargetShape(this.shadowTargetShape)
                            .setShadowSamplingPattern(this.shadowPattern);
                }
            }
        }
    }

    // העדכון של ה-Setters שיקראו לפונקציה:
    public SimpleRayTracer setSoftShadows(boolean softShadows) {
        this.softShadows = softShadows;
        return this;
    }

    public SimpleRayTracer setShadowTargetShape(TargetShape shape) {
        this.shadowTargetShape = shape;
        updateLightsWithShadows();
        return this;
    }

    public SimpleRayTracer setShadowSamplingPattern(SamplingPattern pattern) {
        this.shadowPattern = pattern;
        updateLightsWithShadows();
        return this;
    }

    public SimpleRayTracer setShadowSamples(int gridSize) {
        this.shadowSampler = new Sampler(gridSize);
        updateLightsWithShadows();
        return this;
    }

}
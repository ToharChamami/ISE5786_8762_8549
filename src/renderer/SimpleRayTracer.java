package renderer;

import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import renderer.sampling.Sampler;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;
import scene.Scene;

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

    /**
     * Toggle switch for shadow rendering type.
     * {@code false} for hard shadows (default), {@code true} for soft shadows.
     */
    private boolean softShadows = false;

    /**
     * The geometric boundary shape configuration of the area light source.
     */
    private TargetShape shadowTargetShape = TargetShape.SQUARE;

    /**
     * The mathematical distribution pattern template for the shadow beam rays.
     */
    private SamplingPattern shadowPattern = SamplingPattern.REGULAR_GRID;

    /**
     * The sampling engine responsible for generating distribution offsets.
     */
    private Sampler shadowSampler = new Sampler(1);

    /**
     * Constructs a SimpleRayTracer with the specified scene.
     *
     * @param scene the scene to be traced and rendered
     */
    public SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Traces a ray into the scene and determines the color of the closest intersection point.
     *
     * @param ray the ray to trace
     * @return the calculated color at the intersection point, or the background color if no intersection occurs
     */
    @Override
    public Color traceRay(Ray ray) {
        // מציאת כל החיתוכים (וודא שזה מחזיר List של הטיפוס הנכון - כנראה GeoPoint)
        var intersections = _scene.geometries.findIntersections(ray);

        if (intersections == null || intersections.isEmpty()) {
            return _scene.background;
        }

        // מציאת החיתוך הקרוב ביותר מתוך הרשימה
        // אם ray.findClosestGeoPoint קיימת, השתמש בה.
        // אם לא, השתמש במתודת עזר פשוטה:
        Point closest = ray.findClosestPoint(intersections);

        return calCcolor(closest, ray);
    }

    /**
     * Enables or disables soft shadows rendering feature.
     *
     * @param softShadows {@code true} to enable soft shadows, {@code false} for default hard shadows
     * @return the SimpleRayTracer object itself for builder pattern chaining
     */
    public SimpleRayTracer setSoftShadows(boolean softShadows) {
        this.softShadows = softShadows;
        return this;
    }

    /**
     * Configures the grid density matrix size for shadow sampling.
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
}
package renderer;

import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import java.util.ArrayList;
import java.util.List;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;
import scene.Scene;

/**
 * Performance test suite for Mini-Project 2 - Bounding Volume Hierarchy (BVH).
 * Runs 12 different configurations on the same complex scene (dark desk) and measures execution times.
 */
public class BvhPerformanceTest {
    /**
     * Material property for wooden objects in the scene.
     */
    private final Material woodMat = new Material().setKD(0.7).setKS(0.2).setShininess(30);

    /**
     * Color representation for the dark wood textures.
     */
    private final Color woodColor = new Color(50, 25, 15);

    /**
     * Material property for paper sheets placed on the desk.
     */
    private static final Material paperMat = new Material().setKD(0.8).setKS(0.1).setShininess(10);

    /**
     * Diffuse gray color representation for papers to avoid overexposure.
     */
    private static final Color paperColor = new Color(180, 180, 180);

    /**
     * Material property for the reflective ceramic vase body.
     */
    private static final Material vaseMat = new Material().setKD(0.7).setKS(0.6).setShininess(80).setKR(0.1);

    /**
     * Deep blue color representation for the vase.
     */
    private static final Color vaseColor = new Color(30, 60, 100);

    /**
     * Material property allocated to organic plant and flower components.
     */
    private static final Material plantMat = new Material().setKD(0.6).setKS(0.2).setShininess(20);

    /**
     * Default constructor
     */
    public BvhPerformanceTest() {
    }

    /**
     * Helper function for common camera builder setups.
     *
     * @param scene The scene being tracked by the camera builder.
     * @return A pre-configured {@code Camera.Builder} instance.
     */
    protected Camera.Builder getCameraBuilder(Scene scene) {
        return Camera.getBuilder()
                .setLocation(new Point(0, 150, 350))
                .setDirection(new Vector(0, -0.3, -1), new Vector(0, 1, -0.3))
                .setVpSize(200, 200)
                .setSoftShadows(true)
                .setVpDistance(300)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(0) //
                .setDebugPrint(0.1)
                .setSoftShadows(true)
                .setShadowTargetShape(TargetShape.CIRCLE)
                .setShadowSamples(9)
                .setShadowSamplingPattern(SamplingPattern.JITTERED_GRID);
    }

    /**
     * Helper function to create the common dark scene and lighting environment.
     *
     * @param name The name of the scene.
     * @return A configured {@code Scene} instance with baseline dark elements.
     */
    private static Scene createDarkScene(String name) {
        Scene scene = new Scene(name)
                .setBackground(new Color(10, 10, 15))
                .setAmbientLight(new AmbientLight(new Color(15, 15, 15), Double3.ONE));

        scene.geometries.add(
                new Plane(new Point(0, -100, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(20, 20, 25))
                        .setMaterial(new Material().setKD(0.5).setKS(0.1).setShininess(5))
        );

        scene.lights.add(new SpotLight(new Color(300, 270, 220), new Point(100, 250, 100), new Vector(-1, -2, -1))
                .setKl(0.00001).setKq(0.000001).setRadius(10));

        scene.lights.add(new PointLight(new Color(40, 50, 80), new Point(-200, 100, 200))
                .setKl(0.0001).setKq(0.00001).setRadius(15));

        return scene;
    }

    /**
     * Helper method to manually construct a hierarchical tree structure using existing geometries.
     * The method organizes baseline table elements, legs, and props into separate composite geometry layers.
     *
     * @return A {@code Geometries} collection containing the organized manual hierarchy layers.
     */
    protected Geometries getManualHierarchyRoot() {
        Geometries root = new Geometries();
        root.add(new Geometries(getTableTop().toArray(new Intersectable[0])));
        root.add(new Geometries(getRightLegs().toArray(new Intersectable[0])));
        root.add(new Geometries(getLeftLegs().toArray(new Intersectable[0])));
        root.add(new Geometries(getPapers().toArray(new Intersectable[0])));
        root.add(new Geometries(getVaseBody().toArray(new Intersectable[0])));
        for (Geometries flower : getFlowers()) {
            root.add(flower);
        }
        root.add(getTeapotOnTable());
        return root;
    }

    /**
     * Central execution method to render the scene configuration and print performance tracking.
     * Sets up global CBR configurations, hooks thread loads, fires tracers, and benchmarks operational timings.
     *
     * @param testName     The designated name of the rendered output image file.
     * @param scene        The active scene container containing objects and lights.
     * @param enableCbr    Flag determining whether Conservative Bounding Region (CBR) optimization is active.
     * @param threadsCount Total rendering threads to allocate (0 for disabled, -1 for max available).
     */
    private void runConfig(String testName, Scene scene, boolean enableCbr, int threadsCount) {
        Intersectable.cbrActive = enableCbr;

        if (enableCbr) {
            scene.geometries.createBoundingBox();
        }

        Camera camera = getCameraBuilder(scene).setMultithreading(threadsCount).setDebugPrint(0).build();

        System.out.println(">>> Starting Configuration: " + testName);
        long start = System.currentTimeMillis();
        camera.renderImage();
        long end = System.currentTimeMillis();

        camera.writeToImage(testName);
        System.out.println("Finished in: " + (end - start) / 1000.0 + " seconds.\n");
    }

    // =========================================================================
    // 1. Flattened Scene Configurations
    // =========================================================================

    /**
     * Test method for a flat scene structure running without acceleration or multithreading.
     */
    @Test
    public void test01_Flat_NoAcceleration_NoMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Simple unaccelerated single-threaded rendering baseline loop
        Scene scene = createDarkScene("Flat_NoAcc_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();

        runConfig("01_Flat_NoAcc_NoMT", scene, false, 0);
    }

    /**
     * Test method for a flat scene structure running with multithreading but no acceleration.
     */
    @Test
    public void test02_Flat_NoAcceleration_WithMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Simple unaccelerated execution scaling utilizing multi-core workers
        Scene scene = createDarkScene("Flat_NoAcc_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();
        runConfig("02_Flat_NoAcc_WithMT", scene, false, -1);
    }

    /**
     * Test method for a flat scene structure utilizing basic CBR optimizations without multithreading.
     */
    @Test
    public void test03_Flat_CBR_NoMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Evaluating bounding region speedups on unorganized flat geometries
        Scene scene = createDarkScene("Flat_CBR_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();
        runConfig("03_Flat_CBR_NoMT", scene, true, 0);
    }

    /**
     * Test method for a flat scene structure utilizing both CBR optimizations and multithreading.
     */
    @Test
    public void test04_Flat_CBR_WithMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Benchmarking concurrent thread loads on a flat bounding-box environment
        Scene scene = createDarkScene("Flat_CBR_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();
        runConfig("04_Flat_CBR_WithMT", scene, true, -1);
    }

    // =========================================================================
    // 2. Manual Hierarchy Configurations
    // =========================================================================

    /**
     * Test method for a manually layered scene running without acceleration or multithreading.
     */
    @Test
    public void test05_ManualHierarchy_NoAcceleration_NoMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Verifying rendering calculations for hardcoded group nestings
        Scene scene = createDarkScene("Manual_NoAcc_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("05_Manual_NoAcc_NoMT", scene, false, 0);
    }

    /**
     * Test method for a manually layered scene running with multithreading but no acceleration.
     */
    @Test
    public void test06_ManualHierarchy_NoAcceleration_WithMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Scaling manual cluster groups over concurrent thread pools
        Scene scene = createDarkScene("Manual_NoAcc_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("06_Manual_NoAcc_WithMT", scene, false, -1);
    }

    /**
     * Test method for a manually layered scene utilizing CBR optimizations without multithreading.
     */
    @Test
    public void test07_ManualHierarchy_CBR_NoMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Processing manual bounding boxes inside a single core execution thread
        Scene scene = createDarkScene("Manual_CBR_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("07_Manual_CBR_NoMT", scene, true, 0);
    }

    /**
     * Test method for a manually layered scene utilizing both CBR optimizations and multithreading.
     */
    @Test
    public void test08_ManualHierarchy_CBR_WithMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Combining structured layers, bounding boxes, and active parallel threads
        Scene scene = createDarkScene("Manual_CBR_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("08_Manual_CBR_WithMT", scene, true, -1);
    }

    // =========================================================================
    // 3. Automatic BVH Configurations
    // =========================================================================

    /**
     * Test method for automated tree construction and processing without multithreading.
     */
    @Test
    public void test09_AutoHierarchy_NoAcceleration_NoMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Verifying correct automated split execution and linear traversals
        Scene scene = createDarkScene("Auto_NoAcc_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();

        long startTree = System.currentTimeMillis();
        scene.geometries.buildBVH();
        long endTree = System.currentTimeMillis();
        System.out.println("Automatic BVH Tree build time: " + (endTree - startTree) + " ms.");

        runConfig("09_Auto_NoAcc_NoMT", scene, false, 0);
    }

    /**
     * Test method for automated tree construction processing utilizing thread pools.
     */
    @Test
    public void test10_AutoHierarchy_NoAcceleration_WithMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Rendering the automatic split tree structure concurrently
        Scene scene = createDarkScene("Auto_NoAcc_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();
        scene.geometries.buildBVH();
        runConfig("10_Auto_NoAcc_WithMT", scene, false, -1);
    }

    /**
     * Test method for full automatic BVH acceleration running single-threaded.
     */
    @Test
    public void test11_AutoHierarchy_CBR_NoMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Maximizing tree traversing speedups sequentially inside one worker ray thread
        Scene scene = createDarkScene("Auto_CBR_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();
        scene.geometries.buildBVH();
        runConfig("11_Auto_CBR_NoMT", scene, true, 0);
    }

    /**
     * Test method for complete automatic BVH acceleration paired with multi-core rendering.
     */
    @Test
    public void test12_AutoHierarchy_CBR_WithMT() {
        // ============ Equivalence Partitions Tests ==============
        // EP01: Evaluating total optimal framework throughput (BVH Tree + CBR + Multithreading)
        Scene scene = createDarkScene("Auto_CBR_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        scene.geometries.flatten();
        scene.geometries.buildBVH();
        runConfig("12_Auto_CBR_WithMT", scene, true, -1);
    }

    /**
     * Helper function generating table surface components.
     *
     * @return A list of geometries representing the tabletop planks and boundaries.
     */
    protected List<Intersectable> getTableTop() {
        List<Intersectable> list = new ArrayList<>();
        list.add(new Polygon(new Point(120, 0, 80), new Point(120, 0, -80), new Point(-120, 0, -80), new Point(-120, 0, 80)).setEmission(woodColor).setMaterial(woodMat));
        list.add(new Polygon(new Point(120, -5, -80), new Point(120, -5, 80), new Point(-120, -5, 80), new Point(-120, -5, -80)).setEmission(woodColor).setMaterial(woodMat));
        list.add(new Polygon(new Point(120, 0, 80), new Point(-120, 0, 80), new Point(-120, -5, 80), new Point(120, -5, 80)).setEmission(woodColor).setMaterial(woodMat));
        list.add(new Polygon(new Point(120, -5, -80), new Point(-120, -5, -80), new Point(-120, 0, -80), new Point(120, 0, -80)).setEmission(woodColor).setMaterial(woodMat));
        list.add(new Polygon(new Point(120, 0, -80), new Point(120, -5, -80), new Point(120, -5, 80), new Point(120, 0, 80)).setEmission(woodColor).setMaterial(woodMat));
        list.add(new Polygon(new Point(-120, 0, 80), new Point(-120, -5, 80), new Point(-120, -5, -80), new Point(-120, 0, -80)).setEmission(woodColor).setMaterial(woodMat));
        return list;
    }

    /**
     * Helper method to create a square table leg using four polygons.
     * The polygons are constructed with vertices ordered counter-clockwise
     * to ensure the normal vectors face outwards.
     *
     * @param topCenter The center point at the top of the leg.
     * @param width     The width and depth of the square leg.
     * @param height    The height of the leg extending downwards.
     * @return A Geometries object containing the four polygon faces of the leg.
     */
    private Geometries createSquareLeg(Point topCenter, double width, double height) {
        Geometries leg = new Geometries();
        double w = width / 2;
        double x = topCenter.getX();
        double y = topCenter.getY();
        double z = topCenter.getZ();

        leg.add(new Polygon(new Point(x - w, y, z + w), new Point(x - w, y - height, z + w), new Point(x + w, y - height, z + w), new Point(x + w, y, z + w)).setEmission(woodColor).setMaterial(woodMat));
        leg.add(new Polygon(new Point(x + w, y, z - w), new Point(x + w, y - height, z - w), new Point(x - w, y - height, z - w), new Point(x - w, y, z - w)).setEmission(woodColor).setMaterial(woodMat));
        leg.add(new Polygon(new Point(x + w, y, z + w), new Point(x + w, y - height, z + w), new Point(x + w, y - height, z - w), new Point(x + w, y, z - w)).setEmission(woodColor).setMaterial(woodMat));
        leg.add(new Polygon(new Point(x - w, y, z - w), new Point(x - w, y - height, z - w), new Point(x - w, y - height, z + w), new Point(x - w, y, z + w)).setEmission(woodColor).setMaterial(woodMat));

        return leg;
    }

    /**
     * Helper function generating right side legs.
     *
     * @return A list containing cylinders matching the right structural support.
     */
    protected List<Intersectable> getRightLegs() {
        return List.of(
                createSquareLeg(new Point(105, -5, 65), 12, 95),
                createSquareLeg(new Point(105, -5, -65), 12, 95)
        );
    }

    /**
     * Helper function generating left side legs.
     *
     * @return A list containing cylinders matching the left structural support.
     */
    protected List<Intersectable> getLeftLegs() {
        return List.of(
                createSquareLeg(new Point(-105, -5, 65), 12, 95),
                createSquareLeg(new Point(-105, -5, -65), 12, 95)
        );
    }

    /**
     * Helper function constructing flat layered papers on the desk surface.
     *
     * @return A list of flat bounding polygons representing papers.
     */
    protected List<Intersectable> getPapers() {
        return List.of(
                new Polygon(new Point(20, 0.1, 30), new Point(20, 0.1, -10), new Point(-20, 0.1, -10), new Point(-20, 0.1, 30)).setEmission(paperColor).setMaterial(paperMat),
                new Polygon(new Point(-5, 0.3, 40), new Point(-35, 0.3, 25), new Point(-25, 0.3, -5), new Point(5, 0.3, 10)).setEmission(paperColor).setMaterial(paperMat)
        );
    }

    /**
     * Helper function creating the localized vase frame geometries.
     *
     * @return A list of intersecting elements forming the flower base holder.
     */
    protected List<Intersectable> getVaseBody() {
        Point vaseBaseCenter = new Point(60, 10, -20);
        return List.of(
                new Sphere(vaseBaseCenter, 12).setEmission(vaseColor).setMaterial(vaseMat),
                new Cylinder(10, new Ray(vaseBaseCenter, new Vector(0, 1, 0)), 6).setEmission(vaseColor).setMaterial(vaseMat)
        );
    }

    /**
     * Helper function collecting all generated plant groupings.
     *
     * @return A list of aggregated flower structures.
     */
    protected List<Geometries> getFlowers() {
        List<Geometries> flowers = new ArrayList<>();
        flowers.add(createSingleFlowerGroup(new Point(50, 38, -10), new Color(200, 30, 80))); // ורוד כהה
        flowers.add(createSingleFlowerGroup(new Point(72, 42, -25), new Color(180, 20, 20))); // אדום עמוק
        flowers.add(createSingleFlowerGroup(new Point(65, 35, -5), new Color(200, 100, 0))); // כתום
        return flowers;
    }

    /**
     * Helper function constructing the classic Utah Teapot mesh item.
     * This function is placeholder, Teapot mesh creation logic is externally sourced.
     *
     * @return A spatial network representing a simplified teapot object.
     */
    protected Geometries getTeapotOnTable() {
        Point teapotCenter = new Point(-80, 10, 40);
        double scale = 0.2;
        return TeapotTest.buildTeapot(teapotCenter, scale);
    }

    /**
     * Generates a fully contained tight individual flower group bounding space wrapper.
     * The logic is updated to create thicker, brighter, vase-aligned stems.
     *
     * @param center     The baseline central cluster anchor point (where the flower is).
     * @param petalColor Base color for surrounding petal components.
     * @return A standalone encapsulated structural geometry tree node.
     */
    protected Geometries createSingleFlowerGroup(Point center, Color petalColor) {
        Geometries flowerGroup = new Geometries();
        Point vaseTop = new Point(60, 12, -20);

        int segments = 15;
        Vector step = vaseTop.subtract(center).scale(1.0 / segments);
        Point current = center;

        for (int i = 0; i <= segments; i++) {
            flowerGroup.add(new Sphere(current, 1.8).setEmission(new Color(30, 150, 40)).setMaterial(plantMat));
            if (i < segments) {
                current = current.add(step);
            }
        }

        flowerGroup.add(new Sphere(center, 2.5).setEmission(new Color(200, 180, 0)).setMaterial(plantMat));
        for (int i = 0; i < 5; i++) {
            double angle = i * (2 * Math.PI / 5);
            double px = center.getX() + 3.5 * Math.cos(angle);
            double py = center.getY() + 1.0;
            double pz = center.getZ() + 3.5 * Math.sin(angle);
            flowerGroup.add(new Sphere(new Point(px, py, pz), 2.2).setEmission(petalColor).setMaterial(plantMat));
        }
        return flowerGroup;
    }

}
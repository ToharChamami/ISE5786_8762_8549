package renderer;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import java.util.List;
import org.junit.jupiter.api.Test;
import scene.Scene;

/**
 * Performance test suite for Mini-Project 2 - Bounding Volume Hierarchy (BVH).
 * Runs 12 different configurations on the same complex scene (dark desk) and measures execution times.
 */
public class BvhPerformanceTest extends ManualBVHTest {

    /**
     * Default constructor
     */
    public BvhPerformanceTest() {
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
     * Recursive helper method to replicate and completely flatten a hierarchical geometry structure.
     * Unpacks all composite node containers down to a single dimensional array list of leaf structures.
     *
     * @param hierarchicalGeos The original hierarchical geometry structure to flatten.
     * @return A completely flattened {@code Geometries} list containing all leaf shapes.
     */
    protected Geometries flattenGeometries(Geometries hierarchicalGeos) {
        Geometries flatResult = new Geometries();
        flattenHelper(hierarchicalGeos, flatResult);
        return flatResult;
    }

    /**
     * Recursive helper that uses reflection to access internal collections and flatten them.
     * Dynamically bypasses collection encapsulation constraints to harvest raw renderable primitives.
     *
     * @param geo        The current geometry element being unpacked.
     * @param flatResult The accumulated list containing the flattened geometries.
     */
    @SuppressWarnings("unchecked")
    private void flattenHelper(Intersectable geo, Geometries flatResult) {
        if (geo instanceof Geometries gCollection) {
            try {
                java.lang.reflect.Field field = Geometries.class.getDeclaredField("geometries");
                field.setAccessible(true);
                List<Intersectable> children = (List<Intersectable>) field.get(gCollection);
                for (Intersectable child : children) {
                    flattenHelper(child, flatResult);
                }
            } catch (Exception e) {
                flatResult.add(geo);
            }
        } else {
            flatResult.add(geo);
        }
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));

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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
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
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        scene.geometries.buildBVH();
        runConfig("12_Auto_CBR_WithMT", scene, true, -1);
    }
}
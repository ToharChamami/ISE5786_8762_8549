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
import special.TeapotTest;

/**
 * Test class generating a dark, moody 3D desk with papers and flowers.
 * Demonstrates the massive performance boost of Manual BVH (Stage 2-B).
 */
public class ManualBVHTest {

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
    private final Material paperMat = new Material().setKD(0.8).setKS(0.1).setShininess(10);

    /**
     * Diffuse gray color representation for papers to avoid overexposure.
     */
    private final Color paperColor = new Color(180, 180, 180);

    /**
     * Material property for the reflective ceramic vase body.
     */
    private final Material vaseMat = new Material().setKD(0.7).setKS(0.6).setShininess(80).setKR(0.1);

    /**
     * Deep blue color representation for the vase.
     */
    private final Color vaseColor = new Color(30, 60, 100);

    /**
     * Material property allocated to organic plant and flower components.
     */
    private final Material plantMat = new Material().setKD(0.6).setKS(0.2).setShininess(20);

    /**
     * ctor for javadock
     */
    public ManualBVHTest() {
    }

    ;

    /**
     * Helper function to create the common dark scene and lighting environment.
     *
     * @param name The name of the scene.
     * @return A configured {@code Scene} instance with baseline dark elements.
     */
    protected Scene createDarkScene(String name) {
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
     * Helper function for common camera builder setups.
     *
     * @param scene The scene being tracked by the camera builder.
     * @return A pre-configured {@code Camera.Builder} instance.
     */
    protected Camera.Builder getCameraBuilder(Scene scene) {
        return Camera.getBuilder()
                .setLocation(new Point(0, 150, 350)) // מבט מלמעלה וקדימה
                .setDirection(new Vector(0, -0.3, -1), new Vector(0, 1, -0.3))
                .setVpSize(200, 200)
                .setSoftShadows(true)
                .setVpDistance(300)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(0) // **מכובה!** כדי למדוד נטו את ה-BVH
                .setDebugPrint(0.1)
                .setSoftShadows(true)
                .setShadowTargetShape(TargetShape.CIRCLE)
                .setShadowSamples(9)
                .setShadowSamplingPattern(SamplingPattern.JITTERED_GRID);
    }

    // ============ Equivalence Partitions Tests ==============
    // EP01: Adding all components explicitly as a flattened sequence without structured nests
    // =====================================================================

    /**
     * Test method analyzing unaccelerated, flat unorganized rendering loops.
     */
    @Test
    public void testDarkDeskFlat() {
        Scene scene = createDarkScene("DarkDeskFlat");

        scene.geometries.add(getTableTop().toArray(new Intersectable[0]));
        scene.geometries.add(getRightLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getLeftLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getPapers().toArray(new Intersectable[0]));
        scene.geometries.add(getVaseBody().toArray(new Intersectable[0]));

        for (Geometries flower : getFlowers()) {
            scene.geometries.add(flower);
        }

        scene.geometries.add(getTeapotOnTable());

        Intersectable.cbrActive = true;
        scene.geometries.createBoundingBox();
        Camera camera = getCameraBuilder(scene).build();

        System.out.println("Starting FLAT Render (Dark Scene)...");
        long start = System.currentTimeMillis();
        camera.renderImage();
        camera.writeToImage("Table_Dark_Flat");
        long end = System.currentTimeMillis();
        System.out.println("FLAT Render Time: " + (end - start) / 1000.0 + " seconds");
    }

    // =====================================================================
    // מבחן מס' 2: המבנה ההיררכי (HIERARCHICAL) - שלב 2-B (מהיר מאוד!)
    // =====================================================================

    /**
     * Test method assessing manual spatial grouping structures and hardcoded layer optimization.
     */
    @Test
    public void testDarkDeskHierarchical() {
        Scene scene = createDarkScene("DarkDeskHierarchical");

        Geometries tableTop = new Geometries(getTableTop().toArray(new Intersectable[0]));
        Geometries rightLegs = new Geometries(getRightLegs().toArray(new Intersectable[0]));
        Geometries leftLegs = new Geometries(getLeftLegs().toArray(new Intersectable[0]));
        Geometries fullDesk = new Geometries(tableTop, rightLegs, leftLegs);
        Geometries papersGroup = new Geometries(getPapers().toArray(new Intersectable[0]));
        Geometries vaseGroup = new Geometries(getVaseBody().toArray(new Intersectable[0]));
        for (Geometries flower : getFlowers()) {
            vaseGroup.add(flower);
        }
        Geometries teapotGroup = getTeapotOnTable();
        scene.geometries.add(fullDesk, papersGroup, vaseGroup, teapotGroup);

        // 8. יצירת עץ הקופסאות התוחמות (BVH Construction)

        Intersectable.cbrActive = true;
        scene.geometries.createBoundingBox();

        Camera camera = getCameraBuilder(scene).build();

        System.out.println("Starting HIERARCHICAL Render (Dark Scene)...");
        long start = System.currentTimeMillis();
        scene.geometries.buildBVH();
        camera.renderImage();
        camera.writeToImage("Table_Dark_Hierarchical");
        long end = System.currentTimeMillis();
        System.out.println("HIERARCHICAL Render Time: " + (end - start) / 1000.0 + " seconds");
    }

    // =====================================================================
    // פונקציות בנאי - יוצרות את החפצים בסצנה
    // =====================================================================

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
     * Helper function generating right side legs.
     *
     * @return A list containing cylinders matching the right structural support.
     */
    protected List<Intersectable> getRightLegs() {
        return List.of(
                new Cylinder(95, new Ray(new Point(105, -5, 65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat),
                new Cylinder(95, new Ray(new Point(105, -5, -65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat)
        );
    }

    /**
     * Helper function generating left side legs.
     *
     * @return A list containing cylinders matching the left structural support.
     */
    protected List<Intersectable> getLeftLegs() {
        return List.of(
                new Cylinder(95, new Ray(new Point(-105, -5, 65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat),
                new Cylinder(95, new Ray(new Point(-105, -5, -65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat)
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
     * Constructs a unified flower cluster including individual stems and surrounding pedal globes.
     *
     * @param center     The baseline central cluster anchor point.
     * @param petalColor The emission color factor allocated to outer spheres.
     * @return An asset collection representing the fully populated flower structure.
     */
    protected Geometries createFlower(Point center, Color petalColor) {
        Geometries flowerGroup = new Geometries();

        Color stemColor = new Color(34, 139, 34); // ירוק כהה
        Material stemMat = new Material().setKD(0.6).setKS(0.1).setShininess(10);

        double startY = 0.0;
        double endY = center.getY();
        int steps = 6;
        for (int j = 0; j <= steps; j++) {
            double currentY = startY + (endY - startY) * j / steps;
            flowerGroup.add(new Sphere(new Point(center.getX(), currentY, center.getZ()), 0.8)
                    .setEmission(stemColor)
                    .setMaterial(stemMat));
        }

        Material plantMat = new Material().setKD(0.5).setKS(0.5).setShininess(20);
        int petalsCount = 8;
        for (int i = 0; i < petalsCount; i++) {
            double angle = 2 * Math.PI * i / petalsCount;
            double px = center.getX() + 3.5 * Math.cos(angle);
            double py = center.getY() + 1.0;
            double pz = center.getZ() + 3.5 * Math.sin(angle);
            flowerGroup.add(new Sphere(new Point(px, py, pz), 2.2).setEmission(petalColor).setMaterial(plantMat));
        }

        flowerGroup.createBoundingBox();
        return flowerGroup;
    }

    /**
     * Builds and downscales the classic Utah Teapot mesh item to fit precisely on the desk surface boundaries.
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
     *
     * @param center     Anchor target coordinate.
     * @param petalColor Base color for surrounding petal components.
     * @return A standalone encapsulated structural geometry tree node.
     */
    protected Geometries createSingleFlowerGroup(Point center, Color petalColor) {
        Geometries flowerGroup = new Geometries();
        Point vaseTop = new Point(60, 12, -20); // יורד לתוך פתח האגרטל, לא נעצר בשפה העליונה

        Vector stemDir = vaseTop.subtract(center).normalize();
        double stemLength = center.distance(vaseTop);
        flowerGroup.add(new Cylinder(stemLength, new Ray(center, stemDir), 1.2).setEmission(new Color(20, 100, 30)).setMaterial(plantMat));
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

    /**
     * Test method checking automatic pipeline sorting and partitioning on unstructured linear shapes.
     */
    @Test
    public void testDarkDeskAutomaticBVH() {
        Scene scene = createDarkScene("DarkDeskAutoBVH");
        scene.geometries.add(getTableTop().toArray(new Intersectable[0]));
        scene.geometries.add(getRightLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getLeftLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getPapers().toArray(new Intersectable[0]));
        scene.geometries.add(getVaseBody().toArray(new Intersectable[0]));
        for (Geometries flower : getFlowers()) scene.geometries.add(flower);
        scene.geometries.add(getTeapotOnTable());

        scene.geometries.buildBVH();

        Intersectable.cbrActive = true;
        scene.geometries.createBoundingBox();
        Camera camera = getCameraBuilder(scene).build();

        System.out.println("Starting AUTOMATIC BVH Render...");
        long start = System.currentTimeMillis();
        camera.renderImage();
        camera.writeToImage("Table_Dark_AutomaticBVH");
        long end = System.currentTimeMillis();
        System.out.println("AUTOMATIC Render Time: " + (end - start) / 1000.0 + " seconds");
    }
}
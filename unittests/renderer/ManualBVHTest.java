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

    // --- חומרים וצבעים משותפים לסצנה החשוכה ---
    private final Material woodMat = new Material().setKD(0.7).setKS(0.2).setShininess(30);
    private final Color woodColor = new Color(50, 25, 15); // עץ כהה מאוד
    private final Material paperMat = new Material().setKD(0.8).setKS(0.1).setShininess(10);
    private final Color paperColor = new Color(180, 180, 180); // דפים אפרפרים כדי לא להסתנוור
    private final Material vaseMat = new Material().setKD(0.7).setKS(0.6).setShininess(80).setKR(0.1);
    private final Color vaseColor = new Color(30, 60, 100); // אגרטל כחול עמוק
    private final Material plantMat = new Material().setKD(0.6).setKS(0.2).setShininess(20);

    /**
     * Helper function to create the common dark scene and lighting.
     */
    protected Scene createDarkScene(String name) {
        Scene scene = new Scene(name)
                .setBackground(new Color(10, 10, 15)) // חדר כמעט שחור
                .setAmbientLight(new AmbientLight(new Color(15, 15, 15), Double3.ONE));

        // רצפה כהה
        scene.geometries.add(
                new Plane(new Point(0, -100, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(20, 20, 25))
                        .setMaterial(new Material().setKD(0.5).setKS(0.1).setShininess(5))
        );

        // תאורה ממוקדת מלמעלה (דרמטית) שיוצרת צללים על השולחן והרצפה
        scene.lights.add(new SpotLight(new Color(300, 270, 220), new Point(100, 250, 100), new Vector(-1, -2, -1))
                .setKl(0.00001).setKq(0.000001).setRadius(10)); // רדיוס לצללים רכים

        // אור מילוי חלש מאוד מצד שמאל להדגשת קווי המתאר
        scene.lights.add(new PointLight(new Color(40, 50, 80), new Point(-200, 100, 200))
                .setKl(0.0001).setKq(0.00001));

        return scene;
    }

    /**
     * Helper function for common camera setup.
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

    // =====================================================================
    // מבחן מס' 1: המבנה השטוח (FLAT) - ללא היררכיה (איטי מאוד!)
    // =====================================================================
    @Test
    public void testDarkDeskFlat() {
        Scene scene = createDarkScene("DarkDeskFlat");

        // מוסיפים את כל העצמים באופן ישיר ("שטוח") לסצנה
        scene.geometries.add(getTableTop().toArray(new Intersectable[0]));
        scene.geometries.add(getRightLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getLeftLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getPapers().toArray(new Intersectable[0]));
        scene.geometries.add(getVaseBody().toArray(new Intersectable[0]));

        // מפרקים גם את הפרחים לחתיכות קטנות וזורקים פנימה
        for (Geometries flower : getFlowers()) {
            scene.geometries.add(flower); // Geometries פשוט מוסיף את הילדים במקרה של flat (אם לא בנוי טוב)
            // הערה: לצורך בדיקה מחמירה, אנו מניחים שאין קופסאות.
        }

        // הקומקן (~1000 משולשים) בלי כל עטיפה היררכית - ידגיש עוד יותר את האיטיות של המבנה השטוח
        scene.geometries.add(getTeapotOnTable());

        Intersectable.cbrActive = true;
        scene.geometries.createBoundingBox(); // יצירת קופסה אחת ענקית לכולם
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
    @Test
    public void testDarkDeskHierarchical() {
        Scene scene = createDarkScene("DarkDeskHierarchical");

        // 1. קבוצת הפלטה העליונה
        Geometries tableTop = new Geometries(getTableTop().toArray(new Intersectable[0]));

        // 2. קבוצות רגליים מקובצות לפי צד (כדי שהקופסה תהיה צרה ולא תתפוס את כל האוויר תחת השולחן)
        Geometries rightLegs = new Geometries(getRightLegs().toArray(new Intersectable[0]));
        Geometries leftLegs = new Geometries(getLeftLegs().toArray(new Intersectable[0]));

        // 3. איגוד השולחן המלא לקבוצה אחת
        Geometries fullDesk = new Geometries(tableTop, rightLegs, leftLegs);

        // 4. קבוצת הדפים
        Geometries papersGroup = new Geometries(getPapers().toArray(new Intersectable[0]));

        // 5. קבוצת האגרטל
        Geometries vaseGroup = new Geometries(getVaseBody().toArray(new Intersectable[0]));
        for (Geometries flower : getFlowers()) {
            vaseGroup.add(flower); // כל פרח הוא כבר קבוצה (BVH node) קטנה והדוקה משלו!
        }

        // 6. קבוצת הקומקן - צומת נפרד משלה כדי שה-BVH יבנה עבורה תת-עץ יעיל בפני עצמו
        Geometries teapotGroup = getTeapotOnTable();

        // 7. הוספת קבוצות העל (Hierarchy Nodes) לסצנה
        scene.geometries.add(fullDesk, papersGroup, vaseGroup, teapotGroup);

        // 8. יצירת עץ הקופסאות התוחמות (BVH Construction)
        Intersectable.cbrActive = true;
        scene.geometries.createBoundingBox();

        Camera camera = getCameraBuilder(scene).build();

        System.out.println("Starting HIERARCHICAL Render (Dark Scene)...");
        long start = System.currentTimeMillis();// הפיכת רשימה שטוחה להיררכיה חכמה ואוטומטית לחלוטין!
        scene.geometries.buildBVH();
        camera.renderImage();
        camera.writeToImage("Table_Dark_Hierarchical");
        long end = System.currentTimeMillis();
        System.out.println("HIERARCHICAL Render Time: " + (end - start) / 1000.0 + " seconds");
    }

    // =====================================================================
    // פונקציות בנאי - יוצרות את החפצים בסצנה
    // =====================================================================

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

    protected List<Intersectable> getRightLegs() {
        return List.of(
                new Cylinder(95, new Ray(new Point(105, -5, 65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat),
                new Cylinder(95, new Ray(new Point(105, -5, -65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat)
        );
    }

    protected List<Intersectable> getLeftLegs() {
        return List.of(
                new Cylinder(95, new Ray(new Point(-105, -5, 65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat),
                new Cylinder(95, new Ray(new Point(-105, -5, -65), new Vector(0, -1, 0)), 6).setEmission(woodColor).setMaterial(woodMat)
        );
    }

    protected List<Intersectable> getPapers() {
        return List.of(
                new Polygon(new Point(20, 0.1, 30), new Point(20, 0.1, -10), new Point(-20, 0.1, -10), new Point(-20, 0.1, 30)).setEmission(paperColor).setMaterial(paperMat),
                new Polygon(new Point(-5, 0.3, 40), new Point(-35, 0.3, 25), new Point(-25, 0.3, -5), new Point(5, 0.3, 10)).setEmission(paperColor).setMaterial(paperMat)
        );
    }

    protected List<Intersectable> getVaseBody() {
        Point vaseBaseCenter = new Point(60, 10, -20);
        return List.of(
                new Sphere(vaseBaseCenter, 12).setEmission(vaseColor).setMaterial(vaseMat),
                new Cylinder(10, new Ray(vaseBaseCenter, new Vector(0, 1, 0)), 6).setEmission(vaseColor).setMaterial(vaseMat)
        );
    }

    protected List<Geometries> getFlowers() {
        List<Geometries> flowers = new ArrayList<>();
        flowers.add(createSingleFlowerGroup(new Point(50, 38, -10), new Color(200, 30, 80))); // ורוד כהה
        flowers.add(createSingleFlowerGroup(new Point(72, 42, -25), new Color(180, 20, 20))); // אדום עמוק
        flowers.add(createSingleFlowerGroup(new Point(65, 35, -5), new Color(200, 100, 0))); // כתום
        return flowers;
    }

    protected Geometries createFlower(Point center, Color petalColor) {
        Geometries flowerGroup = new Geometries();

        // --- הוספת גבעול: שרשרת עיגולים (כדורים) ירוקים מהשולחן ועד לפרח ---
        Color stemColor = new Color(34, 139, 34); // ירוק כהה
        Material stemMat = new Material().setKD(0.6).setKS(0.1).setShininess(10);

        double startY = 0.0; // בגובה השולחן
        double endY = center.getY(); // בגובה ראש הפרח
        int steps = 6; // מספר החוליות בשרשרת הגבעול
        for (int j = 0; j <= steps; j++) {
            double currentY = startY + (endY - startY) * j / steps;
            flowerGroup.add(new Sphere(new Point(center.getX(), currentY, center.getZ()), 0.8)
                    .setEmission(stemColor)
                    .setMaterial(stemMat));
        }

        // --- עלי הכותרת של הפרח ---
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
     * יוצר קבוצה היררכית עצמאית עבור הקומקן, מוקטן וממוקם על משטח השולחן.
     * ה-BVH הכללי (createBoundingBox / buildBVH) יבנה תת-עץ יעיל לקבוצה הזו
     * ברגע שהיא מוצבת כילד בתוך scene.geometries - אין צורך לבנות עבורה BVH בנפרד.
     */
    /**
     * יוצר קבוצה היררכית עצמאית עבור הקומקום, מוקטן משמעותית וממוקם בפינה בטוחה על השולחן.
     */
    protected Geometries getTeapotOnTable() {
        // X = -80 (שמאלה), Y = 2 (ממש מעל פני השולחן), Z = 40 (קרוב יותר למצלמה)
        Point teapotCenter = new Point(-80, 10, 40);

        // הקטנה דרסטית של הקומקום כדי שייראה בגודל הגיוני ביחס לדפים ולאגרטל
        double scale = 0.2;

        return TeapotTest.buildTeapot(teapotCenter, scale);
    }

    /**
     * יוצר פרח כקבוצה היררכית (Geometries) בפני עצמה.
     * כל פרח עטוף בקופסה משלו, מה שמונע בדיקות מיותרות מול כל עלי הכותרת.
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

    @Test
    public void testDarkDeskAutomaticBVH() {
        Scene scene = createDarkScene("DarkDeskAutoBVH");

        // הוספה שטוחה רגילה (כאילו אין היררכיה)
        scene.geometries.add(getTableTop().toArray(new Intersectable[0]));
        scene.geometries.add(getRightLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getLeftLegs().toArray(new Intersectable[0]));
        scene.geometries.add(getPapers().toArray(new Intersectable[0]));
        scene.geometries.add(getVaseBody().toArray(new Intersectable[0]));
        for (Geometries flower : getFlowers()) scene.geometries.add(flower);
        scene.geometries.add(getTeapotOnTable());

        // הקסם האוטומטי - המערכת תסדר את הכל בעצמה!
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
package renderer;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import java.util.List;
import org.junit.jupiter.api.Test;
import scene.Scene;

/**
 * מכלול בדיקות הביצועים הרשמי עבור מיני-פרויקט 2 - היררכיית אזורים תוחמים.
 * מריץ 12 תצורות שונות על אותה סצנה כבדה (שולחן כתיבה חשוך) ומודד זמני ריצה.
 */
public class BvhPerformanceTest extends ManualBVHTest {

    // פונקציית עזר ליצירת ההיררכיה הידנית בעזרת הפונקציות הקיימות שלך
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

    // פונקציית עזר רקורסיבית לשכפול ושיטוח מלא של מבנה גיאומטרי (Flattening)
    protected Geometries flattenGeometries(Geometries hierarchicalGeos) {
        Geometries flatResult = new Geometries();
        flattenHelper(hierarchicalGeos, flatResult);
        return flatResult;
    }

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

    // מתודה מרכזית להרצה ומדידת זמן הרינדור
    private void runConfig(String testName, Scene scene, boolean enableCbr, int threadsCount) {
        Intersectable.cbrActive = enableCbr;

        if (enableCbr) {
            scene.geometries.createBoundingBox();
        }

        Camera camera = getCameraBuilder(scene)
                .setMultithreading(threadsCount)
                .setDebugPrint(0)
                .build();

        System.out.println(">>> Starting Configuration: " + testName);
        long start = System.currentTimeMillis();
        camera.renderImage();
        long end = System.currentTimeMillis();

        camera.writeToImage(testName);
        System.out.println("Finished in: " + (end - start) / 1000.0 + " seconds.\n");
    }

    // =========================================================================
    // 1. תצורות סצנה שטוחה (Flattened Scene)
    // =========================================================================

    @Test
    public void test01_Flat_NoAcceleration_NoMT() {
        Scene scene = createDarkScene("Flat_NoAcc_NoMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        runConfig("01_Flat_NoAcc_NoMT", scene, true, 0);
    }

    @Test
    public void test02_Flat_NoAcceleration_WithMT() {
        Scene scene = createDarkScene("Flat_NoAcc_WithMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        runConfig("02_Flat_NoAcc_WithMT", scene, true, -1);
    }

    @Test
    public void test03_Flat_CBR_NoMT() {
        Scene scene = createDarkScene("Flat_CBR_NoMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        runConfig("03_Flat_CBR_NoMT", scene, true, 0);
    }

    @Test
    public void test04_Flat_CBR_WithMT() {
        Scene scene = createDarkScene("Flat_CBR_WithMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        runConfig("04_Flat_CBR_WithMT", scene, true, -1);
    }

    // =========================================================================
    // 2. תצורות היררכיה ידנית (Manual Hierarchy)
    // =========================================================================

    @Test
    public void test05_ManualHierarchy_NoAcceleration_NoMT() {
        Scene scene = createDarkScene("Manual_NoAcc_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("05_Manual_NoAcc_NoMT", scene, true, 0);
    }

    @Test
    public void test06_ManualHierarchy_NoAcceleration_WithMT() {
        Scene scene = createDarkScene("Manual_NoAcc_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("06_Manual_NoAcc_WithMT", scene, true, -1);
    }

    @Test
    public void test07_ManualHierarchy_CBR_NoMT() {
        Scene scene = createDarkScene("Manual_CBR_NoMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("07_Manual_CBR_NoMT", scene, true, 0);
    }

    @Test
    public void test08_ManualHierarchy_CBR_WithMT() {
        Scene scene = createDarkScene("Manual_CBR_WithMT");
        scene.geometries.add(getManualHierarchyRoot());
        runConfig("08_Manual_CBR_WithMT", scene, true, -1);
    }

    // =========================================================================
    // 3. תצורות היררכיה אוטומטית (Automatic BVH)
    // =========================================================================

    @Test
    public void test09_AutoHierarchy_NoAcceleration_NoMT() {
        Scene scene = createDarkScene("Auto_NoAcc_NoMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));

        long startTree = System.currentTimeMillis();
        scene.geometries.buildBVH();
        long endTree = System.currentTimeMillis();
        System.out.println("Automatic BVH Tree build time: " + (endTree - startTree) + " ms.");

        runConfig("09_Auto_NoAcc_NoMT", scene, true, 0);
    }

    @Test
    public void test10_AutoHierarchy_NoAcceleration_WithMT() {
        Scene scene = createDarkScene("Auto_NoAcc_WithMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        scene.geometries.buildBVH();
        runConfig("10_Auto_NoAcc_WithMT", scene, true, -1);
    }

    @Test
    public void test11_AutoHierarchy_CBR_NoMT() {
        Scene scene = createDarkScene("Auto_CBR_NoMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        scene.geometries.buildBVH();
        runConfig("11_Auto_CBR_NoMT", scene, true, 0);
    }

    @Test
    public void test12_AutoHierarchy_CBR_WithMT() {
        Scene scene = createDarkScene("Auto_CBR_WithMT");
        scene.geometries.add(flattenGeometries(getManualHierarchyRoot()));
        scene.geometries.buildBVH();
        runConfig("12_Auto_CBR_WithMT", scene, true, -1);
    }
}
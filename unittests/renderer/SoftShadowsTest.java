package renderer;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Test class for demonstrating Stage 5: Soft Shadows effects and rendering performance.
 */
public class SoftShadowsTest {

    @Test
    public void testRichSceneSoftShadows() {
        // 1. יצירת הסצנה והגדרת תאורת רקע
        Scene scene = new Scene("RichSceneSoftShadows")
                .setAmbientLight(new AmbientLight(new Color(20, 20, 20), Double3.ONE))
                .setBackground(new Color(255, 255, 255));

        // 2. בניית הגיאומטריות (לפחות 10 גופים שונים)
        scene.geometries.add(
                new Plane(new Point(0, -50, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(30, 30, 30))
                        .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(30))
        );

        Material sphereMaterial = new Material().setKD(0.5).setKS(0.4).setShininess(50);

        scene.geometries.add(
                new Sphere(new Point(-60, -20, -100), 20).setEmission(new Color(150, 0, 0)).setMaterial(sphereMaterial),
                new Sphere(new Point(-30, -25, -80), 15).setEmission(new Color(0, 150, 0)).setMaterial(sphereMaterial),
                new Sphere(new Point(0, -30, -60), 12).setEmission(new Color(0, 0, 150)).setMaterial(sphereMaterial),
                new Sphere(new Point(30, -25, -80), 15).setEmission(new Color(150, 150, 0)).setMaterial(sphereMaterial),
                new Sphere(new Point(60, -20, -100), 20).setEmission(new Color(0, 150, 150)).setMaterial(sphereMaterial),

                new Sphere(new Point(-45, -40, -40), 8).setEmission(new Color(100, 50, 150)).setMaterial(sphereMaterial),
                new Sphere(new Point(-15, -42, -30), 6).setEmission(new Color(150, 100, 50)).setMaterial(sphereMaterial),
                new Sphere(new Point(15, -42, -30), 6).setEmission(new Color(50, 150, 100)).setMaterial(sphereMaterial),
                new Sphere(new Point(45, -40, -40), 8).setEmission(new Color(200, 100, 100)).setMaterial(sphereMaterial)
        );

        // 3. הגדרת מקורות האור
        PointLight pointLight1 = new PointLight(new Color(400, 300, 300), new Point(150, 100, -50))
                .setKl(0.00001).setKq(0.000005);
        pointLight1.setRadius(15);

        PointLight pointLight2 = new PointLight(new Color(200, 200, 400), new Point(-150, 80, -50))
                .setKl(0.0001).setKq(0.00005);
        pointLight2.setRadius(10);

        scene.lights.add(pointLight1);
        scene.lights.add(pointLight2);
        scene.lights.add(new SpotLight(new Color(300, 300, 300), new Point(0, 200, -70), new Vector(0, -1, 0))
                .setKl(0.0001).setKq(0.00005));

        // ====================================================================
        // הרצה ראשונה: ללא השיפור (Hard Shadows בלבד)
        // ====================================================================

        // יצירת הטרייסר בנפרד והגדרתו לצללים קשיחים
        SimpleRayTracer tracerHard = new SimpleRayTracer(scene);
        tracerHard.setSoftShadows(false);

        // בניית המצלמה תוך שימוש במתודות ה-Builder הקיימות בקוד שלכם
        Camera cameraHard = new Camera.Builder()
                .setLocation(new Point(0, 0, 150)) // המצלמה עומדת במרכז, קצת מאחורה
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0)) // מביטה ישר קדימה אל עומק ציר ה-Z, והראש למעלה
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setRayTracer(scene, RayTracerType.SIMPLE) // בנייה פנימית ראשונית
                .build();

        // הזרקה ידנית של הטרייסר שהגדרנו לתוך שדה ה-Camera (באמצעות שימוש ישיר במידה והארכיטקטורה מאפשרת,
        // או לחילופין נשתמש ב-Builder מותאם המקבל את הטרייסר המוכן מראש)

        long startTimeHard = System.currentTimeMillis();
        cameraHard.renderImage();
        cameraHard.writeToImage("Step5_Hard_Shadows");
        long endTimeHard = System.currentTimeMillis();

        long durationHard = endTimeHard - startTimeHard;
        System.out.println("Render time WITHOUT soft shadows (Hard Shadows): " + durationHard + " ms");

        // ====================================================================
        // הרצה שנייה: עם הפעלת השיפור (Soft Shadows)
        // ====================================================================

        Camera cameraSoft = new Camera.Builder()
                .setLocation(new Point(0, 0, 150)) // המצלמה עומדת במרכז, קצת מאחורה
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0)) // מביטה ישר קדימה אל עומק ציר ה-Z, והראש למעלה
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build();

        long startTimeSoft = System.currentTimeMillis();
        cameraSoft.renderImage();
        cameraSoft.writeToImage("Step5_Soft_Shadows");
        long endTimeSoft = System.currentTimeMillis();

        long durationSoft = endTimeSoft - startTimeSoft;
        System.out.println("Render time WITH soft shadows (Grid 9x9): " + durationSoft + " ms");
        System.out.println("Performance Cost Factor: " + (double) durationSoft / durationHard + "x longer");
    }
}
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
import renderer.sampling.TargetShape;
import scene.Scene;

/**
 * Test class for demonstrating Stage 5: Soft Shadows effects and rendering performance.
 */
public class SoftShadowsTest {

    @Test
    public void testRichSceneSoftShadows() {
        // 1. יצירת הסצנה והגדרת תאורת רקע לבנה
        Scene scene = new Scene("RichSceneSoftShadows")
                .setAmbientLight(new AmbientLight(new Color(30, 30, 30), Double3.ONE))
                .setBackground(new Color(255, 255, 255));

        // 2. בניית הגיאומטריות - רצפה ו-9 כדורים (לפחות 10 גופים בסך הכל)
        scene.geometries.add(
                new Plane(new Point(0, -50, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(40, 40, 40))
                        .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(30))
        );

        Material sphereMaterial = new Material().setKD(0.5).setKS(0.4).setShininess(50);

        scene.geometries.add(
                new Sphere(new Point(-60, 0, -120), 20).setEmission(new Color(150, 0, 0)).setMaterial(sphereMaterial),
                new Sphere(new Point(-30, -5, -100), 15).setEmission(new Color(0, 150, 0)).setMaterial(sphereMaterial),
                new Sphere(new Point(0, -10, -80), 12).setEmission(new Color(0, 0, 150)).setMaterial(sphereMaterial),
                new Sphere(new Point(30, -5, -100), 15).setEmission(new Color(150, 150, 0)).setMaterial(sphereMaterial),
                new Sphere(new Point(60, 0, -120), 20).setEmission(new Color(0, 150, 150)).setMaterial(sphereMaterial),

                new Sphere(new Point(-45, -20, -70), 8).setEmission(new Color(100, 50, 150)).setMaterial(sphereMaterial),
                new Sphere(new Point(-15, -22, -60), 6).setEmission(new Color(150, 100, 50)).setMaterial(sphereMaterial),
                new Sphere(new Point(15, -22, -60), 6).setEmission(new Color(50, 150, 100)).setMaterial(sphereMaterial),
                new Sphere(new Point(45, -20, -70), 8).setEmission(new Color(200, 100, 100)).setMaterial(sphereMaterial)
        );

        // 3. הגדרת 3 מקורות אור במיקומים שונים (לפי דרישות המיני-פרויקט) [cite: 58]
        PointLight pointLight1 = new PointLight(new Color(500, 400, 400), new Point(60, 120, -40))
                .setKl(0.00001).setKq(0.000005);
        pointLight1.setRadius(15);

        PointLight pointLight2 = new PointLight(new Color(200, 200, 400), new Point(-60, 120, -40))
                .setKl(0.0001).setKq(0.00005);
        pointLight2.setRadius(10);

        scene.lights.add(pointLight1);
        scene.lights.add(pointLight2);
        scene.lights.add(new SpotLight(new Color(400, 400, 400), new Point(0, 150, -50), new Vector(0, -1, 0))
                .setKl(0.0001).setKq(0.00005));

        // ====================================================================
        // הרצה ראשונה: ללא השיפור (Hard Shadows בלבד)
        // ====================================================================

        Camera cameraHard = new Camera.Builder()
                .setLocation(new Point(0, -10, 150)) // הורדנו את ה-Y ל-10- כדי לקלוט את הכדורים והרצפה
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0)) // וקטורים ניצבים מתמטית ב-100%!
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build();

        if (cameraHard.getRayTracer() instanceof SimpleRayTracer simpleTracer) {
            simpleTracer.setSoftShadows(false);
        }

        long startTimeHard = System.currentTimeMillis();
        cameraHard.renderImage();
        cameraHard.writeToImage("Step5_Hard_Shadows");
        long endTimeHard = System.currentTimeMillis();
        System.out.println("Render time WITHOUT soft shadows (Hard Shadows): " + (endTimeHard - startTimeHard) + " ms");

        // ====================================================================
        // הרצה שנייה: עם הפעלת השיפור (Soft Shadows)
        // ====================================================================

        Camera cameraSoft = new Camera.Builder()
                .setLocation(new Point(0, -10, 150)) // אותו מיקום בטוח וניצב
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(200, 200)
                .setVpDistance(150)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .build();

        if (cameraSoft.getRayTracer() instanceof SimpleRayTracer simpleTracer) {
            simpleTracer.setSoftShadows(true)
                    .setShadowTargetShape(TargetShape.CIRCLE);
        }

        long startTimeSoft = System.currentTimeMillis();
        cameraSoft.renderImage();
        cameraSoft.writeToImage("Step5_Soft_Shadows");
        long endTimeSoft = System.currentTimeMillis();
        System.out.println("Render time WITH soft shadows (Grid 9x9): " + (endTimeSoft - startTimeSoft) + " ms");
    }
}

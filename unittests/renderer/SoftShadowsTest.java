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
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;
import scene.Scene;

/**
 * Test class for demonstrating Stage 5: Soft Shadows effects.
 * Scene is designed to clearly show the difference between hard and soft shadows.
 */
public class SoftShadowsTest {

    @Test
    public void testRichSceneSoftShadows() {

        Scene scene = new Scene("RichSceneSoftShadows")
                .setAmbientLight(new AmbientLight(new Color(25, 25, 25), Double3.ONE))
                .setBackground(new Color(10, 10, 30));

        Material floorMat = new Material().setKD(0.6).setKS(0.2).setShininess(20);
        Material sphereMat = new Material().setKD(0.5).setKS(0.4).setShininess(60);

        scene.geometries.add(
                new Plane(new Point(0, -50, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(30, 30, 30))
                        .setMaterial(floorMat)
        );

        // כדור מרכזי גדול - מטיל צל ברור על הרצפה
        scene.geometries.add(
                new Sphere(new Point(0, -10, -80), 35)
                        .setEmission(new Color(180, 30, 30))
                        .setMaterial(sphereMat)
        );
        // כדור ירוק שמאל
        scene.geometries.add(
                new Sphere(new Point(-70, -25, -80), 22)
                        .setEmission(new Color(30, 160, 30))
                        .setMaterial(sphereMat)
        );
        // כדור כחול ימין
        scene.geometries.add(
                new Sphere(new Point(70, -25, -80), 22)
                        .setEmission(new Color(30, 30, 180))
                        .setMaterial(sphereMat)
        );
        // כדור צהוב קטן - שמאל קדימה
        scene.geometries.add(
                new Sphere(new Point(-40, -38, -30), 12)
                        .setEmission(new Color(180, 180, 0))
                        .setMaterial(sphereMat)
        );
        // כדור ציאן קטן - ימין קדימה
        scene.geometries.add(
                new Sphere(new Point(40, -38, -30), 12)
                        .setEmission(new Color(0, 180, 180))
                        .setMaterial(sphereMat)
        );
        // כדור סגול - שמאל רחוק
        scene.geometries.add(
                new Sphere(new Point(-90, -40, -40), 8)
                        .setEmission(new Color(140, 0, 140))
                        .setMaterial(sphereMat)
        );
        // כדור כתום - ימין רחוק
        scene.geometries.add(
                new Sphere(new Point(90, -40, -40), 8)
                        .setEmission(new Color(200, 80, 0))
                        .setMaterial(sphereMat)
        );
        // כדור אחורי רחוק
        scene.geometries.add(
                new Sphere(new Point(0, -38, -160), 12)
                        .setEmission(new Color(60, 120, 60))
                        .setMaterial(sphereMat)
        );
        // כדור מרחף באוויר
        scene.geometries.add(
                new Sphere(new Point(0, 40, -120), 18)
                        .setEmission(new Color(100, 100, 100))
                        .setMaterial(sphereMat)
        );

        PointLight mainLight = new PointLight(
                new Color(600, 500, 400),
                new Point(80, 120, -40))
                .setKl(0.0001).setKq(0.000002);
        mainLight.setRadius(50);
        scene.lights.add(mainLight);

        PointLight sideLight = new PointLight(
                new Color(300, 300, 500),
                new Point(-80, 100, -40))
                .setKl(0.0002).setKq(0.000005);
        sideLight.setRadius(40);
        scene.lights.add(sideLight);

        SpotLight topSpot = new SpotLight(
                new Color(400, 400, 400),
                new Point(0, 200, -80),
                new Vector(0, -1, 0))
                .setKl(0.0001).setKq(0.000003);
        topSpot.setRadius(45);
        scene.lights.add(topSpot);

        Camera cameraHard = Camera.getBuilder()
                .setLocation(new Point(0, 30, 800))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(200, 200)
                .setVpDistance(800)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-1)
                .setDebugPrint(0.1)
                .build();

        if (cameraHard.getRayTracer() instanceof SimpleRayTracer simpleTracer) {
            simpleTracer.setSoftShadows(false);
        }

        System.out.println("Starting Hard Shadows render...");
        long startHard = System.currentTimeMillis();
        cameraHard.renderImage();
        cameraHard.writeToImage("Step5_Hard_Shadows");
        long endHard = System.currentTimeMillis();
        System.out.println("Hard Shadows render time: " + (endHard - startHard) + " ms");

        //  Soft Shadows
        Camera cameraSoft = Camera.getBuilder()
                .setLocation(new Point(0, 30, 800))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpSize(200, 200)
                .setVpDistance(800)
                .setResolution(800, 800)
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setMultithreading(-1)
                .setDebugPrint(0.1)
                .build();

        if (cameraSoft.getRayTracer() instanceof SimpleRayTracer simpleTracer) {
            simpleTracer.setSoftShadows(true)
                    .setShadowTargetShape(TargetShape.CIRCLE)
                    .setShadowSamples(9) // 9x9 = 81 samples
                    .setShadowSamplingPattern(SamplingPattern.JITTERED_GRID); // הפעלת בונוס ה-Jittered!
        }

        System.out.println("Starting Soft Shadows render (9x9 grid)...");
        long startSoft = System.currentTimeMillis();
        cameraSoft.renderImage();
        cameraSoft.writeToImage("Step5_Soft_Shadows");
        long endSoft = System.currentTimeMillis();
        System.out.println("Soft Shadows render time: " + (endSoft - startSoft) + " ms");

        System.out.println("\n=== Performance Comparison ===");
        System.out.println("Hard Shadows: " + (endHard - startHard) + " ms");
        System.out.println("Soft Shadows: " + (endSoft - startSoft) + " ms");
        System.out.printf("Slowdown factor: %.1fx%n",
                (double) (endSoft - startSoft) / (endHard - startHard));
    }
}
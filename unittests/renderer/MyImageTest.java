package renderer;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Test class for generating a custom scene image.
 */
public class MyImageTest {
    /**
     * Default constructor for the test class
     */
    public MyImageTest() {
    }

    /**
     * Test method to render and save the custom scene.
     */
    @Test
    public void myCustomScene() {
        Scene scene = new Scene("My Custom Scene");
        scene.setBackground(new primitives.Color(40, 40, 40));
        scene.setAmbientLight(new AmbientLight(new primitives.Color(java.awt.Color.WHITE), new Double3(0.15)));

        Camera.Builder cameraBuilder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
                .setVpDistance(1000)
                .setVpSize(200, 200)
                .setResolution(600, 600);

        scene.geometries.add(
                new Triangle(new Point(-500, -50, 500), new Point(500, -50, 500), new Point(0, -50, -500))
                        .setEmission(new primitives.Color(20, 20, 20))
                        .setMaterial(new Material().setKR(0.8)), // Almost perfect mirror

                new Sphere(new Point(-20, 0, 50), 30D)
                        .setEmission(new primitives.Color(java.awt.Color.BLUE))
                        .setMaterial(new Material().setKD(0.2).setKS(0.2).setShininess(30).setKT(0.6)),

                new Sphere(new Point(40, -30, 0), 20D)
                        .setEmission(new primitives.Color(java.awt.Color.RED))
                        .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(60))
        );

        scene.lights.add(
                new SpotLight(new primitives.Color(700, 400, 400), new Point(50, 150, 150), new Vector(-1, -1, -2))
                        .setKl(4E-5).setKq(2E-7)
        );

        cameraBuilder.build()
                .renderImage()
                .writeToImage("MyCustomRealisticImage");
    }
}

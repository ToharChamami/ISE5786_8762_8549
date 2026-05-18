package renderer;

import geometries.api.Geometry;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

import static java.awt.Color.BLUE;

/**
 * Test rendering scenes with multiple light sources.
 */
class MultipleLightsTests {
    /**
     * Default constructor for the test class
     */
    public MultipleLightsTests() {
    }

    /**
     * Constant for tests resolution
     */
    private static final int RESOLUTION = 500;

    /**
     * Produce a picture of a sphere lighted by multiple lights
     * (Directional, Point, and Spot).
     */
    @Test
    void sphereMultipleLights() {
        Scene scene = new Scene("Test scene multiple lights sphere");
        Camera.Builder camera = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION);

        Geometry sphere = new Sphere(new Point(0, 0, -50), 50D)
                .setEmission(new Color(BLUE).reduce(2))
                .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(301));

        scene.geometries.add(sphere);

        // Light sources
        // 1. Directional Light: Dim red light coming from the top right
        scene.lights.add(new DirectionalLight(new Color(150, 0, 0), new Vector(1, -1, -1)));

        // 2. Point Light: Green light placed on the bottom left
        scene.lights.add(new PointLight(new Color(0, 250, 0), new Point(-50, -50, 25))
                .setKl(0.001).setKq(0.0002));

        // 3. Spot Light: Bright white-yellowish spotlight focused from the front right
        scene.lights.add(new SpotLight(new Color(400, 300, 100), new Point(40, 40, 50), new Vector(-1, -1, -2))
                .setKl(0.001).setKq(0.0001));

        camera.build()
                .renderImage()
                .writeToImage("multipleLightsSphere");
    }

    /**
     * Produce a picture of two triangles lighted by multiple lights
     * (Directional, Point, and Spot).
     */
    @Test
    void trianglesMultipleLights() {
        // Scene and Camera setup with specific ambient light
        Scene scene = new Scene("Test scene multiple lights triangles")
                .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        Camera.Builder camera = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(200, 200)
                .setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION);

        Point[] vertices = {
                new Point(-110, -110, -150), // shared left-bottom
                new Point(95, 100, -150),    // shared right-top
                new Point(110, -110, -150),  // right-bottom
                new Point(-75, 78, 100)      // left-top
        };

        Material material = new Material()
                .setKD(new Double3(0.2, 0.6, 0.4))
                .setKS(new Double3(0.2, 0.4, 0.3))
                .setShininess(301);

        Geometry triangle1 = new Triangle(vertices[0], vertices[1], vertices[2]).setMaterial(material);
        Geometry triangle2 = new Triangle(vertices[0], vertices[1], vertices[3]).setMaterial(material);

        scene.geometries.add(triangle1, triangle2);

        // Light sources
        // 1. Directional Light: Dim blueish overall lighting
        scene.lights.add(new DirectionalLight(new Color(0, 0, 150), new Vector(0, -1, 0)));

        // 2. Point Light: Bright red light located near the center bottom
        scene.lights.add(new PointLight(new Color(400, 0, 0), new Point(10, -80, -100))
                .setKl(0.002).setKq(0.0002));

        // 3. Spot Light: Strong green spotlight hitting the top triangle
        scene.lights.add(new SpotLight(new Color(0, 400, 0), new Point(-40, 40, -50), new Vector(2, -1, -2))
                .setKl(0.001).setKq(0.0001));

        camera.build()
                .renderImage()
                .writeToImage("multipleLightsTriangles");
    }
}
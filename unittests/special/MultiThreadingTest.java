package special;

import org.junit.jupiter.api.Test;
import renderer.Camera;

/**
 * Test class to measure multi-threading performance improvements.
 * Utilizes the heavy Teapot scene from TeapotTest.
 */
public class MultiThreadingTest extends TeapotTest {

    @Test
    public void test01_NoThreads() {
        System.out.println("--- Starting Base Render (0 Threads) ---");
        // הקריאה החוזרת ל-setMultithreading תדרוס את הערך המקורי שב-prepareTeapot
        Camera camera = prepareTeapot()
                .setMultithreading(0)
                .setDebugPrint(1.0)
                .build();

        long start = System.currentTimeMillis();
        camera.renderImage();
        camera.writeToImage("MT_01_NoThreads");
        long end = System.currentTimeMillis();

        System.out.println("\nBase Render Time: " + (end - start) / 1000.0 + " seconds\n");
    }

    @Test
    public void test02_ParallelStreams() {
        System.out.println("--- Starting Parallel Streams Render (-1) ---");
        Camera camera = prepareTeapot()
                .setMultithreading(-1)
                .setDebugPrint(1.0)
                .build();

        long start = System.currentTimeMillis();
        camera.renderImage();
        camera.writeToImage("MT_02_Streams");
        long end = System.currentTimeMillis();

        System.out.println("\nStreams Render Time: " + (end - start) / 1000.0 + " seconds\n");
    }

    @Test
    public void test03_RawThreads() {
        System.out.println("--- Starting Raw Threads Render (-2) ---");
        Camera camera = prepareTeapot()
                .setMultithreading(-2) // ניתן גם לנסות מספר חיובי כמו 3 או 4
                .setDebugPrint(1.0)
                .build();

        long start = System.currentTimeMillis();
        camera.renderImage();
        camera.writeToImage("MT_03_RawThreads");
        long end = System.currentTimeMillis();

        System.out.println("\nRaw Threads Render Time: " + (end - start) / 1000.0 + " seconds\n");
    }
}
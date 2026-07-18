package renderer;

import org.junit.jupiter.api.Test;

/**
 * Test class to measure multi-threading performance improvements.
 * Utilizes the heavy Teapot scene from TeapotTest.
 */
public class MultiThreadingTest extends TeapotTest {

    /**
     * fefult ctor
     */
    public MultiThreadingTest() {
    }

    ;

    /**
     * EP01: Single-threaded sequential ray tracing comparison test
     * Test method checking baseline sequential execution performance without multithreading active.
     */
    @Test
    public void test01_NoThreads() {
        System.out.println("--- Starting Base Render (0 Threads) ---");
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

    /**
     * EP01: Automated pool parallel pipeline scaling evaluation
     * Test method validating concurrent image rendering acceleration using a parallel streams framework.
     */
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

    /**
     * EP01: Distributed raw concurrent runtime metric gathering
     * Test method benchmarking concurrent pixel processing utilizing manual raw thread allocations.
     */
    @Test
    public void test03_RawThreads() {
        System.out.println("--- Starting Raw Threads Render (-2) ---");
        Camera camera = prepareTeapot()
                .setMultithreading(-2)
                .setDebugPrint(1.0)
                .build();

        long start = System.currentTimeMillis();
        camera.renderImage();
        camera.writeToImage("MT_03_RawThreads");
        long end = System.currentTimeMillis();

        System.out.println("\nRaw Threads Render Time: " + (end - start) / 1000.0 + " seconds\n");
    }
}
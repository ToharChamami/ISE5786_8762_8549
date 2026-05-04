package renderer;

import org.junit.jupiter.api.Test;
import primitives.Color;

/**
 * Unit tests for the ImageWriter class.
 */
public class ImageWriterTests {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final int STEP = 50;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 255);
    private static final Color GRID_COLOR = new Color(255, 0, 0);

    /**
     * Test method for creating a grid image to verify the ImageWriter.
     */
    @Test
    public void testImageWriter() {
        ImageWriter imageWriter = new ImageWriter(WIDTH, HEIGHT);
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                Color color = (i % STEP == 0 || j % STEP == 0) ? GRID_COLOR : BACKGROUND_COLOR;

                imageWriter.writePixel(i, j, color);
            }
        }

        imageWriter.writeToImage("test_grid");
    }
}
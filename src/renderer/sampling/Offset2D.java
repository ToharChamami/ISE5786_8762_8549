package renderer.sampling;

/**
 * Value object representing a 2D offset in a normalized coordinate system ranging from -0.5 to 0.5.
 * This class is immutable to ensure absolute thread safety during multi-threaded rendering.
 *
 * @author YourName & Partner
 */
public final class Offset2D {

    /**
     * the horizontal offset displacement
     */
    private final double x;

    /**
     * the vertical offset displacement
     */
    private final double y;

    /**
     * Constructs a new Offset2D with the given coordinates.
     *
     * @param x the horizontal offset displacement
     * @param y the vertical offset displacement
     */
    public Offset2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the horizontal X coordinate of the offset.
     *
     * @return the x coordinate value
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the vertical Y coordinate of the offset.
     *
     * @return the y coordinate value
     */
    public double getY() {
        return y;
    }
}
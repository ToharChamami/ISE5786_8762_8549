package renderer.sampling;

/**
 * Value object representing a 2D offset in a normalized coordinate system ranging from -0.5 to 0.5.
 * This class is immutable to ensure absolute thread safety during multi-threaded rendering.
 *
 * @param x the horizontal offset displacement
 * @param y the vertical offset displacement
 */
public record Offset2D(double x, double y) {
}
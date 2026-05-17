package lighting;

import primitives.Color;

/**
 * Class representing ambient light in the scene.
 * This class is immutable.
 */
public final class AmbientLight extends Light {

    /**
     * Constant representing no ambient light (Black color).
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Constructor to initialize ambient light with intensity.
     *
     * @param intensity the background lighting intensity
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }

}
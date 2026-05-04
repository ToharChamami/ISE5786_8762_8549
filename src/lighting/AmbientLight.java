package lighting;

import primitives.Color;

/**
 * Class representing ambient light in the scene.
 * This class is immutable.
 */
public final class AmbientLight {
    /**
     * The intensity of the ambient light
     */
    private final Color intensity;

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
        this.intensity = intensity;
    }

    /**
     * Getter for intensity.
     *
     * @return the intensity color
     */
    public Color getIntensity() {
        return intensity;
    }
}
package lighting;

import primitives.Color;

/**
 * Abstract base class for all light types in the scene.
 * Holds the basic property of light intensity.
 */
abstract class Light {
    /**
     * The intensity (color) of the light source
     */
    protected final Color _intensity;

    /**
     * Constructor to initialize the light intensity.
     *
     * @param intensity the color intensity of the light
     */
    protected Light(Color intensity) {
        this._intensity = intensity;
    }

    /**
     * Getter for the light intensity.
     *
     * @return the intensity color of the light
     */
    public Color getIntensity() {
        return _intensity;
    }
}
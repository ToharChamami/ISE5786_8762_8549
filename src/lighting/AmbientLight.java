package lighting;

import primitives.Color;
import primitives.Double3;

/**
 * Ambient light for the scene, inheriting from the base Light class.
 * Provides uniform background illumination to all objects in the scene.
 */
public class AmbientLight extends Light {

    /**
     * Default ambient light color (Black with intensity 0)
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK, Double3.ZERO);

    /**
     * Constructs an ambient light source with an initial color intensity
     * and a Double3 attenuation factor.
     * * @param ia the original illumination color intensity
     *
     * @param ka the attenuation factor as a Double3 vector
     */
    public AmbientLight(Color ia, Double3 ka) {
        super(ia.scale(ka));
    }

    /**
     * Constructs an ambient light source with an initial color intensity
     * and a scalar double attenuation factor.
     * * @param ia the original illumination color intensity
     *
     * @param ka the attenuation factor as a double scalar
     */
    public AmbientLight(Color ia, double ka) {
        super(ia.scale(ka));
    }

    /**
     * Constructs an ambient light source directly with a given intensity.
     * * @param intensity the background lighting intensity color
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }
}
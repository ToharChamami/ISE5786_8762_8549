package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Directional light source (like the sun). Has a direction but no position.
 */
public class DirectionalLight extends Light implements LightSource {
    /**
     * The normalized direction vector of the light
     */
    private final Vector direction;

    /**
     * Constructor to initialize directional light.
     *
     * @param intensity the intensity of the light
     * @param direction the direction of the light
     *
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        this.direction = direction.normalize();
    }

    /**
     * {@inheritDoc}
     * Returns the constant light intensity which is independent of the point's position.
     */
    @Override
    public Color getIntensity(Point p) {
        return getIntensity();
    }

    /**
     * {@inheritDoc}
     * Returns the fixed normalized direction vector of the light source.
     */
    @Override
    public Vector getL(Point p) {
        return direction;
    }
}
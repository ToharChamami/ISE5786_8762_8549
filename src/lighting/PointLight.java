package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Representation of a point light source in the scene (such as a light bulb).
 */
public class PointLight extends Light implements LightSource {
    private final Point position;
    private double kC = 1d;
    private double kL = 0d;
    private double kQ = 0d;

    /**
     * Constructs a point light source with a given intensity and position.
     * * @param intensity the color intensity of the light source
     *
     * @param position the position point of the light source in the scene
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        this.position = position;
    }

    /**
     * Sets the constant attenuation factor and returns the object itself.
     * * @param kC the constant attenuation factor
     *
     * @return the PointLight object itself for chaining
     */
    public PointLight setKc(double kC) {
        this.kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation factor and returns the object itself.
     * * @param kL the linear attenuation factor
     *
     * @return the PointLight object itself for chaining
     */
    public PointLight setKl(double kL) {
        this.kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation factor and returns the object itself.
     * * @param kQ the quadratic attenuation factor
     *
     * @return the PointLight object itself for chaining
     */
    public PointLight setKq(double kQ) {
        this.kQ = kQ;
        return this;
    }

    /**
     * {@inheritDoc}
     * Calculates the intensity at a specific point taking distance attenuation into account.
     */
    @Override
    public Color getIntensity(Point p) {
        double d = position.distance(p);
        double attenuation = kC + kL * d + kQ * d * d;
        return getIntensity().scale(1d / attenuation);
    }

    /**
     * {@inheritDoc}
     * Calculates the normalized direction vector from the light source to the point.
     */
    @Override
    public Vector getL(Point p) {
        return p.subtract(position).normalize();
    }
}
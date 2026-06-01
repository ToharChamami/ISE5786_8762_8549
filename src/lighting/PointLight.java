package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Representation of a point light source in the scene (such as a light bulb).
 */
public class PointLight extends Light implements LightSource {
    /**
     * The position of the point light
     */
    private final Point _position;

    /**
     * Constant attenuation factor
     */
    private double kC = 1d;

    /**
     * Linear attenuation factor
     */
    private double kL = 0d;

    /**
     * Quadratic attenuation factor
     */
    private double kQ = 0d;

    /**
     * NEW FIELD FOR MINI-PROJECT 1
     * Default is 0 (infinitesimal point light)
     */
    private double _radius = 0;

    /**
     * Constructs a point light source with a given intensity and position.
     *
     * @param intensity the color intensity of the light source
     * @param position  the position point of the light source in the scene
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        this._position = position;
    }

    /**
     * Sets the constant attenuation factor and returns the object itself.
     *
     * @param kC the constant attenuation factor
     * @return the PointLight object itself for chaining
     */
    public PointLight setKc(double kC) {
        this.kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation factor and returns the object itself.
     *
     * @param kL the linear attenuation factor
     * @return the PointLight object itself for chaining
     */
    public PointLight setKl(double kL) {
        this.kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation factor and returns the object itself.
     *
     * @param kQ the quadratic attenuation factor
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
        double d = _position.distance(p);
        double attenuation = kC + kL * d + kQ * d * d;
        return _intensity.scale(1d / attenuation);
    }

    /**
     * {@inheritDoc}
     * Calculates the normalized direction vector from the light source to the point.
     */
    @Override
    public Vector getL(Point p) {
        if (p.equals(_position)) {
            return null;
        }
        return p.subtract(_position).normalize();
    }

    @Override
    public double getDistance(Point point) {
        return this._position.distance(point);
    }

    /**
     * Sets the physical radius of the light source to enable soft shadows.
     * Uses the builder pattern for method chaining.
     *
     * @param _radius the size/radius of the area light source
     * @return the PointLight object itself for chaining
     * @throws IllegalArgumentException if the radius parameter is negative
     */
    public PointLight setRadius(double _radius) {
        if (_radius < 0) {
            throw new IllegalArgumentException("Light radius cannot be negative");
        }
        this._radius = _radius;
        return this;
    }

    /**
     * Gets the physical radius of this light source.
     *
     * @return the radius of the light source
     */
    public double getRadius() {
        return this._radius;
    }

    /**
     * Gets the position of this light source.
     *
     * @return the position of the light source
     */
    public Point getPosition() {
        return this._position;
    }

}
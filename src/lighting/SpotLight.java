package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Representation of a spotlight source in the scene (such as a flashlight).
 */
public class SpotLight extends PointLight {
    /**
     * The direction of the spotlight
     */
    private final Vector _direction;

    /**
     * The narrow beam factor
     */
    private double narrowBeam = 1d;

    /**
     * Constructs a spotlight source with a given intensity, position, and direction.
     *
     * @param intensity the color intensity of the light source
     * @param position  the position point of the light source
     * @param direction the direction vector of the light beam
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        this._direction = direction.normalize();
    }

    /**
     * Sets the concentration beam factor and returns the object itself.
     *
     * @param narrowBeam concentration factor exponent
     * @return the SpotLight object itself for chaining
     */
    public SpotLight setNarrowBeam(double narrowBeam) {
        this.narrowBeam = narrowBeam;
        return this;
    }

    /**
     * {@inheritDoc}
     * Overrides to maintain Fluent Interface and return SpotLight type.
     */
    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    /**
     * {@inheritDoc}
     * Overrides to maintain Fluent Interface and return SpotLight type.
     */
    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    /**
     * {@inheritDoc}
     * Overrides to maintain Fluent Interface and return SpotLight type.
     */
    @Override
    public SpotLight setKq(double kQ) {
        super.setKq(kQ);
        return this;
    }

    /**
     * {@inheritDoc}
     * Calculates the spotlight intensity factoring both distance attenuation and beam angle direction.
     */
    @Override
    public Color getIntensity(Point p) {
        Vector l = getL(p);
        double cosAlpha = _direction.dotProduct(l);

        if (cosAlpha <= 0) {
            return Color.BLACK;
        }

        double factor = narrowBeam == 1d ? cosAlpha : Math.pow(cosAlpha, narrowBeam);
        return super.getIntensity(p).scale(factor);
    }

    @Override
    public double getDistance(Point point) {
        return this.get_position().distance(point);
    }
}
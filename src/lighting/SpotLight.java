package lighting;

import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Representation of a spotlight source in the scene (such as a flashlight).
 */
public class SpotLight extends PointLight {

    /**
     * Normalized direction vector representing the primary axis of the spotlight beam.
     */
    private final Vector _direction;

    /**
     * Concentration exponent value regulating the sharpness and angular width of the spotlight beam.
     * Higher values focus the light toward the central axis, resulting in a narrower and sharper beam spotlight cone.
     */
    private double narrowBeam = 1d;

    /**
     * Constructs a new SpotLight with a specified intensity, position, and direction.
     * The constructor initializes the spotlight source by invoking the parent light's constructor
     * and stores a normalized copy of the given direction vector.
     *
     * @param intensity The color intensity of the spotlight.
     * @param position  The point in space where the spotlight is located.
     * @param direction The orientation vector of the light beam.
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        this._direction = direction.normalize();
    }

    /**
     * Sets the narrow beam concentration factor for this spotlight.
     * The method updates the inner beam concentration property and returns the current
     * instance to allow for method chaining (builder pattern style).
     *
     * @param narrowBeam The exponent factor representing the narrowness or concentration of the beam.
     * @return The current {@code SpotLight} instance for chaining method calls.
     */
    public SpotLight setNarrowBeam(double narrowBeam) {
        this.narrowBeam = narrowBeam;
        return this;
    }

    @Override
    public SpotLight setKc(double kC) {
        return (SpotLight) super.setKc(kC);
    }

    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    @Override
    public SpotLight setKq(double kQ) {
        super.setKq(kQ);
        return this;
    }

    @Override
    public SpotLight setRadius(double radius) {
        super.setRadius(radius);
        return this;
    }

    @Override
    public SpotLight setSampler(renderer.sampling.Sampler sampler) {
        super.setSampler(sampler);
        return this;
    }

    @Override
    public SpotLight setShadowTargetShape(renderer.sampling.TargetShape shape) {
        super.setShadowTargetShape(shape);
        return this;
    }

    @Override
    public SpotLight setShadowSamplingPattern(renderer.sampling.SamplingPattern pattern) {
        super.setShadowSamplingPattern(pattern);
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        Vector l = getL(p);
        double cosAlpha = _direction.dotProduct(l);
        if (cosAlpha <= 0) return Color.BLACK;
        double factor = (narrowBeam == 1d) ? cosAlpha : Math.pow(cosAlpha, narrowBeam);
        return super.getIntensity(p).scale(factor);
    }

    @Override
    public List<Vector> getLBeam(Point p) {
        if (_sampler == null || getRadius() <= 0) return List.of(getL(p));

        List<Point> points = _sampler.generateSamplePoints3D(getPosition(), this._direction, getRadius(), _targetShape, _samplingPattern);

        return createBeam(points, p);
    }
}
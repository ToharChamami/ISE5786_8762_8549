package lighting;

import java.util.ArrayList;
import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.sampling.Sampler;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;

/**
 * Represents a point light source with attenuation factors and soft shadow support.
 * The class defines a light source that radiates light equally in all directions from a specific point
 * in 3D space, with optional radius and sampling configuration for soft shadows.
 */
public class PointLight extends Light implements LightSource {
    /**
     * The position of the point light source in 3D space.
     */
    private final Point _position;

    /**
     * The constant attenuation factor of the light.
     */
    private double kC = 1d;

    /**
     * The linear attenuation factor of the light.
     */
    private double kL = 0d;

    /**
     * The quadratic attenuation factor of the light.
     */
    private double kQ = 0d;

    /**
     * The radius of the light source, used for soft shadows.
     */
    private double _radius = 0;

    /**
     * The sampler utilized to generate point distributions for soft shadows.
     */
    protected Sampler _sampler = null;

    /**
     * The geometric target shape configuration for sampling points.
     */
    protected TargetShape _targetShape = TargetShape.CIRCLE;

    /**
     * The pattern configuration applied for rendering samples on the target shape.
     */
    protected SamplingPattern _samplingPattern = SamplingPattern.REGULAR_GRID;

    /**
     * Constructs a PointLight with a specified intensity and position.
     * The constructor initializes the basic intensity of the light source and places it at the given point.
     *
     * @param intensity The basic intensity color of the light source.
     * @param position  The position point of the light source.
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        this._position = position;
    }

    /**
     * Configures the constant attenuation factor of this light source.
     * The method sets the constant factor and returns the instance for method chaining.
     *
     * @param kC The new constant attenuation factor.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setKc(double kC) {
        this.kC = kC;
        return this;
    }

    /**
     * Configures the linear attenuation factor of this light source.
     * The method sets the linear factor and returns the instance for method chaining.
     *
     * @param kL The new linear attenuation factor.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setKl(double kL) {
        this.kL = kL;
        return this;
    }

    /**
     * Configures the quadratic attenuation factor of this light source.
     * The method sets the quadratic factor and returns the instance for method chaining.
     *
     * @param kQ The new quadratic attenuation factor.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setKq(double kQ) {
        this.kQ = kQ;
        return this;
    }

    /**
     * Configures the radius of the light source area.
     * The method assigns a size radius used primarily for simulating soft shadow beams.
     *
     * @param radius The radius value of the light source.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setRadius(double radius) {
        this._radius = radius;
        return this;
    }

    /**
     * Configures the sampler used for generating light distribution points.
     * The method attaches a specific sampler implementation to enable soft shadow algorithms.
     *
     * @param sampler The sampler instance used for distributed sampling.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setSampler(Sampler sampler) {
        this._sampler = sampler;
        return this;
    }

    /**
     * Configures the geometry target shape for soft shadow calculations.
     * The method specifies what geometric form the light area resembles during calculations.
     *
     * @param shape The target geometric shape for shadow sampling.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setShadowTargetShape(TargetShape shape) {
        this._targetShape = shape;
        return this;
    }

    /**
     * Configures the sampling pattern for soft shadow generation.
     * The method defines how sample points are distributed over the target shape surface.
     *
     * @param pattern The specific pattern applied to sample generation.
     * @return The updated {@code PointLight} object itself.
     */
    public PointLight setShadowSamplingPattern(SamplingPattern pattern) {
        this._samplingPattern = pattern;
        return this;
    }

    /**
     * Retrieves the radius of the light source area.
     * The method returns the dimension used for area light calculations.
     *
     * @return The double value representing the light radius.
     */
    public double getRadius() {
        return this._radius;
    }

    /**
     * Retrieves the position of the light source in the scene.
     * The method returns the exact 3D coordinates representing where the light originates.
     *
     * @return A {@code Point} object representing the position.
     */
    public Point getPosition() {
        return this._position;
    }

    @Override
    public Color getIntensity(Point p) {
        double d = _position.distance(p);
        return _intensity.scale(1d / (kC + kL * d + kQ * d * d));
    }

    @Override
    public Vector getL(Point p) {
        return p.equals(_position) ? null : p.subtract(_position).normalize();
    }

    @Override
    public double getDistance(Point point) {
        return _position.distance(point);
    }

    @Override
    public List<Vector> getLBeam(Point p) {
        if (_sampler == null || _radius <= 0) return List.of(getL(p));

        List<Point> points = _sampler.generateSamplePoints3D(_position, getL(p), _radius, _targetShape, _samplingPattern);
        return createBeam(points, p);
    }

    /**
     * Creates a beam of normalized vectors from a list of points toward a target point.
     * The method iterates through the given list of points, calculates the direction vector
     * from each point to the target point, and normalizes the resulting vectors.
     *
     * @param points The list of source points from which the beam vectors originate.
     * @param p      The target point toward which all beam vectors are directed.
     * @return containing the normalized direction vectors of the beam.
     */
    protected List<Vector> createBeam(List<Point> points, Point p) {
        List<Vector> beam = new ArrayList<>(points.size());
        for (Point point : points) {
            beam.add(p.subtract(point).normalize());
        }
        return beam;
    }
}
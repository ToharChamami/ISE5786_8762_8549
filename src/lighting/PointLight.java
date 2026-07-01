package lighting;

import java.util.ArrayList;
import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Vector;
import renderer.sampling.Sampler;
import renderer.sampling.SamplingPattern;
import renderer.sampling.TargetShape;

public class PointLight extends Light implements LightSource {
    private final Point _position;
    private double kC = 1d, kL = 0d, kQ = 0d, _radius = 0;

    protected Sampler _sampler = null;
    protected TargetShape _targetShape = TargetShape.CIRCLE;
    protected SamplingPattern _samplingPattern = SamplingPattern.REGULAR_GRID;

    public PointLight(Color intensity, Point position) {
        super(intensity);
        this._position = position;
    }

    // Setter methods (Chaining)
    public PointLight setKc(double kC) {
        this.kC = kC;
        return this;
    }

    public PointLight setKl(double kL) {
        this.kL = kL;
        return this;
    }

    public PointLight setKq(double kQ) {
        this.kQ = kQ;
        return this;
    }

    public PointLight setRadius(double radius) {
        this._radius = radius;
        return this;
    }

    public PointLight setSampler(Sampler sampler) {
        this._sampler = sampler;
        return this;
    }

    public PointLight setShadowTargetShape(TargetShape shape) {
        this._targetShape = shape;
        return this;
    }

    public PointLight setShadowSamplingPattern(SamplingPattern pattern) {
        this._samplingPattern = pattern;
        return this;
    }

    public double getRadius() {
        return this._radius;
    }

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

        // יצירת הנקודות במרחב לפי ה-Sampler שלך
        List<Point> points = _sampler.generateSamplePoints3D(_position, getL(p), _radius, _targetShape, _samplingPattern);
        return createBeam(points, p);
    }

    // פונקציית העזר המשותפת (DRY)
    // פונקציית עזר למניעת כפילות קוד (DRY)
    protected List<Vector> createBeam(List<Point> points, Point p) {
        List<Vector> beam = new ArrayList<>(points.size());
        for (Point point : points) {
            // התיקון הקריטי: p פחות point! (כדי שהוקטור יצא מהאור אל הנקודה)
            beam.add(p.subtract(point).normalize());
        }
        return beam;
    }
}
package lighting;

import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Representation of a spotlight source in the scene (such as a flashlight).
 */
public class SpotLight extends PointLight {
    private final Vector _direction;
    private double narrowBeam = 1d;

    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        this._direction = direction.normalize();
    }

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

    // דריסת הפונקציות כדי לתמוך בשרשור (Builder pattern)
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

        // ההבדל מהמרצה: שימוש ב-_direction של הספוט במקום ב-getL(p)
        List<Point> points = _sampler.generateSamplePoints3D(getPosition(), this._direction, getRadius(), _targetShape, _samplingPattern);

        // קריאה לפונקציית העזר שנמצאת ב-PointLight
        return createBeam(points, p);
    }
}
package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Tube class represents a semi-infinite cylinder in 3D space,
 * defined by an axis ray and a radius. [cite: 154, 653]
 */
public class Tube extends RadialGeometry {
    /**
     * The axis ray of the tube
     */
    protected final Ray _axis;

    /**
     * Constructor to initialize a tube with an axis ray and a radius. [cite: 126, 653]
     *
     * @param axis    the axis ray of the tube [cite: 634]
     * @param _radius the radius of the tube [cite: 634]
     */
    public Tube(double _radius, Ray axis) {
        super(_radius);
        _axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        Point origin = _axis.origin();
        Vector v = _axis.direction();
        double t = v.dotProduct(point.subtract(origin));
        Point oTag = isZero(t) ? origin : origin.add(v.scale(t));
        return point.subtract(oTag).normalize();
    }

    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        return null;
    }
}

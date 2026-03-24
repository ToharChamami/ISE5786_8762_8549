package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

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
        // TDD Fix implementation
        // the origin of the axis
        Point origin = _axis.origin();
        // V is the direction of the axis
        Vector v = _axis.direction();
        // t = v * (P - P0)
        double t = v.dotProduct(point.subtract(origin));
        // If t is 0, the projection is exactly at the origin of the ray
        if (t == 0) {
            return point.subtract(origin).normalize();
        }
        // O' = P0 + t * v
        Point oTag = origin.add(v.scale(t));
        // Normal = (P - O') normalized
        return point.subtract(oTag).normalize();
    }
}

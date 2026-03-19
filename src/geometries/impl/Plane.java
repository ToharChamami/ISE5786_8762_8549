package geometries.impl;

import geometries.api.Geometry;
import primitives.Point;
import primitives.Vector;

/**
 * Class Plane represents a plane in a 3D system.
 */
public final class Plane extends Geometry {
    /**
     * Point on the plane
     */
    private final Point _point;
    /**
     * Normal vector to the plane
     */
    private final Vector _normal;

    /**
     * Constructor to initialize a plane from three points.
     *
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        _normal = null;
    }

    /**
     * Constructor to initialize a plane from a point and a normal vector.
     *
     * @param point  a point on the plane
     * @param normal the normal vector to the plane
     */
    public Plane(Point point, Vector normal) {

        _point = point;
        _normal = normal.normalize();
    }

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }
}

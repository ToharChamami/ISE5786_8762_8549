package geometries.impl;

import geometries.api.Geometry;
import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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
     * @throws IllegalArgumentException if the points are co-linear in any way
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        //calculate two vector in the plane
        Vector v1 = p2.subtract(p1);
        Vector v2 = p3.subtract(p1);
        _normal = v1.crossProduct(v2).normalize();
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

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point head = ray.origin();
        Vector direction = ray.direction();

        double denominatornv = _normal.dotProduct(direction);

        if (isZero(denominatornv)) {
            return null;
        }

        Vector headToPoint;
        try {
            headToPoint = _point.subtract(head);
        } catch (IllegalArgumentException _) {
            return null;
        }

        double PointMinusHead = _normal.dotProduct(headToPoint);

        double t = alignZero(PointMinusHead / denominatornv);

        return (t > 0) ? List.of(ray.getPoint(t)) : null;
    }
}

package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * The Sphere class represents a sphere in 3D Cartesian coordinate system.
 */
public final class Sphere extends RadialGeometry {
    /**
     * Center point of the sphere
     */
    private final Point _center;

    /**
     * Constructor to initialize a sphere with its center and radius.
     *
     * @param center the center point of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        _center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        // TDD Fix: Subtract center from the point to get the direction vector and normalize it to ensure length is 1.0.
        return point.subtract(_center).normalize();

    }

    @Override
    public List<Point> findIntersections(Ray ray) {
        Point head = ray.origin();
        Vector direction = ray.direction();
        Point center = _center;
        double radius = _radius;

        Vector L;
        try {
            L = center.subtract(head);
        } catch (IllegalArgumentException e) {
            return List.of(ray.getPoint(radius));
        }

        double tm = direction.dotProduct(L);
        double dSquared = L.lengthSquared() - tm * tm;
        double rSquared = radius * radius;

        if (dSquared >= rSquared) {
            return null;
        }

        double th = Math.sqrt(rSquared - dSquared);

        double t1 = tm - th;
        double t2 = tm + th;

        if (t1 > 0 && t2 > 0) {
            return List.of(ray.getPoint(t1), ray.getPoint(t2));
        }
        return (t1 > 0) ? List.of(ray.getPoint(t1)) :
                (t2 > 0) ? List.of(ray.getPoint(t2)) : null;
    }
}

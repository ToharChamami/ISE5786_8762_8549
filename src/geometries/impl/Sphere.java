package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;

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
        Vector L;
        try {
            L = _center.subtract(ray.origin());
        } catch (IllegalArgumentException e) {
            return List.of(ray.getPoint(_radius));
        }

        double tm = ray.direction().dotProduct(L);
        double dSquared = L.lengthSquared() - tm * tm;
        double thSquared = alignZero(_radiusSquared - dSquared);
        if (thSquared <= 0)
            return null;

        double th = Math.sqrt(thSquared);
        double t2 = alignZero(tm + th);
        if (t2 <= 0) return null;

        double t1 = alignZero(tm - th);
        return t1 <= 0
                ? List.of(ray.getPoint(t2))
                : List.of(ray.getPoint(t1), ray.getPoint(t2));
    }
}

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
        return point.subtract(_center).normalize();
    }

    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        Vector l;
        try {
            l = _center.subtract(ray.origin());
        } catch (IllegalArgumentException e) {
            return List.of(new Intersection(this, ray.getPoint(_radius)));
        }

        double tm = ray.direction().dotProduct(l);
        double dSquared = l.lengthSquared() - tm * tm;
        double thSquared = alignZero(_radiusSquared - dSquared);

        if (thSquared <= 0)
            return null;

        double th = Math.sqrt(thSquared);
        double t2 = alignZero(tm + th);
        if (t2 <= 0) return null;

        double t1 = alignZero(tm - th);

        return t1 <= 0
                ? List.of(new Intersection(this, ray.getPoint(t2)))
                : List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
    }
}

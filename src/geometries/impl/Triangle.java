package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Triangle class represents a triangle in 3D space.
 * Inherits from Polygon.
 * * @author Tohar Chamami
 */
public final class Triangle extends Polygon {

    /**
     * Constructor to initialize a triangle with its three vertices.
     *
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        var planeIntersections = _plane.findIntersections(ray);
        if (planeIntersections == null) return null;

        Point p0 = ray.origin();
        Vector v = ray.direction();

        Vector v1 = _vertices.get(0).subtract(p0);
        Vector v2 = _vertices.get(1).subtract(p0);
        Vector n1 = v1.crossProduct(v2).normalize();
        double s1 = alignZero(v.dotProduct(n1));
        if (s1 == 0) return null;

        Vector v3 = _vertices.get(2).subtract(p0);
        Vector n2 = v2.crossProduct(v3).normalize();
        double s2 = alignZero(v.dotProduct(n2));
        if (s1 * s2 <= 0) return null;

        Vector n3 = v3.crossProduct(v1).normalize();
        double s3 = alignZero(v.dotProduct(n3));
        if (s1 * s3 <= 0) return null;

        return List.of(new Intersection(this, planeIntersections.get(0)));
    }
}
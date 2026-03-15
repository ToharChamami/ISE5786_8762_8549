package geometries.impl;

import primitives.Point;
import primitives.Vector;

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
    public Vector getNormal(Point point) {
        return super.getNormal(point); // Uses Polygon's normal calculation
    }
}

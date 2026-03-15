package geometries.impl;

import geometries.api.Geometry;
 import primitives.*;

/**
 * Class Plane represents a plane in a 3D system.
 */
public class Plane extends Geometry {

    private final Point point;
    private final Vector normal;


    /**
     * Constructor to initialize a plane from three points.
     * @param p1 first point
     * @param p2 second point
     * @param p3 third point
     */
    public Plane(Point p1, Point p2, Point p3){
        point = p1;
        normal= null;
    }

    /**
     * Constructor to initialize a plane from a point and a normal vector.
     * @param _point a point on the plane
     * @param _normal the normal vector to the plane
     */
    public Plane(Point _point, Vector _normal) {

        point = _point;
        normal = _normal.normalize();
    }

    @Override
     public Vector getNormal (Point point) {
        return normal;
    }
}

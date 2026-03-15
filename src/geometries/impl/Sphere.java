package geometries.impl;

import primitives.*;

/**
 * The Sphere class represents a sphere in 3D Cartesian coordinate system.
 */
public class Sphere extends RadialGeometry {
    private final Point center;

    /**
     * Constructor to initialize a sphere with its center and radius.
     *
     * @param _center the center point of the sphere
     * @param _radius the radius of the sphere
     */
    public Sphere(Point _center, double _radius) {
        super(_radius);
        center = _center;
    }

    @Override
    public Vector getNormal(Point point) {
        return null;

    }
}

package geometries.impl;

import primitives.Point;
import primitives.Vector;

/**
 * The Sphere class represents a sphere in 3D Cartesian coordinate system.
 */
public final class Sphere extends RadialGeometry {
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
        return null;

    }
}

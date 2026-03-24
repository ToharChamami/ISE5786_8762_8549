package geometries.impl;

import primitives.Point;
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
}

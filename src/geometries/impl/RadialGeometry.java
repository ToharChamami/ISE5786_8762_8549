package geometries.impl;

import geometries.api.Geometry;

/**
 * The RadialGeometry class is an abstract base class for all geometric shapes
 * that are defined by a radius.
 */
public abstract class RadialGeometry extends Geometry {

    /**
     * The radius of the geometric shape.
     */
    public double radius;

    /**
     * The square of the radius (radius * radius).
     * Pre-calculated to improve performance in geometric calculations.
     */
    public double radiusSquared;


    /**
     * Constructs a new RadialGeometry with the specified radius.
     * Automatically calculates and stores the squared radius.
     * * @param _radius The radius of the shape. Must be a positive value.
     *
     * @throws IllegalArgumentException If the radius is less than or equal to zero.
     */
    public RadialGeometry(double _radius) {
        if (_radius < 0 || _radius == 0) {
            throw new IllegalArgumentException("radius must be positive");

        }
         this.radius = _radius;
         this.radiusSquared = _radius * _radius;
    }
}

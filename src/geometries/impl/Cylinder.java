package geometries.impl;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Cylinder class represents a finite cylinder in 3D space.
 * It inherits from Tube and adds a height property.
 */
public final class Cylinder extends Tube {

    private final double _height;

    /**
     * Constructor to initialize a cylinder with its dimensions and axis.
     *
     * @param _axis   the axis ray of the cylinder [cite: 114]
     * @param _radius the radius of the cylinder [cite: 126]
     * @param height  the height of the cylinder [cite: 165]
     */
    public Cylinder(double height, Ray _axis, double _radius) {
        super(_radius, _axis);
        _height = height;
    }

    @Override
    public Vector getNormal(Point point) {
        return null;
    }
}

package geometries.impl;

import primitives.Ray;

/**
 * Cylinder class represents a finite cylinder in 3D space.
 * It inherits from Tube and adds a height property.
 */
public final class Cylinder extends Tube {
    /**
     * The height of the cylinder
     */
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

}

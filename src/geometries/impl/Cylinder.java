package geometries.impl;

import primitives.*;


/**
 * Cylinder class represents a finite cylinder in 3D space.
 * It inherits from Tube and adds a height property.
 */
public class Cylinder extends Tube{

 protected final double height;
    /**
     * Constructor to initialize a cylinder with its dimensions and axis.
     * @param _axis   the axis ray of the cylinder [cite: 114]
     * @param _radius the radius of the cylinder [cite: 126]
     * @param _height the height of the cylinder [cite: 165]
     */
 public Cylinder (double _height, double _radius ,Ray _axis ){
     super(_axis,_radius);
     this.height = _height;
 }

 @Override
 public Vector getNormal(Point point) {
     return null;
 }
}

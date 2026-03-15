package geometries.impl;
import primitives.*;

/**
 * Tube class represents a semi-infinite cylinder in 3D space,
 * defined by an axis ray and a radius. [cite: 154, 653]
 */
public class Tube extends RadialGeometry {

    protected final Ray axis;
    /**
     * Constructor to initialize a tube with an axis ray and a radius. [cite: 126, 653]
     * @param _axis   the axis ray of the tube [cite: 634]
     * @param _radius the radius of the tube [cite: 634]
     */
    public Tube(Ray _axis, double _radius) {
        super(_radius);
        axis = _axis;
    }


    @Override
    public Vector getNormal(Point point) {
        return null;
    }
}

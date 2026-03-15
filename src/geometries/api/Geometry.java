package geometries.api;

import primitives.Point;
import primitives.Vector;


/**
 * The Geometry interface serves as a base for all geometric shapes
 * in a 3D Cartesian coordinate system.
 * <p>
 * Any class representing a physical shape must implement this interface
 * to provide fundamental geometric calculations.
 */
public abstract class Geometry {

    /**
     * Calculates the normal vector to the geometric body at a specific point on its surface.
     * <p>
     * The normal vector is perpendicular (at a 90-degree angle) to the tangent plane
     * of the body at the given point.
     * </p>
     * * @param point The point on the surface of the geometry where the normal is to be calculated.
     *
     * @return A normalized {@code Vector} representing the normal to the body at the given point.
     */
    public abstract Vector getNormal(Point point);
}

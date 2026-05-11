package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

/**
 * The Geometry interface serves as a base for all geometric shapes
 * in a 3D Cartesian coordinate system.
 * <p>
 * Any class representing a physical shape must implement this interface
 * to provide fundamental geometric calculations.
 * </p>
 */
public abstract class Geometry extends Intersectable {
    /**
     * Basic default constructor for documentation tools
     */
    public Geometry() { /* Basic default constructor for documentation tools */ }

    /**
     * The material of the geometry
     */
    private Material _material = new Material();

    /**
     * Gets the material of the geometry.
     *
     * @return the material of the geometry
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Sets the material of the geometry.
     *
     * @param material the new material
     * @return the geometry object itself for chaining
     */
    public Geometry setMaterial(Material material) {
        this._material = material;
        return this;
    }

    /**
     * The emission color of the geometry
     */
    private Color _emission = Color.BLACK;

    /**
     * Returns the emission color of the geometry
     *
     * @return The color
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Sets the emission color (Builder Pattern)
     *
     * @param emission The new emission color
     * @return This geometry object
     */
    public Geometry setEmission(Color emission) {
        this._emission = emission;
        return this;
    }

    /**
     * Calculates the normal vector to the geometric body at a specific point on its surface.
     * <p>
     * The normal vector is perpendicular (at a 90-degree angle) to the tangent plane
     * of the body at the given point.
     * </p>
     *
     * @param point The point on the surface of the geometry where the normal is to be calculated.
     * @return A normalized {@code Vector} representing the normal to the body at the given point.
     */
    public abstract Vector getNormal(Point point);
}

package geometries.api;

import java.util.List;

import lighting.LightSource;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Abstract class representing objects that can be intersected by a ray.
 * This class serves as the base for all geometries in the scene.
 * * @author Tohar Chamami
 */
public abstract class Intersectable {

    /**
     * Default constructor to satisfy JavaDoc.
     */
    public Intersectable() {
    }

    /**
     * Passive Data Structure (PDS) to bundle a point and the geometry it belongs to.
     * This class is final and cannot be inherited.
     */
    public static final class Intersection {
        /**
         * The geometry containing the point
         */
        public final Geometry geometry;
        /**
         * The point on the geometry surface
         */
        public final Point point;
        /**
         * The material of the intersected geometry
         */
        public final Material material;

        /**
         * The normal vector at the intersection point
         */
        public Vector normal;

        /**
         * The direction of the ray to the intersection point
         */
        public Vector v;

        /**
         * The dot product of the normal and the ray direction
         */
        public double vNormal;

        /**
         * The current light source being processed
         */
        public LightSource light;

        /**
         * The direction of the light to the intersection point
         */
        public Vector l;

        /**
         * The dot product of the normal and the light direction
         */
        public double lNormal;

        /**
         * Constructor for Intersection
         *
         * @param geometry The geometry where the intersection occurred
         * @param point    The point of intersection
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
            this.material = geometry == null ? new Material() : geometry.getMaterial();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof Intersection other) {
                return this.geometry == other.geometry && this.point.equals(other.point);
            }
            return false;
        }

        @Override
        public String toString() {
            return "Intersection: geometry=" + geometry + ", point=" + point;
        }
    }

    /**
     * Public API for finding intersections using the NVI (Non-Virtual Interface) pattern.
     *
     * @param ray The ray to intersect with the objects
     * @return List of Intersections, or null if no intersections found
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray);
    }

    /**
     * Finds intersections between a ray and the geometry (backward compatibility).
     *
     * @param ray the ray to intersect with
     * @return a list of intersection points, or null if there are none
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null
                : intersections.stream()
                  .map(intersection -> intersection.point)
                  .toList();
    }

    /**
     * Abstract helper method for finding intersections.
     * Each concrete geometry must implement this method.
     *
     * @param ray The ray to intersect
     * @return List of Intersections
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);
}
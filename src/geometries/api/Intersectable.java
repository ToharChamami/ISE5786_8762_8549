package geometries.api;

import java.util.List;
import primitives.Point;
import primitives.Ray;

/**
 * Abstract class representing objects that can be intersected by a ray.
 * This class serves as the base for all geometries in the scene.
 * * @author Tohar Chamami
 */
public abstract class Intersectable {

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
         * Constructor for Intersection
         *
         * @param geometry The geometry where the intersection occurred
         * @param point    The point of intersection
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
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
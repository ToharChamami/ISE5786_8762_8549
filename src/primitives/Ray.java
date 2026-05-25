package primitives;

import geometries.api.Intersectable.Intersection;
import java.util.List;
import java.util.Objects;

/**
 * The Ray class represents a ray in 3D space, defined by a starting point (origin)
 * and a direction vector.
 * <p>
 * A ray is an infinite line starting from a specific point and extending
 * indefinitely in a given direction.
 */
public final class Ray {

    /**
     * The starting point of the ray.
     */
    public final Point _origin;

    /**
     * The direction vector of the ray.
     * This vector is stored in a normalized form (unit vector).
     */
    public final Vector _direction;

    /**
     * Small offset to prevent self-intersection
     */
    private static final double DELTA = 0.1;

    /**
     * Constructs a new Ray with a given origin and direction.
     * The direction vector is automatically normalized during initialization.
     *
     * @param origin    The starting point of the ray.
     * @param direction The direction vector of the ray (will be normalized).
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Constructor for a ray with a slight offset along the normal.
     *
     * @param head      the original starting point
     * @param direction the direction of the ray
     * @param normal    the normal vector at the intersection
     */
    public Ray(Point head, Vector direction, Vector normal) {
        Vector delta = normal.scale(normal.dotProduct(direction) > 0 ? DELTA : -DELTA);
        this._origin = head.add(delta);
        this._direction = direction.normalize();
    }

    /**
     * Getter for the origin point of the ray.
     *
     * @return The origin point.
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Getter for the direction vector of the ray.
     *
     * @return The normalized direction vector.
     */
    public Vector direction() {
        return _direction;
    }

    @Override
    public String toString() {
        return "Ray:" + _origin + _direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _origin.equals(other._origin) && _direction.equals(other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }

    /**
     * Calculates a point on the ray at a given distance t from the head.
     *
     * @param t The distance from the head of the ray.
     * @return The point P = _origin + t * _direction.
     */
    public Point getPoint(double t) {

        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException _) {
            return _origin;
        }
    }

    /**
     * Finds the closest point to the ray origin.
     * Maintains backward compatibility by delegating to findClosestIntersection.
     *
     * @param points list of points
     * @return the closest point, or null if the list is empty
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null ? null
                : findClosestIntersection(
                points.stream()
                .map(point -> new Intersection(null, point))
                .toList()
        ).point;
    }

    /**
     * Finds the closest intersection point to the ray origin.
     *
     * @param intersections list of intersections
     * @return the closest intersection, or null if the list is empty
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null)
            return null;

        Intersection closest = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for (var intersection : intersections) {
            double distance = _origin.distanceSquared(intersection.point);
            if (distance < minDistance) {
                minDistance = distance;
                closest = intersection;
            }
        }
        return closest;
    }

}

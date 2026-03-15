package primitives;

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
     * Constructs a new Ray with a given origin and direction.
     * The direction vector is automatically normalized during initialization.
     * * @param _origin    The starting point of the ray.
     *
     * @param direction The direction vector of the ray (will be normalized).
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Getter for the origin point of the ray.
     * * @return The origin point.
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Getter for the direction vector of the ray.
     * * @return The normalized direction vector.
     */
    public Vector direction() {
        return _direction;
    }


    /**
     * Returns a string representation of the ray.
     * * @return A string containing the origin and direction values.
     */
    @Override
    public String toString() {
        return "Ray:" + _origin + _direction;
    }


    /**
     * Indicates whether some other object is "equal to" this one.
     * Two rays are considered equal if they have the same origin and direction.
     * * @param obj The reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return _origin.equals(other._origin) && _direction.equals(other._direction);
    }


    /**
     * Returns a hash code value for the ray.
     * * @return A hash code value based on the origin and direction.
     */
    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }
}

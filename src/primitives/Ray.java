package primitives;

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
    public final Point origin;

    /**
     * The direction vector of the ray.
     * This vector is stored in a normalized form (unit vector).
     */
    public final Vector direction;

    /**
     * Constructs a new Ray with a given origin and direction.
     * The direction vector is automatically normalized during initialization.
     * * @param _origin    The starting point of the ray.
     *
     * @param _direction The direction vector of the ray (will be normalized).
     */
    public Ray(Point _origin, Vector _direction) {
        origin = _origin;
        direction = _direction.normalize();
    }

}

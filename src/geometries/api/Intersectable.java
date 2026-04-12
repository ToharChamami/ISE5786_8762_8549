package geometries.api;

import java.util.List;
import primitives.Point;
import primitives.Ray;

/**
 * Abstract class representing objects that can be intersected by a ray. [cite: 17, 18]
 */
public abstract class Intersectable {
    /**
     * Basic default constructor for documentation tools
     */
    public Intersectable() {
    }

    /**
     * Finds the intersection points between a given ray and the geometry. [cite: 18, 22]
     *
     * @param ray The ray to check for intersections [cite: 18]
     * @return A list of intersection points, or null if there are no intersections [cite: 27]
     */
    public abstract List<Point> findIntersections(Ray ray);
}
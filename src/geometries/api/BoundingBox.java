package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an Axis-Aligned Bounding Box (AABB) for Conservative Bounding Region (CBR) acceleration.
 * The box is defined by minimum and maximum coordinates along the X, Y, and Z axes to restrict spatial bounds.
 */
public class BoundingBox {

    /**
     * Minimum X coordinate boundary of the bounding box.
     */
    public final double minX;

    /**
     * Maximum X coordinate boundary of the bounding box.
     */
    public final double maxX;

    /**
     * Minimum Y coordinate boundary of the bounding box.
     */
    public final double minY;

    /**
     * Maximum Y coordinate boundary of the bounding box.
     */
    public final double maxY;

    /**
     * Minimum Z coordinate boundary of the bounding box.
     */
    public final double minZ;

    /**
     * Maximum Z coordinate boundary of the bounding box.
     */
    public final double maxZ;

    /**
     * Constructs a BoundingBox with specific minimum and maximum values for each axis.
     * The method initializes the absolute geometric boundaries of the volume across all three spatial dimensions.
     *
     * @param minX The lowest bounds along the X axis.
     * @param maxX The highest bounds along the X axis.
     * @param minY The lowest bounds along the Y axis.
     * @param maxY The highest bounds along the Y axis.
     * @param minZ The lowest bounds along the Z axis.
     * @param maxZ The highest bounds along the Z axis.
     */
    public BoundingBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    /**
     * Performs a fast ray-box intersection test using the Kay and Kajiya Slab Method.
     * The method computes the ray-clip overlapping intervals across parallel slabs for each major axis
     * and evaluates if a common spatial overlap exists along the ray path.
     *
     * @param ray The ray to test against the bounding box bounds.
     * @return {@code true} if the ray intersects the bounding volume, {@code false} otherwise.
     */
    public boolean intersect(Ray ray) {
        Point orig = ray._origin;
        Vector dir = ray._direction;

        double tMin = (minX - orig.getX()) / dir.getX();
        double tMax = (maxX - orig.getX()) / dir.getX();
        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tyMin = (minY - orig.getY()) / dir.getY();
        double tyMax = (maxY - orig.getY()) / dir.getY();
        if (tyMin > tyMax) {
            double temp = tyMin;
            tyMin = tyMax;
            tyMax = temp;
        }

        if ((tMin > tyMax) || (tyMin > tMax)) return false;
        if (tyMin > tMin) tMin = tyMin;
        if (tyMax < tMax) tMax = tyMax;

        double tzMin = (minZ - orig.getZ()) / dir.getZ();
        double tzMax = (maxZ - orig.getZ()) / dir.getZ();
        if (tzMin > tzMax) {
            double temp = tzMin;
            tzMin = tzMax;
            tzMax = temp;
        }

        if ((tMin > tzMax) || (tzMin > tMax)) return false;
        if (tzMax < tMax) tMax = tzMax;

        return tMax > 0;
    }
}
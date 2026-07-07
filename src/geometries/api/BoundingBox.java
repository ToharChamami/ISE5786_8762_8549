package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an Axis-Aligned Bounding Box (AABB) for Conservative Bounding Region (CBR) acceleration.
 */
public class BoundingBox {
    public final double minX, maxX;
    public final double minY, maxY;
    public final double minZ, maxZ;

    /**
     * Constructs a BoundingBox with specific minimum and maximum values for each axis.
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
     * Fast ray-box intersection test using the Slab Method.
     *
     * @param ray The ray to test against the bounding box
     * @return true if the ray intersects the box, false otherwise
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

        // תיקון קריטי: נוספה שורת הבדיקה לחיתוך בציר ה-Z
        if ((tMin > tzMax) || (tzMin > tMax)) return false;
        if (tzMax < tMax) tMax = tzMax;

        // וידוא קריטי 2: האם הקופסה נמצאת כולה מאחורי המצלמה? אם כן - נפסל!
        return tMax > 0;
    }
}
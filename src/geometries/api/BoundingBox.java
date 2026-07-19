package geometries.api;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;

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
     * a static delta
     */

    private static final double DELTA = 0.1;

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
        // הרחיבי את הקופסה מעט לכל כיוון
        this.minX = minX - DELTA;
        this.maxX = maxX + DELTA;
        this.minY = minY - DELTA;
        this.maxY = maxY + DELTA;
        this.minZ = minZ - DELTA;
        this.maxZ = maxZ + DELTA;
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

        // 1. נחלץ את ערכי הכיוון ונוודא שהם לעולם לא 0 מוחלט
        double dirX = alignZero(dir.getX()) == 0 ? 0.00001 : dir.getX();
        double dirY = alignZero(dir.getY()) == 0 ? 0.00001 : dir.getY();
        double dirZ = alignZero(dir.getZ()) == 0 ? 0.00001 : dir.getZ();

        // 2. ציר ה-X
        double tMin = (minX - orig.getX()) / dirX;
        double tMax = (maxX - orig.getX()) / dirX;
        if (tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        // 3. ציר ה-Y
        double tyMin = (minY - orig.getY()) / dirY;
        double tyMax = (maxY - orig.getY()) / dirY;
        if (tyMin > tyMax) {
            double temp = tyMin;
            tyMin = tyMax;
            tyMax = temp;
        }

        // בדיקת חפיפה בין X ל-Y
        if ((tMin > tyMax) || (tyMin > tMax)) return false;
        if (tyMin > tMin) tMin = tyMin;
        if (tyMax < tMax) tMax = tyMax;

        // 4. ציר ה-Z
        double tzMin = (minZ - orig.getZ()) / dirZ;
        double tzMax = (maxZ - orig.getZ()) / dirZ;
        if (tzMin > tzMax) {
            double temp = tzMin;
            tzMin = tzMax;
            tzMax = temp;
        }

        // בדיקת חפיפה סופית עם Z
        if ((tMin > tzMax) || (tzMin > tMax)) return false;

        // אם עברנו הכל, יש פגיעה בקופסה!
        return true;
    }
}
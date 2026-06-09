package renderer.sampling;

import java.util.ArrayList;
import java.util.List;
import primitives.Point;
import primitives.Vector;

/**
 * Responsible for generating a normalized distribution of 2D offset points
 * within a target boundary. Implements caching to optimize performance.
 * * @author YourName & Partner
 */
public class Sampler {
    /**
     *
     */
    private final int _gridSize;
    /**
     *
     */
    private final List<Offset2D> _cachedSquarePoints;

    /**
     * Constructs a Sampler engine and pre-calculates the uniform grid structure.
     * This ensures high performance by avoiding object creation during the render loop.
     * * @param gridSize the density matrix size (number of rows/columns in the grid)
     */
    public Sampler(int gridSize) {
        this._gridSize = gridSize;
        this._cachedSquarePoints = new ArrayList<>(gridSize * gridSize);
        initializeGrid();
    }

    /**
     * Pre-computes a uniform grid matrix within a [-0.5, 0.5] square arena.
     */
    private void initializeGrid() {
        if (_gridSize <= 1) {
            _cachedSquarePoints.add(new Offset2D(0, 0));
            return;
        }

        double step = 1.0 / _gridSize;

        for (int i = 0; i < _gridSize; i++) {
            for (int j = 0; j < _gridSize; j++) {
                // Calculate the center point of each grid sub-cell
                double x = -0.5 + (i + 0.5) * step;
                double y = -0.5 + (j + 0.5) * step;
                _cachedSquarePoints.add(new Offset2D(x, y));
            }
        }
    }

    /**
     * Retrieves the list of generated sample offsets, filtered by the requested target shape.
     * If a CIRCLE shape is requested, points outside the inscribed disk are discarded.
     * * @param shape the geometric target boundary type (SQUARE or CIRCLE)
     *
     * @return an unmodifiable or freshly filtered list of Offset2D objects
     */
    public List<Offset2D> getSamplePoints(TargetShape shape) {
        if (shape == TargetShape.SQUARE) {
            return _cachedSquarePoints;
        }
        List<Offset2D> circlePoints = new ArrayList<>();
        for (Offset2D pt : _cachedSquarePoints) {
            if ((pt.getX() * pt.getX() + pt.getY() * pt.getY()) <= 0.25) {
                circlePoints.add(pt);
            }
        }
        return circlePoints;
    }

    /**
     *
     * @return
     */
    public int getGridSize() {
        return this._gridSize; // או השם של משתנה גודל הרשת אצלך במחלקה
    }

    /**
     * Generates physical 3D points in the world coordinate system based on the sampling pattern,
     * target shape, and the coordinate system specified by the given normal vector.
     *
     * @param center      The center point of the sampling area (e.g., light position)
     * @param normal      The direction/normal vector of the area (e.g., light direction)
     * @param size        The physical size (radius or width) of the sampling area
     * @param targetShape The target boundary shape (SQUARE or CIRCLE)
     * @return A list of physical 3D points representing the samples
     */
    public List<Point> generateSamplePoints3D(Point center, Vector normal, double size, TargetShape targetShape) {
        List<Offset2D> offsets = getSamplePoints(targetShape);
        List<Point> points3D = new ArrayList<>(offsets.size());

        // 1. Build a local coordinate system orthogonal to the normal vector
        Vector vTo = normal.normalize();
        Vector vRight;

        // Prevent cross product with a parallel vector (e.g., AXIS_X)
        if (primitives.Util.isZero(vTo.dotProduct(Vector.AXIS_X) - 1) ||
                primitives.Util.isZero(vTo.dotProduct(Vector.AXIS_X) + 1)) {
            vRight = vTo.crossProduct(Vector.AXIS_Y).normalize();
        } else {
            vRight = vTo.crossProduct(Vector.AXIS_X).normalize();
        }
        Vector vUp = vRight.crossProduct(vTo).normalize();

        // 2. Transform each 2D offset into a 3D point in the world system
        for (Offset2D offset : offsets) {
            Point p = center;

            // Scale the normalized offset [-0.5, 0.5] by the physical size
            double deltaX = offset.getX() * size * 2;
            double deltaY = offset.getY() * size * 2;

            if (!primitives.Util.isZero(deltaX)) {
                p = p.add(vRight.scale(deltaX));
            }
            if (!primitives.Util.isZero(deltaY)) {
                p = p.add(vUp.scale(deltaY));
            }
            points3D.add(p);
        }

        return points3D;
    }
}
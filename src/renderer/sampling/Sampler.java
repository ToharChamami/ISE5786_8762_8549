package renderer.sampling;

import java.util.ArrayList;
import java.util.List;

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
}
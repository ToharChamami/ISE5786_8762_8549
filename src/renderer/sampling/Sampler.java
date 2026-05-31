package renderer.sampling;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for generating a normalized distribution of 2D offset points
 * within a target boundary. Implements caching to optimize performance.
 * * @author YourName & Partner
 */
public class Sampler {
    private final int gridSize; // e.g., 9 for a 9x9 matrix (81 samples)
    private final List<Offset2D> cachedSquarePoints;

    /**
     * Constructs a Sampler engine and pre-calculates the uniform grid structure.
     * This ensures high performance by avoiding object creation during the render loop.
     * * @param gridSize the density matrix size (number of rows/columns in the grid)
     */
    public Sampler(int gridSize) {
        this.gridSize = gridSize;
        this.cachedSquarePoints = new ArrayList<>(gridSize * gridSize);
        initializeGrid();
    }

    /**
     * Pre-computes a uniform grid matrix within a [-0.5, 0.5] square arena.
     */
    private void initializeGrid() {
        if (gridSize <= 1) {
            cachedSquarePoints.add(new Offset2D(0, 0));
            return;
        }

        double step = 1.0 / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                // Calculate the center point of each grid sub-cell
                double x = -0.5 + (i + 0.5) * step;
                double y = -0.5 + (j + 0.5) * step;
                cachedSquarePoints.add(new Offset2D(x, y));
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
            return cachedSquarePoints;
        }

        // Rejection Sampling for Circle: x^2 + y^2 <= radius^2 (where radius = 0.5, so radius^2 = 0.25)
        List<Offset2D> circlePoints = new ArrayList<>();
        for (Offset2D pt : cachedSquarePoints) {
            if ((pt.getX() * pt.getX() + pt.getY() * pt.getY()) <= 0.25) {
                circlePoints.add(pt);
            }
        }
        return circlePoints;
    }
}
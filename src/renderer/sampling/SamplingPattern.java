package renderer.sampling;

/**
 * Supported pattern distributions for generating the sample beam.
 */
public enum SamplingPattern {
    /**
     * Regular grid sampling pattern without random offsets
     */
    REGULAR_GRID,
    /**
     * Grid sampling pattern with random offsets within each cell
     */
    JITTERED_GRID
}

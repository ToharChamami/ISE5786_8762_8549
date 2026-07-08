package lighting;

import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface representing external light sources in the scene.
 * Defines functionality needed for directional and intensity lighting calculations on geometries.
 */
public interface LightSource {

    /**
     * Calculates the absolute distance from the light source position to a given target point.
     * Used primary to evaluate shadow ray intersections and range attenuation thresholds.
     *
     * @param point The point in 3D space to measure the distance to.
     * @return The distance value between the light source and the target point.
     */
    double getDistance(Point point);

    /**
     * Calculates the intensity color of the light reaching a given spatial point.
     * Takes distance-based attenuation factors into account depending on the specific source implementation.
     *
     * @param p The target point on a geometry surface.
     * @return The calculated color intensity reaching the point.
     */
    Color getIntensity(Point p);

    /**
     * Calculates the normalized direction vector of the light pointing from the source to a given target point.
     *
     * @param p The target point on a geometry surface.
     * @return The normalized direction vector from the light source to the point.
     */
    Vector getL(Point p);

    /**
     * Returns a collection beam of directional vectors from the light source boundary to the target point.
     * Used to calculate distributed ray configurations for soft shadowing features.
     * The default implementation fallback returns a single standard structural directional vector.
     *
     * @param p The target point on a geometry surface.
     * @return A {@code List} containing the generated normalized directional vectors forming the beam.
     */
    default List<Vector> getLBeam(Point p) {
        return List.of(getL(p));
    }
}
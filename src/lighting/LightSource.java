package lighting;

import java.util.List;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Interface representing external light sources in the scene.
 * Defines functionality needed for lighting calculations on geometries.
 */
public interface LightSource {

    /**
     * Calculates the distance from the light source to a given point.
     *
     * @param point the point in space to measure the distance to
     * @return the distance between the light source and the point
     */
    double getDistance(Point point);

    /**
     * Calculates the intensity of the light reaching a given point.
     * Takes attenuation factors into account based on the implementation.
     *
     * @param p the target point on a geometry
     * @return the color intensity at the given point
     */
    public Color getIntensity(Point p);

    /**
     * Calculates the direction vector of the light from the source to a given point.
     * The returned vector must be normalized.
     *
     * @param p the target point on a geometry
     * @return the normalized direction vector from the light source to the point
     */
    public Vector getL(Point p);

    /**
     * Returns a beam of direction vectors from the light source to the point.
     * Default implementation returns a single directional vector.
     */
    default List<Vector> getLBeam(Point p) {
        return List.of(getL(p));
    }
}
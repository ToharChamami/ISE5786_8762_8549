package geometries.impl;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import primitives.Ray;

/**
 * Composite class for a collection of geometric objects.
 */
public class Geometries extends Intersectable {
    /**
     * List of intersectable geometric objects
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Empty constructor
     */
    public Geometries() {
    }

    /**
     * Constructor with variable number of geometries
     *
     * @param geometries objects to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds geometric objects to the collection
     *
     * @param geometries objects to add
     */
    public void add(Intersectable... geometries) {
        Collections.addAll(this.geometries, geometries);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        List<Intersection> result = null;
        for (Intersectable item : geometries) {
            var itemIntersections = item.calcIntersections(ray);
            if (itemIntersections != null) {

                if (result == null)
                    result = new ArrayList<>(itemIntersections);
                else
                    result.addAll(itemIntersections);
            }
        }
        return result;
    }
}
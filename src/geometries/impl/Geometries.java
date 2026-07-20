package geometries.impl;

import geometries.api.BoundingBox;
import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import primitives.Ray;

/**
 * Composite class for a collection of geometric objects.
 */
public class Geometries extends Intersectable {
    /**
     * List of intersect geometric objects
     */
    private final List<Intersectable> _geometries = new ArrayList<>();

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
        Collections.addAll(_geometries, geometries);
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        List<Intersection> result = null;
        for (Intersectable item : _geometries) {
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

    @Override
    protected void createBoundingBoxHelper() {
        if (_geometries.isEmpty()) return;

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        boolean hasBoxes = false;
        boolean hasInfinites = false;

        for (Intersectable item : _geometries) {
            item.createBoundingBox();
            BoundingBox itemBox = item.getBoundingBox();

            if (itemBox != null) {
                hasBoxes = true;
                if (itemBox.minX < minX) minX = itemBox.minX;
                if (itemBox.maxX > maxX) maxX = itemBox.maxX;
                if (itemBox.minY < minY) minY = itemBox.minY;
                if (itemBox.maxY > maxY) maxY = itemBox.maxY;
                if (itemBox.minZ < minZ) minZ = itemBox.minZ;
                if (itemBox.maxZ > maxZ) maxZ = itemBox.maxZ;
            } else {
                hasInfinites = true;
            }
        }

        if (hasBoxes && !hasInfinites) {
            this.box = new BoundingBox(minX, maxX, minY, maxY, minZ, maxZ);
        } else {
            this.box = null;
        }
    }

    /**
     * Builds a Bounding Volume Hierarchy tree for the geometries to optimize intersection performance.
     * The method recursively processes nested geometry groups and splits finite items from infinite items
     * using bounding boxes to construct the acceleration structure.
     */
    public void buildBVH() {
        createBoundingBox();
        for (Intersectable item : this._geometries) {
            if (item instanceof Geometries geometries) {
                geometries.buildBVH();
            }
        }

        if (this._geometries.size() <= 1) return;
        Geometries finites = new Geometries();
        Geometries infinites = new Geometries();
        for (Intersectable item : this._geometries) {
            if (item.getBoundingBox() != null) {
                finites.add(item);
            } else {
                infinites.add(item);
            }
        }

        if (finites._geometries.size() <= 2) return;
        finites.buildBVHTree();

        _geometries.clear();
        if (box == null) {
            _geometries.add(infinites);
            _geometries.add(finites);
        } else {
            _geometries.addAll(finites._geometries);
        }
    }

    /**
     * Recursively builds a Bounding Volume Hierarchy tree from a list of finite geometries.
     * The method finds the longest axis of the overall bounding box, sorts the items by their centers
     * along that axis, and splits them into two halves to form a balanced binary tree structure.
     */
    private void buildBVHTree() {
        createBoundingBox();
        if (_geometries.size() <= 2) return;

        double minX = box.minX, maxX = box.maxX;
        double minY = box.minY, maxY = box.maxY;
        double minZ = box.minZ, maxZ = box.maxZ;
        double sizeX = maxX - minX;
        double sizeY = maxY - minY;
        double sizeZ = maxZ - minZ;

        if (sizeX > sizeY && sizeX > sizeZ) {
            _geometries.sort(Comparator.comparingDouble(g -> (g.getBoundingBox().minX + g.getBoundingBox().maxX) / 2));
        } else if (sizeY > sizeX && sizeY > sizeZ) {
            _geometries.sort(Comparator.comparingDouble(g -> (g.getBoundingBox().minY + g.getBoundingBox().maxY) / 2));
        } else {
            _geometries.sort(Comparator.comparingDouble(g -> (g.getBoundingBox().minZ + g.getBoundingBox().maxZ) / 2));
        }

        int mid = _geometries.size() / 2;
        Geometries leftList = new Geometries();
        leftList._geometries.addAll(_geometries.subList(0, mid));
        Geometries rightList = new Geometries();
        leftList._geometries.addAll(_geometries.subList(mid, _geometries.size()));

        leftList.buildBVH();
        rightList.buildBVH();

        _geometries.clear();
        _geometries.add(leftList);
        _geometries.add(rightList);
    }

    /**
     * Recursive helper method to replicate and completely flatten a hierarchical geometry structure.
     * Unpacks all composite node containers down to a single dimensional array list of leaf structures.
     */
    public void flatten() {
        List<Intersectable> flatList = flattenHelper();
        _geometries.clear();
        _geometries.addAll(flatList);
    }

    /**
     * Recursive helper.
     *
     * @return accumulated list containing all the basic geometries.
     */
    private List<Intersectable> flattenHelper() {
        List<Intersectable> flatList = new ArrayList<>();
        for (var geo : _geometries) {
            if (geo instanceof Geometries geos)
                flatList.addAll(geos.flattenHelper());
            else
                flatList.add(geo);
        }
        return flatList;
    }

}
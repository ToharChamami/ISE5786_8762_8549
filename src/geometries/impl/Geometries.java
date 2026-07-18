package geometries.impl;

import geometries.api.BoundingBox;
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
     * List of intersect geometric objects
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

    @Override
    protected void createBoundingBoxHelper() {
        if (geometries.isEmpty()) return;

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        boolean hasBoxes = false;
        boolean hasInfinites = false;

        for (Intersectable item : geometries) {
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
        for (Intersectable item : this.geometries) {
            if (item instanceof Geometries) {
                ((Geometries) item).buildBVH();
            }
        }

        if (this.geometries.size() <= 1) return;

        createBoundingBox();

        List<Intersectable> finites = new ArrayList<>();
        List<Intersectable> infinites = new ArrayList<>();
        for (Intersectable item : this.geometries) {
            if (item.getBoundingBox() != null) {
                finites.add(item);
            } else {
                infinites.add(item);
            }
        }

        if (finites.size() <= 1) return;

        Intersectable root = buildBVHTree(finites);

        this.geometries.clear();
        this.geometries.addAll(infinites);
        this.geometries.add(root);
    }

    /**
     * Recursively builds a Bounding Volume Hierarchy tree from a list of finite geometries.
     * The method finds the longest axis of the overall bounding box, sorts the items by their centers
     * along that axis, and splits them into two halves to form a balanced binary tree structure.
     *
     * @param list The list of intersectable geometries to be organized into the BVH tree.
     * @return representing the root node (parent) of the generated tree structure.
     */
    private Intersectable buildBVHTree(List<Intersectable> list) {
        if (list.isEmpty()) return new Geometries();
        if (list.size() == 1) return list.get(0);
        if (list.size() == 2) {
            Geometries pair = new Geometries(list.get(0), list.get(1));
            pair.createBoundingBox();
            return pair;
        }

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        for (Intersectable item : list) {
            var box = item.getBoundingBox();
            if (box != null) {
                if (box.minX < minX) minX = box.minX;
                if (box.maxX > maxX) maxX = box.maxX;
                if (box.minY < minY) minY = box.minY;
                if (box.maxY > maxY) maxY = box.maxY;
                if (box.minZ < minZ) minZ = box.minZ;
                if (box.maxZ > maxZ) maxZ = box.maxZ;
            }
        }

        double sizeX = maxX - minX;
        double sizeY = maxY - minY;
        double sizeZ = maxZ - minZ;

        if (sizeX > sizeY && sizeX > sizeZ) {
            list.sort(java.util.Comparator.comparingDouble(g -> (g.getBoundingBox().minX + g.getBoundingBox().maxX) / 2));
        } else if (sizeY > sizeX && sizeY > sizeZ) {
            list.sort(java.util.Comparator.comparingDouble(g -> (g.getBoundingBox().minY + g.getBoundingBox().maxY) / 2));
        } else {
            list.sort(java.util.Comparator.comparingDouble(g -> (g.getBoundingBox().minZ + g.getBoundingBox().maxZ) / 2));
        }

        int mid = list.size() / 2;
        List<Intersectable> leftList = new ArrayList<>(list.subList(0, mid));
        List<Intersectable> rightList = new ArrayList<>(list.subList(mid, list.size()));

        Intersectable left = buildBVHTree(leftList);
        Intersectable right = buildBVHTree(rightList);

        Geometries parent = new Geometries(left, right);
        parent.createBoundingBox();
        return parent;
    }
}
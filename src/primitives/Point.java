package primitives;

/**
 * The class represents a point in 3-D
 * It is used to define coordinates and perform spatial operations.
 */
public class Point {

    /**
     * A constant representing the origin of the axes (0, 0, 0).
     */
    public static final Point ZERO = new Point(Double3.ZERO);

    /**
     * An object containing the coordinate values (x, y, z).
     */
    protected final Double3 _xyz;

    /**
     * Constructs a new point based on three individual coordinate values.
     * * @param x The value on the x-axis.
     *
     * @param y The value on the y-axis.
     * @param z The value on the z-axis.
     */
    public Point(double x, double y, double z) {
        _xyz = new Double3(x, y, z);
    }

    /**
     * Constructs a new point using an existing Double3 object.
     * * @param xyz An object containing the three coordinate values.
     */
    public Point(Double3 xyz) {
        _xyz = xyz;
    }

    /**
     * Subtracts a point from the current point to create a new vector.
     * * @param other The point to subtract from this point.
     *
     * @return A new {@code Vector} representing the direction and distance from the other point to this one.
     */
    public Vector substruct(Point other) {
        return new Vector(_xyz.subtract(other._xyz));
    }


    /**
     * Adds a vector to the current point to create a new point.
     * * @param vector The vector to add to this point.
     *
     * @return A new {@code Point} resulting from the translation of this point by the given vector.
     */
    public Point add(Vector vector) {
        return new Point(_xyz.add(vector._xyz));
    }

    /**
     * * Calculates the square distance between two points
     * *
     * * @param other the other point
     * * @return square distance
     */
    public double distanceSquared(Point other) {
        Double3 diff = _xyz.subtract(other._xyz);
        return diff._d1() * diff._d1() + diff._d2() * diff._d2() + diff._d3() * diff._d3();
    }

    /**
     * Calculates the distance between two points
     *
     * @param other the other point
     * @return distance
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

}

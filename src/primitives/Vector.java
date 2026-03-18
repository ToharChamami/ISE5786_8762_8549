package primitives;

/**
 * Class Vector is the basic class representing a vector in a 3D system.
 */

public final class Vector extends Point {

    public static final Vector AXIS_X = new Vector(1, 0, 0);
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructor to initialize Vector with three number values
     *
     * @throws IllegalArgumentException in case of zero vector
     */
    public Vector(double x, double y, double z) {

        super(x, y, z);
        if (_xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("Vector(0,0,0) is not allowed");
        }
    }

    /**
     * Constructor to initialize Vector with a Double3 object
     *
     * @throws IllegalArgumentException in case of zero vector
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (_xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("Vector(0,0,0) is not allowed");
        }

    }


    /**
     * Returns a string representation of the object.
     * The returned string consists of an arrow prefix ({@code ->})
     * followed by the string representation of the superclass.
     * * @return A string representing this object.
     */
    @Override
    public String toString() {
        return "->" + super.toString();
    }


    /**
     * Indicates whether some other object is "equal to" this one.
     * This implementation checks for identity, nullity, and class compatibility,
     * then delegates the equality logic to the superclass implementation.
     * * @param obj The reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument
     * based on the superclass logic; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }

    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /**
     * Scalar multiplication
     *
     * @param scalar multiplication factor
     * @return new scaled vector
     */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /**
     * Dot product between two vectors
     *
     * @param other other vector
     * @return scalar result
     */

    public double dotProduct(Vector other) {
        return _xyz._d1() * other._xyz._d1() +
                _xyz._d2() * other._xyz._d2() +
                _xyz._d3() * other._xyz._d3();
    }

    /**
     * Cross product between two vectors
     *
     * @param other other vector
     * @return new orthogonal vector
     */
    public Vector crossProduct(Vector other) {
        double x = _xyz._d2() * other._xyz._d3() - _xyz._d3() * other._xyz._d2();
        double y = _xyz._d3() * other._xyz._d1() - _xyz._d1() * other._xyz._d3();
        double z = _xyz._d1() * other._xyz._d2() - _xyz._d2() * other._xyz._d1();

        return new Vector(x, y, z);
    }

    
    public double lengthSquared() {
        return dotProduct(this);
    }

    public double length() {
        return Math.sqrt(lengthSquared());
    }

    /**
     * Normalizes the vector
     *
     * @return new normalized vector
     */
    public Vector normalize() {
        return scale(1 / length());
    }

}






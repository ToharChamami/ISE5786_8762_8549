package primitives;

/**
 * Class Vector is the basic class representing a vector in a 3D system.
 */

public final class Vector extends Point {

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
    @Override
    public String toString() {
        return "Vector: " + super.toString();
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
        return _xyz._d1() * other._xyz._d2() +
                _xyz._d2() * other._xyz._d2() +
                _xyz._d3() * other._xyz._d3();
    }


}






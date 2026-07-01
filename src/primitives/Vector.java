package primitives;

import java.util.List;

/**
 * Class Vector is the basic class representing a vector in a 3D system.
 */

public final class Vector extends Point {
    /**
     * Unit vector on the X axis
     */
    public static final Vector AXIS_X = new Vector(1, 0, 0);
    /**
     * Unit vector on the Y axis
     */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /**
     * Unit vector on the Z axis
     */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructor to initialize a vector from three coordinates.
     *
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     * @throws IllegalArgumentException if it's a zero vector
     */
    public Vector(double x, double y, double z) {
        this(new Double3(x, y, z));
    }

    /**
     * Constructor to initialize a vector from a Double3 object.
     *
     * @param xyz coordinates
     * @throws IllegalArgumentException if it's a zero vector
     */
    public Vector(Double3 xyz) {
        if (xyz.equals(Double3.ZERO)) {
            throw new IllegalArgumentException("Vector(0,0,0) is not allowed");
        }
        super(xyz);
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }

    /**
     * Adds two vectors.
     *
     * @param other the other vector
     * @return a new vector
     */
    public Vector add(Vector other) {
        return new Vector(_xyz.add(other._xyz));
    }

    /**
     * Scales a vector by a scalar.
     *
     * @param scalar the scalar value
     * @return a new vector
     */
    public Vector scale(double scalar) {
        return new Vector(_xyz.scale(scalar));
    }

    /**
     * Dot product between two vectors.
     *
     * @param other the other vector
     * @return the scalar product
     */
    public double dotProduct(Vector other) {
        return _xyz._d1() * other._xyz._d1() +
                _xyz._d2() * other._xyz._d2() +
                _xyz._d3() * other._xyz._d3();
    }

    /**
     * Cross product between two vectors.
     *
     * @param other the other vector
     * @return a new vector orthogonal to both
     */
    public Vector crossProduct(Vector other) {
        double x = _xyz._d2() * other._xyz._d3() - _xyz._d3() * other._xyz._d2();
        double y = _xyz._d3() * other._xyz._d1() - _xyz._d1() * other._xyz._d3();
        double z = _xyz._d1() * other._xyz._d2() - _xyz._d2() * other._xyz._d1();

        return new Vector(x, y, z);
    }

    /**
     * Squared length of the vector.
     *
     * @return length squared
     */
    public double lengthSquared() {
        return dotProduct(this);
    }

    /**
     * Length of the vector.
     *
     * @return length
     */
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

    /**
     * Generates an orthogonal basis for the given vector.
     * Returns a pair of vectors (vRight, vUp) such that they are orthogonal to this vector
     * and to each other.
     *
     * @return a list containing [vRight, vUp]
     */
    public List<Vector> getOrthogonalBasis() {
        Vector vTo = this.normalize();
        Vector vRight;

        // בודקים מקבילות לציר X כדי למנוע מכפלה וקטורית עם וקטור אפס
        if (Math.abs(vTo.dotProduct(Vector.AXIS_X)) < 0.8) {
            vRight = vTo.crossProduct(Vector.AXIS_X).normalize();
        } else {
            vRight = vTo.crossProduct(Vector.AXIS_Y).normalize();
        }

        Vector vUp = vRight.crossProduct(vTo).normalize();

        return List.of(vRight, vUp);
    }

}






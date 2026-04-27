package renderer;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Camera class representing the observer's location and orientation.
 * Implements the Builder pattern.
 */
public class Camera implements Cloneable {
    /**
     * Camera location
     */
    private Point _p0;
    /**
     * Vector pointing towards the scene
     */
    private Vector _vTo;
    /**
     * Vector pointing up
     */
    private Vector _vUp;
    /**
     * Vector pointing to the right
     */
    private Vector _vRight;

    /**
     * View plane width
     */
    private double _width = 0;
    /**
     * View plane height
     */
    private double _height = 0;
    /**
     * Distance from camera to the view plane
     */
    private double _distance = 0;

    /**
     * Number of pixels in the X axis (columns)
     */
    private int _nX = 1;
    /**
     * Number of pixels in the Y axis (rows)
     */
    private int _nY = 1;

    /**
     * Center point of the view plane
     */
    private Point _vpCenter;
    /**
     * Width of a single pixel
     */
    private double _pixelWidth;
    /**
     * Height of a single pixel
     */
    private double _pixelHeight;

    /**
     * Private default constructor for Camera.
     */
    private Camera() {
    }

    /**
     * Static method to get a new Builder object.
     *
     * @return a new Camera.Builder instance
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Skeleton for ray construction method
     *
     * @param nX resolution X
     * @param nY resolution Y
     * @param j  column index
     * @param i  row index
     * @return null for now
     */
    public Ray constructRay(int nX, int nY, int j, int i) {

        Point pIJ = _vpCenter;

        double xJ = (j - (nX - 1) / 2.0) * _pixelWidth;
        double yI = -(i - (nY - 1) / 2.0) * _pixelHeight;

        if (xJ != 0) {
            pIJ = pIJ.add(_vRight.scale(xJ));
        }
        if (yI != 0) {
            pIJ = pIJ.add(_vUp.scale(yI));
        }

        return new Ray(_p0, pIJ.subtract(_p0));
    }

    /**
     * Constructs a ray through a specific pixel using the camera's resolution.
     *
     * @param j column index of the pixel
     * @param i row index of the pixel
     * @return Ray from camera through the pixel center
     */
    public Ray constructRay(int j, int i) {
        return constructRay(_nX, _nY, j, i);
    }


    /**
     * Inner static class for building Camera objects.
     */
    public static class Builder {

        /**
         * The camera object being built
         */
        final private Camera _camera = new Camera();

        /**
         * Default constructor for Builder
         */
        public Builder() {
        }

        /**
         * Sets the camera's location.
         *
         * @param location the location point
         * @return the Builder object
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera's direction vectors.
         *
         * @param to vector pointing towards the view plane
         * @param up vector pointing up
         * @return the Builder object
         */
        public Builder setDirection(Vector to, Vector up) {
            _camera._vTo = to;
            _camera._vUp = up;
            return this;
        }

        /**
         * Sets the camera's target point and up vector.
         *
         * @param target the point the camera is looking at
         * @param up     vector pointing up
         * @return the Builder object
         */
        public Builder setDirection(Point target, Vector up) {
            _camera._vTo = target.subtract(_camera._p0);
            _camera._vUp = up;
            return this;
        }

        /**
         * Sets the camera's target point (default up vector is Y-axis).
         *
         * @param target the point the camera is looking at
         * @return the Builder object
         */
        public Builder setDirection(Point target) {
            _camera._vTo = target.subtract(_camera._p0);
            _camera._vUp = Vector.AXIS_Y;
            return this;
        }

        /**
         * Sets the view plane size.
         *
         * @param width  the width of the view plane
         * @param height the height of the view plane
         * @return the Builder object
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance to the view plane.
         *
         * @param distance distance from camera to view plane
         * @return the Builder object
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the resolution of the view plane.
         *
         * @param nX number of pixels in X axis
         * @param nY number of pixels in Y axis
         * @return the Builder object
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Validates all parameters and builds the camera.
         *
         * @return a new cloned Camera object
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();
            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException _) {
                return null;
            }
        }

        /**
         * Checks if resolution data is positive.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution (nX, nY) must be positive");
            }
        }

        /**
         * Checks location and direction, and computes orthogonal vectors.
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null) {
                throw new java.util.MissingResourceException("Missing camera location (p0)", "Camera", "p0");
            }
            if (_camera._vTo == null) {
                throw new java.util.MissingResourceException("Missing camera direction vector (vTo)", "Camera", "vTo");
            }
            if (_camera._vUp == null) {
                throw new java.util.MissingResourceException("Missing camera 'up' vector (vUp)", "Camera", "vUp");
            }

            _camera._vTo = _camera._vTo.normalize();

            try {
                _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
                _camera._vUp = _camera._vRight.crossProduct(_camera._vTo).normalize();
            } catch (IllegalArgumentException _) {
                throw new IllegalArgumentException("Direction vector (vTo) and Up vector (vUp) cannot be parallel");
            }
        }

        /**
         * Checks view plane data and calculates helper fields.
         */
        private void checkViewPlane() {
            if (_camera._width <= 0 || _camera._height <= 0) {
                throw new IllegalArgumentException("View Plane width and height must be positive");
            }
            if (_camera._distance <= 0) {
                throw new IllegalArgumentException("View Plane distance must be positive");
            }

            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
        }
    }

}
package renderer;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Camera class representing the observer's location and orientation.
 */
public class Camera implements Cloneable {

    private Point _p0;
    private Vector _vTo;
    private Vector _vUp;
    private Vector _vRight;

    private double _width = 0;
    private double _height = 0;
    private double _distance = 0;

    private int _nX = 1;
    private int _nY = 1;

    private Point _vpCenter;
    private double _pixelWidth;
    private double _pixelHeight;

    private Camera() {
    }

    /**
     * Static method to get a new Builder object [cite: 447, 601]
     */
    public static class Builder {

        final private Camera _camera = new Camera();

        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        public Builder setDirection(Vector to, Vector up) {
            _camera._vTo = to;
            _camera._vUp = up;
            return this;
        }

        public Builder setDirection(Point target, Vector up) {
            _camera._vTo = target.subtract(_camera._p0);
            _camera._vUp = up;
            return this;
        }

        public Builder setDirection(Point target) {
            _camera._vTo = target.subtract(_camera._p0);
            _camera._vUp = Vector.AXIS_Y;
            return this;
        }

        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

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

        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution (nX, nY) must be positive");
            }
        }

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

    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Skeleton for ray construction method [cite: 449-450, 602]
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

    // Temporary helper for CameraTests (optional if constructRay matches)
    public Ray constructRay(int j, int i) {
        return constructRay(_nX, _nY, j, i);
    }

}
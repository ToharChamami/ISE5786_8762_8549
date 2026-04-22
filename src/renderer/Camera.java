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
        return null;
    }

    // Temporary helper for CameraTests (optional if constructRay matches)
    public Ray constructRay(int j, int i) {
        return constructRay(_nX, _nY, j, i);
    }

}
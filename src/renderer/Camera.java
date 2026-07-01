package renderer;

import java.util.MissingResourceException;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * Camera class representing the observer's location and orientation.
 * Implements the Builder pattern.
 */
public class Camera implements Cloneable {

    /**
     * The image writer used to generate the image file
     */
    private ImageWriter _imageWriter;
    /**
     * The ray tracer used to calculate pixel colors
     */
    private RayTracerBase _rayTracer;
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
     * Amount of threads to use for rendering image by the camera
     */
    private int _threadsCount = 0; // 0 means no multithreading by default

    /**
     * Amount of threads to spare for Java VM threads
     */
    private static final int SPARE_THREADS = 2;

    /**
     * Debug print interval in seconds. If 0, no progress output
     */
    private double _printInterval = 0;

    /**
     * Pixel manager for supporting multi-threading and progress print
     */
    private PixelManager _pixelManager;

    // סוג הטיפוס לבחירת מצב הריצה
    public enum RenderingMode {
        NONE,       // ללא תהליכונים (קוד ישן)
        THREADS,    // תהליכונים גולמיים חכמים
        STREAMS     // זרימה מקבילית
    }

    private RenderingMode _renderingMode = RenderingMode.NONE;

    /**
     * Private default constructor for Camera.
     */
    private Camera() {
    }

    // Soft shadow settings for the Camera
    private boolean _softShadows = false;
    private renderer.sampling.TargetShape _shadowTargetShape = renderer.sampling.TargetShape.CIRCLE;
    private renderer.sampling.SamplingPattern _shadowSamplingPattern = renderer.sampling.SamplingPattern.REGULAR_GRID;
    private int _shadowSamples = 1;

    /**
     * Renders the image by casting rays for every pixel.
     * Chooses the rendering method based on threadsCount.
     *
     * @return the camera itself for chaining
     */
    public Camera renderImage() {
        // 1. בדיקת תנאי קדם (שהמצלמה מאותחלת, רזולוציה, וכו')
        // ... (הקוד הקיים שלך לבדיקות תקינות)

        // 2. אתחול מנהל הפיקסלים (בהנחה שרוחב וגובה התמונה מוגדרים ב-nX, nY)
        // הערה: נניח שזמן ההדפסה המבוקש הוא כל 1% או לפי הגדרת ה-PixelManager שקיבלתם
        long printInterval = 1000; // לדוגמה, הדפסה כל שנייה או לפי הכללים של המרצה

        // 3. ניתוב לפי המצב שנבחר
        switch (_renderingMode) {
            case NONE -> renderImageNoThreads();
            case STREAMS -> {
                _pixelManager = new PixelManager(_nY, _nX, printInterval);
                renderImageStream();
            }
            case THREADS -> {
                _pixelManager = new PixelManager(_nY, _nX, printInterval);
                renderImageRawThreads();
            }
        }

        return this;
    }

    /**
     * Render image without multi-threading
     *
     * @return the camera object itself
     */
    private Camera renderImageNoThreads() {
        for (int i = 0; i < _nY; ++i) {
            for (int j = 0; j < _nX; ++j) {
                castRay(j, i);
            }
        }
        return this;
    }

    /**
     * Render image using multi-threading by parallel streaming
     *
     * @return the camera object itself
     */
    private Camera renderImageStream() {
        java.util.stream.IntStream.range(0, _nY).parallel()
                .forEach(i -> java.util.stream.IntStream.range(0, _nX).parallel()
                        .forEach(j -> castRay(j, i)));
        return this;
    }

    /**
     * Render image using multi-threading by creating and running raw threads
     *
     * @return the camera object itself
     */
    private Camera renderImageRawThreads() {
        var threads = new java.util.LinkedList<Thread>();

        for (int i = 0; i < _threadsCount; i++) {
            threads.add(new Thread(() -> {
                PixelManager.Pixel pixel;
                while ((pixel = _pixelManager.nextPixel()) != null) {
                    castRay(pixel.col(), pixel.row());
                }
            }));
        }

        // start threads here
        for (var thread : threads) {
            thread.start();
        }

        // wait for all the threads
        try {
            for (var thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    /**
     * Casts a ray through a specific pixel and colors it.
     *
     * @param j column index
     * @param i row index
     */
    private void castRay(int j, int i) {
        Ray ray = constructRay(j, i);
        Color color = _rayTracer.traceRay(ray);
        _imageWriter.writePixel(j, i, color);

        if (_pixelManager != null) {
            _pixelManager.pixelDone();
        }
    }

    /**
     * Prints a grid on the image.
     *
     * @param interval the grid interval
     * @param color    the grid color
     * @return the camera itself for chaining
     */
    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; ++i) {
            for (int j = 0; j < _nX; ++j) {
                if (i % interval == 0 || j % interval == 0) {
                    _imageWriter.writePixel(j, i, color);
                }
            }
        }
        return this;
    }

    /**
     * Delegates writing the image to the ImageWriter.
     *
     * @param imageName the name of the file to save the image as
     */
    public void writeToImage(String imageName) {
        _imageWriter.writeToImage(imageName);
    }

    /**
     * Returns the ray tracer used by the camera.
     *
     * @return the current ray tracer instance
     */
    public RayTracerBase getRayTracer() {
        return this._rayTracer;
    }

    @Override
    public Camera clone() {
        try {
            Camera cloned = (Camera) super.clone();
            if (this._rayTracer != null) {
                cloned._rayTracer = this._rayTracer;
            }
            if (this._imageWriter != null) {
                cloned._imageWriter = this._imageWriter;
            }
            return cloned;
        } catch (Exception e) {
            throw new AssertionError("Camera clone failed", e);
        }
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
     * Constructs a ray passing through the center of a specific pixel on the view plane.
     *
     * @param j column index of the pixel
     * @param i row index of the pixel
     * @return Ray from the camera's origin through the pixel center
     */
    public Ray constructRay(int j, int i) {
        Point pIJ = _vpCenter;

        double xJ = (j - (_nX - 1) / 2.0) * _pixelWidth;
        double yI = -(i - (_nY - 1) / 2.0) * _pixelHeight;

        if (!primitives.Util.isZero(xJ)) {
            pIJ = pIJ.add(_vRight.scale(xJ));
        }
        if (!primitives.Util.isZero(yI)) {
            pIJ = pIJ.add(_vUp.scale(yI));
        }

        return new Ray(_p0, pIJ.subtract(_p0));
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
         * Vector pointing towards the scene
         */
        private Vector _vTo = null;
        /**
         * Target point for the camera to look at
         */
        private Point _target = null;
        /**
         * Vector pointing up
         */
        private Vector _vUp = Vector.AXIS_Y;

        /**
         * Default constructor for Builder
         */
        public Builder() {
        }

        public Builder setRenderingMode(RenderingMode mode) {
            this._target._renderingMode = mode;
            return this;
        }

        public Builder setThreadsCount(int threadsCount) {
            if (threadsCount < 1) {
                throw new IllegalArgumentException("Threads count must be at least 1");
            }
            this._target._threadsCount = threadsCount;
            return this;
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
            this._vTo = to;
            this._vUp = up;
            this._target = null;
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
            this._target = target;
            this._vUp = up;
            this._vTo = null;
            return this;
        }

        /**
         * Sets the camera's target point (default up vector is Y-axis).
         *
         * @param target the point the camera is looking at
         * @return the Builder object
         */
        public Builder setDirection(Point target) {
            this._target = target;
            this._vTo = null;
            this._vUp = Vector.AXIS_Y;
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
         * Set multi-threading.
         *
         * @param threads number of threads.
         *                -2 = logical processors less SPARE_THREADS
         *                -1 = stream processing parallelization
         *                0 = no multi-threading
         * @return builder object itself
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2) {
                throw new IllegalArgumentException("Multithreading parameter must be -2 or higher");
            }
            if (threads == -2) {
                int cores = Runtime.getRuntime().availableProcessors() - SPARE_THREADS;
                _camera._threadsCount = cores <= 2 ? 1 : cores;
            } else {
                _camera._threadsCount = threads;
            }
            return this;
        }

        /**
         * Enables or disables soft shadows for the camera's ray tracer.
         *
         * @param softShadows true to enable soft shadows, false for hard shadows
         * @return the Builder object
         */
        public Builder setSoftShadows(boolean softShadows) {
            _camera._softShadows = softShadows;
            return this;
        }

        /**
         * Sets the target shape used for soft shadow sampling.
         *
         * @param shape the shadow target shape (SQUARE or CIRCLE)
         * @return the Builder object
         */
        public Builder setShadowTargetShape(renderer.sampling.TargetShape shape) {
            _camera._shadowTargetShape = shape;
            return this;
        }

        /**
         * Sets the sampling pattern used for soft shadow sampling.
         *
         * @param pattern the sampling pattern (REGULAR_GRID or JITTERED_GRID)
         * @return the Builder object
         */
        public Builder setShadowSamplingPattern(renderer.sampling.SamplingPattern pattern) {
            _camera._shadowSamplingPattern = pattern;
            return this;
        }

        /**
         * Sets the grid size (number of samples per axis) used for soft shadow sampling.
         *
         * @param gridSize the sampling grid size
         * @return the Builder object
         */
        public Builder setShadowSamples(int gridSize) {
            _camera._shadowSamples = gridSize;
            return this;
        }

        /**
         * Set debug printing interval. If it's zero, there won't be printing at all.
         *
         * @param interval printing interval in seconds
         * @return builder object itself
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("interval parameter must be non-negative");
            }
            _camera._printInterval = interval;
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

            if (_camera._rayTracer == null) {
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);
            }
            if (_camera._rayTracer instanceof SimpleRayTracer tracer) {
                tracer.setSoftShadows(_camera._softShadows)
                        .setShadowTargetShape(_camera._shadowTargetShape)
                        .setShadowSamplingPattern(_camera._shadowSamplingPattern)
                        .setShadowSamples(_camera._shadowSamples);
            }
            try {
                return _camera.clone();
            } catch (Exception _) {
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
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Checks location and direction, and computes orthogonal vectors.
         */
        private void checkLocationAndDirection() {

            if (_camera._p0 == null) {
                throw new MissingResourceException("Missing camera location", "Camera", "Location");
            }

            if (_vTo == null && _target != null) {
                _vTo = _target.subtract(_camera._p0);
            }

            if (_vTo == null || _vUp == null) {
                throw new MissingResourceException("Missing camera direction", "Camera", "Direction");
            }

            _camera._vTo = _vTo.normalize();
            _camera._vUp = _vUp.normalize();

            if (!primitives.Util.isZero(_camera._vTo.dotProduct(_camera._vUp))) {
                throw new IllegalArgumentException("vTo and vUp are not orthogonal");
            }

            _camera._vRight = _camera._vTo.crossProduct(_camera._vUp).normalize();
        }

        /**
         * Checks view plane data and calculates helper fields.
         */
        private void checkViewPlane() {

            if (alignZero(_camera._width) <= 0 || alignZero(_camera._height) <= 0) {
                throw new IllegalArgumentException("View Plane width and height must be positive");
            }
            if (alignZero(_camera._distance) <= 0) {
                throw new IllegalArgumentException("View Plane distance must be positive");
            }

            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
        }

        /**
         * Sets the ray tracer for the camera.
         *
         * @param scene the scene to render
         * @param type  the type of ray tracer to use
         * @return the Builder object
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            if (type == RayTracerType.SIMPLE) {
                _camera._rayTracer = new SimpleRayTracer(scene);
            } else {
                throw new IllegalArgumentException("Unsupported RayTracerType");
            }
            return this;
        }
    }

}
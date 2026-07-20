package renderer;

import java.util.MissingResourceException;
import java.util.stream.IntStream;
import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static primitives.Util.alignZero;

/**
 * Camera class representing the observer's location and orientation in 3D space.
 * Implements the Builder design pattern for structured initialization and parameter validation.
 */
public class Camera implements Cloneable {

    /**
     * The image writer used to generate and handle the output image file.
     */
    private ImageWriter _imageWriter;

    /**
     * The ray tracer scene processor used to calculate individual pixel colors.
     */
    private RayTracerBase _rayTracer;

    /**
     * Camera core physical location position point in 3D coordinate space.
     */
    private Point _p0;

    /**
     * Forward vector pointing directly towards the view plane scene center.
     */
    private Vector _vTo;

    /**
     * Upward orientation vector pointing to the local top sky frame boundary.
     */
    private Vector _vUp;

    /**
     * Rightward orientation vector calculated orthogonally from the to and up axes.
     */
    private Vector _vRight;

    /**
     * Absolute width dimension allocated to the virtual view plane layer.
     */
    private double _width = 0;

    /**
     * Absolute height dimension allocated to the virtual view plane layer.
     */
    private double _height = 0;

    /**
     * Distance length separating the camera focal center point from the view plane.
     */
    private double _distance = 0;

    /**
     * Total number of discrete pixels spanning along the horizontal X axis.
     */
    private int _nX = 1;

    /**
     * Total number of discrete pixels spanning along the vertical Y axis.
     */
    private int _nY = 1;

    /**
     * Absolute central spatial point coordinate of the projected view plane grid.
     */
    private Point _vpCenter;

    /**
     * Individual width step factor calculated for a single horizontal pixel block.
     */
    private double _pixelWidth;

    /**
     * Individual height step factor calculated for a single vertical pixel block.
     */
    private double _pixelHeight;

    /**
     * Total runtime thread count allocated to process the active image loop.
     */
    private int _threadsCount = 0;

    /**
     * Amount of background operational threads reserved exclusively for JVM management tasks.
     */
    private static final int SPARE_THREADS = 2;

    /**
     * Active debug logging print refresh time threshold interval parameter measured in seconds.
     */
    private double _printInterval = 0;

    /**
     * Internal pixel indexing progress coordinator used to safe-sync thread tasks.
     */
    private PixelManager _pixelManager;

    /**
     * Soft shadow global toggle determining if distributed ray bundling is applied.
     */
    private boolean _softShadows = false;

    /**
     * Geometric bounding grid boundary profile template allocated for shadow calculations.
     */
    private renderer.sampling.TargetShape _shadowTargetShape = renderer.sampling.TargetShape.CIRCLE;

    /**
     * Spatial arrangement configuration strategy used to plot shadow grid target arrays.
     */
    private renderer.sampling.SamplingPattern _shadowSamplingPattern = renderer.sampling.SamplingPattern.REGULAR_GRID;

    /**
     * Total number of sample rays cast into light source bounding vectors for soft shadowing.
     */
    private int _shadowSamples = 1;

    /**
     * Private default constructor for Camera.
     * Prevents direct instance creations outside the associated static builder workflow.
     */
    private Camera() {
    }

    /**
     * Renders the complete scene image by triggering rays through every individual view plane pixel.
     * Selects and delegates processing loops based on the configured concurrency thread strategies.
     *
     * @return The current {@code Camera} instance for chained operational setups.
     */
    public Camera renderImage() {
        _pixelManager = new PixelManager(_nY, _nX, _printInterval);

        return switch (_threadsCount) {
            case 0 -> renderImageNoThreads();
            case -1 -> renderImageStream();
            default -> renderImageRawThreads();
        };
    }

    /**
     * Render engine fallback processing pixel loops sequentially inside the primary caller thread.
     *
     * @return The current {@code Camera} instance reference.
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
     * Render engine processing image grids concurrently utilizing Java parallel stream pipelines.
     *
     * @return The current {@code Camera} instance reference.
     */
    private Camera renderImageStream() {
        IntStream.range(0, _nY).parallel()
                .forEach(i -> IntStream.range(0, _nX).parallel()
                        .forEach(j -> castRay(j, i)));
        return this;
    }

    /**
     * Render engine processing image grids using raw low-level synchronized thread workers.
     *
     * @return The current {@code Camera} instance reference.
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

        for (var thread : threads) {
            thread.start();
        }

        try {
            for (var thread : threads) {
                thread.join();
            }
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    /**
     * Casts a generated ray through a specific pixel center coordinate and updates the image target color.
     *
     * @param j Horizontal column pixel element coordinate index.
     * @param i Vertical row pixel element coordinate index.
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
     * Prints a visual validation orientation grid overlay across the output image boundaries.
     *
     * @param interval Step sequence interval defining row and column grid spacing lines.
     * @param color    The designated color factor value painted onto the line markers.
     * @return The current {@code Camera} instance reference for call chaining.
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
     * Triggers the internal image writer to commit and serialize data fields to a physical image file.
     *
     * @param imageName The file name identification string assigned to the generated output graphic.
     */
    public void writeToImage(String imageName) {
        _imageWriter.writeToImage(imageName);
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
     * Instantiates a clean setup instance of the Camera Builder helper tool.
     *
     * @return A new {@code Camera.Builder} instance.
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Constructs a calculated viewing ray passing directly through the geometric center of a designated pixel element.
     *
     * @param j Horizontal column grid position index.
     * @param i Vertical row grid position index.
     * @return A new {@code Ray} starting from the camera location and passing through the targeted pixel midpoints.
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
     * Handles parameter gathering, coordinate checks, and structural orthogonality validations.
     */
    public static class Builder {

        /**
         * The configuration camera target instance being assembled by the builder.
         */
        private final Camera _camera = new Camera();

        /**
         * Temporary storage for the forward target orientation vector.
         */
        private Vector _vTo = null;

        /**
         * Target coordinate spot position point the camera is designated to track.
         */
        private Point _target = null;

        /**
         * Temporary storage for the upward vertical orientation vector.
         */
        private Vector _vUp = Vector.AXIS_Y;

        /**
         * Default constructor for Builder.
         * Sets up standard uninitialized parameters ready for properties population.
         */
        public Builder() {
        }

        /**
         * Sets the physical location coordinates for the camera focus center point.
         *
         * @param location The coordinate spot point representing the camera base positioning.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera directional tracking parameters using absolute forward and up vector guides.
         *
         * @param to Vector indicating the forward view direction axis path.
         * @param up Vector indicating the upward sky alignment axis path.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setDirection(Vector to, Vector up) {
            this._vTo = to;
            this._vUp = up;
            this._target = null;
            return this;
        }

        /**
         * Sets camera directional vectors automatically based on a fixed spatial coordinate point target.
         *
         * @param target Geometric destination point position to align the lens vectors towards.
         * @param up     Vector indicating the upward sky alignment axis path.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setDirection(Point target, Vector up) {
            this._target = target;
            this._vUp = up;
            this._vTo = null;
            return this;
        }

        /**
         * Sets camera direction fields towards a specific point using a default vertical Y-axis vector guide.
         *
         * @param target Geometric destination point position to align the lens vectors towards.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setDirection(Point target) {
            this._target = target;
            this._vTo = null;
            this._vUp = Vector.AXIS_Y;
            return this;
        }

        /**
         * Sets the absolute physical size dimensions assigned to the virtual view plane grid.
         *
         * @param width  Horizontal length width boundary value.
         * @param height Vertical length height boundary value.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the distance length threshold separating the camera origin from the view plane surface.
         *
         * @param distance Numerical distance dimension value.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the image matrix resolution split parameters (total pixel counts across major layout axes).
         *
         * @param nX Horizontal coordinate total pixel rows.
         * @param nY Vertical coordinate total pixel rows.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Sets the thread execution counts and model settings for handling image processing tasks.
         *
         * @param threads Multi-thread allocation parameter indicator code (-2 for auto, -1 for streams, 0 for single).
         * @return The active {@code Builder} instance reference.
         * @throws IllegalArgumentException If the input parameter value falls below -2.
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
         * Enables or disables soft shadow calculations within the active ray tracing framework.
         *
         * @param softShadows {@code true} to activate soft shadows, {@code false} for default sharp shadows.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setSoftShadows(boolean softShadows) {
            _camera._softShadows = softShadows;
            return this;
        }

        /**
         * Sets the geometric layout target sampling matrix profile used to calculate soft shading.
         *
         * @param shape Designated sampling geometric shape structure (SQUARE or CIRCLE).
         * @return The active {@code Builder} instance reference.
         */
        public Builder setShadowTargetShape(renderer.sampling.TargetShape shape) {
            _camera._shadowTargetShape = shape;
            return this;
        }

        /**
         * Sets the distribution algorithm strategy configuration used to drop rays over soft shadow planes.
         *
         * @param pattern The active grid distribution configuration pattern (REGULAR_GRID or JITTERED_GRID).
         * @return The active {@code Builder} instance reference.
         */
        public Builder setShadowSamplingPattern(renderer.sampling.SamplingPattern pattern) {
            _camera._shadowSamplingPattern = pattern;
            return this;
        }

        /**
         * Sets the maximum amount of individual bundle rays cast per light profile area to calculate soft shadow boundaries.
         *
         * @param gridSize Sample count density step value.
         * @return The active {@code Builder} instance reference.
         */
        public Builder setShadowSamples(int gridSize) {
            _camera._shadowSamples = gridSize;
            return this;
        }

        /**
         * Sets the print step logging timeout parameters used to output console render status indicators.
         *
         * @param interval Time tracking gap threshold input measured in seconds.
         * @return The active {@code Builder} instance reference.
         * @throws IllegalArgumentException If the provided interval parameter value drops below zero.
         */
        public Builder setDebugPrint(double interval) {
            if (interval < 0) {
                throw new IllegalArgumentException("interval parameter must be non-negative");
            }
            _camera._printInterval = interval;
            return this;
        }

        /**
         * Validates all gathered field inputs, processes geometric alignments, and instantiates the compiled camera model.
         *
         * @return A validated, cloned {@code Camera} instance configuration block, or {@code null} if initialization fails.
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
         * Checks if the resolution fields are valid and initializes the interior ImageWriter dependency.
         *
         * @throws IllegalArgumentException If horizontal or vertical pixel dimensions drop below or equal 0.
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0) {
                throw new IllegalArgumentException("Resolution (nX, nY) must be positive");
            }
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
        }

        /**
         * Evaluates camera position points and ensures direction tracking vectors maintain accurate 90-degree orthogonal states.
         *
         * @throws MissingResourceException If the baseline position parameters or targeted vector parameters are omitted.
         * @throws IllegalArgumentException If the direction vectors are parallel or fail mutual orthogonality parameters.
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
         * Validates view plane dimensional measurements and pre-computes operational pixel width and height scaling factors.
         *
         * @throws IllegalArgumentException If plane size bounds or focal distance entries evaluate to zero or lower.
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
         * Sets up and attaches a specific ray tracing scene execution engine template to the active camera.
         *
         * @param scene The global environment data matrix tracking objects and lighting maps.
         * @param type  The target tracer algorithm structural identifier code.
         * @return The active {@code Builder} instance reference.
         * @throws IllegalArgumentException If the input type parameter does not map to recognized engine variations.
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
package scene;

import geometries.impl.Geometries;
import java.util.ArrayList;
import java.util.List;
import lighting.AmbientLight;
import lighting.LightSource;
import primitives.Color;

/**
 * Class representing a complete scene.
 * This class is a Plain Data Structure (PDS).
 */
public class Scene {
    /**
     * The name of the scene
     */
    public final String name;
    /**
     * The background color of the scene
     */
    public Color background = Color.BLACK;
    /**
     * The ambient light of the scene
     */
    public AmbientLight ambientLight = AmbientLight.NONE;
    /**
     * The geometries in the scene
     */
    public Geometries geometries = new Geometries();

    /**
     * The external light sources in the scene
     */
    public List<LightSource> lights = new ArrayList<>();

    /**
     * Constructor that accepts only the scene name.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param background the background color
     * @return the current Scene object (this) for method chaining
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     *
     * @param ambientLight the ambient light
     * @return the current Scene object (this) for method chaining
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometries in the scene.
     *
     * @param geometries the geometries collection
     * @return the current Scene object (this) for method chaining
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }

}
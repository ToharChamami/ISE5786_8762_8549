package primitives;

/**
 * PDS class representing the material of a geometry.
 */
public class Material {
    /**
     * Ambient light attenuation factor
     */
    public Double3 kA = Double3.ONE;

    /**
     * Default constructor to satisfy JavaDoc
     */
    public Material() {
    }

    /**
     * Setter for kA using Double3.
     *
     * @param kA the attenuation factor
     * @return the Material object itself for chaining
     */
    public Material setKA(Double3 kA) {
        this.kA = kA;
        return this;
    }

    /**
     * Setter for kA using a single double.
     *
     * @param kA the attenuation factor for all 3 components
     * @return the Material object itself for chaining
     */
    public Material setKA(double kA) {
        this.kA = new Double3(kA);
        return this;
    }
}
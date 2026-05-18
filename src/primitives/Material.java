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
     * Diffuse attenuation factor
     */
    public Double3 kD = Double3.ZERO;

    /**
     * Specular attenuation factor
     */
    public Double3 kS = Double3.ZERO;

    /**
     * Shininess level of the material
     */
    public int nShininess = 0;

    /**
     * Setter for kD using a Double3.
     *
     * @param kD diffuse attenuation factor
     * @return the Material object itself for chaining
     */
    public Material setKD(Double3 kD) {
        this.kD = kD;
        return this;
    }

    /**
     * Setter for kD using a single double.
     *
     * @param kD diffuse attenuation factor
     * @return the Material object itself for chaining
     */
    public Material setKD(double kD) {
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Setter for kS using a Double3.
     *
     * @param kS specular attenuation factor
     * @return the Material object itself for chaining
     */
    public Material setKS(Double3 kS) {
        this.kS = kS;
        return this;
    }

    /**
     * Setter for kS using a single double.
     *
     * @param kS specular attenuation factor
     * @return the Material object itself for chaining
     */
    public Material setKS(double kS) {
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Setter for nShininess.
     *
     * @param nShininess the shininess level
     * @return the Material object itself for chaining
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
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
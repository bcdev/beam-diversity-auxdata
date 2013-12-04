package org.esa.beam.operator.mph_chl;

class MaxResult {
    private double reflectance;
    private double wavelength;

    public void setReflectance(double reflectance) {
        this.reflectance = reflectance;
    }

    public double getReflectance() {
        return reflectance;
    }

    public void setWavelength(double wavelength) {
        this.wavelength = wavelength;
    }

    public double getWavelength() {
        return wavelength;
    }
}

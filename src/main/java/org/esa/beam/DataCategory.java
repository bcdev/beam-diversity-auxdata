package org.esa.beam;

/**
 * Enum indicating category of data to process
 *
 * @author olafd
 */
public enum DataCategory {
    NDVI("NDVI"),
    NDVI_MAXCOMPOSIT("NDVI_MAXCOMPOSIT"),
    TRMM("TRMM"),
    CMAP("CMAP"),
    SOIL_MOISTURE("SOIL_MOISTURE"),
    ACTUAL_EVAPOTRANSPIRATION("ACTUAL_EVAPOTRANSPIRATION"),
    AIR_TEMPERATURE("AIR_TEMPERATURE");

    private final String label;

    private DataCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

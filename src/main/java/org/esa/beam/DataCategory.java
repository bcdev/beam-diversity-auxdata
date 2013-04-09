package org.esa.beam;

/**
 * Enum indicating category of data to process
 *
 * @author olafd
 */
public enum DataCategory {
    NDVI("NDVI"),
    NDVI_NEW("NDVI_NEW"),
    GLOBVEG("GLOBVEG"),
    NDVI_MAXCOMPOSIT("NDVI_MAXCOMPOSIT"),
    NDVI_MAXCOMPOSIT_NEW("NDVI_MAXCOMPOSIT_NEW"),
    TRMM_BIWEEKLY("TRMM_BIWEEKLY"),
    TRMM_YEARLY("TRMM_YEARLY"),
    GPCP("GPCP"),
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

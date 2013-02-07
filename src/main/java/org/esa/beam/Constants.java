package org.esa.beam;

/**
 * Diversity auxdata constants
 *
 * @author olafd
 */
public class Constants {

    public static final String NDVI_FLAG_NAME = "NDVI_FLAG";

    public static final String NDVI_GOOD_VALUE_FLAG_NAME = "GOOD_VALUE";
    public static final String NDVI_GOOD_VALUE_POSSIBLY_SNOW_FLAG_NAME = "GOOD_VALUE_POSSIBLY_SNOW";
    public static final String NDVI_NDVI_FROM_SPLINE_FLAG_NAME = "NDVI_FROM_SPLINE";
    public static final String NDVI_NDVI_FROM_SPLINE_POSSIBLY_SNOW_FLAG_NAME = "NDVI_FROM_SPLINE_POSSIBLY_SNOW";
    public static final String NDVI_NDVI_FROM_AVERAGE_SEASONAL_PROFILE_FLAG_NAME = "NDVI_FROM_AVERAGE_SEASONAL_PROFILE";
    public static final String NDVI_NDVI_FROM_AVERAGE_SEASONAL_PROFILE_POSSIBLY_SNOW_FLAG_NAME = "NDVI_FROM_AVERAGE_SEASONAL_PROFILE_POSSIBLY_SNOW";
    public static final String NDVI_MISSING_DATA_FLAG_NAME = "MISSING_DATA";

    public static final int GOOD_VALUE = 0;
    public static final int GOOD_VALUE_POSSIBLY_SNOW = 1;
    public static final int NDVI_FROM_SPLINE = 2;
    public static final int NDVI_FROM_SPLINE_POSSIBLY_SNOW = 3;
    public static final int NDVI_FROM_AVERAGE_SEASONAL_PROFILE = 4;
    public static final int NDVI_FROM_AVERAGE_SEASONAL_PROFILE_POSSIBLY_SNOW = 5;
    public static final int MISSING_DATA = 6;

    public static final String[] MONTHS =
            {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    public static final String[] CMAP_MONTHS =
            {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};

    public static final int NDVI_INVALID_VALUE = -10000;
    public static final int NDVI_MISSING_DATA_VALUE = -199;

    public static final int NDVI_REFERENCE_GRID_WIDTH = 4950;
    public static final int NDVI_REFERENCE_GRID_HEIGHT = 2091;

    public static final double NDVI_REFERENCE_GRID_ZEROLATLON_X = 2475.0;
    public static final double NDVI_REFERENCE_GRID_ZEROLATLON_Y = 1226.0;   // note that this is NOT GRID_HEIGHT/2

    public static final String[] BIWEEKLY_START_DATES =
            {"0101", "0116", "0201", "0215", "0301", "0316", "0401", "0416", "0501", "0516", "0601", "0616",
             "0701", "0716", "0801", "0816", "0901", "0916", "1001", "1016", "1101", "1116", "1201", "1216"};

    public static final String[] BIWEEKLY_END_DATES =
            {"0105", "0131", "0214", "0229", "0315", "0331", "0415", "0430", "0515", "0531", "0615", "0630",
                    "0715", "0731", "0815", "0831", "0915", "0930", "1015", "1031", "1115", "1130", "1215", "1231"};

    public static final String[] HALFMONTHS =
            {"jan_a", "jan_b", "feb_a", "feb_b", "mar_a", "mar_b", "apr_a", "apr_b", "may_a", "may_b", "jun_a", "jun_b",
                    "jul_a", "jul_b", "aug_a", "aug_b", "sep_a", "sep_b", "oct_a", "oct_b", "nov_a", "nov_b", "dec_a", "dec_b"};

    public static final double TRMM_INVALID_VALUE = -10000.0;
    public static final double SM_INVALID_VALUE = -1.0;


}

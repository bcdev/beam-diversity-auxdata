package org.esa.beam;

import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Diversity auxdata utility class
 *
 * @author olafd
 */
public class DiversityAuxdataUtils {

    public static FlagCoding createNdviFlagCoding() {
        FlagCoding ndviFC = new FlagCoding(Constants.NDVI_FLAG_NAME);

        ndviFC.addFlag(Constants.NDVI_GOOD_VALUE_FLAG_NAME,
                       Constants.GOOD_VALUE,
                       "Good value");
        ndviFC.addFlag(Constants.NDVI_GOOD_VALUE_POSSIBLY_SNOW_FLAG_NAME,
                       Constants.GOOD_VALUE_POSSIBLY_SNOW,
                       "Good value, possibly snow");
        ndviFC.addFlag(Constants.NDVI_NDVI_FROM_SPLINE_FLAG_NAME,
                       Constants.NDVI_FROM_SPLINE,
                       "NDVI retrived from spline interpolation");
        ndviFC.addFlag(Constants.NDVI_NDVI_FROM_SPLINE_POSSIBLY_SNOW_FLAG_NAME,
                       Constants.NDVI_FROM_SPLINE_POSSIBLY_SNOW,
                       "NDVI retrived from spline interpolation, possibly snow");
        ndviFC.addFlag(Constants.NDVI_NDVI_FROM_AVERAGE_SEASONAL_PROFILE_FLAG_NAME,
                       Constants.NDVI_FROM_AVERAGE_SEASONAL_PROFILE,
                       "NDVI retrieved from average seasonal profile");
        ndviFC.addFlag(Constants.NDVI_NDVI_FROM_AVERAGE_SEASONAL_PROFILE_POSSIBLY_SNOW_FLAG_NAME,
                       Constants.NDVI_FROM_AVERAGE_SEASONAL_PROFILE_POSSIBLY_SNOW,
                       "NDVI retrieved from average seasonal profile, possibly snow");
        ndviFC.addFlag(Constants.NDVI_MISSING_DATA_FLAG_NAME,
                       Constants.MISSING_DATA,
                       "missing data");

        return ndviFC;
    }


    public static void addPatternToAutoGrouping(Product targetProduct, String groupPattern) {
        Product.AutoGrouping autoGrouping = targetProduct.getAutoGrouping();
        String stringPattern = autoGrouping != null ? autoGrouping.toString() + ":" + groupPattern : groupPattern;
        targetProduct.setAutoGrouping(stringPattern);
    }

    public static Product[] sortNdviProductsByMonth(Product[] products, String filter) {
        // we have e.g.
//        06apr15a.n17-VIg
//        06apr15b.n17-VIg
//        06aug15a.n17-VIg
        // we want
//        06jan15a.n17-VIg
//        06jan15b.n17-VIg
//        06feb15a.n17-VIg
//        etc.
        List<Product> sortedProductsList = new ArrayList<Product>();
        for (int i = 0; i < 2*Constants.MONTHS.length; i++) {
            sortedProductsList.add(new Product("dummy", "dummy", 0, 0));
        }

        for (Product product : products) {
            if (product != null && (filter == null || product.getName().contains(filter))) {
                final String monthNameSubstring = product.getName().substring(2, 5);  // e.g. "jan"
                final char monthHalfChar = product.getName().charAt(7);  // 'a' or 'b'
                for (int i = 0; i < Constants.MONTHS.length; i++) {
                    if (monthNameSubstring.equals(Constants.MONTHS[i])) {
                        if (monthHalfChar == 'a') {
                            sortedProductsList.set(2 * i, product);
                        } else if (monthHalfChar == 'b') {
                            sortedProductsList.set(2 * i + 1, product);
                        }
                    }
                }
            }
        }

        for (int i = sortedProductsList.size()-1; i>=0; i--) {
            Product product = sortedProductsList.get(i);
            if (product.getName().equals("dummy")) {
                sortedProductsList.remove(product);
            }
        }

        return sortedProductsList.toArray(new Product[sortedProductsList.size()]);
    }

    public static Product[] sortSoilMoistureProductsByMonth(Product[] products, String filter) {
        // we have e.g.
//        DIVERSITY_SM_BIWEEKLY_jan_b
//        DIVERSITY_SM_BIWEEKLY_feb_b
//        DIVERSITY_SM_BIWEEKLY_jan_a
        // we want
//        DIVERSITY_SM_BIWEEKLY_jan_a
//        DIVERSITY_SM_BIWEEKLY_jan_b
//        DIVERSITY_SM_BIWEEKLY_feb_a
//        etc.
        List<Product> sortedProductsList = new ArrayList<Product>();
        for (int i = 0; i < 2*Constants.MONTHS.length; i++) {
            sortedProductsList.add(new Product("dummy", "dummy", 0, 0));
        }

        for (Product product : products) {
            if (product != null && (filter == null || product.getName().contains(filter))) {
                final String monthNameSubstring = product.getName().substring(22, 25);  // e.g. "jan"
                final char monthHalfChar = product.getName().charAt(26);  // 'a' or 'b'
                for (int i = 0; i < Constants.MONTHS.length; i++) {
                    if (monthNameSubstring.equals(Constants.MONTHS[i])) {
                        if (monthHalfChar == 'a') {
                            sortedProductsList.set(2 * i, product);
                        } else if (monthHalfChar == 'b') {
                            sortedProductsList.set(2 * i + 1, product);
                        }
                    }
                }
            }
        }

        for (int i = sortedProductsList.size()-1; i>=0; i--) {
            Product product = sortedProductsList.get(i);
            if (product.getName().equals("dummy")) {
                sortedProductsList.remove(product);
            }
        }

        return sortedProductsList.toArray(new Product[sortedProductsList.size()]);
    }


    public static boolean isDateWithinPeriod(Date startDate, Date endDate, Date dateToCheck) {
        return !(dateToCheck.before(startDate) || dateToCheck.after(endDate));
    }

}

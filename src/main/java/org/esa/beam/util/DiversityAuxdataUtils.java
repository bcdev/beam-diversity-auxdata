package org.esa.beam.util;

import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Diversity auxdata utility class
 *
 * @author olafd
 */
public class DiversityAuxdataUtils {

    public static final int NUM_8_DAY_PERIODS_IN_YEAR = 46;
    public static final int NUM_PENTAD_PERIODS_IN_YEAR = 73;

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

    /**
     * Sorts an array of products by month. Products must have same name except their month indicator (MMM_a or MMM_b)
     *
     * @param products         - the unsorted array
     * @param filter           - a filter substring which the product name must contain
     * @param monthCharPos     - position where MMM starts in product name (e.g. 24 for the 'j' in 'DIVERSITY_TRMM_BIWEEKLY_jan_b')
     * @param halfMonthCharPos - position where month suffix 'a' or 'b' starts in product name
     *                         (e.g. 27 for the 'b' in 'DIVERSITY_TRMM_BIWEEKLY_jan_b')
     * @return the sorted array
     */
    public static Product[] sortProductsByMonth(Product[] products, String filter, int monthCharPos, int halfMonthCharPos) {
        List<Product> sortedProductsList = new ArrayList<Product>();
        for (int i = 0; i < 2 * Constants.MONTHS.length; i++) {
            sortedProductsList.add(new Product("dummy", "dummy", 0, 0));
        }

        for (Product product : products) {
            if (product != null && (filter == null || product.getName().contains(filter))) {
                final String monthNameSubstring = product.getName().substring(monthCharPos, monthCharPos + 3);  // e.g. "jan"
                final char monthHalfChar = product.getName().charAt(halfMonthCharPos);  // 'a' or 'b'
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

        for (int i = sortedProductsList.size() - 1; i >= 0; i--) {
            Product product = sortedProductsList.get(i);
            if (product.getName().equals("dummy")) {
                sortedProductsList.remove(product);
            }
        }

        return sortedProductsList.toArray(new Product[sortedProductsList.size()]);
    }

    public static Product[] sortAirTempProductsByMonthIndex(Product[] products) {
        List<Product> sortedProductsList = new ArrayList<Product>();
        for (int i = 0; i < Constants.MONTHS.length; i++) {
            sortedProductsList.add(new Product("dummy", "dummy", 0, 0));
        }

        // t012006.tif
        for (Product product : products) {
            if (product != null) {
                final int productMonth = Integer.parseInt(product.getName().substring(1, 3)) - 1;
                for (int i = 0; i < Constants.MONTHS.length; i++) {
                    if (productMonth == i) {
                        sortedProductsList.set(i, product);
                    }
                }
            }
        }

        for (int i = sortedProductsList.size()-1; i >= 0; i--) {
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

    public static SubBiweeklyProductFraction get8DayProductFractionsForBiweeklyPeriods(String biweeklyStartDate,
                                                                                       String biweeklyEndDate) {

        Calendar cal = getCalendar(biweeklyStartDate, biweeklyEndDate);

        int startDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int endDayOfYear = cal.get(Calendar.DAY_OF_YEAR);


        SubBiweeklyProductFraction result = new SubBiweeklyProductFraction();
        List<String> product8DayIdentifiers = new ArrayList<String>();
        List<Double> productFractions = new ArrayList<Double>();

        for (int i = 0; i < NUM_8_DAY_PERIODS_IN_YEAR; i++) {
            int startDayof8DayPeriod = 8 * i + 1;
            int startDiff = startDayOfYear - startDayof8DayPeriod;
            int endDiff = startDayof8DayPeriod + 16 - endDayOfYear - 1;

            if (startDiff == 0 && endDiff == 0) {
                // exactly fitting, no side overlaps, two fractions == 0.5
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod));
                productFractions.add(0.5);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod + 8));
                productFractions.add(0.5);
                break;
            } else if (startDiff == 0 && endDiff > 0) {
                // only right overlap, 2 fractions
                final double norm = 8.0 + (8.0 - endDiff);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod));
                productFractions.add(8.0 / norm);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod + 8));
                productFractions.add((8.0 - endDiff) / norm);
                break;
            } else if (startDiff > 0 && endDiff == 0) {
                // only left overlap, 2 fractions
                final double norm = (8.0 - startDiff) + 8.0;
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod));
                productFractions.add((8.0 - startDiff) / norm);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod + 8));
                productFractions.add(8.0 / norm);
                break;
            } else if (startDiff < 0 && startDiff > -8 && endDiff > 0 && endDiff < 8) {
                // left and right overlap, 3 fractions
                final double norm = 8.0 - startDiff + (8.0 - endDiff);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod - 8));
                productFractions.add(-startDiff / norm);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod));
                productFractions.add(8.0 / norm);
                product8DayIdentifiers.add(String.format("%03d", startDayof8DayPeriod + 8));
                productFractions.add((8.0 - endDiff) / norm);
                break;
            }
        }

        result.setSubPeriodStartDoys(product8DayIdentifiers.toArray(new String[product8DayIdentifiers.size()]));
        result.setFractionsOfBiweeklyPeriod(productFractions.toArray(new Double[productFractions.size()]));

        return result;
    }

    // todo: if we need CMAP, use this in the same way as get8DayProductFractionsForBiweeklyPeriods for the AE products
    // todo: test this method!!!
    public static SubBiweeklyProductFraction getPentadProductFractionsForBiweeklyPeriods(String biweeklyStartDate,
                                                                                          String biweeklyEndDate) {

        Calendar cal = getCalendar(biweeklyStartDate, biweeklyEndDate);

        int startDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int endDayOfYear = cal.get(Calendar.DAY_OF_YEAR);


        SubBiweeklyProductFraction result = new SubBiweeklyProductFraction();
        List<String> productPentadIdentifiers = new ArrayList<String>();
        List<Double> productFractions = new ArrayList<Double>();

        for (int i = 0; i < NUM_PENTAD_PERIODS_IN_YEAR; i++) {
            int startDayOfPentadPeriod = 5 * i + 1;
            int startDiff = startDayOfYear - startDayOfPentadPeriod;
            int endDiff = startDayOfPentadPeriod + 15 - endDayOfYear - 1;

            if (startDiff == 0 && endDiff == 0) {
                // exactly fitting, no side overlaps, three fractions == 0.333
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod));
                productFractions.add(0.333333);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 5));
                productFractions.add(0.333333);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 10));
                productFractions.add(0.333333);
                break;
            } else if (startDiff == 0 && endDiff > 0) {
                // only right overlap, 4 fractions
                final double norm = 15.0 + (5.0 - endDiff);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 5));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 10));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 15));
                productFractions.add((5.0 - endDiff) / norm);
                break;
            } else if (startDiff > 0 && endDiff == 0) {
                // only left overlap, 4 fractions
                final double norm = (5.0 - startDiff) + 15.0;
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod));
                productFractions.add((5.0 - startDiff) / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 5));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 10));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 15));
                productFractions.add(5.0 / norm);
                break;
            } else if (startDiff < 0 && startDiff > -5 && endDiff > 0 && endDiff < 5) {
                // left and right overlap, 4 fractions
                final double norm = 15.0 - startDiff - endDiff;
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod - 5));
                productFractions.add(-startDiff / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod));
                productFractions.add(5.0 / norm);
                productPentadIdentifiers.add(String.format("%03d", startDayOfPentadPeriod + 5));
                productFractions.add((5.0 - endDiff) / norm);
                break;
            }
        }

        result.setSubPeriodStartDoys(productPentadIdentifiers.toArray(new String[productPentadIdentifiers.size()]));
        result.setFractionsOfBiweeklyPeriod(productFractions.toArray(new Double[productFractions.size()]));

        return result;
    }

    public static boolean hasBiweeklyOverlap(String productDoY, SubBiweeklyProductFraction eightDayProductFractions) {
        final String[] eightDayProductDoys = eightDayProductFractions.getSubPeriodStartDoys();
        for (String eightDayProductDoy : eightDayProductDoys) {
            if (eightDayProductDoy.equalsIgnoreCase(productDoY)) {
                return true;
            }
        }

        return false;
    }


    public static double get8DayProductFraction(String doy, SubBiweeklyProductFraction eightDayProductFractions) {
        final String[] productDoys = eightDayProductFractions.getSubPeriodStartDoys();
        final Double[] productFractions = eightDayProductFractions.getFractionsOfBiweeklyPeriod();
        for (int i = 0; i < productDoys.length; i++) {
            if (productDoys[i].equalsIgnoreCase(doy)) {
                return productFractions[i];
            }
        }
        return 0.0;
    }

    private static Calendar getCalendar(String biweeklyStartDate, String biweeklyEndDate) {
        Calendar cal = Calendar.getInstance();

        int startDateYear = Integer.parseInt(biweeklyStartDate.substring(0, 4));      // yyyyMMdd
        int startDateMonth = Integer.parseInt(biweeklyStartDate.substring(4, 6)) - 1;
        int startDateDay = Integer.parseInt(biweeklyStartDate.substring(6, 8));
        cal.set(startDateYear, startDateMonth, startDateDay);

        int endDateYear = Integer.parseInt(biweeklyStartDate.substring(0, 4));
        int endDateMonth = Integer.parseInt(biweeklyEndDate.substring(4, 6)) - 1;
        int endDateDay = Integer.parseInt(biweeklyEndDate.substring(6, 8));
        cal.set(endDateYear, endDateMonth, endDateDay);
        return cal;
    }

}

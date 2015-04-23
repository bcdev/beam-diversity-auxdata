package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductNameComparator;
import org.esa.beam.util.ProductUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Operator for computing a Diversity monthly 'NDVI max' composite from two halfmonthly datasets
 *
 * @author olafd
 */

/**
 * Operator for preparation/modification of Diversity NDVI auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.NdviProbav", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for computing a Diversity monthly 'NDVI max' composite from two halfmonthly datasets.")
public class NdviProbaVOp extends Operator {

    @SourceProducts(description = "NDVI source products")
    private Product[] sourceProducts;
    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    @Parameter(interval = "[1,12]",
               description = "The month to process in 'NDVI_PROBAV' mode (to be given as integer from 1-12)")
    private int month;

    @Parameter(valueSet = {"01", "16"},
               defaultValue = "01",
               description = "Biweekly start day: 01 for days 1-15, 16 for days 16-30(31)")
    private String probavBiweeklyStartDay;

    private Product[] sortedDataSourceProducts;
    private Product tenDaySourceProduct;
    private Product fiveDaySourceProduct;
    private Band ndviSourceBand;

    @Override
    public void initialize() throws OperatorException {
        Arrays.sort(sourceProducts, new ProductNameComparator());
        sortedDataSourceProducts = sourceProducts;

//        if (sortedDataSourceProducts.length != 3) {
//            throw new OperatorException("Month " + month + " does not contain 3 source products - cannot proceed.");
//        }

        // we *should* have 3 products per month: days 1-10, 11-20, 21-30
        // if there's only one, fill both biweekly target products with this
        // if there's two products, set biweekly target products accordingly
        setTenDayPeriodSourceProducts();

        final Product monthlyNdviProduct = createMonthlyProduct();
        setTargetProduct(monthlyNdviProduct);
    }

    private void setTenDayPeriodSourceProducts() {
        if (sortedDataSourceProducts.length == 3) {
            // the normal case
            fiveDaySourceProduct = sortedDataSourceProducts[1];
            if (probavBiweeklyStartDay.equals("01")) {
                tenDaySourceProduct = sortedDataSourceProducts[0];
            } else {
                tenDaySourceProduct = sortedDataSourceProducts[2];
            }
        } else if (sortedDataSourceProducts.length == 2) {
            // one product missing for given month
            if (probavBiweeklyStartDay.equals("01")) {
                tenDaySourceProduct = sortedDataSourceProducts[0];
                fiveDaySourceProduct = sortedDataSourceProducts[0];
            } else {
                tenDaySourceProduct = sortedDataSourceProducts[1];
                fiveDaySourceProduct = sortedDataSourceProducts[1];
            }
        } else if (sortedDataSourceProducts.length == 1) {
            // two products missing for given month
            tenDaySourceProduct = sortedDataSourceProducts[0];
            fiveDaySourceProduct = sortedDataSourceProducts[0];
        } else {
            throw new OperatorException("Month " + month + ": inconsistent number (" +
                                                sortedDataSourceProducts.length +
                                                ") of source products - cannot proceed.");
        }
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        final Rectangle targetRectangle = targetTile.getRectangle();

        final Band tenDayNdviBand = tenDaySourceProduct.getBand(ndviSourceBand.getName());
        final Tile tenDayNdviTile = getSourceTile(tenDayNdviBand, targetRectangle);
        final Band fiveDayNdviBand = fiveDaySourceProduct.getBand(ndviSourceBand.getName());
        final Tile fiveDayNdviTile = getSourceTile(fiveDayNdviBand, targetRectangle);

        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {

                if (tenDayNdviBand.isPixelValid(x, y) && fiveDayNdviBand.isPixelValid(x, y)) {
                    float tenDayNdvi = tenDayNdviTile.getSampleFloat(x, y);
                    float fiveDayNdvi = fiveDayNdviTile.getSampleFloat(x, y);
                    float ndviAve;
                    if (sortedDataSourceProducts.length == 3) {
                        ndviAve = (float) ((2.0 * tenDayNdvi + fiveDayNdvi) / 3.0);
                    } else {
                        ndviAve = (float) ((tenDayNdvi + fiveDayNdvi) / 2.0);
                    }
                    targetTile.setSample(x, y, ndviAve);
                } else if (tenDayNdviBand.isPixelValid(x, y)) {
                    targetTile.setSample(x, y, tenDayNdviTile.getSampleFloat(x, y));
                } else if (fiveDayNdviBand.isPixelValid(x, y)) {
                    targetTile.setSample(x, y, fiveDayNdviTile.getSampleFloat(x, y));
                } else {
                    targetTile.setSample(x, y, ndviSourceBand.getNoDataValue());
                }
            }
        }
    }

    private Product createMonthlyProduct() {
        int width = sortedDataSourceProducts[0].getSceneRasterWidth();
        int height = sortedDataSourceProducts[0].getSceneRasterHeight();

        Product monthlyProduct = new Product("DIVERSITY_NDVI_PROBAV",
                                             "DIVERSITY_NDVI_PROBAV",
                                             width,
                                             height);
        ProductUtils.copyGeoCoding(sortedDataSourceProducts[0], monthlyProduct);
        monthlyProduct.setDescription("PROBA-V Level3 Top Of Canopy 15-Day Synthesis at 333M resolution");
        ProductUtils.copyMetadata(sortedDataSourceProducts[0], monthlyProduct);
        final MetadataElement mphElement = monthlyProduct.getMetadataRoot().getElement("MPH");
        for (MetadataAttribute attr:mphElement.getAttributes()) {
            if (attr.getName().startsWith("PRO")) {
                // remove PROCESSING_DATE/TIME, PRODUCT_REFERENCE, as they do not match to 15-day product
                mphElement.removeAttribute(attr);
            }
            if (attr.getName().equals("SYNTHESIS_PERIOD")) {
                // change from 10 to 15
                mphElement.removeAttribute(attr);
                final MetadataAttribute synthesisPeriodAttr = new MetadataAttribute("SYNTHESIS_PERIOD",
                                                                                 ProductData.createInstance("15"), true);
                synthesisPeriodAttr.setUnit("Days");
                mphElement.addAttribute(synthesisPeriodAttr);
            }
        }

        if (probavBiweeklyStartDay.equals("01")) {
            monthlyProduct.setStartTime(tenDaySourceProduct.getStartTime());
            ProductData.UTC halfMonthTime = getHalfMonthTime(15, 23, 59, 59);
            monthlyProduct.setEndTime(halfMonthTime);
        } else {
            monthlyProduct.setEndTime(tenDaySourceProduct.getEndTime());
            ProductData.UTC halfMonthTime = getHalfMonthTime(16, 0, 0, 0);
            monthlyProduct.setStartTime(halfMonthTime);
        }

        ndviSourceBand = sortedDataSourceProducts[0].getBandAt(0);

        Band targetBand = new Band(ndviSourceBand.getName(),
                                   ndviSourceBand.getDataType(),
                                   width, height);
        targetBand.setNoDataValue(ndviSourceBand.getNoDataValue());
        targetBand.setNoDataValueUsed(ndviSourceBand.isNoDataValueUsed());
        targetBand.setUnit(ndviSourceBand.getUnit());
        targetBand.setScalingFactor(ndviSourceBand.getScalingFactor());
        targetBand.setScalingOffset(ndviSourceBand.getScalingOffset());
        targetBand.setDescription(ndviSourceBand.getDescription());
        monthlyProduct.addBand(targetBand);

        return monthlyProduct;
    }

    private ProductData.UTC getHalfMonthTime(int day, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.set(Integer.parseInt(year), month-1, day, hour, min, sec);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        return ProductData.UTC.create(cal.getTime(), 0);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NdviProbaVOp.class);
        }
    }
}

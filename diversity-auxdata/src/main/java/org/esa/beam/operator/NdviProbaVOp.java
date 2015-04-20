package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
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

    @Parameter(interval = "[1,12]",
               description = "The month to process in 'NDVI_PROBAV' mode (to be given as integer from 1-12)")
    private int month;

    @Parameter(valueSet = {"0", "1"},
               defaultValue = "0",
               description = "Biweekly index: 0 = days 1-15, 1 = days 16-30")
    private int probavBiweeklyIndex;

    private Product[] sortedDataSourceProducts;
    private Product tenDaySourceProduct;
    private Product fiveDaySourceProduct;
    private Band ndviSourceBand;

    @Override
    public void initialize() throws OperatorException {
        Arrays.sort(sourceProducts, new ProductNameComparator());
        sortedDataSourceProducts = sourceProducts;

        if (sortedDataSourceProducts.length != 3) {
            throw new OperatorException("Month " + month + " does not contain 3 source products - cannot proceed.");
        }

        // we have 3 products per month: days 1-10, 11-20, 21-30
        fiveDaySourceProduct = sortedDataSourceProducts[1];
        if (probavBiweeklyIndex == 0) {
            tenDaySourceProduct = sortedDataSourceProducts[0];
        } else {
            tenDaySourceProduct = sortedDataSourceProducts[2];
        }

        final Product monthlyNdviProduct = createMonthlyProduct();

        setTargetProduct(monthlyNdviProduct);
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
                    final float tenDayNdvi = tenDayNdviTile.getSampleFloat(x, y);
                    final float fiveDayNdvi = fiveDayNdviTile.getSampleFloat(x, y);
                    final float ndviAve = (float) ((2.0*tenDayNdvi + fiveDayNdvi)/3.0);
                    targetTile.setSample(x, y, ndviAve);
                } else if (tenDayNdviBand.isPixelValid(x, y)) {
                    targetTile.setSample(x, y, tenDayNdviTile.getSampleFloat(x, y));
                } else if(fiveDayNdviBand.isPixelValid(x, y)) {
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
        // todo: copy (some of) the metadata

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

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NdviProbaVOp.class);
        }
    }
}

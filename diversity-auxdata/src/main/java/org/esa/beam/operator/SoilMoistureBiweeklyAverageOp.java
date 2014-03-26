package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;

import java.awt.*;

/**
 * Operator for preparation/modification of Diversity Soil Moisture auxdata: computes biweekly averages
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.SM.average", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity Soil Moisture auxdata: computes biweekly averages.")
public class SoilMoistureBiweeklyAverageOp extends Operator {

    @SourceProducts(description = "Soil Moisture source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The start date string")
    private String startdateString;

    @Override
    public void initialize() throws OperatorException {
        createTargetProduct();
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        final Rectangle targetRectangle = targetTile.getRectangle();

        final Tile[] sourceTiles = new Tile[sourceProducts.length];

        for (int i = 0; i < sourceProducts.length; i++) {
            final Product sourceProduct = sourceProducts[i];
            final Band smBand = sourceProduct.getBand(SoilMoistureOp.SM_TARGET_BAND_PREFIX);
            if (smBand != null) {
                sourceTiles[i] = getSourceTile(smBand, targetRectangle);
            }
        }

        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {
                final double smAve = getSmBiweeklyAverage(sourceTiles, x, y);
                targetTile.setSample(x, y, smAve);
            }
        }
    }

    static double getSmBiweeklyAverage(Tile[] sourceTiles, int x, int y) {
        double sampleSum = 0.0;
        int numSamples = 0;
        for (final Tile sourceTile : sourceTiles) {
            if (sourceTile != null) {
                final double sample = sourceTile.getSampleDouble(x, y);
                final boolean isSampleInvalid = isSampleInvalid(sample);
                if (!isSampleInvalid) {
                    sampleSum += sample;
                    numSamples++;
                }
            }
        }
        if (numSamples > 0) {
            sampleSum /= numSamples;
        } else {
            sampleSum = Constants.SM_INVALID_VALUE;
        }
        return sampleSum;
    }

    private static boolean isSampleInvalid(double sampleDouble) {
        return sampleDouble < 0.0;
    }

    private void createTargetProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        targetProduct = new Product("DIVERSITY_SM_BIWEEKLY_" + startdateString,
                                    "DIVERSITY_SM_BIWEEKLY",
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sourceProducts[0], targetProduct);

        Band targetBand = new Band(SoilMoistureOp.SM_TARGET_BAND_PREFIX + "_" + startdateString, ProductData.TYPE_FLOAT32, width, height);
        targetBand.setNoDataValue(Constants.SM_INVALID_VALUE);
        targetBand.setNoDataValueUsed(true);
        targetProduct.addBand(targetBand);

        setTargetProduct(targetProduct);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(SoilMoistureBiweeklyAverageOp.class);
        }
    }
}

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
import org.esa.beam.util.SubBiweeklyProductFraction;
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.ProductUtils;

import java.awt.*;

/**
 * Operator for preparation/modification of Diversity Actual Evapotranspiration auxdata: computes biweekly from 8day-products
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Evapo.biweekly", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity Actual Evapotranspiration auxdata: " +
                          "computes biweekly from 8day-products")
public class ActualEvapoBiweeklyFrom8DayOp extends Operator {

    @SourceProducts(description = "Actual Evapo source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The start date string")
    private String startdateString;

    @Parameter(defaultValue = "", description = "The 8-day product fraction object")
    private SubBiweeklyProductFraction eightDayProductFractions;


    @Override
    public void initialize() throws OperatorException {
        createTargetProduct();


    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
        final Rectangle targetRectangle = targetTile.getRectangle();

        final Tile[] sourceTiles = new Tile[sourceProducts.length];
        final double[] sourceProductFractions = getSourceProductFractions();

        for (int i = 0; i < sourceProducts.length; i++) {
            final Product sourceProduct = sourceProducts[i];
            final Band aeBand = sourceProduct.getBand("band_1");
            if (aeBand != null) {
                sourceTiles[i] = getSourceTile(aeBand, targetRectangle);
            }
        }

        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {
                final double aeWeightedAve = getAeBiweeklyAverage(sourceTiles, sourceProductFractions,  x, y);
                targetTile.setSample(x, y, aeWeightedAve);
            }
        }
    }

    static double getAeBiweeklyAverage(Tile[] sourceTiles, double[] sourceProductFractions, int x, int y) {
        double sampleSum = 0.0;
        int numSamples = 0;
        for (int i=0; i<sourceTiles.length; i++) {
            if (sourceTiles[i] != null) {
                final double sample = sourceTiles[i].getSampleDouble(x, y);
                final boolean isSampleInvalid = isSampleInvalid(sample);
                if (!isSampleInvalid) {
                    sampleSum += sourceProductFractions[i]*sample;
                    numSamples++;
                }
            }
        }
        if (numSamples > 0) {
            final double norm = numSamples * 1.0 / sourceProductFractions.length;
            sampleSum /= norm;
        } else {
            sampleSum = Constants.AE_INVALID_VALUE;
        }
        return sampleSum;
    }

    private static boolean isSampleInvalid(double sampleDouble) {
        return sampleDouble == Constants.AE_INVALID_VALUE;
    }

    private double[] getSourceProductFractions() {
        double[] sourceProductFractions = new double[sourceProducts.length];

        for (int i = 0; i < sourceProducts.length; i++) {
            Product sourceProduct = sourceProducts[i];
            final String sourceProductDoY = sourceProduct.getName().substring(27, 30);
            final double fraction = DiversityAuxdataUtils.get8DayProductFraction(sourceProductDoY, eightDayProductFractions);
            sourceProductFractions[i] = fraction;
        }

        return sourceProductFractions;
    }

    private void createTargetProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        targetProduct = new Product("DIVERSITY_AE_BIWEEKLY_" + startdateString,
                                    "DIVERSITY_AE_BIWEEKLY",
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sourceProducts[0], targetProduct);

        Band targetBand = new Band(ActualEvapoOp.AE_TARGET_BAND_PREFIX + "_" + startdateString, ProductData.TYPE_FLOAT32, width, height);
        targetBand.setNoDataValue(Constants.AE_INVALID_VALUE);
        targetBand.setNoDataValueUsed(true);
        targetProduct.addBand(targetBand);

        setTargetProduct(targetProduct);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(ActualEvapoBiweeklyFrom8DayOp.class);
        }
    }
}

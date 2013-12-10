package org.esa.beam.operator.mph_chl;


import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.pointop.*;
import org.esa.beam.jai.ResolutionLevel;
import org.esa.beam.jai.VirtualBandOpImage;

import java.awt.*;

@OperatorMetadata(alias = "Diversity.MPH.CHL",
        version = "1.0",
        authors = "Tom Block",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Computes maximum peak height of chlorophyll")
public class MphChlOp extends PixelOperator {

    private static final double[] MERIS_WAVELENGTHS = {0., 412., 442., 490., 510., 560., 619., 664., 681., 709., 753., 760., 779., 865., 885., 900.};
    private static final double RATIO_C = (MERIS_WAVELENGTHS[8] - MERIS_WAVELENGTHS[7]) / (MERIS_WAVELENGTHS[9] - MERIS_WAVELENGTHS[7]);
    private static final double RATIO_P = (MERIS_WAVELENGTHS[7] - MERIS_WAVELENGTHS[6]) / (MERIS_WAVELENGTHS[8] - MERIS_WAVELENGTHS[6]);
    private static final int REFL_6_IDX = 0;
    private static final int REFL_7_IDX = 1;
    private static final int REFL_8_IDX = 2;
    private static final int REFL_9_IDX = 3;
    private static final int REFL_10_IDX = 4;
    private static final int REFL_14_IDX = 5;

    @SourceProduct
    private Product sourceProduct;
    @Parameter(defaultValue = "not (cloud_classif_flags.F_LAND or cloud_classif_flags.F_CLOUD_BUFFER or cloud_classif_flags.F_CLOUD_SHADOW or cloud_classif_flags.F_CLOUD or cloud_classif_flags.F_MIXED_PIXEL or l1_flags.INVALID )",
            description = "Expression defining pixels not considered for processing.")
    private String invalidPixelExpression;

    private VirtualBandOpImage invalidOpImage;

    @Override
    protected void computePixel(int x, int y, Sample[] sourceSamples, WritableSample[] targetSamples) {
        if (isSampleValid(x, y)) {
            final double r_6 = sourceSamples[REFL_6_IDX].getDouble();
            final double r_7 = sourceSamples[REFL_7_IDX].getDouble();
            final double r_8 = sourceSamples[REFL_8_IDX].getDouble();
            final double r_9 = sourceSamples[REFL_9_IDX].getDouble();
            final double r_10 = sourceSamples[REFL_10_IDX].getDouble();
            final double r_14 = sourceSamples[REFL_14_IDX].getDouble();

            double maxBrr = r_8;
            double maxLambda = MERIS_WAVELENGTHS[8];
            if (r_9 > maxBrr) {
                maxBrr = r_9;
                maxLambda = MERIS_WAVELENGTHS[9];
            }
            double mph = computeMph(maxBrr, r_7, r_14, maxLambda, MERIS_WAVELENGTHS[7], MERIS_WAVELENGTHS[14]);

            if (mph > 0.05 && r_10 > maxBrr) {
                maxBrr = r_10;
                maxLambda = MERIS_WAVELENGTHS[10];
                mph = computeMph(maxBrr, r_7, r_14, maxLambda, MERIS_WAVELENGTHS[7], MERIS_WAVELENGTHS[14]);
            }

            final double SICF_peak = r_8 - r_7 - ((r_9 - r_7) * RATIO_C);
            final double SIPF_peak = r_7 - r_6 - ((r_8 - r_6) * RATIO_P);

            int cyano_flag = 0;
            if (SICF_peak < 0.0 && SIPF_peak > 0.0) {
                cyano_flag = 1;
            }

            double chl;
            if (cyano_flag == 0) {
                final double mph_sq = mph * mph;
                final double mph_p3 = mph_sq * mph;
                final double mph_p4 = mph_sq * mph_sq;

                chl = 5.2392E9 * mph_p4 - 1.9524E8 * mph_p3 + 2.4649E6 * mph_sq + 4.0172E3 * mph + 1.9726;
            } else {
                final double exponent = 35.79 * mph;
                chl = 22.44 * Math.pow(10, exponent);
            }

            targetSamples[0].set(chl);
            targetSamples[1].set(cyano_flag);
        } else {
            setToInvalid(targetSamples);
        }
    }

    // package access for testing only tb 2013-12-04
    static void setToInvalid(WritableSample[] targetSamples) {
        targetSamples[0].set(Double.NaN);
        targetSamples[1].set(0.0);
    }

    // package access for testing only tb 2013-12-04
    static double computeMph(double rBr_Max, double r_7, double r_14, double wl_max, double wl_7, double wl_14) {
        return rBr_Max - r_7 - ((r_14 - r_7) * (wl_max - wl_7) / (wl_14 - wl_7));
    }

    @Override
    protected void configureSourceSamples(SampleConfigurer sampleConfigurer) throws OperatorException {
        sampleConfigurer.defineSample(0, "brr_6");
        sampleConfigurer.defineSample(1, "brr_7");
        sampleConfigurer.defineSample(2, "brr_8");
        sampleConfigurer.defineSample(3, "brr_9");
        sampleConfigurer.defineSample(4, "brr_10");
        sampleConfigurer.defineSample(5, "brr_14");
    }

    @Override
    protected void configureTargetSamples(SampleConfigurer sampleConfigurer) throws OperatorException {
        sampleConfigurer.defineSample(0, "Chl");
        sampleConfigurer.defineSample(1, "cyano_flag");
    }

    @Override
    protected void configureTargetProduct(ProductConfigurer productConfigurer) {
        final Band chlBand = productConfigurer.addBand("Chl", ProductData.TYPE_FLOAT32);
        chlBand.setUnit("mg/m^3");
        chlBand.setGeophysicalNoDataValue(Double.NaN);

        productConfigurer.addBand("cyano_flag", ProductData.TYPE_INT8);

        productConfigurer.copyGeoCoding();

        final Product targetProduct = productConfigurer.getTargetProduct();
        final FlagCoding cyanoFlagCoding = new FlagCoding("cyano_flag");
        cyanoFlagCoding.addFlag("cyano_flag", 1, "Cyanobacteria dominated waters");
        targetProduct.getFlagCodingGroup().add(cyanoFlagCoding);
    }

    @Override
    protected void prepareInputs() throws OperatorException {
        if (!sourceProduct.isCompatibleBandArithmeticExpression(invalidPixelExpression)) {
            final String message = String.format("The given expression '%s' is not compatible with the source product.", invalidPixelExpression);
            throw new OperatorException(message);
        }
        invalidOpImage = VirtualBandOpImage.createMask(invalidPixelExpression, sourceProduct, ResolutionLevel.MAXRES);
    }

    private boolean isSampleValid(int x, int y) {
        return invalidOpImage.getData(new Rectangle(x, y, 1, 1)).getSample(x, y, 0) != 0;
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MphChlOp.class);
        }
    }
}

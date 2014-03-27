package org.esa.beam.diversity.mph_chl;


import org.esa.beam.framework.datamodel.*;
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
        version = "1.3",
        authors = "Tom Block",
        copyright = "(c) 2013, 2014 by Brockmann Consult",
        description = "Computes maximum peak height of chlorophyll")
public class MphChlOp extends PixelOperator {
    //                                                 0    1     2     3     4     5     6     7     8     9     10    11    12    13    14    15
    private static final double[] MERIS_WAVELENGTHS = {0., 412., 442., 490., 510., 560., 619., 664., 681., 709., 753., 760., 779., 865., 885., 900.};
    private static final double RATIO_P = (MERIS_WAVELENGTHS[7] - MERIS_WAVELENGTHS[6]) / (MERIS_WAVELENGTHS[8] - MERIS_WAVELENGTHS[6]);
    private static final double RATIO_C = (MERIS_WAVELENGTHS[8] - MERIS_WAVELENGTHS[7]) / (MERIS_WAVELENGTHS[9] - MERIS_WAVELENGTHS[7]);
    private static final double RATIO_B = (MERIS_WAVELENGTHS[9] - MERIS_WAVELENGTHS[7]) / (MERIS_WAVELENGTHS[13] - MERIS_WAVELENGTHS[7]);
    private static final int REFL_6_IDX = 0;
    private static final int REFL_7_IDX = 1;
    private static final int REFL_8_IDX = 2;
    private static final int REFL_9_IDX = 3;
    private static final int REFL_10_IDX = 4;
    private static final int REFL_14_IDX = 5;

    private static final String CYANO_FLAG_NAME = "CYANO";
    private static final String CYANO_FLAG_DESCRIPTION = "Cyanobacteria dominated waters";
    private static final String FLOATING_FLAG_NAME = "FLOATING";
    private static final String FLOATING_FLAG_DESCRIPTION = "Floating vegetation or cyanobacteria on water surface";
    private static final String ADJACENCY_FLAG_NAME = "ADJACENCY";
    private static final String ADJACENCY_FLAG_DESCRIPTION = "Pixel suspect of adjacency effects";

    @SourceProduct
    private Product sourceProduct;
    @Parameter(defaultValue = "not (l1_flags.LAND_OCEAN or l1_flags.INVALID)",
            description = "Expression defining pixels considered for processing.")
    private String validPixelExpression;

    @Parameter(defaultValue = "1000.0",
            description = "Maximum chlorophyll, arithmetically higher values are capped.")
    private double cyanoMaxValue;

    @Parameter(defaultValue = "500.0",
            description = "Chlorophyll threshold, above which all cyanobacteria dominated waters are 'float.")
    private double chlThreshForFloatFlag;

    @Parameter(defaultValue = "false",
            description = "Switch to true to write 'mph' band.")
    boolean exportMph;

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

            boolean useTwoArgsCyanoCheck = false;

            double maxBrr = r_8;
            double maxLambda = MERIS_WAVELENGTHS[8];
            if (r_9 > maxBrr) {
                maxBrr = r_9;
                maxLambda = MERIS_WAVELENGTHS[9];
            }
            double mph = computeMph(maxBrr, r_7, r_14, maxLambda, MERIS_WAVELENGTHS[7], MERIS_WAVELENGTHS[14]);

            if (r_10 > maxBrr) {
                maxBrr = r_10;
                maxLambda = MERIS_WAVELENGTHS[10];
            }

            boolean floating_flag = false;
            boolean adj_flag = false;
            if (maxLambda == MERIS_WAVELENGTHS[10]) {
                adj_flag = true;
            }

            if (mph > 0.02) {
                mph = computeMph(maxBrr, r_7, r_14, maxLambda, MERIS_WAVELENGTHS[7], MERIS_WAVELENGTHS[14]);
                adj_flag = false;
                if (maxLambda == MERIS_WAVELENGTHS[10]) {
                    floating_flag = true;
                    useTwoArgsCyanoCheck = true;
                }
            }

            final double SIPF_peak = r_7 - r_6 - ((r_8 - r_6) * RATIO_P);
            final double SICF_peak = r_8 - r_7 - ((r_9 - r_7) * RATIO_C);

            boolean cyano_flag;
            if (useTwoArgsCyanoCheck) {
                cyano_flag = isCyano(SICF_peak, SIPF_peak);
            } else {
                final double BAIR_peak = r_9 - r_7 - ((r_14 - r_7) * RATIO_B);
                cyano_flag = isCyano(SICF_peak, SIPF_peak, BAIR_peak);
            }

            int immersed_eucaryotes = 0;
            int immersed_cyano = 0;
            int floating_cyano = 0;
            int floating_vegetation = 0;
            double chl = Double.NaN;
            if (!floating_flag && !cyano_flag) {
                chl = computeChlPolynomial(mph);
                immersed_eucaryotes = 1;
            } else if (floating_flag && !adj_flag && !cyano_flag) {
                setToInvalid(targetSamples, exportMph);
                floating_vegetation = 1;
            } else if (!adj_flag && cyano_flag) {
                chl = computeChlExponential(mph);
                if (floating_flag || chl > chlThreshForFloatFlag) {
                    floating_cyano = 1;
                } else if (!floating_flag && chl < chlThreshForFloatFlag) {
                    immersed_cyano = 1;
                }
            }

            if (chl > cyanoMaxValue) {
                chl = cyanoMaxValue;
            }

            targetSamples[0].set(chl);
            targetSamples[1].set(encodeFlags(cyano_flag, floating_flag, adj_flag));
            targetSamples[2].set(immersed_eucaryotes);
            targetSamples[3].set(immersed_cyano);
            targetSamples[4].set(floating_cyano);
            targetSamples[5].set(floating_vegetation);
            if (exportMph) {
                targetSamples[6].set(mph);
            }
        } else {
            setToInvalid(targetSamples, exportMph);
        }
    }

    // package access for testing only tb 2014-01-31
    static int encodeFlags(boolean cyano_flag, boolean floating_flag, boolean adj_flag) {
        int flag = 0;
        if (cyano_flag) {
            flag = flag | 0x1;
        }
        if (floating_flag) {
            flag = flag | 0x2;
        }
        if (adj_flag) {
            flag = flag | 0x4;
        }
        return flag;
    }

    // package access for testing only tb 2014-01-30
    static double computeChlExponential(double mph) {
        double chl;
        final double exponent = 35.79 * mph;
        chl = 22.44 * Math.exp(exponent);

        return chl;
    }

    // package access for testing only tb 2014-01-30
    static double computeChlPolynomial(double mph) {
        final double mph_sq = mph * mph;
        final double mph_p3 = mph_sq * mph;
        final double mph_p4 = mph_sq * mph_sq;

        return 5.2392E9 * mph_p4 - 1.9524E8 * mph_p3 + 2.4649E6 * mph_sq + 4.0172E3 * mph + 1.9726;
    }

    // package access for testing only tb 2013-01-30
    static boolean isCyano(double SICF_peak, double SIPF_peak, double BAIR_peak) {
        return SICF_peak < 0.0 && SIPF_peak > 0.0 && BAIR_peak > 0.001;
    }

    // package access for testing only tb 2013-01-30
    static boolean isCyano(double SICF_peak, double SIPF_peak) {
        return SICF_peak < 0.0 && SIPF_peak > 0.0;
    }

    // package access for testing only tb 2013-12-04
    static void setToInvalid(WritableSample[] targetSamples, boolean exportMph) {
        targetSamples[0].set(Double.NaN);
        targetSamples[1].set(0.0);  // mph_chl_flag
        targetSamples[2].set(0.0);  // immersed eucaryotes
        targetSamples[3].set(0.0);  // immersed cyanobacteria
        targetSamples[4].set(0.0);  // floating cyanobacteria
        targetSamples[5].set(0.0);  // floating vegetation
        if (exportMph) {
            targetSamples[6].set(Double.NaN);
        }
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
        sampleConfigurer.defineSample(0, "chl");
        sampleConfigurer.defineSample(1, "mph_chl_flags");
        sampleConfigurer.defineSample(2, "immersed_eucaryotes");
        sampleConfigurer.defineSample(3, "immersed_cyanobacteria");
        sampleConfigurer.defineSample(4, "floating_cyanobacteria");
        sampleConfigurer.defineSample(5, "floating_vegetation");
        if (exportMph) {
            sampleConfigurer.defineSample(6, "mph");
        }
    }

    @Override
    protected void configureTargetProduct(ProductConfigurer productConfigurer) {
        final Band chlBand = productConfigurer.addBand("chl", ProductData.TYPE_FLOAT32);
        chlBand.setUnit("mg/m^3");
        chlBand.setGeophysicalNoDataValue(Double.NaN);

        productConfigurer.addBand("immersed_eucaryotes", ProductData.TYPE_INT8);
        productConfigurer.addBand("immersed_cyanobacteria", ProductData.TYPE_INT8);
        productConfigurer.addBand("floating_cyanobacteria", ProductData.TYPE_INT8);
        productConfigurer.addBand("floating_vegetation", ProductData.TYPE_INT8);

        if (exportMph) {
            final Band mphBand = productConfigurer.addBand("mph", ProductData.TYPE_FLOAT32);
            mphBand.setUnit("dl");
            mphBand.setGeophysicalNoDataValue(Double.NaN);
        }
        final Band flagBand = productConfigurer.addBand("mph_chl_flags", ProductData.TYPE_INT8);

        super.configureTargetProduct(productConfigurer);

        final Product targetProduct = productConfigurer.getTargetProduct();
        final FlagCoding flagCoding = new FlagCoding("mph_chl_flags");
        flagCoding.addFlag(CYANO_FLAG_NAME, 1, CYANO_FLAG_DESCRIPTION);
        flagCoding.addFlag(FLOATING_FLAG_NAME, 2, FLOATING_FLAG_DESCRIPTION);
        flagCoding.addFlag(ADJACENCY_FLAG_NAME, 4, ADJACENCY_FLAG_DESCRIPTION);
        targetProduct.getFlagCodingGroup().add(flagCoding);
        flagBand.setSampleCoding(flagCoding);

        final ProductNodeGroup<Mask> maskGroup = targetProduct.getMaskGroup();
        maskGroup.add(mask(CYANO_FLAG_NAME, CYANO_FLAG_DESCRIPTION, "mph_chl_flags.CYANO", Color.cyan, 0.5f, targetProduct));
        maskGroup.add(mask(FLOATING_FLAG_NAME, FLOATING_FLAG_DESCRIPTION, "mph_chl_flags.FLOATING", Color.green, 0.5f, targetProduct));
        maskGroup.add(mask(ADJACENCY_FLAG_NAME, ADJACENCY_FLAG_DESCRIPTION, "mph_chl_flags.ADJACENCY", Color.red, 0.5f, targetProduct));
    }

    @Override
    protected void prepareInputs() throws OperatorException {
        if (!sourceProduct.isCompatibleBandArithmeticExpression(validPixelExpression)) {
            final String message = String.format("The given expression '%s' is not compatible with the source product.", validPixelExpression);
            throw new OperatorException(message);
        }
        invalidOpImage = VirtualBandOpImage.createMask(validPixelExpression, sourceProduct, ResolutionLevel.MAXRES);
    }

    private boolean isSampleValid(int x, int y) {
        return invalidOpImage.getData(new Rectangle(x, y, 1, 1)).getSample(x, y, 0) != 0;
    }

    protected Mask mask(String name, String description, String expression, Color color, float transparency, Product product) {
        return Mask.BandMathsType.create(name, description, product.getSceneRasterWidth(), product.getSceneRasterHeight(), expression, color, transparency);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MphChlOp.class);
        }
    }
}

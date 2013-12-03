package org.esa.beam.operator.mph_chl;


import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.pointop.*;

@OperatorMetadata(alias = "Diversity.MPH.CHL",
        version = "1.0",
        authors = "Tom Block",
        copyright = "(c) 2013 by Brockmann Consult",
        description = "Computes maximum peak height of chlorophyll")
public class MphChlOp extends PixelOperator {

    private static final double[] MERIS_WAVELENGTHS = {0., 412., 442., 490., 510., 560., 619., 664., 681., 709., 753., 760., 779., 865., 885., 900.};
    private static final double RATIO_C = (MERIS_WAVELENGTHS[8] - MERIS_WAVELENGTHS[7]) / (MERIS_WAVELENGTHS[9] - MERIS_WAVELENGTHS[7]);
    private static final double RATIO_P = (MERIS_WAVELENGTHS[7] - MERIS_WAVELENGTHS[6]) / (MERIS_WAVELENGTHS[8] - MERIS_WAVELENGTHS[6]);

    @SourceProduct
    private Product sourceProduct;

    @Override
    protected void computePixel(int x, int y, Sample[] sourceSamples, WritableSample[] targetSamples) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void configureSourceSamples(SampleConfigurer sampleConfigurer) throws OperatorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void configureTargetSamples(SampleConfigurer sampleConfigurer) throws OperatorException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void configureTargetProduct(ProductConfigurer productConfigurer) {
        productConfigurer.addBand("Chl", ProductData.TYPE_FLOAT32);
        productConfigurer.addBand("cyano_flag", ProductData.TYPE_INT8);

        productConfigurer.copyGeoCoding();

        final Product targetProduct = productConfigurer.getTargetProduct();
        final FlagCoding cyanoFlagCoding = new FlagCoding("cyano_flag");
        cyanoFlagCoding.addFlag("cyano_flag", 1, "Cyanobacteria dominated waters");
        targetProduct.getFlagCodingGroup().add(cyanoFlagCoding);

    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MphChlOp.class);
        }
    }
}

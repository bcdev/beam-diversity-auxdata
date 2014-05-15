package org.esa.beam.diversity.mph_chl;

import com.bc.ceres.glevel.MultiLevelImage;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Kernel;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.TiePointGrid;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.util.ProductUtils;

import javax.media.jai.*;
import javax.media.jai.operator.ConvolveDescriptor;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for MPH CHL pixel operator.
 * Allows for post-processing of whole image (currently: JAI low-pass filtering).
 * todo: add tests!!
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.MPH.CHL",
                  version = "1.3.1",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013, 2014 by Brockmann Consult",
                  description = "Wrapper for MPH CHL pixel operator")
public class MphChlMasterOp extends Operator {

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

    @Parameter(defaultValue = "true",
               description = "Switch to true to apply a 3x3 low-pass filter on the result.")
    boolean applyLowPassFilter;

    @SourceProduct
    private Product sourceProduct;

    @Override
    public void initialize() throws OperatorException {
        HashMap<String, Object> mphChlParams = createMphChlParameterMap();
        Product mphChlProduct = createMphChlPixelProduct(mphChlParams);

        if (applyLowPassFilter) {
            setTargetProduct(createFilteredProduct(mphChlProduct));
        } else {
            setTargetProduct(mphChlProduct);
        }

    }

    private Product createFilteredProduct(Product mphChlProduct) {
        Product filteredProduct = new Product(mphChlProduct.getName(),
                                              mphChlProduct.getProductType(),
                                              mphChlProduct.getSceneRasterWidth(),
                                              mphChlProduct.getSceneRasterHeight());

        ProductUtils.copyMetadata(mphChlProduct, filteredProduct);
        ProductUtils.copyGeoCoding(mphChlProduct, filteredProduct);
        ProductUtils.copyFlagCodings(mphChlProduct, filteredProduct);
        ProductUtils.copyFlagBands(mphChlProduct, filteredProduct, true);
        ProductUtils.copyMasks(mphChlProduct, filteredProduct);
        filteredProduct.setStartTime(mphChlProduct.getStartTime());
        filteredProduct.setEndTime(mphChlProduct.getEndTime());

        for (int i = 0; i < mphChlProduct.getNumTiePointGrids(); i++) {
            TiePointGrid srcTPG = mphChlProduct.getTiePointGridAt(i);
            if (!filteredProduct.containsTiePointGrid(srcTPG.getName())) {
                filteredProduct.addTiePointGrid(srcTPG.cloneTiePointGrid());
            }
        }

        for (Band b : mphChlProduct.getBands()) {
            if (!b.isFlagBand()) {
                // currently we have chl as only meaningful band to filter
                if (b.getName().equals("chl")) {
                    final KernelJAI jaiKernel = getJaiKernel();
                    RenderingHints rh = new RenderingHints(JAI.KEY_BORDER_EXTENDER, BorderExtender.createInstance(
                            BorderExtenderCopy.BORDER_COPY));
                    final MultiLevelImage sourceImage = b.getSourceImage();
                    final RenderedOp filteredImage = ConvolveDescriptor.create(sourceImage, jaiKernel, rh);
                    ProductUtils.copyBand(b.getName(), mphChlProduct, filteredProduct, false);
                    filteredProduct.getBand(b.getName()).setSourceImage(filteredImage);
                } else {
                    // just copy bands
                    if (!filteredProduct.containsBand(b.getName())) {
                        ProductUtils.copyBand(b.getName(), mphChlProduct, filteredProduct, true);
                    }
                }
            }
        }

        return filteredProduct;
    }


    private KernelJAI getJaiKernel() {
        final Kernel lowPassKernel = new Kernel(3, 3, 1.0 / 16.0, new double[]{
                +1, +2, +1,
                +2, +4, +2,
                +1, +2, +1,
        });
        final double[] data = lowPassKernel.getKernelData(null);
        final float[] scaledData = new float[data.length];
        final double factor = lowPassKernel.getFactor();
        for (int i = 0; i < data.length; i++) {
            scaledData[i] = (float) (data[i] * factor);
        }
        return new KernelJAI(lowPassKernel.getWidth(), lowPassKernel.getHeight(),
                             lowPassKernel.getXOrigin(), lowPassKernel.getYOrigin(),
                             scaledData);
    }

    private Product createMphChlPixelProduct(HashMap<String, Object> mphChlParams) {
        Map<String, Product> sourceProducts = new HashMap<String, Product>(1);
        sourceProducts.put("sourceProduct", sourceProduct);
        return GPF.createProduct("Diversity.MPH.CHL.Pixel", mphChlParams, sourceProducts);
    }


    private HashMap<String, Object> createMphChlParameterMap() {
        HashMap<String, Object> mphChlParams = new HashMap<String, Object>();
        mphChlParams.put("validPixelExpression", validPixelExpression);
        mphChlParams.put("cyanoMaxValue", cyanoMaxValue);
        mphChlParams.put("chlThreshForFloatFlag", chlThreshForFloatFlag);
        mphChlParams.put("exportMph", exportMph);

        return mphChlParams;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MphChlMasterOp.class);
        }
    }
}

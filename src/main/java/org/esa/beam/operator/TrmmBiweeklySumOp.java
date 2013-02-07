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

import javax.media.jai.JAI;
import javax.media.jai.operator.AddDescriptor;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.text.SimpleDateFormat;

/**
 * Operator for preparation/modification of Diversity TRMM auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Trmm.sum", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity TRMM auxdata: computes biweekly sums.")
public class TrmmBiweeklySumOp extends Operator {

    @SourceProducts(description = "TRMM source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The start date string")
    private String startdateString;

    private static final String THREE_HR_PRECIP_BAND_NAME = "pcp";
    private static final String TOTAL_PRECIP_BAND_NAME = "total_pcp";

    public static final SimpleDateFormat sdfTrmm = new SimpleDateFormat("yyyyMMdd");


    @Override
    public void initialize() throws OperatorException {
        createTargetProduct();

        // do biweekly summation
        final Band precipBand0 = sourceProducts[0].getBand(THREE_HR_PRECIP_BAND_NAME);
        RenderedImage precipImageSum = precipBand0.getSourceImage();
        for (int j = 1; j < sourceProducts.length; j++) {
            final Band precipBandJ = sourceProducts[j].getBand(THREE_HR_PRECIP_BAND_NAME);
            RenderedImage precipImageJ = precipBandJ.getSourceImage();
            precipImageSum = AddDescriptor.create(precipImageSum, precipImageJ, null);
        }
        RenderedImage finalPrecipImageSum = filterNegativePrecip(precipImageSum);

        targetProduct.getBand(TOTAL_PRECIP_BAND_NAME + "_" + startdateString).setSourceImage(finalPrecipImageSum);
    }

    private RenderedImage filterNegativePrecip(RenderedImage precipImageSum) {
        // set all negative precips to invalid value
        double[] low = new double[]{-1.E7};
        double[] high = new double[]{-1.E-3};
        double[] map = new double[]{Constants.TRMM_INVALID_VALUE};

        // threshold operation.
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(precipImageSum);
        pb.add(low);
        pb.add(high);
        pb.add(map);
        return JAI.create("threshold", pb);
    }

    private void createTargetProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        targetProduct = new Product("DIVERSITY_TRMM_BIWEEKLY_" + startdateString,
                                    "DIVERSITY_TRMM_BIWEEKLY",
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sourceProducts[0], targetProduct);

        Band targetBand = new Band(TOTAL_PRECIP_BAND_NAME + "_" + startdateString, ProductData.TYPE_FLOAT32, width, height);
        targetBand.setNoDataValue(Constants.TRMM_INVALID_VALUE);
        targetBand.setNoDataValueUsed(true);
        targetProduct.addBand(targetBand);

        setTargetProduct(targetProduct);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(TrmmBiweeklySumOp.class);
        }
    }
}

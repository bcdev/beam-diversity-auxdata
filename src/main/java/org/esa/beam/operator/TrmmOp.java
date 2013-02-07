package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.gpf.operators.standard.WriteOp;
import org.esa.beam.util.ProductUtils;

import javax.media.jai.JAI;
import javax.media.jai.operator.AddDescriptor;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Operator for preparation/modification of Diversity TRMM auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Trmm", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity TRMM auxdata.")
public class TrmmOp extends Operator {

    @SourceProducts(description = "TRMM source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "Output data directory")
    private File outputDataDir;

    @Parameter(defaultValue = "", description = "The start date string")
    private String startdateString;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    public static final String PRECIP_BAND_NAME = "pcp";

    public static final SimpleDateFormat sdfTrmm = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void initialize() throws OperatorException {
        // do biweekly summation
        if (sourceProducts != null && sourceProducts.length > 0) {
            final Band precipBand0 = sourceProducts[0].getBand(PRECIP_BAND_NAME);
            RenderedImage precipImageSum = precipBand0.getSourceImage();
            for (int j = 1; j < sourceProducts.length; j++) {
                final Band precipBandJ = sourceProducts[j].getBand(PRECIP_BAND_NAME);
                RenderedImage precipImageJ = precipBandJ.getSourceImage();
                precipImageSum = AddDescriptor.create(precipImageSum, precipImageJ, null);
            }
            RenderedImage finalPrecipImageSum = filterNegativePrecip(precipImageSum);

            Product biweeklyProduct = createBiweeklyProduct();
            biweeklyProduct.getBand(PRECIP_BAND_NAME).setSourceImage(finalPrecipImageSum);
            // do reprojection
            Product biweeklyReprojectedProduct = ReferenceReprojection.reproject(biweeklyProduct);

            // write final, biweekly, reprojected product with total precip band
            writeReprojectedBiweeklyProduct(biweeklyReprojectedProduct, startdateString);
        }

        setDummyTargetProduct();
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


    private void writeReprojectedBiweeklyProduct(Product product, String startDate) {
        final String outputFilename = "TRMM_pcp_" + startDate + "_biweekly.tif";
        final File file = new File(outputDataDir, outputFilename);
        WriteOp writeOp = new WriteOp(product, file, "GeoTIFF");
        writeOp.writeProduct(ProgressMonitor.NULL);
        System.out.println("Written biweekly TRMM file '" + file.getAbsolutePath() + "'.");
    }

    private Product createBiweeklyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product biweeklyProduct = new Product("DIVERSITY_TRMM_BIWEEKLY",
                                              "DIVERSITY_TRMM_BIWEEKLY",
                                              width,
                                              height);
        ProductUtils.copyGeoCoding(sourceProducts[0], biweeklyProduct);

        Band precipSourceBand = sourceProducts[0].getBand(PRECIP_BAND_NAME);
        ProductUtils.copyBand(precipSourceBand.getName(), sourceProducts[0], biweeklyProduct, true);
        biweeklyProduct.getBand(precipSourceBand.getName()).setNoDataValue(Constants.NDVI_INVALID_VALUE);
        biweeklyProduct.getBand(precipSourceBand.getName()).setNoDataValueUsed(true);
        biweeklyProduct.getBand(precipSourceBand.getName()).setDescription("Total precipitation (mm)");

        return biweeklyProduct;
    }

    private void setDummyTargetProduct() {
        setTargetProduct(new Product("dummy", "dummy", 0, 0));
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(TrmmOp.class);
        }
    }
}

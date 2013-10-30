package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.Constants;
import org.esa.beam.DataCategory;
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
@OperatorMetadata(alias = "Diversity.Auxdata.Cmorph.sum", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity CMORPH auxdata: computes biweekly sums.")
public class CmorphSumOp extends Operator {

    @SourceProducts(description = "CMORPH source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "CMORPH_DAILY",
               valueSet = {"CMORPH_DAILY", "CMORPH_BIWEEKLY"},
               description = "Processing mode (i.e. the data to process")
    private DataCategory category;

    @Parameter(defaultValue = "", description = "Output data directory")
    private File outputDataDir;

    @Parameter(defaultValue = "", description = "The start date string")
    private String startdateString;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    public static final SimpleDateFormat sdfCmorph = new SimpleDateFormat("yyyyMMdd");

    public static final String HOURLY_PRECIP_BAND_NAME = "PRECIP_HOURLY";
    public static final String DAILY_PRECIP_BAND_NAME = "PRECIP_DAILY";
    public static final String BIWEEKLY_PRECIP_BAND_NAME = "PRECIP_BIWEEKLY";

    private String sumBandName;

    @Override
    public void initialize() throws OperatorException {

        final String precipSrcBandName =
                category == DataCategory.CMORPH_DAILY ? HOURLY_PRECIP_BAND_NAME : DAILY_PRECIP_BAND_NAME;
        final String precipTargetBandName =
                category == DataCategory.CMORPH_DAILY ? DAILY_PRECIP_BAND_NAME : BIWEEKLY_PRECIP_BAND_NAME;
        // do daily or biweekly summation
        if (sourceProducts != null && sourceProducts.length > 0) {
            final Band precipBand0 = sourceProducts[0].getBand(precipSrcBandName);
            RenderedImage precipImageSum = filterNegativePrecip(precipBand0.getSourceImage());
            for (int j = 1; j < sourceProducts.length; j++) {
                final Band precipBandJ = sourceProducts[j].getBand(precipSrcBandName);
                RenderedImage precipImageJ = filterNegativePrecip(precipBandJ.getSourceImage());
                precipImageSum = AddDescriptor.create(precipImageSum, precipImageJ, null);
            }

            RenderedImage finalPrecipImageSum = precipImageSum;

            Product sumProduct = createSumProduct(precipSrcBandName, precipTargetBandName);
            sumProduct.getBand(sumBandName).setSourceImage(finalPrecipImageSum);
            Product sumReprojectedProduct = sumProduct;
            if (category == DataCategory.CMORPH_BIWEEKLY) {
                // do reprojection
                sumReprojectedProduct = ReferenceReprojection.reproject(sumProduct);
                writeFinalBiweeklySumProduct(sumReprojectedProduct);
            } else {
                // swap at zero meridian
                CmorphSwapOp swapOp = new CmorphSwapOp();
                swapOp.setSourceProduct(sumReprojectedProduct);
                writeFinalDailySumProduct(swapOp.getTargetProduct());
            }
        }

        setDummyTargetProduct();
    }

    private RenderedImage filterNegativePrecip(RenderedImage precipImageSum) {
        // set all negative precips to zero
        double[] low = new double[]{-1.E7};
        double[] high = new double[]{1.E-3};
        double[] map = new double[]{0.0};

        // threshold operation.
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(precipImageSum);
        pb.add(low);
        pb.add(high);
        pb.add(map);
        return JAI.create("threshold", pb);
    }


    private void writeFinalDailySumProduct(Product product) {
        final String outputFilename = "CMORPH_pcp_" + startdateString + ".nc";
        final File file = new File(outputDataDir, outputFilename);
        WriteOp writeOp = new WriteOp(product, file, "NetCDF-CF");
        writeOp.writeProduct(ProgressMonitor.NULL);
        System.out.println("Written CMORPH sum file '" + file.getAbsolutePath() + "'.");
    }

    private void writeFinalBiweeklySumProduct(Product product) {
        final String outputFilename = "CMORPH_pcp_" + startdateString + ".tif";
        final File file = new File(outputDataDir, outputFilename);
        WriteOp writeOp = new WriteOp(product, file, "GeoTIFF");
        writeOp.writeProduct(ProgressMonitor.NULL);
        System.out.println("Written CMORPH sum file '" + file.getAbsolutePath() + "'.");
    }

    private Product createSumProduct(String precipSrcBandName, String precipTargetBandName) {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product sumProduct = new Product("DIVERSITY_CMORPH_SUM_" + startdateString,
                                              "DIVERSITY_CMORPH_SUM",
                                              width,
                                              height);
        sumProduct.setPreferredTileSize(64, 64);
        ProductUtils.copyGeoCoding(sourceProducts[0], sumProduct);

        Band precipSourceBand = sourceProducts[0].getBand(precipSrcBandName);
        sumBandName = precipTargetBandName;
        ProductUtils.copyBand(precipSourceBand.getName(), sourceProducts[0], sumBandName, sumProduct, true);
        sumProduct.getBand(sumBandName).setNoDataValue(Constants.CMORPH_INVALID_VALUE);
        sumProduct.getBand(sumBandName).setNoDataValueUsed(true);
        sumProduct.getBand(sumBandName).setDescription("Total precipitation (mm)");
        sumProduct.getBand(sumBandName).setUnit("mm");

        return sumProduct;
    }

    private void setDummyTargetProduct() {
        setTargetProduct(new Product("dummy", "dummy", 0, 0));
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(CmorphSumOp.class);
        }
    }
}

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
import org.esa.beam.gpf.operators.standard.WriteOp;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.io.FileUtils;

import java.io.File;

/**
 * Operator for preparation/modification of Diversity GPCP auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.gpcp", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity GPCP auxdata: " +
                          "just changes band names and writes as GeoTIFF.")
public class GpcpOp extends Operator {

    @SourceProducts(description = "TRMM source products")
    private Product[] sourceProducts;

    @Parameter(defaultValue = "", description = "Output data directory")
    private File outputDataDir;

    private static final String PRECIP_BAND_NAME = "pcp";
    private static final String PRECIP_ERROR_BAND_NAME = "pcp_error";

    @Override
    public void initialize() throws OperatorException {
        if (sourceProducts != null && sourceProducts.length > 0) {
            for (int j = 0; j < sourceProducts.length; j++) {
                Product tiffProduct = createTiffProduct(j);
                for (Band b : sourceProducts[j].getBands()) {
                    final String targetBandName = getTargetBandName(b.getName());
                    ProductUtils.copyBand(b.getName(), sourceProducts[j], targetBandName, tiffProduct, true);
                    tiffProduct.getBand(targetBandName).setNoDataValue(Constants.GPCP_INVALID_VALUE);
                    tiffProduct.getBand(targetBandName).setNoDataValueUsed(true);
                    tiffProduct.getBand(targetBandName).setDescription(b.getDescription());

                }
                // do reprojection
                Product tiffReprojectedProduct = ReferenceReprojection.reproject(tiffProduct);
                // write final, biweekly, reprojected product with total precip band
                writeReprojectedTiffProduct(tiffReprojectedProduct);
            }
        }

        setDummyTargetProduct();
    }

    private String getTargetBandName(String sourceBandname) {
        // change band names:
        // Precip_time1,...,Precip_time12 --> pcp_jan,...,pcp_dec
        // Error_time1,...,Error_time12  --> pcp_error_jan,...,pcp_error_dec
        if (sourceBandname.startsWith("Precip_")) {
            final String monthIndexSubstring = sourceBandname.substring(11, sourceBandname.length());
            final int monthIndex = Integer.parseInt(monthIndexSubstring);
            return PRECIP_BAND_NAME + "_" + Constants.MONTHS[monthIndex - 1];
        } else if (sourceBandname.startsWith("Error_")) {
            final String monthIndexSubstring = sourceBandname.substring(10, sourceBandname.length());
            final int monthIndex = Integer.parseInt(monthIndexSubstring);
            return PRECIP_ERROR_BAND_NAME + "_" + Constants.MONTHS[monthIndex - 1];
        } else {
            return null;
        }
    }

    private void writeReprojectedTiffProduct(Product tiffProduct) {
        final File file = new File(outputDataDir, tiffProduct.getName());
        WriteOp writeOp = new WriteOp(tiffProduct, file, "GeoTIFF");
        writeOp.writeProduct(ProgressMonitor.NULL);
        System.out.println("Written yearly GPCP file '" + file.getAbsolutePath() + "'.");
    }

    private Product createTiffProduct(int sourceProductIndex) {
        final String name = sourceProducts[sourceProductIndex].getName() + ".tif";
        final String type = sourceProducts[sourceProductIndex].getProductType();
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product tiffProduct = new Product(name, type,width, height);
        ProductUtils.copyGeoCoding(sourceProducts[sourceProductIndex], tiffProduct);
        return tiffProduct;
    }

    private void setDummyTargetProduct() {
        setTargetProduct(new Product("dummy", "dummy", 0, 0));
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(GpcpOp.class);
        }
    }
}

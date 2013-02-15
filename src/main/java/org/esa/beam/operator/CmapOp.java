package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.Constants;
import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.gpf.operators.standard.WriteOp;
import org.esa.beam.gpf.operators.standard.reproject.ReprojectionOp;
import org.esa.beam.util.ProductUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Operator for preparation/modification of Diversity CMAP auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Cmap", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity CMAP auxdata.")
public class CmapOp extends Operator {

    // todo: adapt - should basically the same as ActualEvapoOp, just using pentads instead of 8-day periods

    @SourceProduct
    private Product sourceProduct;
    @Parameter(defaultValue = "", description = "Input data directory")
    private File outputDir;

    public static final String CMAP_OUTPUT_FILE_PREFIX = "precip.mon.mean.";
    public static final String PRECIP_BAND_NAME_PREFIX = "precip_time";

    private int width;
    private int height;

    @Override
    public void initialize() throws OperatorException {
        // first step: band renaming and yearly splitting: 1-12 --> 1979; 13-24 --> 1980, etc.
        List<Product> splittedProductList = new ArrayList<Product>();

        int monthIndex = 0;
        int year = 1979;
        Product splittedProduct = createSplittedProduct(year);
        for (int i=1; i<=395; i++) {
            final Band sourceBand = sourceProduct.getBand(PRECIP_BAND_NAME_PREFIX + i);
            String monthlyBandName = PRECIP_BAND_NAME_PREFIX + "_" + Constants.CMAP_MONTHS[monthIndex++];
            ProductUtils.copyBand(sourceBand.getName(), sourceProduct, monthlyBandName, splittedProduct, true);
            if (monthIndex == 12) {
                splittedProductList.add(splittedProduct);
                monthIndex = 0;
                year++;
                splittedProduct = createSplittedProduct(year);
            }
        }

        // second step: reprojection (upscaling) of splitted products to reference grid
        List<Product> splittedReprojectedProductList = new ArrayList<Product>();

        for (int i = 0; i < splittedProductList.size(); i++) {
            splittedProduct = splittedProductList.get(i);
            Product splittedReprojectedProduct = ReferenceReprojection.reproject(splittedProduct);
            writeReprojectedYearlyProduct(splittedReprojectedProduct, i);
        }

        setDummyTargetProduct();
    }

    private void setDummyTargetProduct() {
        setTargetProduct(new Product("", "", 0, 0));
    }

    private Product createSplittedProduct(int year) {
        width = sourceProduct.getSceneRasterWidth();
        height = sourceProduct.getSceneRasterHeight();

        Product splittedProduct = new Product("DIVERSITY_CMAP_" + Integer.toString(year),
                                              "DIVERSITY_CMAP_" + Integer.toString(year),
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sourceProduct, splittedProduct);

        return splittedProduct;
    }

    private void writeReprojectedYearlyProduct(Product product, int year) {
        File file = new File(outputDir, CMAP_OUTPUT_FILE_PREFIX + Integer.toString(year) + ".tif");
        WriteOp writeOp = new WriteOp(product, file, "GeoTIFF");
        writeOp.writeProduct(ProgressMonitor.NULL);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(CmapOp.class);
        }
    }
}

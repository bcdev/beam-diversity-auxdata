package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.DiversityAuxdataUtils;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;

/**
 * Operator for preparation/modification of Diversity NDVI auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Ndvi", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity NDVI auxdata.")
public class NdviOp extends Operator {

    @SourceProducts(description = "NDVI source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "false", description = "if set to true, flags are written instead of NDVIs")
    private boolean writeFlags;

    private int width;
    private int height;

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        final String sourceProductFilter = writeFlags ? "_flag" : "_data";
        sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, sourceProductFilter, 2, 7);
        createTargetProduct();

        if (writeFlags) {
            final FlagCoding ndviFlagCoding = DiversityAuxdataUtils.createNdviFlagCoding();
            targetProduct.getFlagCodingGroup().add(ndviFlagCoding);
            for (Product product : sortedSourceProducts) {
                if (product.getName().endsWith("_flag")) {
                    final String targetBandName = getTargetBandName("ndvi_flag_", product.getName());
                    ProductUtils.copyBand("band_1", product, targetBandName, targetProduct, true);
                    targetProduct.getBand(targetBandName).setNoDataValue(Constants.NDVI_INVALID_VALUE);
                    targetProduct.getBand(targetBandName).setNoDataValueUsed(true);
                }
            }
            DiversityAuxdataUtils.addPatternToAutoGrouping(targetProduct, "ndvi_flag");
        } else {
            for (Product product : sortedSourceProducts) {
                if (product.getName().endsWith("_data")) {
                    final String targetBandName = getTargetBandName("ndvi_", product.getName());
                    ProductUtils.copyBand("band_1", product, targetBandName, targetProduct, true);
                    targetProduct.getBand(targetBandName).setNoDataValue(Constants.NDVI_INVALID_VALUE);
                    targetProduct.getBand(targetBandName).setNoDataValueUsed(true);
                }
            }
            DiversityAuxdataUtils.addPatternToAutoGrouping(targetProduct, "ndvi");
        }
    }

    private String getTargetBandName(String prefix, String name) {
        // we want as band names
        // 'ndvi_jan_a' for product name e.g. '06jan15a.n17-VIg_data.tif'
        // 'ndvi_jan_b' for product name e.g. '06jan15b.n17-VIg_data.tif'
        // 'ndvi_flag_jan_b' for product name e.g. '06jan15b.n17-VIg_flag.tif'
        // etc.
        return prefix + name.substring(2,5) + "_" + name.substring(7,8);
    }

    private void createTargetProduct() {
        width = sortedSourceProducts[0].getSceneRasterWidth();
        height = sortedSourceProducts[0].getSceneRasterHeight();

        targetProduct = new Product("DIVERSITY_NDVI",
                                    "DIVERSITY_NDVI",
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sortedSourceProducts[0], targetProduct);

        setTargetProduct(targetProduct);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NdviOp.class);
        }
    }
}

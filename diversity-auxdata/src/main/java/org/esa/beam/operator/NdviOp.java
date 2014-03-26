package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.DataCategory;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.ProductNameComparator;
import org.esa.beam.util.ProductUtils;

import java.util.Arrays;

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

    @Parameter(defaultValue = "NDVI",
               valueSet = {"NDVI", "NDVI_NEW", "NDVI_MAXCOMPOSIT", "NDVI_MAXCOMPOSIT_NEW", "TRMM_YEARLY",
                       "TRMM_BIWEEKLY", "CMAP", "SOIL_MOISTURE", "ACTUAL_EVAPOTRANSPIRATION", "AIR_TEMPERATURE"},
               description = "Processing mode (i.e. the data to process")
    private DataCategory category;

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        final String sourceProductFilter = writeFlags ? "_flag" : "_data";

        if (category == DataCategory.NDVI_NEW) {
            Arrays.sort(sourceProducts, new ProductNameComparator());
            sortedSourceProducts = sourceProducts;
        } else {
            sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, sourceProductFilter, 2, 7);
        }

        final Product yearlyNdviProduct = createYearlyProduct();

        if (writeFlags) {
            final FlagCoding ndviFlagCoding = DiversityAuxdataUtils.createNdviFlagCoding();
            yearlyNdviProduct.getFlagCodingGroup().add(ndviFlagCoding);
            for (Product product : sortedSourceProducts) {
                if (category == DataCategory.NDVI && product.getName().endsWith("_flag")) {
                    final String targetBandName = getTargetBandName("ndvi_flag_", product.getName());
                    ProductUtils.copyBand("band_1", product, targetBandName, yearlyNdviProduct, true);
                    yearlyNdviProduct.getBand(targetBandName).setNoDataValue(Constants.NDVI_INVALID_VALUE);
                    yearlyNdviProduct.getBand(targetBandName).setNoDataValueUsed(true);
                }
            }
            DiversityAuxdataUtils.addPatternToAutoGrouping(yearlyNdviProduct, "ndvi_flag");
        } else {
            for (Product product : sortedSourceProducts) {
                String targetBandName;
                if (category == DataCategory.NDVI && product.getName().endsWith("_data")) {
                    targetBandName = getTargetBandName("ndvi_", product.getName());
                } else {
                    targetBandName = getNewGimmsTargetBandName("ndvi_", product.getName());
                }
                ProductUtils.copyBand("band_1", product, targetBandName, yearlyNdviProduct, true);
                yearlyNdviProduct.getBand(targetBandName).setNoDataValue(Constants.NDVI_INVALID_VALUE);
                yearlyNdviProduct.getBand(targetBandName).setNoDataValueUsed(true);
            }
            DiversityAuxdataUtils.addPatternToAutoGrouping(yearlyNdviProduct, "ndvi");
        }

        if (category == DataCategory.NDVI_NEW) {
            final Product yearlyNdviReprojectedProduct = ReferenceReprojection.reproject(yearlyNdviProduct);
            setTargetProduct(yearlyNdviReprojectedProduct);
        } else {
            setTargetProduct(yearlyNdviProduct);
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

    private String getNewGimmsTargetBandName(String prefix, String name) {
        // we want as band names
        // 'ndvi_jul_a' for product name e.g. 'NDVI_1981_07_15a_n07_VI3g.tif'
        // 'ndvi_oct_b' for product name e.g. 'NDVI_1981_10_15b_n07_VI3g.tif'
        // etc.
        final String MM = name.substring(10, 12);
        final int monthIndex = Integer.parseInt(MM) - 1;
        final String suffix = name.substring(15,16);

        return prefix + Constants.MONTHS[monthIndex] + "_" + suffix;
    }

    private Product createYearlyProduct() {
        final int width = sortedSourceProducts[0].getSceneRasterWidth();
        final int height = sortedSourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_NDVI",
                                    "DIVERSITY_NDVI",
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sortedSourceProducts[0], yearlyProduct);

        return yearlyProduct;
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NdviOp.class);
        }
    }
}

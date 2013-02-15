package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.ProductUtils;

import java.util.Arrays;

/**
 * Operator for preparation/modification of Diversity air temperature auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.airtemp", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity air temperature auxdata.")
public class AirTemperatureOp extends Operator {

    @SourceProducts(description = "Air temperature monthly source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        sortedSourceProducts = DiversityAuxdataUtils.sortAirTempProductsByMonthIndex(sourceProducts);

        // create target product and copy the biweekly bands
        final Product yearlyAirTempProduct = createYearlyProduct();

        // reproject to NDVI grid
        final Product yearlyAirTempReprojectedProduct = ReferenceReprojection.reproject(yearlyAirTempProduct);
        setTargetProduct(yearlyAirTempReprojectedProduct);
    }

    private Product createYearlyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_AIRTEMP_" + year,
                                            "DIVERSITY_AIRTEMP",
                                            width,
                                            height);

        ProductUtils.copyGeoCoding(sourceProducts[0], yearlyProduct);
        copyBands(yearlyProduct);

        return yearlyProduct;
    }

    private void copyBands(Product yearlyProduct) {
        for (int i = 0; i < sortedSourceProducts.length; i++) {
            final String targetBandName = "air_temp_" + Constants.MONTHS[i];
            ProductUtils.copyBand("band_1", sortedSourceProducts[i], targetBandName, yearlyProduct, true);
            yearlyProduct.getBand(targetBandName).setNoDataValue(Constants.AIR_TEMP_INVALID_VALUE);
            yearlyProduct.getBand(targetBandName).setNoDataValueUsed(true);
        }
        DiversityAuxdataUtils.addPatternToAutoGrouping(yearlyProduct, "air_temp");
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(AirTemperatureOp.class);
        }
    }
}

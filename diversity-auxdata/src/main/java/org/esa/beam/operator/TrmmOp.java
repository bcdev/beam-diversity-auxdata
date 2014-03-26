package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.util.DiversityAuxdataUtils;
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
 * Operator for preparation/modification of Diversity TRMM auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Trmm", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity TRMM auxdata: merges biweekly data into yearly product")
public class TrmmOp extends Operator {

    @SourceProducts(description = "TRMM biweekly source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, null, 9, 13);

        // create target product and copy the biweekly bands
        final Product yearlyTrmmProduct = createYearlyProduct();

        // reproject to NDVI grid
        final Product yearlyTrmmReprojectedProduct = ReferenceReprojection.reproject(yearlyTrmmProduct);
        setTargetProduct(yearlyTrmmReprojectedProduct);
    }

    private Product createYearlyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_TRMM_" + year,
                                            "DIVERSITY_TRMM",
                                            width,
                                            height);

        ProductUtils.copyGeoCoding(sourceProducts[0], yearlyProduct);
        copyBands(yearlyProduct);

        return yearlyProduct;
    }

    private void copyBands(Product yearlyProduct) {
        for (Product sourceProduct : sortedSourceProducts) {
            final String sourceBandName = sourceProduct.getBandAt(0).getName();
            ProductUtils.copyBand(sourceBandName, sourceProduct, yearlyProduct, true);
            yearlyProduct.getBand(sourceBandName).setNoDataValue(Constants.TRMM_INVALID_VALUE);
            yearlyProduct.getBand(sourceBandName).setNoDataValueUsed(true);
        }
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(TrmmOp.class);
        }
    }
}

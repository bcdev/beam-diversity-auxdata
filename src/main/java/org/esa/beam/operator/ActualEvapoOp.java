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

import java.text.SimpleDateFormat;

/**
 * Operator for preparation/modification of Diversity Actual Evapotranspiration auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Evapo", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity Actual Evapotranspiration auxdata.")
public class ActualEvapoOp extends Operator
{

    @SourceProducts(description = "Actual Evapotranspiration biweekly source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;


    public static final SimpleDateFormat sdfAE = new SimpleDateFormat("yyyyMMdd");

    public static final String AE_TARGET_BAND_PREFIX = "ae";

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, null, 22, 26);

        // create target product and copy the biweekly bands
        final Product yearlyAeProduct = createYearlyProduct();

        // reproject to NDVI grid
        final Product yearlyAeReprojectedProduct = ReferenceReprojection.reproject(yearlyAeProduct);
        setTargetProduct(yearlyAeReprojectedProduct);
    }

    private Product createYearlyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_AE_" + year,
                                            "DIVERSITY_AE",
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
            yearlyProduct.getBand(sourceBandName).setNoDataValue(Constants.SM_INVALID_VALUE);
            yearlyProduct.getBand(sourceBandName).setNoDataValueUsed(true);
        }
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(ActualEvapoOp.class);
        }
    }
}

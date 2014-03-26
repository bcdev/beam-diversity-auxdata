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

import java.text.SimpleDateFormat;

/**
 * Operator for preparation/modification of Diversity Soil Moisture auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.SM", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity Soil Moisture auxdata.")
public class SoilMoistureOp extends Operator {

    @SourceProducts(description = "Soil Moisture biweekly source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;


    public static final SimpleDateFormat sdfSM = new SimpleDateFormat("yyyyMMdd");

    public static final String SM_TARGET_BAND_PREFIX = "sm";

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, null, 22, 26);

        // create target product and copy the biweekly bands
        final Product yearlySmProduct = createYearlyProduct();

        // reproject to NDVI grid
        final Product yearlySmReprojectedProduct = ReferenceReprojection.reproject(yearlySmProduct);
        setTargetProduct(yearlySmReprojectedProduct);
    }

    private Product createYearlyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_SM_" + year,
                                    "DIVERSITY_SM",
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
            super(SoilMoistureOp.class);
        }
    }
}

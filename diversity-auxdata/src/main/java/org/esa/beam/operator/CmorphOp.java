package org.esa.beam.operator;

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
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.ProductUtils;

/**
 * Operator for merging biweekly data into yearly product
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Cmorph", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for merging biweekly data into yearly product")
public class CmorphOp extends Operator {

    @SourceProducts(description = "CMORPH biweekly source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    private Product[] sortedSourceProducts;

    private static final String TOTAL_PRECIP_BAND_NAME = "total_pcp";

    @Override
    public void initialize() throws OperatorException {
        // e.g. CMORPH_pcp_jan_b.tif
        sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, null, 11, 15);

        // create target product and copy the biweekly bands
        final Product yearlyCmorphProduct = createYearlyProduct();

        // reproject to NDVI grid
        final Product yearlyCmorphReprojectedProduct = ReferenceReprojection.reproject(yearlyCmorphProduct);
        setTargetProduct(yearlyCmorphReprojectedProduct);
    }

    private Product createYearlyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_CMORPH_" + year,
                                            "DIVERSITY_CMORPH",
                                            width,
                                            height);

        ProductUtils.copyGeoCoding(sourceProducts[0], yearlyProduct);
        copyBands(yearlyProduct);

        return yearlyProduct;
    }

    private void copyBands(Product yearlyProduct) {
        int index = 0;
        for (Product sourceProduct : sortedSourceProducts) {
            final Band sourceBand = sourceProduct.getBand(CmorphSumOp.BIWEEKLY_PRECIP_BAND_NAME);
            final String sourceBandName = sourceBand.getName();
            final String targetBandName = TOTAL_PRECIP_BAND_NAME + "_" +  Constants.HALFMONTHS[index++];
            ProductUtils.copyBand(sourceBandName, sourceProduct, targetBandName, yearlyProduct, true);
            yearlyProduct.getBand(targetBandName).setNoDataValue(sourceBand.getNoDataValue());
            yearlyProduct.getBand(targetBandName).setNoDataValueUsed(sourceBand.isNoDataValueUsed());
        }
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(CmorphOp.class);
        }
    }
}

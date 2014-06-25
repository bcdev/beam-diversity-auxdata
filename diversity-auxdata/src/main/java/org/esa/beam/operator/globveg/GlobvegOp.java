package org.esa.beam.operator.globveg;

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
import org.esa.beam.operator.ReferenceReprojection;
import org.esa.beam.util.ProductNameComparator;
import org.esa.beam.util.ProductUtils;

import java.util.Arrays;

/**
 * Operator for preparation/modification of Diversity NDVI auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Globveg", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity Globveg products.")
public class GlobvegOp extends Operator {

    @SourceProducts(description = "Globveg source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(description = "The tile to process in 'Globveg' mode (to be given as 'hYYvXX')")
    private String globvegTile;

    private Product[] sortedSourceProducts;

    @Override
    public void initialize() throws OperatorException {

        Arrays.sort(sourceProducts, new ProductNameComparator());
        sortedSourceProducts = sourceProducts;

        final Product yearlyGlobvegProduct = createYearlyProduct();

        for (Product product : sortedSourceProducts) {
            for (Band b : product.getBands()) {
                final String targetBandName = getTargetBandName(b.getName(), product.getName());
                ProductUtils.copyBand(b.getName(), product, targetBandName, yearlyGlobvegProduct, true);
                yearlyGlobvegProduct.getBand(targetBandName).setNoDataValue(b.getNoDataValue());
                yearlyGlobvegProduct.getBand(targetBandName).setNoDataValueUsed(true);
            }
        }
        addPatternToAutoGrouping(yearlyGlobvegProduct, "fapar");
        addPatternToAutoGrouping(yearlyGlobvegProduct, "lai");
        addPatternToAutoGrouping(yearlyGlobvegProduct, "ndvi_kg");

        final Product yearlyGlobvegReprojectedProduct =
                ReferenceReprojection.reproject(yearlyGlobvegProduct,
                                                getEastingNorthing(globvegTile)[0],
                                                getEastingNorthing(globvegTile)[1],
                                                1./360., 1./360., 3600, 3600);
//                ReferenceReprojection.reproject(yearlyGlobvegProduct);
        setTargetProduct(yearlyGlobvegReprojectedProduct);
//        setTargetProduct(yearlyGlobvegProduct);
    }

    static double[] getEastingNorthing(String tile) {
        int latIndex = Integer.parseInt(tile.substring(1,3));
        int lonIndex = Integer.parseInt(tile.substring(4,6));
        double easting = 10.0*(lonIndex - 18) + 5.0;
        double northing = 10.0*(9 - latIndex) - 5.0;

        return new double[]{easting, northing};
    }

    private String getTargetBandName(String prefix, String name) {
        // we want as band names
        // 'xxx_jan01' for product name e.g. 'meris-globveg-20050101-v04h17-1.0.nc'
        // 'xxx_jan16' for product name e.g. 'meris-globveg-20050116-v04h17-1.0.nc'
        // etc.
        final String MM = name.substring(18, 20);
        final int monthIndex = Integer.parseInt(MM) - 1;
        final String suffix = name.substring(20, 22);

        return prefix + "_" + Constants.MONTHS[monthIndex] + suffix;
    }

    private Product createYearlyProduct() {
        final int width = sortedSourceProducts[0].getSceneRasterWidth();
        final int height = sortedSourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_GLOBVEG",
                                            "DIVERSITY_GLOBVEG",
                                            width,
                                            height);
        ProductUtils.copyGeoCoding(sortedSourceProducts[0], yearlyProduct);

        return yearlyProduct;
    }

    private static void addPatternToAutoGrouping(Product targetProduct, String groupPattern) {
        Product.AutoGrouping autoGrouping = targetProduct.getAutoGrouping();
        String stringPattern = autoGrouping != null ? autoGrouping.toString() + ":" + groupPattern : groupPattern;
        targetProduct.setAutoGrouping(stringPattern);
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(GlobvegOp.class);
        }
    }
}

package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.Constants;
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProducts;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Operator for computing a Diversity monthly 'NDVI max' composite from two halfmonthly datasets
 *
 * @author olafd
 */

/**
 * Operator for preparation/modification of Diversity NDVI auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.NdviMaxComposit", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for computing a Diversity monthly 'NDVI max' composite from two halfmonthly datasets.")
public class NdviMaxCompositOp extends Operator {

    @SourceProducts(description = "NDVI source products")
    private Product[] sourceProducts;
    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "false", description = "if set to true, flags are written instead of NDVIs")
    private boolean writeFlags;

    private String TARGET_BAND_PREFIX = "ndvi_max_";

    private int width;
    private int height;
    private Map<String, List<Product>> halfmonthlyDataProductsMap;
    private Map<String, List<Product>> halfmonthlyFlagProductsMap;
    private Product[] sortedDataSourceProducts;
    private Product[] sortedFlagSourceProducts;

    @Override
    public void initialize() throws OperatorException {
        final String sourceDataProductFilter = "_data";
        final String sourceFlagProductFilter = "_flag";
        sortedDataSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, sourceDataProductFilter, 2, 7);
        sortedFlagSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, sourceFlagProductFilter, 2, 7);

        createTargetProduct();

        if (writeFlags) {
            final FlagCoding ndviFlagCoding = DiversityAuxdataUtils.createNdviFlagCoding();
            targetProduct.getFlagCodingGroup().add(ndviFlagCoding);
            TARGET_BAND_PREFIX += "flag_";
            DiversityAuxdataUtils.addPatternToAutoGrouping(targetProduct, "ndvi_max_flag");
        } else {
            DiversityAuxdataUtils.addPatternToAutoGrouping(targetProduct, "ndvi_max");
        }

        halfmonthlyDataProductsMap = new HashMap<String, List<Product>>();
        halfmonthlyFlagProductsMap = new HashMap<String, List<Product>>();

        for (String month : Constants.MONTHS) {
            List<Product> thisMonthDataProducts = new ArrayList<Product>();
            List<Product> thisMonthFlagProducts = new ArrayList<Product>();
            for (Product product : sortedDataSourceProducts) {
                if (product.getName().contains(month)) {
                    thisMonthDataProducts.add(product);
                }
            }
            for (Product product : sortedFlagSourceProducts) {
                if (product.getName().contains(month)) {
                    thisMonthFlagProducts.add(product);
                }
            }
            if (thisMonthDataProducts.size() == 2 && thisMonthFlagProducts.size() == 2) {
                // the normal case
                halfmonthlyDataProductsMap.put(month, thisMonthDataProducts);
                halfmonthlyFlagProductsMap.put(month, thisMonthFlagProducts);

                Band targetBand = new Band(TARGET_BAND_PREFIX + month, ProductData.TYPE_FLOAT32, width, height);
                targetBand.setNoDataValue(Constants.NDVI_INVALID_VALUE);
                targetBand.setNoDataValueUsed(true);
                targetProduct.addBand(targetBand);
            } else {
                System.err.println("Warning: NDVI products for '" + month + "' missing or incomplete - skipping.");
            }
        }

    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        final Rectangle targetRectangle = targetTile.getRectangle();

        final String name = targetBand.getName();
        final String currentMonth = name.substring(TARGET_BAND_PREFIX.length(), name.length());

        final List<Product> currentMonthDataProducts = halfmonthlyDataProductsMap.get(currentMonth);
        List<Product> currentMonthFlagProducts;

        if (currentMonthDataProducts.size() != 2) {
            return;
        }

        final Band firstNdviBand = currentMonthDataProducts.get(0).getBand("band_1");
        final Tile firstNdviTile = getSourceTile(firstNdviBand, targetRectangle);
        final Band secondNdviBand = currentMonthDataProducts.get(1).getBand("band_1");
        final Tile secondNdviTile = getSourceTile(secondNdviBand, targetRectangle);

        Band firstNdviFlagBand;
        Tile firstNdviFlagTile = null;
        Band secondNdviFlagBand;
        Tile secondNdviFlagTile = null;
        if (writeFlags) {
            currentMonthFlagProducts = halfmonthlyFlagProductsMap.get(currentMonth);
            if (currentMonthFlagProducts.size() != 2) {
                return;
            }
            firstNdviFlagBand = currentMonthFlagProducts.get(0).getBand("band_1");
            firstNdviFlagTile = getSourceTile(firstNdviFlagBand, targetRectangle);
            secondNdviFlagBand = currentMonthFlagProducts.get(1).getBand("band_1");
            secondNdviFlagTile = getSourceTile(secondNdviFlagBand, targetRectangle);
        }

        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {
                final boolean isSampleInvalid = isSampleInvalid(firstNdviBand, secondNdviBand, x, y);
                final boolean isSampleMissing = isSampleMissing(firstNdviTile, secondNdviTile, x, y);

                if (!isSampleInvalid && !isSampleMissing) {
                    final int firstNdvi = firstNdviTile.getSampleInt(x, y);
                    final int secondNdvi = secondNdviTile.getSampleInt(x, y);
                    if (firstNdviBand.isPixelValid(x, y) && secondNdviBand.isPixelValid(x, y)) {
                        final int maxNdvi = Math.max(firstNdvi, secondNdvi);
                        if (writeFlags) {
                            if (firstNdvi >= secondNdvi) {
                                final int firstNdviFlag = firstNdviFlagTile.getSampleInt(x, y);
                                targetTile.setSample(x, y, firstNdviFlag);
                            } else {
                                final int secondNdviFlag = secondNdviFlagTile.getSampleInt(x, y);
                                targetTile.setSample(x, y, secondNdviFlag);
                            }
                        } else {
                            targetTile.setSample(x, y, maxNdvi);
                        }
                    } else if (!firstNdviBand.isPixelValid(x, y)) {
                        if (writeFlags) {
                            final int firstNdviFlag = firstNdviFlagTile.getSampleInt(x, y);
                            targetTile.setSample(x, y, firstNdviFlag);
                        } else {
                            targetTile.setSample(x, y, firstNdvi);
                        }
                    } else if (!secondNdviBand.isPixelValid(x, y)) {
                        if (writeFlags) {
                            final int secondNdviFlag = secondNdviFlagTile.getSampleInt(x, y);
                            targetTile.setSample(x, y, secondNdviFlag);
                        } else {
                            targetTile.setSample(x, y, secondNdvi);
                        }
                    }
                } else {
                    targetTile.setSample(x, y, Constants.NDVI_INVALID_VALUE);
                }
            }
        }
    }

    private boolean isSampleInvalid(Band firstNdviBand, Band secondNdviBand, int x, int y) {
        return !firstNdviBand.isPixelValid(x, y) && !secondNdviBand.isPixelValid(x, y);
    }

    private boolean isSampleMissing(Tile firstNdviTile, Tile secondNdviTile, int x, int y) {
        return (firstNdviTile.getSampleInt(x, y) == Constants.NDVI_MISSING_DATA_VALUE &&
                secondNdviTile.getSampleInt(x, y) == Constants.NDVI_MISSING_DATA_VALUE);
    }

    private void createTargetProduct() {
        width = sortedDataSourceProducts[0].getSceneRasterWidth();
        height = sortedDataSourceProducts[0].getSceneRasterHeight();

        targetProduct = new Product("DIVERSITY_NDVI_MAX",
                                    "DIVERSITY_NDVI_MAX",
                                    width,
                                    height);
        ProductUtils.copyGeoCoding(sortedDataSourceProducts[0], targetProduct);

        setTargetProduct(targetProduct);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(NdviMaxCompositOp.class);
        }
    }
}

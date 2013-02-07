package org.esa.beam.operator;

import org.esa.beam.AuxdataSourcesProvider;
import org.esa.beam.Constants;
import org.esa.beam.DataCategory;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Master operator for preparation/modification of various Diversity auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  description = "Master operator for preparation/modification of various Diversity auxdata.")
public class MasterOp extends Operator {
    public static final String VERSION = "1.0-SNAPSHOT";

    @Parameter(defaultValue = "", description = "Input data directory")
    private File inputDataDir;

    @Parameter(defaultValue = "", description = "Output data directory")
    private File outputDataDir;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;

    @Parameter(defaultValue = "false", description = "if set to true, flags are written instead of NDVIs")
    private boolean writeNdviFlags;

    @Parameter(defaultValue = "NDVI",
               valueSet = {"NDVI", "NDVI_MAXCOMPOSIT", "TRMM", "CMAP", "SOIL_MOISTURE", "ACTUAL_EVAPOTRANSPIRATION", "AIR_TEMPERATURE"},
               description = "Processing mode (i.e. the data to process")
    private DataCategory category;


    @Override
    public void initialize() throws OperatorException {
        Product[] ndviSourceProducts;
        switch (category) {
            case NDVI:
                final Product ndviProduct = getNdviProduct();
                setTargetProduct(ndviProduct);
                break;
            case NDVI_MAXCOMPOSIT:
                final Product ndviMaxCompositProduct = getNdviMaxcompositProduct();
                setTargetProduct(ndviMaxCompositProduct);
                break;
            case TRMM:
                final Product trmmProduct = getTrmmProduct();
                setTargetProduct(trmmProduct);
                break;
            case CMAP:
                Product cmapSourceProduct = AuxdataSourcesProvider.getCmapSourceProduct(inputDataDir);
                // todo: continue
                break;
            case SOIL_MOISTURE:
                final Product soilMoistureProduct = getSoilMoistureProduct();
                setTargetProduct(soilMoistureProduct);
                break;
            case ACTUAL_EVAPOTRANSPIRATION:
                break;
            case AIR_TEMPERATURE:
                break;
        }
    }

    private Product getNdviMaxcompositProduct() {
        Product[] ndviSourceProducts;
        ndviSourceProducts = AuxdataSourcesProvider.getNdviSourceProducts(inputDataDir,
                                                                          year,
                                                                          category,
                                                                          writeNdviFlags);
        NdviMaxCompositOp ndviMaxCompositOp = new NdviMaxCompositOp();
        ndviMaxCompositOp.setSourceProducts(ndviSourceProducts);
        ndviMaxCompositOp.setParameter("year", year);
        ndviMaxCompositOp.setParameter("writeFlags", writeNdviFlags);

        return ndviMaxCompositOp.getTargetProduct();
    }

    private Product getNdviProduct() {
        Product[] ndviSourceProducts;
        ndviSourceProducts = AuxdataSourcesProvider.getNdviSourceProducts(inputDataDir,
                                                                          year,
                                                                          category,
                                                                          writeNdviFlags);
        NdviOp ndviOp = new NdviOp();
        ndviOp.setSourceProducts(ndviSourceProducts);
        ndviOp.setParameter("year", year);
        ndviOp.setParameter("writeFlags", writeNdviFlags);

        return ndviOp.getTargetProduct();
    }

    private Product getSoilMoistureProduct() {
        List<Product> biweeklyAverageProductList = new ArrayList<Product>();
        // 1. per biweekly period, get daily single products and compute average products
        SoilMoistureBiweeklyAverageOp smAveOp;
        for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
            String startdateString = year + Constants.BIWEEKLY_START_DATES[i];
            String enddateString = year + Constants.BIWEEKLY_END_DATES[i];
            smAveOp = new SoilMoistureBiweeklyAverageOp();
            Product[] smDailySourceProducts;
            try {
                smDailySourceProducts = AuxdataSourcesProvider.getSmDailySourceProducts(inputDataDir,
                                                                                        year,
                                                                                        startdateString,
                                                                                        enddateString);
                if (smDailySourceProducts != null && smDailySourceProducts.length > 0) {
                    smAveOp.setSourceProducts(smDailySourceProducts);
                    smAveOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
                    biweeklyAverageProductList.add(smAveOp.getTargetProduct());
                }
            } catch (ParseException e) {
                // todo
                e.printStackTrace();
            }

        }
        Product[] biweeklyAverageProducts = biweeklyAverageProductList.toArray(new Product[biweeklyAverageProductList.size()]);

        // 2. for all biweekly periods, collect average products and add as single bands in final yearly product
        SoilMoistureOp smOp = new SoilMoistureOp();
        smOp.setSourceProducts(biweeklyAverageProducts);
        smOp.setParameter("year", year);


        return smOp.getTargetProduct();
    }

    private Product getTrmmProduct() {
        List<Product> biweeklyAverageProductList = new ArrayList<Product>();
        // 1. per biweekly period, get daily single products and compute average products
        TrmmBiweeklySumOp trmmSumOp;
        Product[] trmmBiweeklySourceProducts = null;
        for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
//        for (int i = 2; i < 3; i++) {
            String startdateString = year + Constants.BIWEEKLY_START_DATES[i];
            String enddateString = year + Constants.BIWEEKLY_END_DATES[i];
            trmmSumOp = new TrmmBiweeklySumOp();
            try {
                trmmBiweeklySourceProducts = AuxdataSourcesProvider.getTrmm3HrSourceProducts(inputDataDir,
                                                                                             year,
                                                                                             startdateString,
                                                                                             enddateString);
                if (trmmBiweeklySourceProducts != null && trmmBiweeklySourceProducts.length > 0) {
                    sumTrmm(biweeklyAverageProductList, trmmSumOp, i, trmmBiweeklySourceProducts);
                }
            } catch (ParseException e) {
                // todo
                e.printStackTrace();
            }
        }
        Product[] biweeklyAverageProducts = biweeklyAverageProductList.toArray(new Product[biweeklyAverageProductList.size()]);

        // 2. for all biweekly periods, collect average products and add as single bands in final yearly product
        TrmmOp trmmOp = new TrmmOp();
        trmmOp.setSourceProducts(biweeklyAverageProducts);
        trmmOp.setParameter("year", year);

        return trmmOp.getTargetProduct();
    }

    private void sumTrmm(List<Product> biweeklyAverageProductList, TrmmBiweeklySumOp trmmSumOp, int i, Product[] trmmBiweeklySourceProducts) {
        trmmSumOp.setSourceProducts(trmmBiweeklySourceProducts);
        trmmSumOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
        biweeklyAverageProductList.add(trmmSumOp.getTargetProduct());
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MasterOp.class);
        }
    }
}

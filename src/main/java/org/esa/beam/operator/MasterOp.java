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
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.SubBiweeklyProductFraction;

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
               valueSet = {"NDVI", "NDVI_MAXCOMPOSIT", "TRMM_YEARLY", "TRMM_BIWEEKLY", "CMAP", "SOIL_MOISTURE", "ACTUAL_EVAPOTRANSPIRATION", "AIR_TEMPERATURE"},
               description = "Processing mode (i.e. the data to process")
    private DataCategory category;


    @Override
    public void initialize() throws OperatorException {
        switch (category) {
            case NDVI:
                final Product ndviProduct = getNdviProduct();
                setTargetProduct(ndviProduct);
                break;
            case NDVI_MAXCOMPOSIT:
                final Product ndviMaxCompositProduct = getNdviMaxcompositProduct();
                setTargetProduct(ndviMaxCompositProduct);
                break;
            case TRMM_BIWEEKLY:
                writeTrmmBiweeklyProducts();
                break;
            case TRMM_YEARLY:
                TrmmOp trmmOp = new TrmmOp();
                try {
                    Product[] trmmBiweeklySourceProducts = AuxdataSourcesProvider.getTrmmBiweeklySourceProducts(inputDataDir);
                    trmmOp.setSourceProducts(trmmBiweeklySourceProducts);
                    trmmOp.setParameter("year", year);
                    setTargetProduct(trmmOp.getTargetProduct());
                } catch (ParseException e) {
                    // todo
                    e.printStackTrace();
                }
                break;
            case CMAP:
                final Product cmapProduct = getActualCmapProduct();
                setTargetProduct(cmapProduct);
                break;
            case SOIL_MOISTURE:
                final Product soilMoistureProduct = getSoilMoistureProduct();
                setTargetProduct(soilMoistureProduct);
                break;
            case ACTUAL_EVAPOTRANSPIRATION:
                final Product actualEvapoProduct = getActualEvapoProduct();
                setTargetProduct(actualEvapoProduct);
                break;
            case AIR_TEMPERATURE:
                final Product airTempProduct = getAirTempProduct();
                setTargetProduct(airTempProduct);
                break;
        }
    }

    private void writeTrmmBiweeklyProducts() {
        TrmmBiweeklySumOp trmmSummOp;
        for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
            String startdateString = year + Constants.BIWEEKLY_START_DATES[i];
            String enddateString = year + Constants.BIWEEKLY_END_DATES[i];
            trmmSummOp = new TrmmBiweeklySumOp();
            Product[] trmm3HrSourceProducts = null;
            try {
                trmm3HrSourceProducts = AuxdataSourcesProvider.getTrmm3HrSourceProducts(inputDataDir, startdateString, enddateString);
                trmmSummOp.setSourceProducts(trmm3HrSourceProducts);
                trmmSummOp.setParameter("year", year);
                trmmSummOp.setParameter("outputDataDir", outputDataDir);
                trmmSummOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
            } catch (ParseException e) {
                // todo
                e.printStackTrace();
            }

            setTargetProduct(trmmSummOp.getTargetProduct());

            for (Product sourceProduct : trmm3HrSourceProducts) {
                sourceProduct.dispose();
            }
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

    private Product getActualCmapProduct() {

        // 1. take full input product (all years), split into pentad products with one band each
        Product[] cmapPentadSplittedSourceProducts = AuxdataSourcesProvider.getCmapPentadSplittedSourceProducts(inputDataDir, year);

        // 2. per biweekly period of the year, get overlapping 8-day products and compute fractional average products  (as for AE)
        List<Product> biweeklyAverageProductList = new ArrayList<Product>();
        CmapBiweeklyFromPentadOp cmapAveOp;
        for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
            String startDateString = year + Constants.BIWEEKLY_START_DATES[i];
            String endDateString = year + Constants.BIWEEKLY_END_DATES[i];
            cmapAveOp = new CmapBiweeklyFromPentadOp();

            final SubBiweeklyProductFraction pentadProductFractions =
                    DiversityAuxdataUtils.getPentadProductFractionsForBiweeklyPeriods(startDateString,
                                                                                      endDateString);

            if (cmapPentadSplittedSourceProducts != null && cmapPentadSplittedSourceProducts.length > 0) {
                cmapAveOp.setSourceProducts(cmapPentadSplittedSourceProducts);
                cmapAveOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
                cmapAveOp.setParameter("pentadProductFractions", pentadProductFractions);
                biweeklyAverageProductList.add(cmapAveOp.getTargetProduct());
            }

        }
        Product[] biweeklyAverageProducts = biweeklyAverageProductList.toArray(new Product[biweeklyAverageProductList.size()]);

        // 3. for all biweekly periods, collect average products and add as single bands in final yearly product (as for AE)
        CmapOp cmapOp = new CmapOp();
        cmapOp.setSourceProducts(biweeklyAverageProducts);
        cmapOp.setParameter("year", year);

        return cmapOp.getTargetProduct();
    }

    private Product getActualEvapoProduct() {
        List<Product> biweeklyAverageProductList = new ArrayList<Product>();
        // 1. per biweekly period, get overlapping 8-day products and compute fractional average products
        ActualEvapoBiweeklyFrom8DayOp aeAveOp;
        for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
            String startDateString = year + Constants.BIWEEKLY_START_DATES[i];
            String endDateString = year + Constants.BIWEEKLY_END_DATES[i];
            aeAveOp = new ActualEvapoBiweeklyFrom8DayOp();
            Product[] ae8DaySourceProducts;

            final SubBiweeklyProductFraction eightDayProductFractions =
                    DiversityAuxdataUtils.get8DayProductFractionsForBiweeklyPeriods(startDateString,
                                                                                    endDateString);
            try {
                ae8DaySourceProducts = AuxdataSourcesProvider.getAe8DaySourceProducts(inputDataDir,
                                                                                      year,
                                                                                      eightDayProductFractions,
                                                                                      startDateString,
                                                                                      endDateString);
                if (ae8DaySourceProducts != null && ae8DaySourceProducts.length > 0) {
                    aeAveOp.setSourceProducts(ae8DaySourceProducts);
                    aeAveOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
                    aeAveOp.setParameter("eightDayProductFractions", eightDayProductFractions);
                    biweeklyAverageProductList.add(aeAveOp.getTargetProduct());
                }
            } catch (ParseException e) {
                // todo
                e.printStackTrace();
            }
        }
        Product[] biweeklyAverageProducts = biweeklyAverageProductList.toArray(new Product[biweeklyAverageProductList.size()]);

        // 2. for all biweekly periods, collect average products and add as single bands in final yearly product
        ActualEvapoOp aeOp = new ActualEvapoOp();
        aeOp.setSourceProducts(biweeklyAverageProducts);
        aeOp.setParameter("year", year);

        return aeOp.getTargetProduct();
    }


    private Product getAirTempProduct() {
        Product[] airTempSourceProducts;
        airTempSourceProducts = AuxdataSourcesProvider.getAirTempSourceProducts(inputDataDir, year);
        AirTemperatureOp airTempOp = new AirTemperatureOp();
        airTempOp.setSourceProducts(airTempSourceProducts);
        airTempOp.setParameter("year", year);

        return airTempOp.getTargetProduct();
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MasterOp.class);
        }
    }
}

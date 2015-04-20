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
import org.esa.beam.operator.globveg.GlobvegOp;
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

    @Parameter(defaultValue = "01", description = "The month to process (used for CMORPH_BIWEEKLY)",
               valueSet = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"})
    private String month;

    @Parameter(defaultValue = "1", description = "The day to process (used for CMORPH_DAILY)", interval = "[1,31]")
    private int day;

    @Parameter(defaultValue = "false", description = "if set to true, flags are written instead of NDVIs")
    private boolean writeNdviFlags;

    @Parameter(valueSet = {"NDVI", "NDVI_NEW", "NDVI_MAXCOMPOSIT", "NDVI_MAXCOMPOSIT_NEW",
            "NDVI_PROBAV", "NDVI_WEIGHTAVE_PROBAV",
            "TRMM_YEARLY", "TRMM_BIWEEKLY",
            "CMORPH_DAILY", "CMORPH_BIWEEKLY", "CMORPH_YEARLY",
            "SOIL_MOISTURE", "ACTUAL_EVAPOTRANSPIRATION", "AIR_TEMPERATURE", "GLOBVEG",
            "GPCP", "CMAP"
    },
               description = "Processing mode (i.e. the data to process")
    private DataCategory category;

    @Parameter(valueSet = {"10-iberia", "12-southafrica", "13-west-sudanian-savanna", "15-caatinga", "20-australia"},
               description = "The site to process in 'Globveg' mode (to be given as 'hYYvXX')")
    private String globvegSite;

    @Parameter(description = "The tile to process in 'Globveg' mode (to be given as 'hYYvXX')")
    private String globvegTile;

    @Parameter(description = "The tile to process in 'NDVI_PROBAV' mode (to be given as 'XnnYmm')")
    private String probavTile;

    @Parameter(interval="[1,12]",
               description = "The month to process in 'NDVI_PROBAV' mode (to be given as integer from 1-12)")
    private int probavMonth;

    @Parameter(valueSet = {"0", "1"},
               defaultValue = "0",
               description = "Biweekly index: 0 = days 1-15, 1 = days 16-30")
    private int probavBiweeklyIndex;

    @Parameter(valueSet = {"MERGED", "COMBINED"},
               defaultValue = "MERGED",
               description = "The SM data type (MERGED = old dataset, COMBINED = dataset update, March 2014")
    private String smDataType;

    @Override
    public void initialize() throws OperatorException {
        switch (category) {
            case NDVI:
                final Product ndviProduct = getNdviProduct();
                setTargetProduct(ndviProduct);
                break;
            case NDVI_NEW:
                final Product ndviNewProduct = getNewNdviProduct();
                setTargetProduct(ndviNewProduct);
                break;
            case NDVI_PROBAV:
                final Product ndviProbavProduct = getNdviProbavProduct();
                setTargetProduct(ndviProbavProduct);
                break;
            case GLOBVEG:
                final Product globvegProduct = getGlobvegProduct();
                setTargetProduct(globvegProduct);
                break;
            case NDVI_MAXCOMPOSIT:
                final Product ndviMaxCompositProduct = getNdviMaxcompositProduct();
                setTargetProduct(ndviMaxCompositProduct);
                break;
            case NDVI_MAXCOMPOSIT_NEW:
                final Product ndviNewMaxCompositProduct = getNewNdviMaxcompositProduct();
                setTargetProduct(ndviNewMaxCompositProduct);
                break;
            case TRMM_BIWEEKLY:
                writeTrmmBiweeklyProducts();
                break;
            case TRMM_YEARLY:
                TrmmOp trmmOp = new TrmmOp();
                try {
                    Product[] trmmBiweeklySourceProducts =
                            AuxdataSourcesProvider.getBiweeklySourceProducts(inputDataDir, "TRMM");
                    trmmOp.setSourceProducts(trmmBiweeklySourceProducts);
                    trmmOp.setParameter("year", year);
                    setTargetProduct(trmmOp.getTargetProduct());
                } catch (ParseException e) {
                    throw new OperatorException("Problems while parsing TRMM input - cannot proceed: " + e.getMessage());
                }
                break;
            case CMORPH_BIWEEKLY:
                writeCmorphBiweeklyProducts();
                break;
            case CMORPH_DAILY:
                writeCmorphDailyProducts();
                break;
            case CMORPH_YEARLY:
                CmorphOp cmorphOp = new CmorphOp();
                try {
                    Product[] cmorphBiweeklySourceProducts =
                            AuxdataSourcesProvider.getBiweeklySourceProducts(inputDataDir, "CMORPH");
                    cmorphOp.setSourceProducts(cmorphBiweeklySourceProducts);
                    cmorphOp.setParameter("year", year);
                    setTargetProduct(cmorphOp.getTargetProduct());
                } catch (ParseException e) {
                    throw new OperatorException("Problems while parsing CMORPH input - cannot proceed: " + e.getMessage());
                }
                break;
            case GPCP:
                writeGpcpYearlyProducts();
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
            Product[] trmm3HrSourceProducts;
            try {
                trmm3HrSourceProducts = AuxdataSourcesProvider.getTrmm3HrSourceProducts(inputDataDir, startdateString, enddateString);
                trmmSummOp.setSourceProducts(trmm3HrSourceProducts);
                trmmSummOp.setParameter("year", year);
                trmmSummOp.setParameter("outputDataDir", outputDataDir);
                trmmSummOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
            } catch (ParseException e) {
                throw new OperatorException("Problems while parsing TRMM input - cannot proceed: " + e.getMessage());
            }

            setTargetProduct(trmmSummOp.getTargetProduct());

            for (Product sourceProduct : trmm3HrSourceProducts) {
                sourceProduct.dispose();
            }
        }
    }

    private void writeCmorphBiweeklyProducts() {
        CmorphSumOp cmorphSumOp;

        final int firstDateIndex = 2 * (Integer.parseInt(month) - 1);

        for (int i = firstDateIndex; i <= firstDateIndex + 1; i++) {
//        for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
            String startdateString = year + Constants.BIWEEKLY_START_DATES[i];
            String enddateString = year + Constants.BIWEEKLY_END_DATES[i];
            cmorphSumOp = new CmorphSumOp();
            Product[] cmorph1HrSourceProducts;
            try {
                cmorph1HrSourceProducts = AuxdataSourcesProvider.getCmorph1HrSourceProducts(inputDataDir, startdateString, enddateString);
                cmorphSumOp.setSourceProducts(cmorph1HrSourceProducts);
                cmorphSumOp.setParameter("year", year);
                cmorphSumOp.setParameter("outputDataDir", outputDataDir);
                cmorphSumOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
                cmorphSumOp.setParameter("category", DataCategory.CMORPH_BIWEEKLY);
            } catch (ParseException e) {
                throw new OperatorException("Problems while parsing CMORPH input - cannot proceed: " + e.getMessage());
            }

            setTargetProduct(cmorphSumOp.getTargetProduct());

            for (Product sourceProduct : cmorph1HrSourceProducts) {
                sourceProduct.dispose();
            }
        }
    }

    private void writeCmorphDailyProducts() {
        CmorphSumOp cmorphSumOp;

        String dateString = year + month + String.format("%02d", day);
        cmorphSumOp = new CmorphSumOp();
        Product[] cmorph1HrSourceProducts;
        try {
            cmorph1HrSourceProducts = AuxdataSourcesProvider.getCmorph1HrSourceProducts(inputDataDir, dateString, dateString);
            cmorphSumOp.setSourceProducts(cmorph1HrSourceProducts);
            cmorphSumOp.setParameter("year", year);
            cmorphSumOp.setParameter("outputDataDir", outputDataDir);
            cmorphSumOp.setParameter("startdateString", dateString);
            cmorphSumOp.setParameter("category", DataCategory.CMORPH_DAILY);
        } catch (ParseException e) {
            throw new OperatorException("Problems while parsing CMORPH input - cannot proceed: " + e.getMessage());
        }

        setTargetProduct(cmorphSumOp.getTargetProduct());

        for (Product sourceProduct : cmorph1HrSourceProducts) {
            sourceProduct.dispose();
        }
    }


    private void writeGpcpYearlyProducts() {
        GpcpOp gpcpOp = new GpcpOp();
        Product[] gpcpYearlySourceProducts;
        try {
            gpcpYearlySourceProducts = AuxdataSourcesProvider.getGpcpSourceProducts(inputDataDir);
            gpcpOp.setSourceProducts(gpcpYearlySourceProducts);
            gpcpOp.setParameter("outputDataDir", outputDataDir);
        } catch (ParseException e) {
            throw new OperatorException("Problems while parsing GPCP input - cannot proceed: " + e.getMessage());
        }

        setTargetProduct(gpcpOp.getTargetProduct());

        for (Product sourceProduct : gpcpYearlySourceProducts) {
            sourceProduct.dispose();
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
        ndviMaxCompositOp.setParameter("category", category);

        return ndviMaxCompositOp.getTargetProduct();
    }

    private Product getNewNdviMaxcompositProduct() {
        Product[] ndviSourceProducts;
        ndviSourceProducts = AuxdataSourcesProvider.getNewNdviSourceProducts(inputDataDir,
                                                                             year,
                                                                             category,
                                                                             writeNdviFlags);
        NdviMaxCompositOp ndviMaxCompositOp = new NdviMaxCompositOp();
        ndviMaxCompositOp.setSourceProducts(ndviSourceProducts);
        ndviMaxCompositOp.setParameter("year", year);
        ndviMaxCompositOp.setParameter("writeFlags", writeNdviFlags);
        ndviMaxCompositOp.setParameter("category", category);

        final Product ndviMaxCompositProduct = ndviMaxCompositOp.getTargetProduct();

        Product ndviReprojectedMaxCompositProduct = ndviMaxCompositProduct;
        if (category == DataCategory.NDVI_MAXCOMPOSIT_NEW) {
            ndviReprojectedMaxCompositProduct = ReferenceReprojection.reproject(ndviMaxCompositProduct);
        }

        return ndviReprojectedMaxCompositProduct;
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
        ndviOp.setParameter("category", category);

        return ndviOp.getTargetProduct();
    }

    private Product getNewNdviProduct() {
        Product[] ndviSourceProducts;
        ndviSourceProducts = AuxdataSourcesProvider.getNewNdviSourceProducts(inputDataDir,
                                                                             year,
                                                                             category,
                                                                             writeNdviFlags);
        NdviOp ndviOp = new NdviOp();
        ndviOp.setSourceProducts(ndviSourceProducts);
        ndviOp.setParameter("year", year);
        ndviOp.setParameter("writeFlags", writeNdviFlags);
        ndviOp.setParameter("category", category);

        return ndviOp.getTargetProduct();
    }

    private Product getNdviProbavProduct() {
        Product[] ndviSourceProducts;
        ndviSourceProducts = AuxdataSourcesProvider.getNdviProbavSourceProducts(inputDataDir,
                                                                             year,
                                                                             String.format("%02d", probavMonth),
                                                                             probavTile);
        NdviProbaVOp ndviOp = new NdviProbaVOp();
        ndviOp.setSourceProducts(ndviSourceProducts);
        ndviOp.setParameter("year", year);
        ndviOp.setParameter("month", probavMonth);
        ndviOp.setParameter("biweeklyIndex", probavBiweeklyIndex);

        return ndviOp.getTargetProduct();
    }

    private Product getGlobvegProduct() {
        Product[] globvegSourceProducts;
        globvegSourceProducts = AuxdataSourcesProvider.getGlobvegSourceProducts(inputDataDir,
                                                                                year,
                                                                                globvegSite,
                                                                                globvegTile);
        GlobvegOp globvegOp = new GlobvegOp();
        globvegOp.setSourceProducts(globvegSourceProducts);
        globvegOp.setParameter("globvegTile", globvegTile);

        return globvegOp.getTargetProduct();
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
                                                                                        smDataType,
                                                                                        startdateString,
                                                                                        enddateString);
                if (smDailySourceProducts != null && smDailySourceProducts.length > 0) {
                    smAveOp.setSourceProducts(smDailySourceProducts);
                    smAveOp.setParameter("startdateString", Constants.HALFMONTHS[i]);
                    biweeklyAverageProductList.add(smAveOp.getTargetProduct());
                }
            } catch (ParseException e) {
                throw new OperatorException("Problems while parsing Soil Moisture input - cannot proceed: " + e.getMessage());
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
                throw new OperatorException("Problems while parsing Actual Evapotranspiration input - cannot proceed: " +
                                                    e.getMessage());
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

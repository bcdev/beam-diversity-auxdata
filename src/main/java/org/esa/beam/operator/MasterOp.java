package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.DataCategory;
import org.esa.beam.DiversityAuxdataUtils;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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


    public static final String CMAP_INPUT_FILE_NAME = "precip.mon.mean.nc";

    @Override
    public void initialize() throws OperatorException {
        Product[] ndviSourceProducts;
        switch (category) {
            case NDVI:
                ndviSourceProducts = getNdviSourceProducts(inputDataDir, year);
                NdviOp ndviOp = new NdviOp();
                ndviOp.setSourceProducts(ndviSourceProducts);
                ndviOp.setParameter("year", year);
                ndviOp.setParameter("writeFlags", writeNdviFlags);

                final Product ndviProduct = ndviOp.getTargetProduct();
                setTargetProduct(ndviProduct);
                break;
            case NDVI_MAXCOMPOSIT:
                ndviSourceProducts = getNdviSourceProducts(inputDataDir, year);
                NdviMaxCompositOp ndviMaxCompositOp = new NdviMaxCompositOp();
                ndviMaxCompositOp.setSourceProducts(ndviSourceProducts);
                ndviMaxCompositOp.setParameter("year", year);
                ndviMaxCompositOp.setParameter("writeFlags", writeNdviFlags);

                final Product ndviMaxCompositProduct = ndviMaxCompositOp.getTargetProduct();
                setTargetProduct(ndviMaxCompositProduct);
                break;
            case TRMM:
                TrmmOp trmmOp;
                for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
                    String startdateString = year + Constants.BIWEEKLY_START_DATES[i];
                    String enddateString = year + Constants.BIWEEKLY_END_DATES[i];
                    trmmOp = new TrmmOp();
                    Product[] trmmBiweeklySourceProducts = null;
                    try {
                        trmmBiweeklySourceProducts = getTrmm3HrSourceProducts(inputDataDir, startdateString, enddateString);
                        trmmOp.setSourceProducts(trmmBiweeklySourceProducts);
                        trmmOp.setParameter("year", year);
                        trmmOp.setParameter("outputDataDir", outputDataDir);
                        trmmOp.setParameter("startdateString", startdateString);
                    } catch (ParseException e) {
                        // todo
                        e.printStackTrace();
                    }

                    setTargetProduct(trmmOp.getTargetProduct());

                    for (Product sourceProduct : trmmBiweeklySourceProducts) {
                        sourceProduct.dispose();
                    }
                }
                break;
            case CMAP:
                Product cmapSourceProduct = getCmapSourceProduct(inputDataDir);
                // todo: continue
                break;
            case SOIL_MOISTURE:
                List<Product> biweeklyAverageProductList = new ArrayList<Product>();
                // 1. per biweekly period, get daily single products and compute average products
                SoilMoistureBiweeklyAverageOp smAveOp;
                for (int i = 0; i < Constants.BIWEEKLY_START_DATES.length; i++) {
                    String startdateString = year + Constants.BIWEEKLY_START_DATES[i];
                    String enddateString = year + Constants.BIWEEKLY_END_DATES[i];
                    smAveOp = new SoilMoistureBiweeklyAverageOp();
                    Product[] smDailySourceProducts;
                    try {
                        smDailySourceProducts = getSmDailySourceProducts(inputDataDir, startdateString, enddateString);
                        if (smDailySourceProducts != null && smDailySourceProducts.length > 0) {
                            smAveOp.setSourceProducts(smDailySourceProducts);
                            smAveOp.setParameter("year", year);
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


                final Product soilMoistureProduct = smOp.getTargetProduct();
                setTargetProduct(soilMoistureProduct);

                break;
            case ACTUAL_EVAPOTRANSPIRATION:
                break;
            case AIR_TEMPERATURE:
                break;
        }
    }

    private Product[] getSmDailySourceProducts(File inputDataDir, String startDateString, String endDateString) throws ParseException {
        final FileFilter smProductFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() &&
                        file.getName().startsWith("ESACCI-L3S_SOILMOISTURE-SSMV-MERGED-") &&
                        file.getName().toLowerCase().endsWith(".nc");
            }
        };

        Date startDate = SoilMoistureOp.sdfSM.parse(startDateString);
        Date endDate = SoilMoistureOp.sdfSM.parse(endDateString);

        final String smDir = inputDataDir + File.separator + year;
        final File[] smSourceProductFiles = (new File(smDir)).listFiles(smProductFilter);

        List<Product> smSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        for (File smSourceProductFile : smSourceProductFiles) {
            // e.g. ESACCI-L3S_SOILMOISTURE-SSMV-MERGED-20100105000000-fv00.1.nc
            final String productDateString = smSourceProductFile.getName().substring(36, 44);   // 20100105
            try {
                Date productDate = SoilMoistureOp.sdfSM.parse(productDateString);
                if (DiversityAuxdataUtils.isDateWithinPeriod(startDate, endDate, productDate)) {
                    final Product product = ProductIO.readProduct(smSourceProductFile.getAbsolutePath());
                    if (product != null) {
                        smSourceProductsList.add(product);
                        productIndex++;
                    }
                }
            } catch (IOException e) {
                System.err.println("WARNING: Soil Moisture netCDF file '" +
                                           smSourceProductFile.getName() + "' could not be read - skipping.");
            }
        }

        if (productIndex == 0) {
            System.out.println("WARNING: No Soil Moisture netCDF source products found for biweekly period " +
                                       startDateString + " - nothing to do.");
        }

        return smSourceProductsList.toArray(new Product[smSourceProductsList.size()]);
    }

    private Product[] getNdviSourceProducts(File inputDataDir, String year) {
        final FileFilter ndviHalfmonthlyDirsFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() && file.getName().endsWith("-VIg");
            }
        };

        final String ndviDir = inputDataDir + File.separator + year;
        final File[] ndviHalfmonthlyDirs = (new File(ndviDir)).listFiles(ndviHalfmonthlyDirsFilter);

        List<Product> ndviSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        for (File ndviHalfmonthlyDir : ndviHalfmonthlyDirs) {
            if (!(writeNdviFlags && category == DataCategory.NDVI)) {
                String ndviInputFileName = ndviHalfmonthlyDir.getAbsolutePath() + File.separator +
                        ndviHalfmonthlyDir.getName() + "_data.tif";
                try {
                    final Product product = ProductIO.readProduct(ndviInputFileName);
                    if (product != null) {
                        ndviSourceProductsList.add(product);
                        productIndex++;
                    }
                } catch (IOException e) {
                    System.err.println("Warning: NDVI tif file in halfmonthly directory '" +
                                               ndviHalfmonthlyDir.getName() + "' missing or could not be read - skipping.");
                }
            }

            if (writeNdviFlags || category == DataCategory.NDVI_MAXCOMPOSIT) {
                String ndviFlagInputFileName = ndviHalfmonthlyDir.getAbsolutePath() + File.separator +
                        ndviHalfmonthlyDir.getName() + "_flag.tif";
                try {
                    final Product product = ProductIO.readProduct(ndviFlagInputFileName);
                    if (product != null) {
                        ndviSourceProductsList.add(product);
                        productIndex++;
                    }
                } catch (IOException e) {
                    System.err.println("Warning: NDVI flag tif file in halfmonthly directory '" +
                                               ndviHalfmonthlyDir.getName() + "' missing or could not be read - skipping.");
                }
            }
        }

        if (productIndex == 0) {
            System.out.println("No NDVI source products found for year " + year + " - nothing to do.");
        }

        return ndviSourceProductsList.toArray(new Product[ndviSourceProductsList.size()]);
    }

    private Product[] getTrmm3HrSourceProducts(File inputDataDir, String startDateString, String endDateString) throws ParseException {
        final FileFilter trmm3B42ProductFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() &&
                        file.getName().toLowerCase().startsWith("3b42") &&
                        file.getName().toLowerCase().endsWith(".nc");
            }
        };

        Date startDate = TrmmOp.sdfTrmm.parse(startDateString);
        Date endDate = TrmmOp.sdfTrmm.parse(endDateString);

        final String trmmDir = inputDataDir + File.separator + year;
        final File[] trmmSourceProductFiles = (new File(trmmDir)).listFiles(trmm3B42ProductFilter);

        List<Product> trmmSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        for (File trmmSourceProductFile : trmmSourceProductFiles) {
            final String productDateString = trmmSourceProductFile.getName().substring(5, 13);
            try {
                Date productDate = TrmmOp.sdfTrmm.parse(productDateString);
                if (DiversityAuxdataUtils.isDateWithinPeriod(startDate, endDate, productDate)) {
                    final Product product = ProductIO.readProduct(trmmSourceProductFile.getAbsolutePath());
                    if (product != null) {
                        trmmSourceProductsList.add(product);
                        productIndex++;
                    }
                }
            } catch (IOException e) {
                System.err.println("WARNING: TRMM netCDF file '" +
                                           trmmSourceProductFile.getName() + "' could not be read - skipping.");
            }
        }

        if (productIndex == 0) {
            System.out.println("WARNING: No TRMM netCDF source products found for biweekly period " + startDateString + " - nothing to do.");
        }

        return trmmSourceProductsList.toArray(new Product[trmmSourceProductsList.size()]);
    }

    private Product getCmapSourceProduct(File inputDataDir) {
        Product product = null;
        try {
            // todo: make sure 180deg problem is handled!
            product = ProductIO.readProduct(inputDataDir + File.separator + CMAP_INPUT_FILE_NAME);
        } catch (IOException e) {
            System.err.println("Warning: CMAP source file in directory '" +
                                       inputDataDir.getName() + "' missing or could not be read - skipping.");
        }

        return product;
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(MasterOp.class);
        }
    }
}

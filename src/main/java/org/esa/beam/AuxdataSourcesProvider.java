package org.esa.beam;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.operator.SoilMoistureOp;
import org.esa.beam.operator.TrmmBiweeklySumOp;
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.SubBiweeklyProductFraction;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provides the various Diversity auxdata source files to be processed
 *
 * @author olafd
 */
public class AuxdataSourcesProvider {

    private static final String CMAP_INPUT_FILE_NAME = "precip.mon.mean.nc";

    public static Product[] getAe8DaySourceProducts(File inputDataDir,
                                                    String year,
                                                    SubBiweeklyProductFraction eightDayProductFractions,
                                                    String startDateString,
                                                    String endDateString) throws ParseException {
        final FileFilter aeProductFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() &&
                        file.getName().startsWith("MOD16A2_ET_0.05deg_GEO_") &&
                        file.getName().toLowerCase().endsWith(".tif");
            }
        };

        final String aeDir = inputDataDir + File.separator + year;
        final File[] aeSourceProductFiles = (new File(aeDir)).listFiles(aeProductFilter);

        List<Product> aeSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        for (File aeSourceProductFile : aeSourceProductFiles) {
            // e.g. MOD16A2_ET_0.05deg_GEO_2000017.tif
            final String productDoY = aeSourceProductFile.getName().substring(27, 30);   // here 017
            if (DiversityAuxdataUtils.hasBiweeklyOverlap(productDoY, eightDayProductFractions)) {
                try {
                    final Product product = ProductIO.readProduct(aeSourceProductFile.getAbsolutePath());
                    if (product != null) {
                        aeSourceProductsList.add(product);
                        productIndex++;
                    }
                } catch (IOException e) {
                    System.err.println("WARNING: Actual Evapo TIF file '" +
                                               aeSourceProductFile.getName() + "' could not be read - skipping.");
                }
            }
        }

        if (productIndex == 0) {
            System.out.println("WARNING: No Actual Evapo TIF source products found for biweekly period " +
                                       startDateString + " - nothing to do.");
        }

        return aeSourceProductsList.toArray(new Product[aeSourceProductsList.size()]);
    }

    public static Product[] getSmDailySourceProducts(File inputDataDir, String year, String startDateString, String endDateString) throws ParseException {
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


    public static Product[] getNdviSourceProducts(File inputDataDir, String year, DataCategory category, boolean writeNdviFlags) {
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

    public static Product[] getTrmm3HrSourceProducts(File inputDataDir, String startDateString, String endDateString) throws ParseException {
        final FileFilter trmm3B42ProductFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() &&
                        file.getName().toLowerCase().startsWith("3b42") &&
                        file.getName().toLowerCase().endsWith(".nc");
            }
        };

        Date startDate = TrmmBiweeklySumOp.sdfTrmm.parse(startDateString);
        Date endDate = TrmmBiweeklySumOp.sdfTrmm.parse(endDateString);

        final String trmmDir = inputDataDir.getAbsolutePath();
        final File[] trmmSourceProductFiles = (new File(trmmDir)).listFiles(trmm3B42ProductFilter);

        List<Product> trmmSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        if (trmmSourceProductFiles != null && trmmSourceProductFiles.length > 0) {
            for (File trmmSourceProductFile : trmmSourceProductFiles) {
                final String productDateString = trmmSourceProductFile.getName().substring(5, 13);
                try {
                    Date productDate = TrmmBiweeklySumOp.sdfTrmm.parse(productDateString);
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
        }

        if (productIndex == 0) {
            System.out.println("WARNING: No TRMM netCDF source products found for biweekly period " + startDateString + " - nothing to do.");
        }

        return trmmSourceProductsList.toArray(new Product[trmmSourceProductsList.size()]);
    }

    public static Product[] getTrmmBiweeklySourceProducts(File inputDataDir) throws ParseException {
        final FileFilter trmm3B42ProductFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() &&
                        file.getName().startsWith("TRMM") &&
                        (file.getName().endsWith("_a.tif") || file.getName().endsWith("_b.tif"));
            }
        };

        final String trmmDir = inputDataDir.getAbsolutePath();
        final File[] trmmSourceProductFiles = (new File(trmmDir)).listFiles(trmm3B42ProductFilter);

        List<Product> trmmSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        if (trmmSourceProductFiles != null && trmmSourceProductFiles.length > 0) {
            for (File trmmSourceProductFile : trmmSourceProductFiles) {
                try {
                    final Product product = ProductIO.readProduct(trmmSourceProductFile.getAbsolutePath());
                    if (product != null) {
                        trmmSourceProductsList.add(product);
                        productIndex++;
                    }
                } catch (IOException e) {
                    System.err.println("WARNING: TRMM netCDF file '" +
                                               trmmSourceProductFile.getName() + "' could not be read - skipping.");
                }
            }
        }

        if (productIndex == 0) {
            System.out.println("WARNING: No TRMM biweekly products found in " + trmmDir + " - nothing to do.");
        }

        return trmmSourceProductsList.toArray(new Product[trmmSourceProductsList.size()]);
    }

    public static Product[] getAirTempSourceProducts(File inputDataDir, final String year) {
        final FileFilter airTempMonthlyProductFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                // e.g. 't012006.tif'
                return file.isFile() &&
                        file.getName().contains(year) &&
                        file.getName().toLowerCase().startsWith("t") &&
                        file.getName().toLowerCase().endsWith(".tif");
            }
        };

        final String airTempDir = inputDataDir.getAbsolutePath();
        final File[] airTempSourceProductFiles = (new File(airTempDir)).listFiles(airTempMonthlyProductFilter);

        List<Product> airTempSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        for (File airTempSourceProductFile : airTempSourceProductFiles) {
            // e.g. 't012006.tif'
            try {
                final Product product = ProductIO.readProduct(airTempSourceProductFile.getAbsolutePath());
                if (product != null) {
                    airTempSourceProductsList.add(product);
                    productIndex++;
                }
            } catch (IOException e) {
                System.err.println("WARNING: Actual Air temperature TIF file '" +
                                           airTempSourceProductFile.getName() + "' could not be read - skipping.");
            }
        }

        if (productIndex == 0) {
            System.out.println("WARNING: No Actual Evapo TIF source products found for year " +
                                       year + " - nothing to do.");
        }

        return airTempSourceProductsList.toArray(new Product[airTempSourceProductsList.size()]);
    }

    public static Product[] getCmapPentadSplittedSourceProducts(File inputFile, String year) {
        final Product cmapPentadSourceProduct = getCmapPentadSourceProduct(inputFile);

        // get bands from yearly splitting: 1-73 --> 1979; 74-146 --> 1980, etc.
        List<Product> splittedProductList = new ArrayList<Product>();

        final int iYear = Integer.parseInt(year);
        final int startIndex = (iYear - Constants.CMAP_START_YEAR) * Constants.CMAP_NUM_PENTADS_PER_YEAR + 1;
        for (int i = startIndex; i < startIndex + Constants.CMAP_NUM_PENTADS_PER_YEAR; i++) {
            final Band sourceBand = cmapPentadSourceProduct.getBand(Constants.PRECIP_BAND_NAME_PREFIX + i);
            if (sourceBand != null) {
                final int doy = 5*(i - startIndex) + 1;
                Product splittedProduct = createCmapSplittedProduct(cmapPentadSourceProduct, year, doy);
                String targetBandName = Constants.PRECIP_BAND_NAME_PREFIX;
                ProductUtils.copyBand(sourceBand.getName(), cmapPentadSourceProduct, targetBandName, splittedProduct, true);
                splittedProductList.add(splittedProduct);
            }
        }

        return splittedProductList.toArray(new Product[splittedProductList.size()]);
    }

    private static Product getCmapPentadSourceProduct(File inputDataDir) {
        final String filePath = inputDataDir + File.separator + "precip.pentad.mean.nc";
        Product product = null;
        try {
            product = ProductIO.readProduct(filePath);
        } catch (IOException e) {
            System.err.println("WARNING: Actual CMAP file '" +
                                       filePath + "' missing or could not be read - skipping.");
        }
        return product;
    }

    private static Product createCmapSplittedProduct(Product sourceProduct, String year, int startDoy) {
        final int width = sourceProduct.getSceneRasterWidth();
        final int height = sourceProduct.getSceneRasterHeight();

        Product splittedProduct = new Product(String.format("DIVERSITY_CMAP_%s%03d", year, startDoy),
                                              String.format("DIVERSITY_CMAP_%s%03d", year, startDoy),
                                              width,
                                              height);
        ProductUtils.copyGeoCoding(sourceProduct, splittedProduct);

        return splittedProduct;
    }
}

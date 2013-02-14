package org.esa.beam;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.operator.ActualEvapoOp;
import org.esa.beam.operator.SoilMoistureOp;
import org.esa.beam.operator.TrmmBiweeklySumOp;
import org.esa.beam.util.BiweeklyProductFraction;
import org.esa.beam.util.DiversityAuxdataUtils;

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
                                                    BiweeklyProductFraction eightDayProductFractions,
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

        Date startDate = ActualEvapoOp.sdfAE.parse(startDateString);
        Date endDate = ActualEvapoOp.sdfAE.parse(endDateString);

        final String aeDir = inputDataDir + File.separator + year;
        final File[] aeSourceProductFiles = (new File(aeDir)).listFiles(aeProductFilter);

        List<Product> aeSourceProductsList = new ArrayList<Product>();

        int productIndex = 0;
        for (File aeSourceProductFile : aeSourceProductFiles) {
            // e.g. MOD16A2_ET_0.05deg_GEO_2000017.tif
            final String productDoY = aeSourceProductFile.getName().substring(28, 30);   // here 017
            if (DiversityAuxdataUtils.hasBiweeklyOverlap(productDoY, eightDayProductFractions)) {
                try {
                    Date productDate = SoilMoistureOp.sdfSM.parse(productDoY);
                    if (DiversityAuxdataUtils.isDateWithinPeriod(startDate, endDate, productDate)) {
                        final Product product = ProductIO.readProduct(aeSourceProductFile.getAbsolutePath());
                        if (product != null) {
                            aeSourceProductsList.add(product);
                            productIndex++;
                        }
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

    public static Product getCmapSourceProduct(File inputDataDir) {
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

}

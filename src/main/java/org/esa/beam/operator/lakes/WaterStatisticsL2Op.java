package org.esa.beam.operator.lakes;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.collocation.CollocateOp;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.experimental.Output;
import org.esa.beam.util.math.MathUtils;
import org.esa.beam.util.math.RsMathUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Computes water coverage statistics as WaterStatisticsOp, but for L2 input
 *
 * @author Martin Boettcher, Olaf Danne, Daniel Odermatt
 */
@OperatorMetadata(alias = "WaterStatisticsL2Op",
                  version = "1.0",
                  authors = "Martin Boettcher",
                  copyright = "(c) 2013 by Brockmann Consult GmbH",
                  description = "Computes water coverage statistics as WaterStatisticsOp, but for L2 input")
public class WaterStatisticsL2Op extends Operator implements Output {

    private static SimpleDateFormat CCSDS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    static {
        CCSDS_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    @SourceProduct(alias = "source", description = "The source product with the water_in_basin band.")
    Product sourceProduct;

    @SourceProduct(alias = "mask", description = "The water mask with a water_extent band.")
    private Product maskProduct;

    @Parameter(description = "Region name")
    String regionName;

    @Parameter(alias = "csv", description = "The target file for ASCII output.", notNull = true)
    File outputAsciiFile;


    @Override
    public void initialize() throws OperatorException {
        setDummyTargetProduct();
        final CollocateOp collocateOp = new CollocateOp();
        collocateOp.setMasterProduct(sourceProduct);
        collocateOp.setSlaveProduct(maskProduct);
        Product collocatedInput = collocateOp.getTargetProduct();

        final GeoCoding geoCoding = collocatedInput.getGeoCoding();
        final Band basinExtent = collocatedInput.getBand("basin_extent_M");
        final Band waterInBasin = collocatedInput.getBand("water_in_basin_M");
        final Band cloudsInBasin = collocatedInput.getBand("clouds_in_basin_M");
        final Band cloudsExBasin = collocatedInput.getBand("clouds_ex_basin_M");
        final Band cloudIndicator = collocatedInput.getBand("cloud_indicator_M");
        try {
            basinExtent.readRasterDataFully(ProgressMonitor.NULL);
            waterInBasin.readRasterDataFully(ProgressMonitor.NULL);
            cloudsInBasin.readRasterDataFully(ProgressMonitor.NULL);
            cloudsExBasin.readRasterDataFully(ProgressMonitor.NULL);
            cloudIndicator.readRasterDataFully(ProgressMonitor.NULL);
            GeoPos p0 = new GeoPos();
            GeoPos p1 = new GeoPos();
            GeoPos p2 = new GeoPos();
            GeoPos p3 = new GeoPos();
            GeoPos p4 = new GeoPos();

            double basinExtentArea = 0.0;
            double waterInBasinArea = 0.0;
            double cloudsInBasinArea = 0.0;
            double cloudsExBasinArea = 0.0;
            double fovTotalArea = 0.0;
            double fovBasinArea = 0.0;

            for (int y = 0; y < waterInBasin.getRasterHeight(); ++y) {
                for (int x = 0; x < waterInBasin.getRasterWidth(); ++x) {
                    float basinExtentValue = basinExtent.getSampleFloat(x, y);
                    float waterInBasinValue = waterInBasin.getSampleFloat(x, y);
                    float cloudsInBasinValue = cloudsInBasin.getSampleFloat(x, y);
                    float cloudsExBasinValue = cloudsExBasin.getSampleFloat(x, y);
                    float cloudIndicatorValue = cloudIndicator.getSampleFloat(x, y);
                    geoCoding.getGeoPos(new PixelPos(x + 0.5f, y + 0.5f), p0);
                    geoCoding.getGeoPos(new PixelPos(x + 0.0f, y + 0.5f), p1);
                    geoCoding.getGeoPos(new PixelPos(x + 1.0f, y + 0.5f), p2);
                    geoCoding.getGeoPos(new PixelPos(x + 0.5f, y + 0.0f), p3);
                    geoCoding.getGeoPos(new PixelPos(x + 0.5f, y + 1.0f), p4);
                    double r2 = Math.cos(p0.getLat() * MathUtils.DTOR);
                    double p12 = Math.sqrt(sqr((p2.getLat() - p1.getLat()) * MathUtils.DTOR) + sqr(r2 * (p2.getLon() - p1.getLon()) * MathUtils.DTOR));
                    double p34 = Math.sqrt(sqr((p4.getLat() - p3.getLat()) * MathUtils.DTOR) + sqr(r2 * (p4.getLon() - p3.getLon()) * MathUtils.DTOR));
                    double a = p12 * p34 * sqr(RsMathUtils.MEAN_EARTH_RADIUS / 1000.0);

                    if (waterInBasinValue == 1) {
                        waterInBasinArea += a;
                    }
                    if (basinExtentValue == 1) {
                        basinExtentArea += a;
                    }
                    if (cloudIndicatorValue > 0.5 && basinExtentValue == 1) {
                        fovBasinArea += a;
                    }
                    if (cloudsInBasinValue == 1) {
                        cloudsInBasinArea += a;
                    }
                    if (cloudsExBasinValue == 1) {
                        cloudsExBasinArea += a;
                    }
                    if (cloudIndicatorValue > 0.5) {
                        fovTotalArea += a;
                    }
                }
            }

            PrintStream csvOutputStream = new PrintStream(new FileOutputStream(outputAsciiFile));

            Date startDate;
            if (sourceProduct.getStartTime() != null) {
                startDate = sourceProduct.getStartTime().getAsDate();
            } else {
                // take from input filename:
                // e.g. NDWI_L2_of_MER_FSG_1PNUPA20030306_003039_000004252014_00231_05295_3644.dim
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
                final String startTimeString = sourceProduct.getName().substring(25, 40);
                startDate = (Date) sdf.parseObject(startTimeString);
            }

            csvOutputStream.println("Region" + "\t" +
                                            "Start Date" + "\t" +
                                            "Total Basin Area" + "\t" +
                                            "FOV Area "+ "\t" +
                                            "Basin in FOV Area" + "\t" +
                                            "Water Area" + "\t" +
                                            "Clouds-in-basin Area" + "\t" +
                                            "Clouds-ex-basin Area");
            csvOutputStream.println(regionName + "\t" +
                                            CCSDS_DATE_FORMAT.format(startDate) + "\t" +
                                            basinExtentArea + "\t" +
                                            fovTotalArea + "\t" +
                                            fovBasinArea + "\t" +
                                            waterInBasinArea + "\t" +
                                            cloudsInBasinArea + "\t" +
                                            cloudsExBasinArea);
            csvOutputStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new OperatorException("water statistics operator failed", ex);
        }
    }

    private static double sqr(double v) {
        return v * v;
    }

    private void setDummyTargetProduct() {
        final Product product = new Product("dummy", "dummy", 2, 2);
//        product.addBand("dummy", ProductData.TYPE_INT8);
        setTargetProduct(product);
    }

    /**
     * The service provider interface (SPI) which is referenced
     * in {@code /META-INF/services/org.esa.beam.framework.gpf.OperatorSpi}.
     */
    public static class Spi extends OperatorSpi {

        public Spi() {
            super(WaterStatisticsL2Op.class);
        }
    }

}
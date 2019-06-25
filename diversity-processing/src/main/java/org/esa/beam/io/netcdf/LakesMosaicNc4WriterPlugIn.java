package org.esa.beam.io.netcdf;

import com.bc.ceres.binding.converters.DateFormatConverter;
import org.esa.beam.dataio.netcdf.AbstractNetCdfWriterPlugIn;
import org.esa.beam.dataio.netcdf.ProfileWriteContext;
import org.esa.beam.dataio.netcdf.metadata.ProfileInitPartWriter;
import org.esa.beam.dataio.netcdf.metadata.ProfilePartWriter;
import org.esa.beam.dataio.netcdf.metadata.profiles.cf.CfGeocodingPart;
import org.esa.beam.dataio.netcdf.nc.NFileWriteable;
import org.esa.beam.dataio.netcdf.nc.NVariable;
import org.esa.beam.dataio.netcdf.nc.NWritableFactory;
import org.esa.beam.dataio.netcdf.util.Constants;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.jai.ImageManager;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writer for CF compliant Albedo NetCDF4 output
 *
 * @author olafd
 */
public class LakesMosaicNc4WriterPlugIn extends AbstractNetCdfWriterPlugIn {

    @Override
    public String[] getFormatNames() {
        return new String[]{"NetCDF4-LAKES-MOSAIC"};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[]{Constants.FILE_EXTENSION_NC};
    }

    @Override
    public String getDescription(Locale locale) {
        return "Diversity Lakes NetCDF4 mosaic products";
    }

    @Override
    public ProfilePartWriter createGeoCodingPartWriter() {
        return new CfGeocodingPart();
    }

    @Override
    public ProfileInitPartWriter createInitialisationPartWriter() {
        return new LakesMosaicNc4MainPart();
    }

    @Override
    public NFileWriteable createWritable(String outputPath) throws IOException {
        this.outputPath = outputPath;
        return NWritableFactory.create(outputPath, "netcdf4");
    }

    private String outputPath;

    private class LakesMosaicNc4MainPart implements ProfileInitPartWriter {

        private final SimpleDateFormat COMPACT_ISO_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        private Dimension tileSize;

        LakesMosaicNc4MainPart() {
            COMPACT_ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public void writeProductBody(ProfileWriteContext ctx, Product product) throws IOException {
            NFileWriteable writeable = ctx.getNetcdfFileWriteable();
            tileSize = ImageManager.getPreferredTileSize(product);
            writeable.addDimension("y", product.getSceneRasterHeight());
            writeable.addDimension("x", product.getSceneRasterWidth());

//            addGlobalAttributes(writeable, product, outputPath);

            for (String bandName : DiversityNcConstants.LAKES_MOSAIC_BAND_NAMES) {
                Band b = product.getBand(bandName);
                if (b != null) {
                    final String unit = "1";  // todo
                    addNc4VariableWithAttributes(writeable, b, unit);
                }
            }
        }


        private void addGlobalAttributes(NFileWriteable writeable, Product product, String outputPath) throws IOException {
            writeable.addGlobalAttribute("title", "QA4ECV Albedo Product");
            final String institution = "Mullard Space Science Laboratory, " +
                    "Department of Space and Climate Physics, University College London";
            writeable.addGlobalAttribute("institution", institution);
            writeable.addGlobalAttribute("source", "Satellite observations, BRDF/Albedo Inversion Model");
            writeable.addGlobalAttribute("history", "QA4ECV Processing, 2014-2017");
            writeable.addGlobalAttribute("references", "www.qa4ecv.eu");
            writeable.addGlobalAttribute("tracking_id", UUID.randomUUID().toString());
            writeable.addGlobalAttribute("Conventions", "CF-1.6");
            writeable.addGlobalAttribute("product_version", "1.0");
            writeable.addGlobalAttribute("summary", "This dataset contains Level-3 daily surface broadband albedo " +
                    "products. Level-3 data are raw observations processed to geophysical quantities, and placed onto" +
                    " a regular grid.");
            writeable.addGlobalAttribute("keywords", "Albedo, TIP, FAPAR");
            writeable.addGlobalAttribute("id", product.getName() + ".nc");
            writeable.addGlobalAttribute("naming_authority", "ucl.ac.uk");
            writeable.addGlobalAttribute("keywords_vocabulary", "");
            writeable.addGlobalAttribute("cdm_data_type", "Alb");

            writeable.addGlobalAttribute("comment", "These data were produced in the frame of the QA4ECV " +
                    "(Quality Assurance for Essential Climate Variables) project, funded under theme 9 (Space) " +
                    "of the European Union Framework Program 7.");

            final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            final Date creationDate = calendar.getTime();
            final DateFormatConverter dateFormatConverter = new DateFormatConverter(COMPACT_ISO_FORMAT);
            writeable.addGlobalAttribute("date_created", dateFormatConverter.format(creationDate));
            writeable.addGlobalAttribute("creator_name", institution);
            writeable.addGlobalAttribute("creator_url", "www.ucl.ac.uk/mssl");
            writeable.addGlobalAttribute("creator_email", "qa4ecv-land-all@ucl.ac.uk");
            writeable.addGlobalAttribute("project", "QA4ECV - European Union Framework Program 7");
            writeable.addGlobalAttribute("geospatial_lat_min", "-90.0");
            writeable.addGlobalAttribute("geospatial_lat_max", "90.0");
            writeable.addGlobalAttribute("geospatial_lon_min", "-180.0");
            writeable.addGlobalAttribute("geospatial_lon_max", "180.0");
            writeable.addGlobalAttribute("geospatial_vertical_min", "0.0");
            writeable.addGlobalAttribute("geospatial_vertical_max", "0.0");

            setProductTime(writeable, outputPath, calendar, dateFormatConverter);

            writeable.addGlobalAttribute("time_coverage_duration", "P1D");
            writeable.addGlobalAttribute("time_coverage_resolution", "P1D");
            writeable.addGlobalAttribute("standard_name_vocabulary",
                                         "NetCDF Climate and Forecast (CF) Metadata Convention version 18");
            writeable.addGlobalAttribute("license", "QA4ECV Data Policy: free and open access");
            writeable.addGlobalAttribute("platform", "NOAA-xx, Meteosat MFG, Meteosat MSG, GOES, GMS");
            writeable.addGlobalAttribute("sensor", "AVHRR, MVIRI, SEVIRI, Imager, VISSR");
            writeable.addGlobalAttribute("spatial_resolution", "1km at Equator");
            writeable.addGlobalAttribute("geospatial_lat_units", "degrees_north");
            writeable.addGlobalAttribute("geospatial_lon_units", "degrees_east");
            writeable.addGlobalAttribute("geospatial_lat_resolution", "0.5");
            writeable.addGlobalAttribute("geospatial_lon_resolution", "0.5");
        }

        private void setProductTime(NFileWriteable writeable, String outputPath, Calendar calendar, DateFormatConverter dateFormatConverter) throws IOException {
            final Pattern pattern = Pattern.compile(".[0-9]{7}.");
            Matcher matcher = pattern.matcher(outputPath);
            if (matcher.find()) {
                final int start = matcher.start() + 1;
                final int end = matcher.end() - 1;
                final int year = Integer.parseInt(outputPath.substring(start, start + 4));
                final int doy = Integer.parseInt(outputPath.substring(start + 4, end));
                final int zoneOffset = 0;
                calendar.set(GregorianCalendar.YEAR, year);
                calendar.set(GregorianCalendar.DAY_OF_YEAR, doy);
                calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
                calendar.set(GregorianCalendar.MINUTE, 0);
                calendar.set(GregorianCalendar.SECOND, 0);
                calendar.set(GregorianCalendar.ZONE_OFFSET, zoneOffset);
                final String productStart = dateFormatConverter.format(calendar.getTime());
                writeable.addGlobalAttribute("time_coverage_start", productStart);
                calendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
                calendar.set(GregorianCalendar.MINUTE, 59);
                calendar.set(GregorianCalendar.SECOND, 59);
                calendar.set(GregorianCalendar.ZONE_OFFSET, zoneOffset);
                final String productEnd = dateFormatConverter.format(calendar.getTime());
                writeable.addGlobalAttribute("time_coverage_end", productEnd);
            }
        }

        private void addNc4VariableWithAttributes(NFileWriteable writeable, Band b, String unit) throws IOException {
            NVariable variable = addNc4Variable(writeable, b);
            variable.addAttribute("long_name", b.getName());
            final Float _fillValue = (float) b.getNoDataValue();
            variable.addAttribute("_FillValue", _fillValue);
            variable.addAttribute("units", unit);
            variable.addAttribute("coordinates", "lat lon");
        }

        private NVariable addNc4Variable(NFileWriteable writeable, Band b) throws IOException {
            return writeable.addVariable(b.getName(),
                                         DataTypeUtils.getNetcdfDataType(ProductData.TYPE_FLOAT32),
                                         tileSize, writeable.getDimensions());
        }

    }
}
package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.Constants;
import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.*;
import org.esa.beam.gpf.operators.standard.WriteOp;
import org.esa.beam.gpf.operators.standard.reproject.ReprojectionOp;
import org.esa.beam.util.DiversityAuxdataUtils;
import org.esa.beam.util.ProductUtils;
import org.esa.beam.util.SubBiweeklyProductFraction;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Operator for preparation/modification of Diversity CMAP auxdata
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Cmap", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for preparation/modification of Diversity CMAP auxdata.")
public class CmapOp extends Operator {

    @SourceProducts(description = "Cmap biweekly source products")
    private Product[] sourceProducts;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Parameter(defaultValue = "", description = "The year to process")
    private String year;


    public static final SimpleDateFormat sdfCMAP = new SimpleDateFormat("yyyyMMdd");

    public static final String CMAP_TARGET_BAND_PREFIX = "precip";

    private Product[] sortedSourceProducts;


    @Override
    public void initialize() throws OperatorException {
        sortedSourceProducts = DiversityAuxdataUtils.sortProductsByMonth(sourceProducts, null, 24, 28);

        // create target product and copy the biweekly bands
        final Product yearlyCmapProduct = createYearlyProduct();

        // reproject to NDVI grid
        final Product yearlyCmapReprojectedProduct = ReferenceReprojection.reproject(yearlyCmapProduct);
        setTargetProduct(yearlyCmapReprojectedProduct);
//        setTargetProduct(yearlyCmapProduct);
    }

    private Product createYearlyProduct() {
        final int width = sourceProducts[0].getSceneRasterWidth();
        final int height = sourceProducts[0].getSceneRasterHeight();

        Product yearlyProduct = new Product("DIVERSITY_CMAP_" + year,
                                            "DIVERSITY_CMAP",
                                            width,
                                            height);

        ProductUtils.copyGeoCoding(sourceProducts[0], yearlyProduct);
        copyBands(yearlyProduct);

        return yearlyProduct;
    }

    private void copyBands(Product yearlyProduct) {
        for (Product sourceProduct : sortedSourceProducts) {
            final String sourceBandName = sourceProduct.getBandAt(0).getName();
            ProductUtils.copyBand(sourceBandName, sourceProduct, yearlyProduct, true);
            yearlyProduct.getBand(sourceBandName).setNoDataValue(Constants.CMAP_INVALID_VALUE);
            yearlyProduct.getBand(sourceBandName).setNoDataValueUsed(true);
        }
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(CmapOp.class);
        }
    }
}

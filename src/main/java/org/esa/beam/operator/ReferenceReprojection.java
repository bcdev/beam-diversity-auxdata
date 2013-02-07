package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.gpf.operators.standard.reproject.ReprojectionOp;
import org.esa.beam.jai.ImageManager;
import org.esa.beam.util.ProductUtils;

import java.awt.geom.AffineTransform;

/**
 * Reference reprojection class
 * --> reprojects onto NDVI global grid (4950x2091 pixels)
 *
 * @author olafd
 */
public class ReferenceReprojection {

    public static Product reproject(Product sourceProduct) {
        ReprojectionOp repro = new ReprojectionOp();

        repro.setParameter("easting", 0.0);
        repro.setParameter("northing", 0.0);
        repro.setParameter("crs", "EPSG:4326");
        repro.setParameter("resampling", "Nearest");
        repro.setParameter("includeTiePointGrids", false);
        repro.setParameter("referencePixelX", Constants.NDVI_REFERENCE_GRID_ZEROLATLON_X + 0.5);
        repro.setParameter("referencePixelY", Constants.NDVI_REFERENCE_GRID_ZEROLATLON_Y + 0.5);
        repro.setParameter("orientation", 0.0);
        repro.setParameter("pixelSizeX", 0.07272727);
        repro.setParameter("pixelSizeY", 0.07272727);
        repro.setParameter("width", Constants.NDVI_REFERENCE_GRID_WIDTH);
        repro.setParameter("height", Constants.NDVI_REFERENCE_GRID_HEIGHT);
        repro.setParameter("orthorectify", true);
        repro.setSourceProduct(sourceProduct);

        final Product reprojectedProduct = repro.getTargetProduct();
        reprojectedProduct.setName(sourceProduct.getName());
        reprojectedProduct.setProductType(sourceProduct.getProductType());

        return reprojectedProduct;
    }

}

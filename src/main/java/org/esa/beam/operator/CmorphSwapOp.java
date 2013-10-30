package org.esa.beam.operator;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.SubProgressMonitor;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.esa.beam.framework.gpf.annotations.TargetProduct;
import org.esa.beam.util.ProductUtils;

import java.awt.*;
import java.io.IOException;

/**
 * Operator for swap at zero meridian
 *
 * @author olafd
 */
@OperatorMetadata(alias = "Diversity.Auxdata.Cmorph.swap", version = "1.0",
                  authors = "Olaf Danne",
                  copyright = "(c) 2013 Brockmann Consult",
                  internal = true,
                  description = "Operator for swap at zero meridian.")
public class CmorphSwapOp extends Operator {

    @SourceProduct(description = "CMORPH source product")
    private Product sourceProduct;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    private int sourceW;
    private int sourceH;

    private Band precipSourceBand;


    @Override
    public void initialize() throws OperatorException {
        sourceW = sourceProduct.getSceneRasterWidth();
        sourceH = sourceProduct.getSceneRasterHeight();
        precipSourceBand = sourceProduct.getBand(CmorphSumOp.DAILY_PRECIP_BAND_NAME);

        setTargetProduct(createTargetProduct());
    }

    @Override
    public void computeTile(Band targetBand, Tile targetTile, ProgressMonitor pm) throws OperatorException {
        final Rectangle targetRectangle = targetTile.getRectangle();

        ProductData rasterData = ProductData.createInstance(new float[sourceW]);

        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            try {
                precipSourceBand.readRasterData(0, y, sourceW, 1, rasterData, SubProgressMonitor.create(pm, 1));
                for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {
                    int sourceX = getSwappedX(targetRectangle.x, x, sourceW);
                    targetTile.setSample(x, y, rasterData.getElemFloatAt(sourceX));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static int getSwappedX(int rectX, int x, int lineWidth) {
        int sourceX;
        if (lineWidth % 2 == 0) {
            // even number of pixels
            if (rectX + x < lineWidth / 2) {
                // left image half
                sourceX = rectX + x + lineWidth / 2;
            } else {
                // right image half
                sourceX = rectX + x - lineWidth / 2;
            }
        } else {
            // odd number of pixels
            if (rectX + x < lineWidth / 2) {
                // left image half
                sourceX = rectX + x + (lineWidth+1)/2;
            } else if (rectX + x > lineWidth / 2) {
                // right image half
                sourceX = rectX + x - (lineWidth+1)/2;
            } else {
                // center column unchanged
                sourceX = rectX + x;
            }
        }
        return sourceX;
    }

    private Product createTargetProduct() {
        Product swapProduct = new Product("CMORPH_SWAP",
                                          "CMORPH_SWAP",
                                          sourceW,
                                          sourceH);

        swapProduct.setPreferredTileSize(sourceW, 16);  // set over full product width!!!
        ProductUtils.copyGeoCoding(sourceProduct, swapProduct);
        ProductUtils.copyMetadata(sourceProduct, swapProduct);
        Band precipBand = swapProduct.addBand(CmorphSumOp.DAILY_PRECIP_BAND_NAME, ProductData.TYPE_FLOAT32);
        precipBand.setValidPixelExpression(precipSourceBand.getValidPixelExpression());
        precipBand.setDescription(precipSourceBand.getDescription());
        precipBand.setUnit(precipSourceBand.getUnit());
        precipBand.setGeophysicalNoDataValue(precipSourceBand.getGeophysicalNoDataValue());
        precipBand.setNoDataValue(precipSourceBand.getNoDataValue());
        precipBand.setNoDataValueUsed(precipSourceBand.isNoDataValueUsed());

        return swapProduct;
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(CmorphSwapOp.class);
        }
    }
}

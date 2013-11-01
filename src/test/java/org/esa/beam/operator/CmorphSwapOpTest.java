package org.esa.beam.operator;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.internal.TileImpl;
import org.esa.beam.operator.CmorphSwapOp;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;


public class CmorphSwapOpTest {
    private static Product product1;
    private static Product product2;

    @BeforeClass
    public static void setup() {
        product1 = new Product("test1", "test1", 5, 1);
        Band b1 = product1.addBand("b1", ProductData.TYPE_FLOAT64);
        b1.setRasterData(b1.createCompatibleRasterData());
        Tile tile1 = new TileImpl(b1, b1.getSourceImage().getData());
        tile1.setSample(0, 0, 2.4f);
        tile1.setSample(0, 0, 3.6f);
        tile1.setSample(0, 0, 4.5f);
        tile1.setSample(0, 0, 6.7f);
        tile1.setSample(0, 0, 8.9f);

        product2 = new Product("test2", "test2", 6, 1);
        Band b2 = product2.addBand("b2", ProductData.TYPE_FLOAT64);
        b2.setRasterData(b2.createCompatibleRasterData());
        Tile tile2 = new TileImpl(b2, b2.getSourceImage().getData());
        tile2.setSample(0, 0, 2.4f);
        tile2.setSample(0, 0, 3.6f);
        tile2.setSample(0, 0, 4.5f);
        tile2.setSample(0, 0, 6.7f);
        tile2.setSample(0, 0, 8.9f);
        tile2.setSample(0, 0, 10.3f);
    }

    @Test
    public void testCmorphSwap() throws IOException {
        float[] actual = new float[]{
                8.9f, 6.7f, 4.5f, 3.6f, 2.4f, 1.1f
        };
        int[] expectedI = new int[]{
                3, 4, 5, 0, 1, 2
        };
        assertEquals(expectedI[0], CmorphSwapOp.getSwappedX(0, actual.length));
        assertEquals(expectedI[1], CmorphSwapOp.getSwappedX(1, actual.length));
        assertEquals(expectedI[2], CmorphSwapOp.getSwappedX(2, actual.length));
        assertEquals(expectedI[3], CmorphSwapOp.getSwappedX(3, actual.length));
        assertEquals(expectedI[4], CmorphSwapOp.getSwappedX(4, actual.length));
        assertEquals(expectedI[5], CmorphSwapOp.getSwappedX(5, actual.length));

        actual = new float[]{
                8.9f, 6.7f, 4.5f, 3.6f, 2.4f
        };

        expectedI = new int[]{
                3, 4, 2, 0, 1
        };

        assertEquals(expectedI[0], CmorphSwapOp.getSwappedX(0, actual.length));
        assertEquals(expectedI[1], CmorphSwapOp.getSwappedX(1, actual.length));
        assertEquals(expectedI[2], CmorphSwapOp.getSwappedX(2, actual.length));
        assertEquals(expectedI[3], CmorphSwapOp.getSwappedX(3, actual.length));
        assertEquals(expectedI[4], CmorphSwapOp.getSwappedX(4, actual.length));
    }
}

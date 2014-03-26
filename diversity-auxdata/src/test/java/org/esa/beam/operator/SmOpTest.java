package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Tile;
import org.esa.beam.framework.gpf.internal.TileImpl;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class SmOpTest {

    @Test
    public void testGetSmBiweeklyAverage() {
        Tile[] tiles = new Tile[3];

        Product p1 = new Product("test1", "test1", 1, 1);
        Band b1 = p1.addBand("b0", ProductData.TYPE_FLOAT64);
        b1.setRasterData(b1.createCompatibleRasterData());
        tiles[0] = new TileImpl(b1, b1.getSourceImage().getData());
        tiles[0].setSample(0, 0, 2.4);

        Product p2 = new Product("test2", "test2", 1, 1);
        Band b2 = p2.addBand("b0", ProductData.TYPE_FLOAT64);
        b2.setRasterData(b2.createCompatibleRasterData());
        tiles[1] = new TileImpl(b2, b2.getSourceImage().getData());
        tiles[1].setSample(0, 0, 1.6);

        Product p3 = new Product("test3", "test3", 1, 1);
        Band b3 = p3.addBand("b0", ProductData.TYPE_FLOAT64);
        b3.setRasterData(b3.createCompatibleRasterData());
        tiles[2] = new TileImpl(b3, b3.getSourceImage().getData());
        tiles[2].setSample(0, 0, 5.0);

        double average = SoilMoistureBiweeklyAverageOp.getSmBiweeklyAverage(tiles, 0, 0);
        assertEquals(3.0, average);

        tiles[0].setSample(0, 0, 0.3);
        tiles[1].setSample(0, 0, 12.4);
        tiles[2].setSample(0, 0, 23.3);
        average = SoilMoistureBiweeklyAverageOp.getSmBiweeklyAverage(tiles, 0, 0);
        assertEquals(12.0, average);

        tiles[0].setSample(0, 0, 0.3);
        tiles[1].setSample(0, 0, Constants.SM_INVALID_VALUE);
        tiles[2].setSample(0, 0, 23.3);
        average = SoilMoistureBiweeklyAverageOp.getSmBiweeklyAverage(tiles, 0, 0);
        assertEquals(11.8, average);

        tiles[0].setSample(0, 0, Constants.SM_INVALID_VALUE);
        tiles[1].setSample(0, 0, Constants.SM_INVALID_VALUE);
        tiles[2].setSample(0, 0, Constants.SM_INVALID_VALUE);
        average = SoilMoistureBiweeklyAverageOp.getSmBiweeklyAverage(tiles, 0, 0);
        assertEquals(Constants.SM_INVALID_VALUE, average);
    }
}

package org.esa.beam.operator.mph_chl;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class MphChlOpAcceptanceTest {

    private File testOutDirectory;

    @Before
    public void setUp() {
        testOutDirectory = new File("output");
        if (!testOutDirectory.mkdirs()) {
            fail("unable to create test directory: " + testOutDirectory);
        }

        GPF.getDefaultInstance().getOperatorSpiRegistry().addOperatorSpi(new MphChlOp.Spi());
    }

    @After
    public void tearDown() {
        GPF.getDefaultInstance().getOperatorSpiRegistry().removeOperatorSpi(new MphChlOp.Spi());

        if (testOutDirectory != null) {
            if (!FileUtils.deleteTree(testOutDirectory)) {
                fail("Unable to delete test directory: " + testOutDirectory);
            }
        }
    }

    // @todo 1 tb/tb reanimate with Daniels i/o data pairs
//    @Test
//    public void testComputeMphChlProduct() throws IOException {
//        final Product brrProduct = MerisBrrProduct.create();
//
//        final Product mphChlProduct = GPF.createProduct("Diversity.MPH.CHL", GPF.NO_PARAMS, brrProduct);
//
//        Product savedProduct = null;
//        try {
//            final String targetProductPath = testOutDirectory.getAbsolutePath() + File.separator + "Diversity_MPHCHL.dim";
//            ProductIO.writeProduct(mphChlProduct, targetProductPath, "BEAM-DIMAP");
//
//            savedProduct = ProductIO.readProduct(targetProductPath);
//            assertNotNull(savedProduct);
//
//            final Band chlBand = savedProduct.getBand("Chl");
//            assertNotNull(chlBand);
//            //assertEquals(117.86557f, chlBand.getSampleFloat(0, 0), 1e-8);   // mph = 0.008270712, chla = 117.86557
//            //assertEquals(101.796295f, chlBand.getSampleFloat(1, 0), 1e-8);    // mph = 0.018348956, chla = 101.796295
//            //assertEquals(201.77931f, chlBand.getSampleFloat(0, 1), 1e-8);    // mph = 0.013674248, chla = 201.77931
//            //assertEquals(201.77931f, chlBand.getSampleFloat(1, 1), 1e-8);    // mph = 0.016069943, chla = 242.23514
//        } finally {
//            if (savedProduct != null) {
//                savedProduct.dispose();
//            }
//        }
//    }

//    @Test
//    public void testWithTomsLocalData() throws IOException {
//        final Product product = ProductIO.readProduct("C:/Data/DIVERSITY/L2_of_MER_FSG_1PNUPA20110605_160100_000003633103_00155_48445_6691.dim");
//
//        final Product mphChlProduct = GPF.createProduct("Diversity.MPH.CHL", GPF.NO_PARAMS, product);
//
//        try {
//            final String targetProductPath = testOutDirectory.getAbsolutePath() + File.separator + "Diversity_MPHCHL.dim";
//            ProductIO.writeProduct(mphChlProduct, targetProductPath, "BEAM-DIMAP");
//        }finally {
//            // weiss auch grad nicht
//        }
//    }

}

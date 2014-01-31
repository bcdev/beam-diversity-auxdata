package org.esa.beam.operator.mph_chl;

import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.GPF;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.util.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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

    @Test
    public void testComputeMphChlProduct() throws IOException {
        final Product brrProduct = MerisBrrProduct.create();

        final Product mphChlProduct = GPF.createProduct("Diversity.MPH.CHL", GPF.NO_PARAMS, brrProduct);

        Product savedProduct = null;
        try {
            final String targetProductPath = testOutDirectory.getAbsolutePath() + File.separator + "Diversity_MPHCHL.dim";
            ProductIO.writeProduct(mphChlProduct, targetProductPath, "BEAM-DIMAP");

            savedProduct = ProductIO.readProduct(targetProductPath);
            assertNotNull(savedProduct);

            final Band chlBand = savedProduct.getBand("chl");
            assertNotNull(chlBand);
            assertEquals(1.5443997383117676f, chlBand.getSampleFloat(0, 0), 1e-8);
            assertEquals(0.6783487796783447f, chlBand.getSampleFloat(1, 0), 1e-8);
            assertEquals(38.66391372680664f, chlBand.getSampleFloat(0, 1), 1e-8);
            assertEquals(Double.NaN, chlBand.getSampleFloat(1, 1), 1e-8);

            final Band flagBand = savedProduct.getBand("mph_chl_flags");
            assertNotNull(flagBand);
            assertEquals(0, flagBand.getSampleInt(0, 0));
            assertEquals(0, flagBand.getSampleInt(1, 0));
            assertEquals(0, flagBand.getSampleInt(0, 1));
            assertEquals(0, flagBand.getSampleInt(1, 1));

            final Band immersed_eucaryotes = savedProduct.getBand("immersed_eucaryotes");
            assertNotNull(immersed_eucaryotes);
            assertEquals(1, immersed_eucaryotes.getSampleInt(0, 0));
            assertEquals(1, immersed_eucaryotes.getSampleInt(1, 0));
            assertEquals(1, immersed_eucaryotes.getSampleInt(0, 1));
            assertEquals(0, immersed_eucaryotes.getSampleInt(1, 1));

            final Band immersed_cyanobacteria = savedProduct.getBand("immersed_cyanobacteria");
            assertNotNull(immersed_cyanobacteria);
            assertEquals(0, immersed_cyanobacteria.getSampleInt(0, 0));
            assertEquals(0, immersed_cyanobacteria.getSampleInt(1, 0));
            assertEquals(0, immersed_cyanobacteria.getSampleInt(0, 1));
            assertEquals(0, immersed_cyanobacteria.getSampleInt(1, 1));

            final Band floating_cyanobacteria = savedProduct.getBand("floating_cyanobacteria");
            assertNotNull(floating_cyanobacteria);
            assertEquals(0, floating_cyanobacteria.getSampleInt(0, 0));
            assertEquals(0, floating_cyanobacteria.getSampleInt(1, 0));
            assertEquals(0, floating_cyanobacteria.getSampleInt(0, 1));
            assertEquals(0, floating_cyanobacteria.getSampleInt(1, 1));

            final Band mphBand = savedProduct.getBand("mph");
            assertNull(mphBand);
        } finally {
            if (savedProduct != null) {
                savedProduct.dispose();
            }
        }
    }

    @Test
    public void testComputeMphChlProduct_withMph() throws IOException {
        final Product brrProduct = MerisBrrProduct.create();

        final Product mphChlProduct = GPF.createProduct("Diversity.MPH.CHL", createParameter(), brrProduct);
        Product savedProduct = null;
        try {
            final String targetProductPath = testOutDirectory.getAbsolutePath() + File.separator + "Diversity_MPHCHL.dim";
            ProductIO.writeProduct(mphChlProduct, targetProductPath, "BEAM-DIMAP");

            savedProduct = ProductIO.readProduct(targetProductPath);
            assertNotNull(savedProduct);

            final Band mphBand = savedProduct.getBand("mph");
            assertNotNull(mphBand);
            assertEquals(-1.1474395432742313E-4f, mphBand.getSampleFloat(0, 0), 1e-8);
            assertEquals(-4.521883383858949E-4f, mphBand.getSampleFloat(1, 0), 1e-8);
            assertEquals(0.003501386847347021f, mphBand.getSampleFloat(0, 1), 1e-8);
            assertEquals(Double.NaN, mphBand.getSampleFloat(1, 1), 1e-8);
        } finally {
            if (savedProduct != null) {
                savedProduct.dispose();
            }
        }
    }

    private Map<String, Object> createParameter() {
        final HashMap<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("exportMph", Boolean.TRUE);
        return parameterMap;
    }

    @Test
    public void testWithFaultyInvalidPixelExpression() {
        final Product brrProduct = MerisBrrProduct.create();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("invalidPixelExpression", "extremely INVALID");

        try {
            GPF.createProduct("Diversity.MPH.CHL", params, brrProduct);
            fail("OperatorException expected");
        } catch (OperatorException expected) {
        }
    }

//    @Test
//    public void testProcessTestProduct() throws IOException {
//        final Product product = ProductIO.readProduct("C:/Data/DIVERSITY/L2_of_MER_FSG_1PNUPA20110605_160100_000003633103_00155_48445_6691.dim");
//
//        final Product mphChlProduct = GPF.createProduct("Diversity.MPH.CHL", GPF.NO_PARAMS, product);
//
//        final String targetProductPath = testOutDirectory.getAbsolutePath() + File.separator + "Diversity_MPHCHL_pow10.dim";
//        ProductIO.writeProduct(mphChlProduct, targetProductPath, "BEAM-DIMAP");
//    }
}

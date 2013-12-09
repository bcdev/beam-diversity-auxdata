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

            final Band chlBand = savedProduct.getBand("Chl");
            assertNotNull(chlBand);
            assertEquals(1.5443997383117676f, chlBand.getSampleFloat(0, 0), 1e-8);
            assertEquals(0.6783487796783447f, chlBand.getSampleFloat(1, 0), 1e-8);
            assertEquals(29.945907592773438f, chlBand.getSampleFloat(0, 1), 1e-8);
            assertEquals(-999.f, chlBand.getSampleFloat(1, 1), 1e-8);

            final Band cyano_flagBand = savedProduct.getBand("cyano_flag");
            assertNotNull(cyano_flagBand);
            assertEquals(0, cyano_flagBand.getSampleInt(0, 0));
            assertEquals(0, cyano_flagBand.getSampleInt(1, 0));
            assertEquals(1, cyano_flagBand.getSampleInt(0, 1));
            assertEquals(0, cyano_flagBand.getSampleInt(1, 1));
        } finally {
            if (savedProduct != null) {
                savedProduct.dispose();
            }
        }
    }

}
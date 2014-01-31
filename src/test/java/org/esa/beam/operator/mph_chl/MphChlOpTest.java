package org.esa.beam.operator.mph_chl;


import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.junit.Assert.*;

public class MphChlOpTest {

    private MphChlOp mphChlOp;

    @Before
    public void setUp() {
        mphChlOp = new MphChlOp();
    }

    @Test
    public void testOperatorMetadata() {
        final OperatorMetadata operatorMetadata = MphChlOp.class.getAnnotation(OperatorMetadata.class);
        assertNotNull(operatorMetadata);
        assertEquals("Diversity.MPH.CHL", operatorMetadata.alias());
        assertEquals("1.2", operatorMetadata.version());
        assertEquals("Tom Block", operatorMetadata.authors());
        assertEquals("(c) 2013, 2014 by Brockmann Consult", operatorMetadata.copyright());
        assertEquals("Computes maximum peak height of chlorophyll", operatorMetadata.description());
    }

    @Test
    public void testSourceProductAnnotation() throws NoSuchFieldException {
        final Field productField = MphChlOp.class.getDeclaredField("sourceProduct");
        assertNotNull(productField);

        final SourceProduct productFieldAnnotation = productField.getAnnotation(SourceProduct.class);
        assertNotNull(productFieldAnnotation);
    }

    @Test
    public void testInvalidPixelExpressionAnnotation() throws NoSuchFieldException {
        final Field validPixelField = MphChlOp.class.getDeclaredField("invalidPixelExpression");

        final Parameter annotation = validPixelField.getAnnotation(Parameter.class);
        assertNotNull(annotation);
        assertEquals("not (cloud_classif_flags.F_LAND or cloud_classif_flags.F_CLOUD_BUFFER or cloud_classif_flags.F_CLOUD_SHADOW or cloud_classif_flags.F_CLOUD or cloud_classif_flags.F_MIXED_PIXEL or l1_flags.INVALID )", annotation.defaultValue());
        assertEquals("Expression defining pixels not considered for processing.", annotation.description());
    }

    @Test
    public void testConfigureTargetProduct() {
        final TestProductConfigurer productConfigurer = new TestProductConfigurer();

        mphChlOp.configureTargetProduct(productConfigurer);

        final Product targetProduct = productConfigurer.getTargetProduct();
        assertNotNull(targetProduct);

        final Band chlBand = targetProduct.getBand("chl");
        assertNotNull(chlBand);
        assertEquals(ProductData.TYPE_FLOAT32, chlBand.getDataType());
        assertEquals("mg/m^3", chlBand.getUnit());
        assertEquals(Double.NaN, chlBand.getGeophysicalNoDataValue(), 1e-8);

        final Band flagBand = targetProduct.getBand("mph_chl_flags");
        Assert.assertNotNull(flagBand);
        assertEquals(ProductData.TYPE_INT8, flagBand.getDataType());

        assertTrue(productConfigurer.isCopyGeoCodingCalled());

        final FlagCoding flagCoding = targetProduct.getFlagCodingGroup().get("mph_chl_flags");
        assertNotNull(flagCoding);
        FlagCoding bandFlagcoding = flagBand.getFlagCoding();
        assertSame(flagCoding, bandFlagcoding);

        final MetadataAttribute cyanoFlag = flagCoding.getFlag("CYANO");
        assertEquals("CYANO", cyanoFlag.getName());
        assertEquals("Cyanobacteria dominated waters", cyanoFlag.getDescription());
        assertEquals(1, cyanoFlag.getData().getElemInt());

        final MetadataAttribute floatingFlag = flagCoding.getFlag("FLOATING");
        assertNotNull(floatingFlag);
        assertEquals("FLOATING", floatingFlag.getName());
        assertEquals("Floating vegetation or cyanobacteria on water surface", floatingFlag.getDescription());
        assertEquals(2, floatingFlag.getData().getElemInt());

        final MetadataAttribute adjacencyFlag = flagCoding.getFlag("ADJACENCY");
        assertNotNull(adjacencyFlag);
        assertEquals("ADJACENCY", adjacencyFlag.getName());
        assertEquals("Pixel suspect of adjacency effects", adjacencyFlag.getDescription());
        assertEquals(4, adjacencyFlag.getData().getElemInt());
    }

    @Test
    public void testConfigureSourceSample() {
        final TestSampleConfigurer sampleConfigurer = new TestSampleConfigurer();

        mphChlOp.configureSourceSamples(sampleConfigurer);

        final HashMap<Integer, String> sampleMap = sampleConfigurer.getSampleMap();
        assertEquals("brr_6", sampleMap.get(0));
        assertEquals("brr_7", sampleMap.get(1));
        assertEquals("brr_8", sampleMap.get(2));
        assertEquals("brr_9", sampleMap.get(3));
        assertEquals("brr_10", sampleMap.get(4));
        assertEquals("brr_14", sampleMap.get(5));
    }

    @Test
    public void testConfigureTargetSample() {
        final TestSampleConfigurer sampleConfigurer = new TestSampleConfigurer();

        mphChlOp.configureTargetSamples(sampleConfigurer);

        final HashMap<Integer, String> sampleMap = sampleConfigurer.getSampleMap();
        assertEquals(2, sampleMap.size());
        assertEquals("chl", sampleMap.get(0));
        assertEquals("mph_chl_flags", sampleMap.get(1));
    }

    @Test
    public void testSetToInvalid() {
        final TestSample[] samples = new TestSample[3];
        samples[0] = new TestSample();
        samples[1] = new TestSample();
        samples[2] = new TestSample();

        MphChlOp.setToInvalid(samples);

        assertEquals(Double.NaN, samples[0].getDouble(), 1e-8);
        assertEquals(0.0, samples[1].getDouble(), 1e-8);
        assertEquals(0.0, samples[2].getDouble(), 1e-8);
    }

    @Test
    public void testComputeMph() {
        double mph = MphChlOp.computeMph(1, 2, 3, 6, 4, 5);
        assertEquals(-3, mph, 1e-8);

        mph = MphChlOp.computeMph(0, 2, 3, 6, 4, 5);
        assertEquals(-4, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 0, 3, 6, 4, 5);
        assertEquals(-5, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 0, 6, 4, 5);
        assertEquals(3, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 3, 6, 0, 5);
        assertEquals(-2.2, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 3, 6, 4, 0);
        assertEquals(-0.5, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 3, 0, 4, 5);
        assertEquals(3.0, mph, 1e-8);
    }

    @Test
    public void testIsCyano() {
        assertFalse(MphChlOp.isCyano(0.0, 1.0, 1.0));
        assertFalse(MphChlOp.isCyano(0.5, 1.0, 1.0));
        assertTrue(MphChlOp.isCyano(-0.1, 1.0, 1.0));

        assertFalse(MphChlOp.isCyano(-1.0, 0.0, 1.0));
        assertFalse(MphChlOp.isCyano(-1.0, -0.1, 1.0));
        assertTrue(MphChlOp.isCyano(-1.0, 0.5, 1.0));

        assertFalse(MphChlOp.isCyano(-1.0, 1.0, 0.0));
        assertFalse(MphChlOp.isCyano(-1.0, 1.0, 0.00049));
        assertTrue(MphChlOp.isCyano(-1.0, 1.0, 0.0051));
    }

    @Test
    public void testComputeChlPolynomial() {
        assertEquals(353732.6926, MphChlOp.computeChlPolynomial(0.1), 1e-8);
        assertEquals(8.2646992, MphChlOp.computeChlPolynomial(0.001), 1e-8);
        assertEquals(1.9726, MphChlOp.computeChlPolynomial(0.0), 1e-8);
    }

    @Test
    public void testComputeChlExponential() {
        assertEquals(500.0, MphChlOp.computeChlExponential(0.1, 500), 1e-8);

        assertEquals(23.25767257114881, MphChlOp.computeChlExponential(0.001, 500), 1e-8);
        assertEquals(22.44, MphChlOp.computeChlExponential(0.0, 500), 1e-8);
    }

    @Test
    public void testEncodeFlags() {
        assertEquals(0, MphChlOp.encodeFlags(false, false, false));
        assertEquals(1, MphChlOp.encodeFlags(true, false, false));
        assertEquals(2, MphChlOp.encodeFlags(false, true, false));
        assertEquals(4, MphChlOp.encodeFlags(false, false, true));
        assertEquals(3, MphChlOp.encodeFlags(true, true, false));
        assertEquals(5, MphChlOp.encodeFlags(true, false, true));
    }

    @Test
    public void testSpi() {
        final MphChlOp.Spi spi = new MphChlOp.Spi();
        final Class<? extends Operator> operatorClass = spi.getOperatorClass();
        assertTrue(operatorClass.isAssignableFrom(MphChlOp.class));
    }
}

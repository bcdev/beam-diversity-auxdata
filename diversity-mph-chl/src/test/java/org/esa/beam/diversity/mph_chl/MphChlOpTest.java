package org.esa.beam.diversity.mph_chl;


import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
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
        assertEquals("Diversity.MPH.CHL.Pixel", operatorMetadata.alias());
        assertEquals("1.3", operatorMetadata.version());
        assertEquals("Tom Block, Daniel Odermatt", operatorMetadata.authors());
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
        final Field validPixelField = MphChlOp.class.getDeclaredField("validPixelExpression");

        final Parameter annotation = validPixelField.getAnnotation(Parameter.class);
        assertNotNull(annotation);
        assertEquals("not (l1_flags.LAND_OCEAN or l1_flags.INVALID)", annotation.defaultValue());
        assertEquals("Expression defining pixels considered for processing.", annotation.description());
    }

    @Test
    public void testCyanoMaxValueAnnotation() throws NoSuchFieldException {
        final Field cyanoMaxValueField = MphChlOp.class.getDeclaredField("cyanoMaxValue");

        final Parameter annotation = cyanoMaxValueField.getAnnotation(Parameter.class);
        assertNotNull(annotation);
        assertEquals("1000.0", annotation.defaultValue());
        assertEquals("Maximum chlorophyll, arithmetically higher values are capped.", annotation.description());
    }

    @Test
    public void testChlThreshForFloatFlagAnnotation() throws NoSuchFieldException {
        final Field chlThreshForFloatFlagField = MphChlOp.class.getDeclaredField("chlThreshForFloatFlag");

        final Parameter annotation = chlThreshForFloatFlagField.getAnnotation(Parameter.class);
        assertNotNull(annotation);
        assertEquals("500.0", annotation.defaultValue());
        assertEquals("Chlorophyll threshold, above which all cyanobacteria dominated waters are 'float.", annotation.description());
    }

    @Test
    public void testExportMphAnnotation() throws NoSuchFieldException {
        final Field exportMphField = MphChlOp.class.getDeclaredField("exportMph");

        final Parameter annotation = exportMphField.getAnnotation(Parameter.class);
        assertNotNull(annotation);
        assertEquals("false", annotation.defaultValue());
        assertEquals("Switch to true to write 'mph' band.", annotation.description());
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

        final Band immersed_eucaryotesBand = targetProduct.getBand("immersed_eucaryotes");
        assertNotNull(immersed_eucaryotesBand);
        assertEquals(ProductData.TYPE_INT8, immersed_eucaryotesBand.getDataType());

        final Band immersed_cyanobacteriaBand = targetProduct.getBand("immersed_cyanobacteria");
        assertNotNull(immersed_cyanobacteriaBand);
        assertEquals(ProductData.TYPE_INT8, immersed_cyanobacteriaBand.getDataType());

        final Band floating_cyanobacteriaBand = targetProduct.getBand("floating_cyanobacteria");
        assertNotNull(floating_cyanobacteriaBand);
        assertEquals(ProductData.TYPE_INT8, floating_cyanobacteriaBand.getDataType());

        final Band floating_vegetationBand = targetProduct.getBand("floating_vegetation");
        assertNotNull(floating_vegetationBand);
        assertEquals(ProductData.TYPE_INT8, floating_vegetationBand.getDataType());

        final Band flagBand = targetProduct.getBand("mph_chl_flags");
        Assert.assertNotNull(flagBand);
        assertEquals(ProductData.TYPE_INT8, flagBand.getDataType());

        assertTrue(productConfigurer.isCopyGeoCodingCalled());

        final FlagCoding flagCoding = targetProduct.getFlagCodingGroup().get("mph_chl_flags");
        assertNotNull(flagCoding);
        FlagCoding bandFlagcoding = flagBand.getFlagCoding();
        assertSame(flagCoding, bandFlagcoding);

        final MetadataAttribute cyanoFlag = flagCoding.getFlag("mph_cyano");
        assertEquals("mph_cyano", cyanoFlag.getName());
        assertEquals("Cyanobacteria dominated waters", cyanoFlag.getDescription());
        assertEquals(1, cyanoFlag.getData().getElemInt());

        final MetadataAttribute floatingFlag = flagCoding.getFlag("mph_floating");
        assertNotNull(floatingFlag);
        assertEquals("mph_floating", floatingFlag.getName());
        assertEquals("Floating vegetation or cyanobacteria on water surface", floatingFlag.getDescription());
        assertEquals(2, floatingFlag.getData().getElemInt());

        final MetadataAttribute adjacencyFlag = flagCoding.getFlag("mph_adjacency");
        assertNotNull(adjacencyFlag);
        assertEquals("mph_adjacency", adjacencyFlag.getName());
        assertEquals("Pixel suspect of adjacency effects", adjacencyFlag.getDescription());
        assertEquals(4, adjacencyFlag.getData().getElemInt());

        final ProductNodeGroup<Mask> maskGroup = targetProduct.getMaskGroup();
        assertNotNull(maskGroup);
        final Mask cyanoMask = maskGroup.get("mph_cyano");
        assertNotNull(cyanoMask);
        assertEquals("Cyanobacteria dominated waters", cyanoMask.getDescription());
        assertEquals(Color.cyan, cyanoMask.getImageColor());
        assertEquals(0.5f, cyanoMask.getImageTransparency(), 1e-8);

        final Mask floatingMask = maskGroup.get("mph_floating");
        assertNotNull(floatingMask);
        assertEquals("Floating vegetation or cyanobacteria on water surface", floatingMask.getDescription());
        assertEquals(Color.green, floatingMask.getImageColor());
        assertEquals(0.5f, floatingMask.getImageTransparency(), 1e-8);

        final Mask adjacencyMask = maskGroup.get("mph_adjacency");
        assertNotNull(adjacencyMask);
        assertEquals("Pixel suspect of adjacency effects", adjacencyMask.getDescription());
        assertEquals(Color.red, adjacencyMask.getImageColor());
        assertEquals(0.5f, adjacencyMask.getImageTransparency(), 1e-8);
    }

    @Test
    public void testConfigureTargetProduct_withMphBand() {
        final TestProductConfigurer productConfigurer = new TestProductConfigurer();

        mphChlOp.exportMph = true;
        mphChlOp.configureTargetProduct(productConfigurer);

        final Product targetProduct = productConfigurer.getTargetProduct();
        assertNotNull(targetProduct);

        final Band mphBand = targetProduct.getBand("mph");
        assertNotNull(mphBand);
        assertEquals(ProductData.TYPE_FLOAT32, mphBand.getDataType());
        assertEquals("dl", mphBand.getUnit());
        assertEquals(Double.NaN, mphBand.getGeophysicalNoDataValue(), 1e-8);
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
        assertEquals(6, sampleMap.size());
        assertEquals("chl", sampleMap.get(0));
        assertEquals("mph_chl_flags", sampleMap.get(1));
        assertEquals("immersed_eucaryotes", sampleMap.get(2));
        assertEquals("immersed_cyanobacteria", sampleMap.get(3));
        assertEquals("floating_cyanobacteria", sampleMap.get(4));
        assertEquals("floating_vegetation", sampleMap.get(5));
    }

    @Test
    public void testConfigureTargetSample_withMph() {
        final TestSampleConfigurer sampleConfigurer = new TestSampleConfigurer();

        mphChlOp.exportMph = true;
        mphChlOp.configureTargetSamples(sampleConfigurer);

        final HashMap<Integer, String> sampleMap = sampleConfigurer.getSampleMap();
        assertEquals(7, sampleMap.size());
        assertEquals("chl", sampleMap.get(0));
        assertEquals("mph_chl_flags", sampleMap.get(1));
        assertEquals("immersed_eucaryotes", sampleMap.get(2));
        assertEquals("immersed_cyanobacteria", sampleMap.get(3));
        assertEquals("floating_cyanobacteria", sampleMap.get(4));
        assertEquals("floating_vegetation", sampleMap.get(5));
        assertEquals("mph", sampleMap.get(6));
    }

    @Test
    public void testSetToInvalid() {
        final TestSample[] samples = createSampleArray(6);

        MphChlOp.setToInvalid(samples, false);

        assertEquals(Double.NaN, samples[0].getDouble(), 1e-8);
        assertEquals(0.0, samples[1].getDouble(), 1e-8);
        assertEquals(0.0, samples[2].getDouble(), 1e-8);
        assertEquals(0.0, samples[3].getDouble(), 1e-8);
        assertEquals(0.0, samples[4].getDouble(), 1e-8);
        assertEquals(0.0, samples[4].getDouble(), 1e-8);
    }

    @Test
    public void testSetToInvalid_withMph() {
        final TestSample[] samples = createSampleArray(7);

        MphChlOp.setToInvalid(samples, true);

        assertEquals(Double.NaN, samples[0].getDouble(), 1e-8);
        assertEquals(0.0, samples[1].getDouble(), 1e-8);
        assertEquals(0.0, samples[2].getDouble(), 1e-8);
        assertEquals(0.0, samples[3].getDouble(), 1e-8);
        assertEquals(0.0, samples[4].getDouble(), 1e-8);
        assertEquals(0.0, samples[5].getDouble(), 1e-8);
        assertEquals(Double.NaN, samples[6].getDouble(), 1e-8);
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
    public void testIsCyano_threeArgs() {
        assertFalse(MphChlOp.isCyano(0.0, 1.0, 1.0));
        assertFalse(MphChlOp.isCyano(0.5, 1.0, 1.0));
        assertTrue(MphChlOp.isCyano(-0.1, 1.0, 1.0));

        assertFalse(MphChlOp.isCyano(-1.0, 0.0, 1.0));
        assertFalse(MphChlOp.isCyano(-1.0, -0.1, 1.0));
        assertTrue(MphChlOp.isCyano(-1.0, 0.5, 1.0));

        assertFalse(MphChlOp.isCyano(-1.0, 1.0, 0.0));
        assertFalse(MphChlOp.isCyano(-1.0, 1.0, 0.0019));
        assertTrue(MphChlOp.isCyano(-1.0, 1.0, 0.0021));
    }

    @Test
    public void testIsCyano_twoArgs() {
         assertFalse(MphChlOp.isCyano(1.0, -1.0));
         assertFalse(MphChlOp.isCyano(-0.1, -1.0));
         assertFalse(MphChlOp.isCyano(1.0, 0.1));
         assertTrue(MphChlOp.isCyano(-0.1, 0.1));
    }

    @Test
    public void testComputeChlPolynomial() {
        Assert.assertEquals(353732.6926, MphChlOp.computeChlPolynomial(0.1), 1e-8);
        Assert.assertEquals(8.2646992, MphChlOp.computeChlPolynomial(0.001), 1e-8);
        Assert.assertEquals(1.9726, MphChlOp.computeChlPolynomial(0.0), 1e-8);
    }

    @Test
    public void testComputeChlExponential() {
        Assert.assertEquals(22.5204566512951240, MphChlOp.computeChlExponential(0.0001), 1e-8);
        Assert.assertEquals(23.25767257114881, MphChlOp.computeChlExponential(0.001), 1e-8);
        Assert.assertEquals(22.44, MphChlOp.computeChlExponential(0.0), 1e-8);
    }

    @Test
    public void testEncodeFlags() {
        Assert.assertEquals(0, MphChlOp.encodeFlags(false, false, false));
        Assert.assertEquals(1, MphChlOp.encodeFlags(true, false, false));
        Assert.assertEquals(2, MphChlOp.encodeFlags(false, true, false));
        Assert.assertEquals(4, MphChlOp.encodeFlags(false, false, true));
        Assert.assertEquals(3, MphChlOp.encodeFlags(true, true, false));
        Assert.assertEquals(5, MphChlOp.encodeFlags(true, false, true));
    }

    @Test
    public void testSpi() {
        final MphChlOp.Spi spi = new MphChlOp.Spi();
        final Class<? extends Operator> operatorClass = spi.getOperatorClass();
        assertTrue(operatorClass.isAssignableFrom(MphChlOp.class));
    }

    private static TestSample[] createSampleArray(int numSamples) {
        final TestSample[] samples = new TestSample[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = new TestSample();
        }
        return samples;
    }
}

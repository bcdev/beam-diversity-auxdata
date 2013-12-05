package org.esa.beam.operator.mph_chl;


import junit.framework.Assert;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals("1.0", operatorMetadata.version());
        assertEquals("Tom Block", operatorMetadata.authors());
        assertEquals("(c) 2013 by Brockmann Consult", operatorMetadata.copyright());
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

        final Band chlBand = targetProduct.getBand("Chl");
        assertNotNull(chlBand);
        assertEquals(ProductData.TYPE_FLOAT32, chlBand.getDataType());

        final Band cyanoFlagBand = targetProduct.getBand("cyano_flag");
        Assert.assertNotNull(cyanoFlagBand);
        assertEquals(ProductData.TYPE_INT8, cyanoFlagBand.getDataType());

        assertTrue(productConfigurer.isCopyGeoCodingCalled());

        final FlagCoding cyanoFlagCoding = targetProduct.getFlagCodingGroup().get("cyano_flag");
        assertNotNull(cyanoFlagCoding);
        final MetadataAttribute cyanoFlag = cyanoFlagCoding.getFlag("cyano_flag");
        assertEquals("cyano_flag", cyanoFlag.getName());
        assertEquals("Cyanobacteria dominated waters", cyanoFlag.getDescription());
        assertEquals(1, cyanoFlag.getData().getElemInt());
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
        assertEquals("l1_flags", sampleMap.get(6));
        assertEquals("gas_flags", sampleMap.get(7));
        assertEquals("ray_corr_flags", sampleMap.get(8));
        assertEquals("cloud_classif_flags", sampleMap.get(9));
    }

    @Test
    public void testConfigureTargetSample() {
        final TestSampleConfigurer sampleConfigurer = new TestSampleConfigurer();

        mphChlOp.configureTargetSamples(sampleConfigurer);

        final HashMap<Integer, String> sampleMap = sampleConfigurer.getSampleMap();
        assertEquals("Chl", sampleMap.get(0));
        assertEquals("cyano_flag", sampleMap.get(1));
    }

    @Test
    public void testSetToInvalid() {
        final TestSample[] samples = new TestSample[2];
        samples[0] = new TestSample();
        samples[1] = new TestSample();

        MphChlOp.setToInvalid(samples);

        assertEquals(-999.0, samples[0].getDouble(), 1e-8);
        assertEquals(0.0, samples[1].getDouble(), 1e-8);
    }

    @Test
    public void testGetMax_789() {
        double[] reflectances = new double[]{3.0, 6.0, 4.0};
        MaxResult maxResult = new MaxResult();

        maxResult = MphChlOp.getMax_789(reflectances, maxResult);
        assertNotNull(maxResult);
        assertEquals(6.0, maxResult.getReflectance(), 1e-8);
        assertEquals(681.0, maxResult.getWavelength());

        reflectances = new double[]{1.0, 1.0, 4.0};
        maxResult = MphChlOp.getMax_789(reflectances, maxResult);
        assertNotNull(maxResult);
        assertEquals(4.0, maxResult.getReflectance(), 1e-8);
        assertEquals(709.0, maxResult.getWavelength());
    }

    @Test
    public void testGetMax_8910() {
        double[] reflectances = new double[]{2.0, 1.0, 0.3};
        MaxResult maxResult = new MaxResult();

        maxResult = MphChlOp.getMax_8910(reflectances, maxResult);
        assertNotNull(maxResult);
        assertEquals(2.0, maxResult.getReflectance(), 1e-8);
        assertEquals(681.0, maxResult.getWavelength());

        reflectances = new double[]{.03, 0.3, 3.0};
        maxResult = MphChlOp.getMax_8910(reflectances, maxResult);
        assertNotNull(maxResult);
        assertEquals(3.0, maxResult.getReflectance(), 1e-8);
        assertEquals(753.0, maxResult.getWavelength());
    }

    @Test
    public void testComputeMph() {
        double mph = MphChlOp.computeMph(1, 2, 3, 4, 5, 6);
        assertEquals(-3, mph, 1e-8);

        mph = MphChlOp.computeMph(0, 2, 3, 4, 5, 6);
        assertEquals(-4, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 0, 3, 4, 5, 6);
        assertEquals(-5, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 0, 4, 5, 6);
        assertEquals(3, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 3, 0, 5, 6);
        assertEquals(-2.2, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 3, 4, 0, 6);
        assertEquals(-0.5, mph, 1e-8);

        mph = MphChlOp.computeMph(1, 2, 3, 4, 5, 0);
        assertEquals(3.0, mph, 1e-8);
    }

    @Test
    public void testAssign_8910() {
        final TestSample[] samples = new TestSample[5];
        final double[] reflectances = new double[3];
        samples[2] = new TestSample();
        samples[2].set(2.0);
        samples[3] = new TestSample();
        samples[3].set(3.0);
        samples[4] = new TestSample();
        samples[4].set(4.0);

        MphChlOp.assign_8910(reflectances, samples);

        assertEquals(2.0, reflectances[0], 1e-8);
        assertEquals(3.0, reflectances[1], 1e-8);
        assertEquals(4.0, reflectances[2], 1e-8);
    }

    @Test
    public void testSpi() {
        final MphChlOp.Spi spi = new MphChlOp.Spi();
        final Class<? extends Operator> operatorClass = spi.getOperatorClass();
        assertTrue(operatorClass.isAssignableFrom(MphChlOp.class));
    }
}

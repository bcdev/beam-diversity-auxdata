package org.esa.beam.operator.mph_chl;


import junit.framework.Assert;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.framework.gpf.Operator;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.framework.gpf.annotations.SourceProduct;
import org.junit.Test;

import java.lang.reflect.Field;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MphChlOpTest {

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
    public void testConfigureTargetProduct() {
        final TestProductConfigurer productConfigurer = new TestProductConfigurer();

        final MphChlOp mphChlOp = new MphChlOp();
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
        final int flagMask = cyanoFlagCoding.getFlagMask("cyano_flag");
        assertEquals(1, flagMask);
    }

    @Test
    public void testSpi() {
        final MphChlOp.Spi spi = new MphChlOp.Spi();
        final Class<? extends Operator> operatorClass = spi.getOperatorClass();
        assertTrue(operatorClass.isAssignableFrom(MphChlOp.class));
    }
}

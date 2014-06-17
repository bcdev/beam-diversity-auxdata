package org.esa.beam.diversity.mph_chl;

import org.esa.beam.jai.VirtualBandOpImage;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.image.Raster;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MphChlProcessingTest {

    private MphChlOp mphChlOp;

    @Before
    public void setUp() {
        mphChlOp = new MphChlOp();
        mphChlOp.setParameterDefaultValues();

        // make sure that every pixel is always valid in this test
        final VirtualBandOpImage virtualBandOpImage = mock(VirtualBandOpImage.class);
        final Raster raster = mock(Raster.class);
        when(raster.getSample(anyInt(), anyInt(), anyInt())).thenReturn(1);
        when(virtualBandOpImage.getData(any(Rectangle.class))).thenReturn(raster);

        mphChlOp.invalidOpImage = virtualBandOpImage;
    }

    @Test
    public void testProcess_maxLambdaAt681_withoutMphBand() {
        final double[] brrs = {0.017987775, 0.015517377, 0.0150528, 0.013397833, 0.012008918, 0.009950145};
        final TestSample[] outputSamples = createOutputSampleArray();
        final TestSample[] inputSamples = createInputSamples(brrs);

        mphChlOp.computePixel(0, 0, inputSamples, outputSamples);

        assertEquals(1.829924038664355, outputSamples[0].getDouble(), 1e-8);  // chl
        assertEquals(0, outputSamples[1].getInt());                           // flags
        assertEquals(0, outputSamples[2].getInt());                           // immersed_cyano
        assertEquals(0, outputSamples[3].getInt());                           // floating_cyano
        assertEquals(0, outputSamples[4].getInt());                           // floating_vegetation
    }

    @Test
    public void testProcess_maxLambdaAt709_withoutMphBand() {
        final double[] brrs = {0.017987775, 0.015517377, 0.0150528, 0.015397833, 0.012008918, 0.009950145};
        final TestSample[] outputSamples = createOutputSampleArray();
        final TestSample[] inputSamples = createInputSamples(brrs);

        mphChlOp.computePixel(0, 0, inputSamples, outputSamples);

        assertEquals(8.382890811688007, outputSamples[0].getDouble(), 1e-8);  // chl
        assertEquals(0, outputSamples[1].getInt());                           // flags
        assertEquals(0, outputSamples[2].getInt());                           // immersed_cyano
        assertEquals(0, outputSamples[3].getInt());                           // floating_cyano
        assertEquals(0, outputSamples[4].getInt());                           // floating_vegetation
    }

    @Test
    public void testProcess_maxLambdaAt753_withoutMphBand() {
        final double[] brrs = {0.017987775, 0.015517377, 0.0150528, 0.015397833, 0.015508918, 0.009950145};
        final TestSample[] outputSamples = createOutputSampleArray();
        final TestSample[] inputSamples = createInputSamples(brrs);

        mphChlOp.computePixel(0, 0, inputSamples, outputSamples);

        assertEquals(8.382890811688007, outputSamples[0].getDouble(), 1e-8);  // chl
        assertEquals(4, outputSamples[1].getInt());                           // flags
        assertEquals(0, outputSamples[2].getInt());                           // immersed_cyano
        assertEquals(0, outputSamples[3].getInt());                           // floating_cyano
        assertEquals(0, outputSamples[4].getInt());                           // floating_vegetation
    }

    private TestSample[] createInputSamples(double[] brrs) {
        final TestSample[] inputSamples = new TestSample[brrs.length];
        for (int i = 0; i < inputSamples.length; i++) {
            inputSamples[i] = new TestSample();
            inputSamples[i].set(brrs[i]);
        }
        return inputSamples;
    }

    private TestSample[] createOutputSampleArray() {
        final TestSample[] outputSamples = new TestSample[6];
        for (int i = 0; i < outputSamples.length; i++) {
            outputSamples[i] = new TestSample();
        }
        return outputSamples;
    }
}

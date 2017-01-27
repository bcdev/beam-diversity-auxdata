/*
 * Copyright (C) 2017 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.l3;

import com.bc.ceres.binding.dom.XppDomElement;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.XppDomWriter;
import com.thoughtworks.xstream.io.xml.XppReader;
import com.thoughtworks.xstream.io.xml.xppdom.XppDom;
import org.esa.beam.binning.Aggregator;
import org.esa.beam.binning.AggregatorConfig;
import org.esa.beam.binning.BinManager;
import org.esa.beam.binning.Observation;
import org.esa.beam.binning.VariableContext;
import org.esa.beam.binning.operator.AggregatorConfigDomConverter;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static java.lang.Float.NaN;
import static org.esa.beam.l3.AggregatorRepresentativeSpectrum.computeMedian;
import static org.esa.beam.l3.AggregatorRepresentativeSpectrum.computeMedianSpectrum;
import static org.esa.beam.l3.AggregatorTestUtils.aggregate;
import static org.esa.beam.l3.AggregatorTestUtils.assertVectorEquals;
import static org.esa.beam.l3.AggregatorTestUtils.obsNT;
import static org.esa.beam.l3.AggregatorTestUtils.vec;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class AggregatorRepresentativeSpectrumTest {

    private VariableContext varCtx;

    @Before
    public void setUp() throws Exception {
        varCtx = new MyVariableContext("r1", "r2", "r3");
    }

    @Test
    public void testComputeMedian() throws Exception {
        assertEquals(1f, computeMedian(1f), 1E-6);
        assertEquals(1f, computeMedian(1f, 1f), 1E-6);
        assertEquals(2f, computeMedian(1f, 3f), 1E-6);
        assertEquals(1f, computeMedian(1f, 1f, 3f), 1E-6);
        assertEquals(2.5f, computeMedian(1f, 2f, 3f, 4f), 1E-6);
    }

    @Test
    public void testComputeMedianSpectrum() throws Exception {
        final int numVars = 3;
        final int numSpectra = 5;
        float[][] data = {
                {1, 2, 3, 2, 4},
                {2, 2, 4, 3, 6},
                {4, 4, 7, 4, 7},
        };
        double[][] allSpectra = new double[numSpectra][numVars];

        double[] medianSpectrum = computeMedianSpectrum(data, allSpectra);
        assertArrayEquals(new double[]{2f, 3f, 4f}, medianSpectrum, 1E-5f);

        assertArrayEquals(new double[]{1f, 2f, 4f}, allSpectra[0], 1E-5f);
        assertArrayEquals(new double[]{2f, 2f, 4f}, allSpectra[1], 1E-5f);
        assertArrayEquals(new double[]{3f, 4f, 7f}, allSpectra[2], 1E-5f);
        assertArrayEquals(new double[]{2f, 3f, 4f}, allSpectra[3], 1E-5f);
        assertArrayEquals(new double[]{4f, 6f, 7f}, allSpectra[4], 1E-5f);
    }

    @Test
    public void testComputeSpectralAngle() throws Exception {
        double[] refSpectrum = {3, 4, 7};
        double[] medianSpectrum = {2, 3, 4};
        AggregatorRepresentativeSpectrum.Method method = AggregatorRepresentativeSpectrum.Method.SpectralAngle;
        double value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(0.11851214679718568, value, 1E-5);

        medianSpectrum = new double[]{1, 3, 7};
        value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(0.2513167064860358, value, 1E-5);
    }

    @Test
    public void testComputeAbsoluteDifference() throws Exception {
        double[] refSpectrum = {3, 4, 7};
        double[] medianSpectrum = {2, 3, 4};
        AggregatorRepresentativeSpectrum.Method method = AggregatorRepresentativeSpectrum.Method.AbsoluteDifference;
        double value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(0.33730158730158727, value, 1E-5);

        medianSpectrum = new double[]{1, 3, 7};
        value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(0.3055555555555555, value, 1E-5);
    }

    @Test
    public void testComputeRMSDifference() throws Exception {
        double[] refSpectrum = {3, 4, 7};
        double[] medianSpectrum = {2, 3, 4};
        AggregatorRepresentativeSpectrum.Method method = AggregatorRepresentativeSpectrum.Method.RMSDifference;
        double value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(1.9148542155126762, value, 1E-5);

        medianSpectrum = new double[]{1, 3, 7};
        value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(1.2909944487358056, value, 1E-5);
    }

    @Test
    public void testComputeBias() throws Exception {
        double[] refSpectrum = {3, 4, 7};
        double[] medianSpectrum = {2, 3, 4};
        AggregatorRepresentativeSpectrum.Method method = AggregatorRepresentativeSpectrum.Method.Bias;
        double value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(-0.33730158730158727, value, 1E-5);

        medianSpectrum = new double[]{1, 3, 7};
        value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(-0.3055555555555555, value, 1E-5);
    }

    @Test
    public void testComputeCoeffOfDeter() throws Exception {
        double[] refSpectrum = {3, 4, 7};
        double[] medianSpectrum = {2, 3, 4};
        AggregatorRepresentativeSpectrum.Method method = AggregatorRepresentativeSpectrum.Method.CoeffOfDetermination;
        double value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(0.9230769230769231, value, 1E-5);

        medianSpectrum = new double[]{1, 3, 7};
        value = method.compute(refSpectrum, medianSpectrum);
        assertEquals(0.9917582417582417, value, 1E-5);
    }

    @Test
    public void testMetadata() {
        Aggregator agg = new AggregatorRepresentativeSpectrum(varCtx, AggregatorRepresentativeSpectrum.Method.SpectralAngle, "r1", "r2", "r3");

        assertArrayEquals(new String[]{"r1", "r2", "r3"}, agg.getSpatialFeatureNames());
        assertArrayEquals(new String[]{"r1", "r2", "r3"}, agg.getTemporalFeatureNames());
        assertArrayEquals(new String[]{"r1", "r2", "r3"}, agg.getOutputFeatureNames());
    }

    @Test
    public void testAggregate_e2e_SpectralAngle() throws Exception {
        Aggregator agg = new AggregatorRepresentativeSpectrum(varCtx, AggregatorRepresentativeSpectrum.Method.SpectralAngle, "r1", "r2", "r3");
        BinManager bm = new BinManager(varCtx, agg);

        // 0 obs
        Observation[][] multipleProductObs = new Observation[][]{{}};
        assertVectorEquals(vec(NaN, NaN, NaN), aggregate(bm, multipleProductObs));

        // 1 obs
        multipleProductObs = new Observation[][]{
                {obsNT(1, 3, 7)}
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));

        // 2 obs: best SpectralAngle using mean
        multipleProductObs = new Observation[][]{
                {obsNT(1, 3, 7)},
                {obsNT(2, 3, 5)}
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));

        // 3 obs: best SpectralAngle using median
        multipleProductObs = new Observation[][]{
                {obsNT(1, 1.5f, 4)},
                {obsNT(2, 3, 5)},
                {obsNT(1, 3, 7)},
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));
    }

    @Test
    public void testAggregate_e2e_AbsoluteDifference() throws Exception {
        Aggregator agg = new AggregatorRepresentativeSpectrum(varCtx, AggregatorRepresentativeSpectrum.Method.AbsoluteDifference, "r1", "r2", "r3");
        BinManager bm = new BinManager(varCtx, agg);

        // 0 obs
        Observation[][] multipleProductObs = new Observation[][]{{}};
        assertVectorEquals(vec(NaN, NaN, NaN), aggregate(bm, multipleProductObs));

        // 1 obs
        multipleProductObs = new Observation[][]{
                {obsNT(1, 3, 7)}
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));

        // 2 obs: best SpectralAngle using mean
        multipleProductObs = new Observation[][]{
                {obsNT(1, 3, 7)},
                {obsNT(2, 3, 5)}
        };
        assertVectorEquals(vec(2, 3, 5), aggregate(bm, multipleProductObs));

        // 3 obs: best SpectralAngle using median
        multipleProductObs = new Observation[][]{
                {obsNT(1, 1.5f, 4)},
                {obsNT(2, 3, 5)},
                {obsNT(1, 3, 7)},
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));
    }

    @Test
    public void testAggregate_e2e_RMSDifference() throws Exception {
        Aggregator agg = new AggregatorRepresentativeSpectrum(varCtx, AggregatorRepresentativeSpectrum.Method.RMSDifference, "r1", "r2", "r3");
        BinManager bm = new BinManager(varCtx, agg);

        // 0 obs
        Observation[][] multipleProductObs = new Observation[][]{{}};
        assertVectorEquals(vec(NaN, NaN, NaN), aggregate(bm, multipleProductObs));

        // 1 obs
        multipleProductObs = new Observation[][]{
                {obsNT(1, 3, 7)}
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));

        // 2 obs: best SpectralAngle using mean
        multipleProductObs = new Observation[][]{
                {obsNT(1, 3, 7)},
                {obsNT(2, 3, 5)}
        };
        assertVectorEquals(vec(1, 3, 7), aggregate(bm, multipleProductObs));

        // 3 obs: best SpectralAngle using median
        multipleProductObs = new Observation[][]{
                {obsNT(1, 1.5f, 4)},
                {obsNT(2, 3, 5)},
                {obsNT(1, 3, 7)},
        };
        assertVectorEquals(vec(2, 3, 5), aggregate(bm, multipleProductObs));
    }

    @Test
    public void testDescriptor() throws Exception {
        String aggregatorDOM  = "<aggregators><aggregator>" +
                "  <type>RepresentativeSpectrum</type>" +
                "  <varNames>r1,r2</varNames>" +
                "  <method>RMSDifference</method>" +
                "</aggregator></aggregators>";
        XppDomElement element = new XppDomElement(createDom(aggregatorDOM));
        AggregatorConfig[] aggregatorConfigs = (AggregatorConfig[]) new AggregatorConfigDomConverter().convertDomToValue(element, null);
        assertNotNull(aggregatorConfigs);
        assertEquals(1, aggregatorConfigs.length);
        assertSame(AggregatorRepresentativeSpectrum.Config.class, aggregatorConfigs[0].getClass());
        AggregatorRepresentativeSpectrum.Config config = (AggregatorRepresentativeSpectrum.Config) aggregatorConfigs[0];
        assertEquals(AggregatorRepresentativeSpectrum.Method.RMSDifference, config.method);
        assertArrayEquals(new String[]{"r1", "r2"}, config.varNames);
    }

    private XppDom createDom(String xml) {
        XppDomWriter domWriter = new XppDomWriter();
        new HierarchicalStreamCopier().copy(new XppReader(new StringReader(xml)), domWriter);
        return domWriter.getConfiguration();
    }
}

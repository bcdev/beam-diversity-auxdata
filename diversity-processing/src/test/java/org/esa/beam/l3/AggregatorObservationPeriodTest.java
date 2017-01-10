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

import org.esa.beam.binning.BinContext;
import org.esa.beam.binning.VariableContext;
import org.esa.beam.binning.support.VectorImpl;
import org.esa.beam.framework.datamodel.ProductData;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;

import static java.lang.Float.NaN;
import static org.esa.beam.l3.AggregatorTestUtils.obs;
import static org.esa.beam.l3.AggregatorTestUtils.vec;
import static org.junit.Assert.*;

public class AggregatorObservationPeriodTest {

    private static double mjd(String date) throws ParseException {
        return ProductData.UTC.parse(date, "yyyy-MM-dd HH:mm:ss").getMJD();
    }

    private BinContext ctx;
    private VariableContext varCtx;

    @Before
    public void setUp() throws Exception {
        ctx = AggregatorTestUtils.createCtx();
        varCtx = new MyVariableContext();
    }

    @Test
    public void testMetadata() {
        varCtx = new MyVariableContext();
        AggregatorObservationPeriod agg = new AggregatorObservationPeriod(varCtx, "2011-03-04", "first_obs", "last_obs");

        assertArrayEquals(new String[]{"day"}, agg.getSpatialFeatureNames());
        assertArrayEquals(new String[]{"first_obs", "last_obs"}, agg.getTemporalFeatureNames());
        assertArrayEquals(new String[]{"first_obs", "last_obs"}, agg.getOutputFeatureNames());
    }

    @Test
    public void testAggregate_noObs() throws Exception {
        AggregatorObservationPeriod agg = new AggregatorObservationPeriod(varCtx, "2011-03-04", "first_obs", "last_obs");

        VectorImpl svec = vec(42);
        VectorImpl tvec = vec(NaN, NaN);
        VectorImpl ovec = vec(NaN, NaN);


        agg.initSpatial(ctx, svec);
        assertTrue(Float.isNaN(svec.get(0)));

        agg.completeSpatial(ctx, 0, svec);
        assertTrue(Float.isNaN(svec.get(0)));


        agg.initTemporal(ctx, tvec);
        assertEquals(Float.POSITIVE_INFINITY, tvec.get(0), 0.0f);
        assertEquals(Float.NEGATIVE_INFINITY, tvec.get(1), 0.0f);

        agg.completeTemporal(ctx, 0, tvec);
        assertTrue(Float.isNaN(tvec.get(0)));
        assertTrue(Float.isNaN(tvec.get(1)));


        agg.computeOutput(tvec, ovec);
        assertTrue(Float.isNaN(ovec.get(0)));
        assertTrue(Float.isNaN(ovec.get(1)));
    }

    @Test
    public void testAggregate_withSingleObs() throws Exception {
        AggregatorObservationPeriod agg = new AggregatorObservationPeriod(varCtx, "2011-03-04", "first_obs", "last_obs");

        VectorImpl svec = vec(42);
        VectorImpl tvec = vec(NaN, NaN);
        VectorImpl ovec = vec(NaN, NaN);


        agg.initSpatial(ctx, svec);

        agg.aggregateSpatial(ctx, obs(mjd("2011-03-05 11:22:33")), svec);
        assertEquals(1, svec.get(0), 0.0f);

        agg.completeSpatial(ctx, 1, svec);


        agg.initTemporal(ctx, tvec);

        agg.aggregateTemporal(ctx, svec, 1, tvec);
        assertEquals(1, tvec.get(0), 0.0f);
        assertEquals(1, tvec.get(1), 0.0f);

        agg.completeTemporal(ctx, 1, tvec);
        assertEquals(1, tvec.get(0), 0.0f);
        assertEquals(1, tvec.get(1), 0.0f);


        agg.computeOutput(tvec, ovec);
        assertEquals(1, ovec.get(0), 0.0f);
        assertEquals(1, ovec.get(1), 0.0f);
    }

    @Test
    public void testAggregate_withMultipleObs() throws Exception {
        AggregatorObservationPeriod agg = new AggregatorObservationPeriod(varCtx, "2011-03-04", "first_obs", "last_obs");

        VectorImpl svec = vec(42);
        VectorImpl tvec = vec(NaN, NaN);
        VectorImpl ovec = vec(NaN, NaN);


        agg.initSpatial(ctx, svec);

        agg.aggregateSpatial(ctx, obs(mjd("2011-03-05 11:22:33")), svec);
        assertEquals(1, svec.get(0), 0.0f);

        agg.completeSpatial(ctx, 1, svec);


        agg.initTemporal(ctx, tvec);

        agg.aggregateTemporal(ctx, vec(1), 1, tvec);
        assertEquals(1, tvec.get(0), 0.0f);
        assertEquals(1, tvec.get(1), 0.0f);

        agg.aggregateTemporal(ctx, vec(7), 1, tvec);
        assertEquals(1, tvec.get(0), 0.0f);
        assertEquals(7, tvec.get(1), 0.0f);

        agg.aggregateTemporal(ctx, vec(3), 1, tvec);
        assertEquals(1, tvec.get(0), 0.0f);
        assertEquals(7, tvec.get(1), 0.0f);

        agg.completeTemporal(ctx, 3, tvec);
        assertEquals(1, tvec.get(0), 0.0f);
        assertEquals(7, tvec.get(1), 0.0f);


        agg.computeOutput(tvec, ovec);
        assertEquals(1, ovec.get(0), 0.0f);
        assertEquals(7, ovec.get(1), 0.0f);
    }

}
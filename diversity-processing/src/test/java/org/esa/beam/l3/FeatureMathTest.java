/*
 * Copyright (C) 2014 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import org.esa.beam.binning.operator.VariableConfig;
import org.esa.beam.binning.support.VectorImpl;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FeatureMathTest {

    static VectorImpl vec(float... values) {
        return new VectorImpl(values);
    }

    @Test
    public void testAssignment() throws Exception {
        MyVariableContext variableContext = new MyVariableContext("A", "B", "C");
        FeatureMath featureMath = new FeatureMath(variableContext, new VariableConfig("B", "B"));

        String[] outputFeatureNames = featureMath.getOutputFeatureNames();
        assertArrayEquals(new String[]{"B"}, outputFeatureNames);

        VectorImpl input = vec(0.1f, 0.2f, 0.3f);
        VectorImpl output = vec(Float.NaN);
        featureMath.compute(input, output);

        assertEquals(0.2f, output.get(0), 1e-5f);
    }

    @Test
    public void testAssignmentWithNameChange() throws Exception {
        MyVariableContext variableContext = new MyVariableContext("A", "B", "C");
        VariableConfig[] variableConfigs = new VariableConfig[]{
                new VariableConfig("B", "B"),
                new VariableConfig("D", "A")
        };
        FeatureMath featureMath = new FeatureMath(variableContext, variableConfigs);

        String[] outputFeatureNames = featureMath.getOutputFeatureNames();
        assertArrayEquals(new String[]{"B", "D"}, outputFeatureNames);

        VectorImpl input = vec(0.1f, 0.2f, 0.3f);
        VectorImpl output= vec(Float.NaN, Float.NaN);
        featureMath.compute(input, output);

        assertEquals(0.2f, output.get(0), 1e-5f);
        assertEquals(0.1f, output.get(1), 1e-5f);
    }

    @Test
    public void testMath() throws Exception {
        MyVariableContext variableContext = new MyVariableContext("A", "B", "C");
        VariableConfig[] variableConfigs = new VariableConfig[]{
                new VariableConfig("D", "A + B"),
                new VariableConfig("E", "A * PI"),
                new VariableConfig("F", "C")
        };
        FeatureMath featureMath = new FeatureMath(variableContext, variableConfigs);

        String[] outputFeatureNames = featureMath.getOutputFeatureNames();
        assertArrayEquals(new String[]{"D", "E", "F"}, outputFeatureNames);

        VectorImpl input = vec(0.1f, 0.2f, 0.3f);
        VectorImpl output= vec(Float.NaN, Float.NaN, Float.NaN);
        featureMath.compute(input, output);

        assertEquals(0.3f, output.get(0), 1e-5f);
        assertEquals(Math.PI * 0.1f, output.get(1), 1e-5f);
        assertEquals(0.3f, output.get(2), 1e-5f);
    }


}
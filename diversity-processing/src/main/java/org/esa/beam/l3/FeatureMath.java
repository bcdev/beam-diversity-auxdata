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

import com.bc.jexp.ParseException;
import com.bc.jexp.Term;
import com.bc.jexp.impl.ParserImpl;
import org.esa.beam.binning.CellProcessor;
import org.esa.beam.binning.CellProcessorConfig;
import org.esa.beam.binning.CellProcessorDescriptor;
import org.esa.beam.binning.VariableContext;
import org.esa.beam.binning.Vector;
import org.esa.beam.binning.WritableVector;
import org.esa.beam.binning.operator.VariableConfig;
import org.esa.beam.framework.gpf.annotations.Parameter;

/**
 * Bandmath in output features of a l3 binning
 */
public class FeatureMath extends CellProcessor {

    private final Term[] compiledExprs;
    private final FeatureEvalEnv evalEnv;

    public FeatureMath(VariableContext varCtx, VariableConfig... variableConfigs) {
        super(getOutputFeatureNames(variableConfigs));
        try {
            VariableContextNamespace namespace = new VariableContextNamespace(varCtx);
            ParserImpl parser = new ParserImpl(namespace, false);
            evalEnv = new FeatureEvalEnv(namespace.getVariableNames());
            compiledExprs = new Term[variableConfigs.length];
            for (int i = 0; i < variableConfigs.length; i++) {
                compiledExprs[i] = parser.parse(variableConfigs[i].getExpr());
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String[] getOutputFeatureNames(VariableConfig[] variableConfigs) {
        String[] result = new String[variableConfigs.length];
        for (int i = 0; i < variableConfigs.length; i++) {
            result[i] = variableConfigs[i].getName().trim();
        }
        return result;
    }

    @Override
    public void compute(Vector inputVector, WritableVector outputVector) {
        evalEnv.setContext(inputVector);
        for (int i = 0; i < compiledExprs.length; i++) {
            Term term = compiledExprs[i];
            float value = (float) term.evalD(evalEnv);
            outputVector.set(i, value);
        }
    }

    public static class Config extends CellProcessorConfig {
        @Parameter(alias = "variables", itemAlias = "variable")
        private VariableConfig[] variableConfigs;
    }

    public static class Descriptor implements CellProcessorDescriptor {

        public static final String NAME = "Math";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public CellProcessor createCellProcessor(VariableContext varCtx, CellProcessorConfig cellProcessorConfig) {
            Config config = (Config) cellProcessorConfig;
            return new FeatureMath(varCtx, config.variableConfigs);
        }

        @Override
        public CellProcessorConfig createConfig() {
            return new Config();
        }
    }
}

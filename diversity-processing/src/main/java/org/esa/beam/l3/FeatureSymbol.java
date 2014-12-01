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

import com.bc.jexp.EvalEnv;
import com.bc.jexp.EvalException;
import com.bc.jexp.Symbol;
import com.bc.jexp.Term;

/**
 * A symbol that evaluates to feature values.
 */
class FeatureSymbol implements Symbol {
    private final String variableName;

    public FeatureSymbol(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String getName() {
        return variableName;
    }

    @Override
    public int getRetType() {
        return Term.TYPE_D;
    }

    @Override
    public boolean evalB(EvalEnv env) throws EvalException {
        float floatValue = getFloatValue(env);
        return floatValue != 0f;
    }

    @Override
    public int evalI(EvalEnv env) throws EvalException {
        float floatValue = getFloatValue(env);
        return (int) floatValue;
    }

    @Override
    public double evalD(EvalEnv env) throws EvalException {
        return getFloatValue(env);
    }

    @Override
    public String evalS(EvalEnv env) throws EvalException {
        return Float.toString(getFloatValue(env));
    }

    private float getFloatValue(EvalEnv env) {
        FeatureEvalEnv recordEvalEnv = (FeatureEvalEnv) env;
        return (float) recordEvalEnv.getValue(variableName);
    }
}

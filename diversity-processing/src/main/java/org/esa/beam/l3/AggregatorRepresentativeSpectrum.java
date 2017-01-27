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

import org.esa.beam.binning.AbstractAggregator;
import org.esa.beam.binning.Aggregator;
import org.esa.beam.binning.AggregatorConfig;
import org.esa.beam.binning.AggregatorDescriptor;
import org.esa.beam.binning.BinContext;
import org.esa.beam.binning.Observation;
import org.esa.beam.binning.VariableContext;
import org.esa.beam.binning.Vector;
import org.esa.beam.binning.WritableVector;
import org.esa.beam.binning.support.GrowableVector;
import org.esa.beam.framework.gpf.annotations.Parameter;

import java.util.Arrays;

import static java.lang.Math.acos;
import static java.lang.Math.sqrt;

/**
 * An aggregator for getting the "most representative spectrum":
 * <p>
 * Collect the median spectrum from the per-pixel observations over 10
 * days, then calculate the spectral angle and spectral difference from
 * each observation (spectrum) to the median spectrum. The spectrum that
 * has the best metric (angle close to unity, smallest difference) is the
 * 'representative spectrum'.
 * <p>
 * Only supported for CompositingType.MOSAICKING.
 */
public class AggregatorRepresentativeSpectrum extends AbstractAggregator {

    private final int[] varIndices;
    private final String[] varNames;
    private final Method method;

    AggregatorRepresentativeSpectrum(VariableContext varCtx, Method method, String... varNames) {
        super(Descriptor.NAME, varNames, varNames, varNames);
        if (varCtx == null) {
            throw new NullPointerException("varCtx");
        }
        if (method == null) {
            throw new NullPointerException("method");
        }
        this.method = method;
        varIndices = new int[varNames.length];
        for (int i = 0; i < varNames.length; i++) {
            int varIndex = varCtx.getVariableIndex(varNames[i]);
            if (varIndex < 0) {
                throw new IllegalArgumentException("setIndexes[" + i + "] < 0");
            }
            varIndices[i] = varIndex;
        }
        this.varNames = varNames;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initSpatial(BinContext binContext, WritableVector writableVector) {
        for (int i = 0; i < varIndices.length; i++) {
            writableVector.set(i, Float.NaN);
        }
    }

    @Override
    public void aggregateSpatial(BinContext binContext, Observation observation, WritableVector writableVector) {
        for (int i = 0; i < varIndices.length; i++) {
            float value = observation.get(varIndices[i]);
            if (Float.isNaN(value)) {
                // if any value isNaN, throw away the complete spectra
                for (int j = 0; j < i; j++) {
                    writableVector.set(j, Float.NaN);
                }
                return;
            } else {
                writableVector.set(i, value);
            }
        }
    }

    @Override
    public void completeSpatial(BinContext binContext, int numSpatialObs, WritableVector writableVector) {
        if (numSpatialObs > 1) {
            throw new IllegalArgumentException("This aggregator only supports MOSAICKING");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initTemporal(BinContext binContext, WritableVector writableVector) {
        for (int i = 0; i < varNames.length; i++) {
            writableVector.set(i, Float.NaN);
            binContext.put(varNames[i], new GrowableVector(10));
        }
    }

    @Override
    public void aggregateTemporal(BinContext binContext, Vector spatialVector, int numSpatialObs, WritableVector temporalVector) {
        float firstValue = spatialVector.get(0);
        if (!Float.isNaN(firstValue)) {
            for (int i = 0; i < varNames.length; i++) {
                GrowableVector measurementsVec = binContext.get(varNames[i]);
                measurementsVec.add(spatialVector.get(i));
            }
        }
    }

    @Override
    public void completeTemporal(BinContext binContext, int numTemporalObs, WritableVector temporalVector) {
        // handle special cases: 0 or 1 observation
        GrowableVector firstVector = binContext.get(varNames[0]);
        int numSpectra = firstVector.size();
        if (numSpectra == 0) {
            return;
        } else if (numSpectra == 1) {
            for (int i = 0; i < varNames.length; i++) {
                GrowableVector measurementsVec = binContext.get(varNames[i]);
                temporalVector.set(i, measurementsVec.get(0));
            }
            return;
        }
        float[][] data = new float[varNames.length][0];
        for (int i = 0; i < varNames.length; i++) {
            GrowableVector vector = binContext.get(varNames[i]);
            data[i] = vector.getElements();
        }
        // I would calculate the median spectrum (as an intermediary step) as the spectrum of per-band median values.
        // For a set of 1 or 2 observations the median is the mean.
        // For 3 or more observations the median is the central value,
        // i.e. the middle one when values are ordered from low to high
        double[][] allSpectra = new double[numSpectra][varNames.length];
        double[] medianSpectrum = computeMedianSpectrum(data, allSpectra);
        int bestIndex = findBest(allSpectra, medianSpectrum);
        if (bestIndex > -1) {
            for (int i = 0; i < varNames.length; i++) {
                temporalVector.set(i, (float) allSpectra[bestIndex][i]);
            }
        }
    }

    private int findBest(double[][] allSpectra, double[] medianSpectrum) {
        int bestIndex = -1;
        double bestValue = Double.POSITIVE_INFINITY;
        for (int i = 0; i < allSpectra.length; i++) {
            double value = method.compute(allSpectra[i], medianSpectrum);
            if (value < bestValue) {
                bestValue = value;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void computeOutput(Vector temporalVector, WritableVector outputVector) {
        for (int i = 0; i < varIndices.length; i++) {
            outputVector.set(i, temporalVector.get(i));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "RepresentativeSpectrum{" +
                "method=" + method +
                "varNames=" + Arrays.toString(varNames) +
                "varIndices=" + Arrays.toString(varIndices) +
                ", spatialFeatureNames=" + Arrays.toString(getSpatialFeatureNames()) +
                ", temporalFeatureNames=" + Arrays.toString(getTemporalFeatureNames()) +
                ", outputFeatureNames=" + Arrays.toString(getOutputFeatureNames()) +
                '}';
    }

    public static class Config extends AggregatorConfig {

        @Parameter(notEmpty = true, notNull = true, description = "The variables making up the spectra.")
        String[] varNames;
        @Parameter(notEmpty = true, notNull = true,
                description = "The method used for finding the best representative spectra",
                defaultValue = "SpectralAngle")
        Method method;

        public Config() {
            super(Descriptor.NAME);
        }
    }

    public static class Descriptor implements AggregatorDescriptor {

        private static final String NAME = "RepresentativeSpectrum";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Aggregator createAggregator(VariableContext varCtx, AggregatorConfig aggregatorConfig) {
            Config config = (Config) aggregatorConfig;
            Method method = config.method != null ? config.method : Method.SpectralAngle;
            return new AggregatorRepresentativeSpectrum(varCtx, method, config.varNames);
        }

        @Override
        public AggregatorConfig createConfig() {
            return new Config();
        }

        @Override
        public String[] getSourceVarNames(AggregatorConfig aggregatorConfig) {
            Config config = (Config) aggregatorConfig;
            return config.varNames;
        }

        @Override
        public String[] getTargetVarNames(AggregatorConfig aggregatorConfig) {
            Config config = (Config) aggregatorConfig;
            return config.varNames;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    static double[] computeMedianSpectrum(float[][] data, double[][] allSpectra) {
        double[] medianSpectrum = new double[allSpectra[0].length];
        for (int i = 0; i < medianSpectrum.length; i++) {
            float[] measurements = data[i];
            for (int spectraIndex = 0; spectraIndex < measurements.length; spectraIndex++) {
                allSpectra[spectraIndex][i] = measurements[spectraIndex];
            }
            Arrays.sort(measurements);
            medianSpectrum[i] = computeMedian(measurements);
        }
        return medianSpectrum;
    }

    static float computeMedian(float... values) {
        if (values.length % 2 == 0) {
            return (values[values.length / 2] + values[values.length / 2 - 1]) / 2;
        } else {
            return values[values.length / 2];
        }
    }

    private static double mean(double sum, int n) {
        if (sum == 0) {
            return 0;
        } else {
            return sum / n;
        }

    }

    enum Method {
        SpectralAngle {
            @Override
            public double compute(double[] spectrum, double[] medianSpectrum) {
                double sumXY = 0;
                double sumXX = 0;
                double sumYY = 0;
                for (int i = 0; i < spectrum.length; i++) {
                    double x = medianSpectrum[i];
                    double y = spectrum[i];
                    sumXX += x * x;
                    sumYY += y * y;
                    sumXY += x * y;
                }
                return acos(sumXY / (sqrt(sumXX) * sqrt(sumYY)));
            }
        },
        AbsoluteDifference {
            @Override
            public double compute(double[] spectrum, double[] medianSpectrum) {
                double sum = 0;
                for (int i = 0; i < spectrum.length; i++) {
                    double x = medianSpectrum[i];
                    double y = spectrum[i];
                    sum += Math.abs((x - y) / y);
                }
                if (sum > 0) {
                    return sum / spectrum.length;
                }
                return mean(sum, spectrum.length);
            }
        },
        RMSDifference {
            @Override
            public double compute(double[] spectrum, double[] medianSpectrum) {
                double sum = 0;
                for (int i = 0; i < spectrum.length; i++) {
                    double x = medianSpectrum[i];
                    double y = spectrum[i];
                    double difference = x - y;
                    sum += difference * difference;
                }
                if (sum > 0) {
                    return Math.sqrt(sum / spectrum.length);
                }
                return sum;
            }
        },
        Bias {
            @Override
            public double compute(double[] spectrum, double[] medianSpectrum) {
                double sum = 0;
                for (int i = 0; i < spectrum.length; i++) {
                    double x = medianSpectrum[i];
                    double y = spectrum[i];
                    sum += (x - y) / y;
                }
                return mean(sum, spectrum.length);
            }
        },
        CoeffOfDetermination {
            @Override
            public double compute(double[] spectrum, double[] medianSpectrum) {
                double sumX = 0;
                double sumY = 0;
                for (int i = 0; i < spectrum.length; i++) {
                    double x = medianSpectrum[i];
                    double y = spectrum[i];
                    sumX += x;
                    sumY += y;
                }
                final double meanX = mean(sumX, spectrum.length);
                final double meanY = mean(sumY, spectrum.length);
                double sumXXYY = 0;
                double sumXX2 = 0;
                double sumYY2 = 0;
                for (int i = 0; i < spectrum.length; i++) {
                    double x = medianSpectrum[i];
                    double y = spectrum[i];
                    final double xx = x - meanX;
                    final double yy = y - meanY;
                    sumXXYY += xx * yy;
                    sumXX2 += xx * xx;
                    sumYY2 += yy * yy;
                }
                return (sumXXYY * sumXXYY) / (sumXX2 * sumYY2);
            }
        };

        public abstract double compute(double[] spectrum, double[] medianSpectrum);
    }
}

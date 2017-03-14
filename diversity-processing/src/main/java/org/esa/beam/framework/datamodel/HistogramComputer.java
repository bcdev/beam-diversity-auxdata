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

package org.esa.beam.framework.datamodel;

import com.bc.ceres.core.PrintWriterProgressMonitor;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.jexp.ParseException;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.util.io.CsvWriter;
import org.esa.beam.util.math.DoubleList;

import javax.media.jai.Histogram;
import javax.media.jai.UnpackedImageData;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * computes histogtrams from 10D products
 */
public class HistogramComputer {

    public static void main(String[] args) throws IOException, ParseException {

        System.setProperty("beam.reader.tileWidth", "1440");
        System.setProperty("beam.reader.tileHeight", "720");

        for (String arg : args) {
            File file = new File(arg);

            String csvName = file.getName() + ".csv";
            try (CsvWriter csvWriter = new CsvWriter(new FileWriter(csvName), "\t")) {

                DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
                NumberFormat format = new DecimalFormat("#0.0###", decimalFormatSymbols);
                String[] header = new String[1000 + 2 + 1];
                header[0] = "bandName";
                header[1] = "belowMin";
                header[2] = "aboveMax";
                for (int i = 3; i < header.length; i++) {
                    header[i] = format.format((i - 3) * 0.001);
                }
                csvWriter.writeRecord(header);

                Product product = ProductIO.readProduct(file);
                try {
                    for (String bandName : product.getBandNames()) {
                        if (bandName.startsWith("Rw")) {
                            System.out.println("bandName = " + bandName);
                            Band band = product.getBand(bandName);

                            ProgressMonitor pm = new PrintWriterProgressMonitor(System.out);

                            MyHistoStxOp op = new MyHistoStxOp(1000, 0, 1);
                            StxFactory.accumulate(band, 0, null, null, op, pm);

                            int[] histogramBins = op.histogram.getBins(0);
                            String[] record = new String[1000 + 2 + 1];
                            record[0] = bandName;
                            record[1] = Integer.toString(op.belowMin);
                            record[2] = Integer.toString(op.aboveMax);
                            for (int i = 3; i < record.length; i++) {
                                record[i] = Integer.toString(histogramBins[i - 3]);
                            }
                            csvWriter.writeRecord(record);
                        }
                    }
                } finally {
                    product.closeIO();
                }
            }
        }
    }

    private static class MyHistoStxOp extends StxOp {

        final Histogram histogram;
        int belowMin = 0;
        int aboveMax = 0;

        MyHistoStxOp(int binCount, double minimum, double maximum) {
            super("Histogram");
            if (Double.isNaN(minimum) || Double.isInfinite(minimum)) {
                minimum = 0.0;
            }
            if (Double.isNaN(maximum) || Double.isInfinite(maximum)) {
                maximum = minimum;
            }
            histogram = StxFactory.createHistogram(binCount, minimum, maximum, false, false);
        }

        @Override
        public void accumulateData(UnpackedImageData dataPixels,
                                   UnpackedImageData maskPixels) {

            // Do not change this code block without doing the same changes in SummaryStxOp.java
            // {{ Block Start

            final DoubleList values = StxOp.asDoubleList(dataPixels);

            final int dataPixelStride = dataPixels.pixelStride;
            final int dataLineStride = dataPixels.lineStride;
            final int dataBandOffset = dataPixels.bandOffsets[0];

            byte[] mask = null;
            int maskPixelStride = 0;
            int maskLineStride = 0;
            int maskBandOffset = 0;
            if (maskPixels != null) {
                mask = maskPixels.getByteData(0);
                maskPixelStride = maskPixels.pixelStride;
                maskLineStride = maskPixels.lineStride;
                maskBandOffset = maskPixels.bandOffsets[0];
            }

            final int width = dataPixels.rect.width;
            final int height = dataPixels.rect.height;

            int dataLineOffset = dataBandOffset;
            int maskLineOffset = maskBandOffset;

            // }} Block End

            final int[] bins = histogram.getBins(0);
            final double lowValue = histogram.getLowValue(0);
            final double highValue = histogram.getHighValue(0);
            final double binWidth = (highValue - lowValue) / bins.length;

            for (int y = 0; y < height; y++) {
                int dataPixelOffset = dataLineOffset;
                int maskPixelOffset = maskLineOffset;
                for (int x = 0; x < width; x++) {
                    if (mask == null || mask[maskPixelOffset] != 0) {
                        final double value = values.getDouble(dataPixelOffset);
                        if (value < lowValue) {
                            belowMin++;
                        } else if (value > highValue) {
                            aboveMax++;
                        } else {
                            int i = (int) ((value - lowValue) / binWidth);
                            if (i == bins.length) {
                                i--;
                            }
                            bins[i]++;
                        }
                    }
                    dataPixelOffset += dataPixelStride;
                    maskPixelOffset += maskPixelStride;
                }
                dataLineOffset += dataLineStride;
                maskLineOffset += maskLineStride;
            }
        }
    }
}

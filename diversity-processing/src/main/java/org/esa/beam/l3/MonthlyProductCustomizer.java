/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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

import org.esa.beam.binning.ProductCustomizer;
import org.esa.beam.binning.ProductCustomizerConfig;
import org.esa.beam.binning.ProductCustomizerDescriptor;
import org.esa.beam.collocation.CollocateOp;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.annotations.Parameter;
import org.esa.beam.util.ProductUtils;

/**
 * Removes num_obs and num_passes bands, depending on configuration.
 */
public class MonthlyProductCustomizer extends ProductCustomizer {

    private final boolean writeNumObs;
    private final boolean writeNumPasses;
    private final Product shallowProduct;

    private Product arcDayProduct;
    private Product arcNightProduct;
    private String arcBand;


    public MonthlyProductCustomizer(boolean writeNumObs, boolean writeNumPasses, Product shallowProduct) {
        this.writeNumObs = writeNumObs;
        this.writeNumPasses = writeNumPasses;
        this.shallowProduct = shallowProduct;
    }

    @Override
    public void customizeProduct(Product product) {
        if (!writeNumObs) {
            Band numObsBand = product.getBand("num_obs");
            if (numObsBand != null) {
                product.removeBand(numObsBand);
            }
        }
        if (!writeNumPasses) {
            Band numPassesBand = product.getBand("num_passes");
            if (numPassesBand != null) {
                product.removeBand(numPassesBand);
            }
        }
        Product shallowCollocated = collocate(product, shallowProduct);
        ProductUtils.copyBand("shallow", shallowCollocated, product, true);
//        ProductUtils.copyBand("extent", shallowCollocated, product, true);

        if (arcDayProduct != null) {
            Product arcDayCollocated = collocate(product, arcDayProduct);
            Band lswt_d_mean = ProductUtils.copyBand(arcBand, arcDayCollocated, "lswt_d_mean", product, true);
            lswt_d_mean.setValidPixelExpression("lswt_d_mean > 0");

            Product arcNightCollocated = collocate(product, arcNightProduct);
            Band lswt_n_mean = ProductUtils.copyBand(arcBand, arcNightCollocated, "lswt_n_mean", product, true);
            lswt_n_mean.setValidPixelExpression("lswt_n_mean > 0");
        } else {
            product.addBand("lswt_d_mean", "NaN");
            product.addBand("lswt_n_mean", "NaN");
        }
    }

    private Product collocate(Product masterProduct, Product slaveProduct) {
        CollocateOp collocateOp = new CollocateOp();
        collocateOp.setParameterDefaultValues();
        collocateOp.setRenameMasterComponents(true);
        collocateOp.setRenameSlaveComponents(false);
        collocateOp.setMasterProduct(masterProduct);
        collocateOp.setSlaveProduct(slaveProduct);
        return collocateOp.getTargetProduct();
    }


    public static class Config extends ProductCustomizerConfig {
        @Parameter(defaultValue = "true")
        private Boolean writeNumObs;
        @Parameter(defaultValue = "true")
        private Boolean writeNumPasses;

        @Parameter()
        private Product arcDayProduct;
        @Parameter()
        private Product arcNightProduct;
        @Parameter()
        private String arcBand;

        @Parameter()
        private Product shallowProduct;
    }

    public static class Descriptor implements ProductCustomizerDescriptor {

        @Override
        public ProductCustomizer createProductCustomizer(ProductCustomizerConfig pcc) {
            Config config = (Config) pcc;

            boolean writeNumObs = config.writeNumObs != null ? config.writeNumObs : true;
            boolean writeNumPasses = config.writeNumPasses != null ? config.writeNumPasses : true;

            MonthlyProductCustomizer productCustomizer = new MonthlyProductCustomizer(writeNumObs, writeNumPasses, config.shallowProduct);
            if (config.arcDayProduct != null && config.arcNightProduct != null && config.arcBand != null) {
                productCustomizer.arcDayProduct = config.arcDayProduct;
                productCustomizer.arcNightProduct = config.arcNightProduct;
                productCustomizer.arcBand = config.arcBand;
            }
            return productCustomizer;
        }

        @Override
        public String getName() {
            return "MonthlyProductDiversity";
        }

        @Override
        public ProductCustomizerConfig createConfig() {
            return new Config();
        }
    }
}

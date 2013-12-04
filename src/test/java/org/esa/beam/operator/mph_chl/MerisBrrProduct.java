package org.esa.beam.operator.mph_chl;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;

class MerisBrrProduct {

    // creates an in memory product of Type MER_FRS_1P_BRR with four pixels
    // Product: MER_FRS_1PNPDE20111116_074012_000001913108_00351_50796_0075_brr.h5
    //
    // px       original [x,y]
    // [0]      [18, 25]
    // [1]      [29, 15]
    // [2]      [37, 22]
    // [3]      [25, 24]
    //
    static Product create() {
        final Product product = new Product("Meris L1B BRR", "MER_FRS_1P_BRR", 2, 2);

        addBrr_06(product);
        addBrr_07(product);
        addBrr_08(product);
        addBrr_09(product);
        addBrr_10(product);
        addBrr_14(product);
        addl2_flags_p1(product);

        addFlagCoding(product);

        return product;
    }

    private static void addBrr_06(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_6", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.034655124f);
        rasterData.setElemFloatAt(1, 0.087953895f);
        rasterData.setElemFloatAt(2, 0.033937406f);
        rasterData.setElemFloatAt(3, 0.032129426f);
        band.setData(rasterData);
    }

    private static void addBrr_07(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_7", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.032847762f);
        rasterData.setElemFloatAt(1, 0.088664845f);
        rasterData.setElemFloatAt(2, 0.030662278f);
        rasterData.setElemFloatAt(3, 0.028754385f);
        band.setData(rasterData);
    }

    private static void addBrr_08(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_8", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.029677795f);
        rasterData.setElemFloatAt(1, 0.09008631f);
        rasterData.setElemFloatAt(2, 0.02861059f);
        rasterData.setElemFloatAt(3, 0.027694196f);
        band.setData(rasterData);
    }

    private static void addBrr_09(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_9", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.050143242f);
        rasterData.setElemFloatAt(1, 0.13810226f);
        rasterData.setElemFloatAt(2, 0.049303167f);
        rasterData.setElemFloatAt(3, 0.041109152f);
        band.setData(rasterData);
    }

    private static void addBrr_10(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_10", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.033532504f);
        rasterData.setElemFloatAt(1, 0.24122652f);
        rasterData.setElemFloatAt(2, 0.031760864f);
        rasterData.setElemFloatAt(3, 0.02975263f);
        band.setData(rasterData);
    }

    private static void addBrr_14(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_14", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.02517954f);
        rasterData.setElemFloatAt(1, 0.26832125f);
        rasterData.setElemFloatAt(2, 0.024442881f);
        rasterData.setElemFloatAt(3, 0.025061151f);
        band.setData(rasterData);
    }

    private static void addl2_flags_p1(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("l2_flags_p1", ProductData.TYPE_INT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemIntAt(0, 655360);
        rasterData.setElemIntAt(1, 328192);
        rasterData.setElemIntAt(2, 524288);
        rasterData.setElemIntAt(3, 524800);
        band.setData(rasterData);
    }

    private static void addFlagCoding(Product merisL1BProduct) {
        final FlagCoding l1_flags = new FlagCoding("l2_flags_p1");
        l1_flags.addFlag("F_COASTLINE", 0x200, "No Description.");
        l1_flags.addFlag("F_LANDCONS", 0x40000, "No Description.");
        merisL1BProduct.getBand("l2_flags_p1").setSampleCoding(l1_flags);
        merisL1BProduct.getFlagCodingGroup().add(l1_flags);
    }

    /*
    Product:	MER_FRS_1PNPDE20111116_074012_000001913108_00351_50796_0075_brr

Image-X:	25	pixel
Image-Y:	24	pixel

BandName	Value	Unit
bands/brr_1:	0.013229539
bands/brr_10:	0.02975263
bands/brr_12:	0.029458823
bands/brr_13:	0.025196435
bands/brr_14:	0.025061151
bands/brr_2:	0.019184396
bands/brr_3:	0.0272099
bands/brr_4:	0.032867614
bands/brr_5:	0.049779385
bands/brr_6:	0.032129426
bands/brr_7:	0.028754385
bands/brr_8:	0.027694196
bands/brr_9:	0.041109152
bands/l2_flags_p1:	524800
bands/l2_flags_p2:	176
bands/l2_flags_p3:	0
bands/toar_1:	0.16353011
bands/toar_10:	0.041962106
bands/toar_11:	0.016947297
bands/toar_12:	0.040147804
bands/toar_13:	0.03227928
bands/toar_14:	0.031191606
bands/toar_15:	0.022615047
bands/toar_2:	0.13186742
bands/toar_3:	0.09971844
bands/toar_4:	0.09165738
bands/toar_5:	0.084506966
bands/toar_6:	0.05587106
bands/toar_7:	0.048049096
bands/toar_8:	0.045536917
bands/toar_9:	0.0531186


   */
}

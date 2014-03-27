package org.esa.beam.diversity.mph_chl;

import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.FlagCoding;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;

class MerisBrrProduct {

    // creates an in memory product of Type MER_FRS_1P_BRR with four pixels
    // Product: L2_of_MER_FSG_1PNUPA20110605_160100_000003633103_00155_48445_6691.dim
    //
    // px       original [x,y]
    // [0]      [636,  507]
    // [1]      [980,  702]
    // [2]      [1117, 750]
    // [3]      [1045, 385]
    //
    static Product create() {
        final Product product = new Product("Meris L1B BRR", "MER_FRS_1P_BRR", 2, 2);

        addBrr_06(product);
        addBrr_07(product);
        addBrr_08(product);
        addBrr_09(product);
        addBrr_10(product);
        addBrr_14(product);

        addl1_flags(product);
        addcloud_classif_flags(product);
        addgas_flags(product);
        addray_corr_flags(product);

        addFlagCodings(product);

        return product;
    }

    private static void addBrr_06(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_6", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.056901217f);
        rasterData.setElemFloatAt(1, 0.08701459f);
        rasterData.setElemFloatAt(2, 0.09533884f);
        rasterData.setElemFloatAt(3, 0.06557705f);
        band.setData(rasterData);
    }

    private static void addBrr_07(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_7", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.047871254f);
        rasterData.setElemFloatAt(1, 0.083326936f);
        rasterData.setElemFloatAt(2, 0.085396804f);
        rasterData.setElemFloatAt(3, 0.057835124f);
        band.setData(rasterData);
    }

    private static void addBrr_08(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_8", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.04565412f);
        rasterData.setElemFloatAt(1, 0.08187773f);
        rasterData.setElemFloatAt(2, 0.080895595f);
        rasterData.setElemFloatAt(3, 0.058175918f);
        band.setData(rasterData);
    }

    private static void addBrr_09(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_9", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.042183727f);
        rasterData.setElemFloatAt(1, 0.07923279f);
        rasterData.setElemFloatAt(2, 0.082916535f);
        rasterData.setElemFloatAt(3, 0.11774466f);
        band.setData(rasterData);
    }

    private static void addBrr_10(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_10", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.02940591f);
        rasterData.setElemFloatAt(1, 0.072338045f);
        rasterData.setElemFloatAt(2, 0.06302089f);
        rasterData.setElemFloatAt(3, 0.25775266f);
        band.setData(rasterData);
    }

    private static void addBrr_14(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("brr_14", ProductData.TYPE_FLOAT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemFloatAt(0, 0.020540202f);
        rasterData.setElemFloatAt(1, 0.07036572f);
        rasterData.setElemFloatAt(2, 0.05602023f);
        rasterData.setElemFloatAt(3, 0.30576614f);
        band.setData(rasterData);
    }

    private static void addl1_flags(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("l1_flags", ProductData.TYPE_INT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemIntAt(0, 2);
        rasterData.setElemIntAt(1, 0);
        rasterData.setElemIntAt(2, 0);
        rasterData.setElemIntAt(3, 16);
        band.setData(rasterData);
    }

    private static void addcloud_classif_flags(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("cloud_classif_flags", ProductData.TYPE_INT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemIntAt(0, 1312);
        rasterData.setElemIntAt(1, 1376);
        rasterData.setElemIntAt(2, 1312);
        rasterData.setElemIntAt(3, 8192);
        band.setData(rasterData);
    }

    private static void addgas_flags(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("gas_flags", ProductData.TYPE_INT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemIntAt(0, 1);
        rasterData.setElemIntAt(1, 1);
        rasterData.setElemIntAt(2, 1);
        rasterData.setElemIntAt(3, 1);
        band.setData(rasterData);
    }

    private static void addray_corr_flags(Product merisL1BProduct) {
        final Band band = merisL1BProduct.addBand("ray_corr_flags", ProductData.TYPE_INT32);
        final ProductData rasterData = band.createCompatibleRasterData();
        rasterData.setElemIntAt(0, 0);
        rasterData.setElemIntAt(1, 0);
        rasterData.setElemIntAt(2, 0);
        rasterData.setElemIntAt(3, 0);
        band.setData(rasterData);
    }

    private static void addFlagCodings(Product merisL1BProduct) {
        final FlagCoding l1_flags = new FlagCoding("l1_flags");
        l1_flags.addFlag("LAND_OCEAN", 0x10, "Pixel is over land, not ocean.");
        l1_flags.addFlag("INVALID", 0x80, "Pixel is invalid.");
        merisL1BProduct.getBand("l1_flags").setSampleCoding(l1_flags);
        merisL1BProduct.getFlagCodingGroup().add(l1_flags);

        final FlagCoding cloud_classif_flags = new FlagCoding("cloud_classif_flags");
        cloud_classif_flags.addFlag("F_CLOUD", 0x1, "none");
        cloud_classif_flags.addFlag("F_CLOUD_BUFFER", 0x800, "none");
        cloud_classif_flags.addFlag("F_CLOUD_SHADOW", 0x1000, "none");
        cloud_classif_flags.addFlag("F_LAND", 0x2000, "none");
        cloud_classif_flags.addFlag("F_MIXED_PIXEL", 0x8000, "none");
        merisL1BProduct.getBand("cloud_classif_flags").setSampleCoding(cloud_classif_flags);
        merisL1BProduct.getFlagCodingGroup().add(cloud_classif_flags);
    }

    /*
   Product:	L2_of_MER_FSG_1PNUPA20110605_160100_000003633103_00155_48445_6691

Image-X:	1045	pixel
Image-Y:	385	pixel
Longitude:	84°47'42" W	degree
Latitude:	12°04'53" N	degree

BandName	Wavelength	Unit	Bandwidth	Unit	Value	Unit	Solar Flux	Unit
corr_latitude:					12.081339	deg
corr_longitude:					-84.79491	deg
l1_flags:					16
rho_toa_1:	412.691	nm	9.937	nm	0.16180843		1667.3027	mW/(m^2*nm)
rho_toa_2:	442.55902	nm	9.946	nm	0.13411917		1826.8854	mW/(m^2*nm)
rho_toa_3:	489.88202	nm	9.957001	nm	0.10526215		1874.9609	mW/(m^2*nm)
rho_toa_4:	509.81903	nm	9.961	nm	0.09748246		1875.5435	mW/(m^2*nm)
rho_toa_5:	559.69403	nm	9.97	nm	0.09950226		1753.1674	mW/(m^2*nm)
rho_toa_6:	619.601	nm	9.979	nm	0.07653052		1605.0806	mW/(m^2*nm)
rho_toa_7:	664.57306	nm	9.985001	nm	0.06805835		1489.2312	mW/(m^2*nm)
rho_toa_8:	680.82104	nm	7.4880004	nm	0.0677347		1431.4186	mW/(m^2*nm)
rho_toa_9:	708.32904	nm	9.992001	nm	0.11199092		1368.971	mW/(m^2*nm)
rho_toa_10:	753.37103	nm	7.4950004	nm	0.2589853		1230.9989	mW/(m^2*nm)
rho_toa_11:	761.50806	nm	3.7440002	nm	0.08669543		1219.8545	mW/(m^2*nm)
rho_toa_12:	778.40906	nm	15.010001	nm	0.27586251		1144.6731	mW/(m^2*nm)
rho_toa_13:	864.87604	nm	20.047	nm	0.3013576		931.8572	mW/(m^2*nm)
rho_toa_14:	884.94403	nm	10.018001	nm	0.29951075		904.1002	mW/(m^2*nm)
rho_toa_15:	900.00006	nm	10.02	nm	0.16240893		870.67334	mW/(m^2*nm)
gas_flags:					1
brr_1:	412.691	nm	9.937	nm	0.07562027		1667.3027	mW/(m^2*nm)
brr_2:	442.55902	nm	9.946	nm	0.06858118		1826.8854	mW/(m^2*nm)
brr_3:	489.88202	nm	9.957001	nm	0.06284752		1874.9609	mW/(m^2*nm)
brr_4:	509.81903	nm	9.961	nm	0.0643221		1875.5435	mW/(m^2*nm)
brr_5:	559.69403	nm	9.97	nm	0.082546		1753.1674	mW/(m^2*nm)
brr_6:	619.601	nm	9.979	nm	0.06557705		1605.0806	mW/(m^2*nm)
brr_7:	664.57306	nm	9.985001	nm	0.057835124		1489.2312	mW/(m^2*nm)
brr_8:	680.82104	nm	7.4880004	nm	0.058175918		1431.4186	mW/(m^2*nm)
brr_9:	708.32904	nm	9.992001	nm	0.11774466		1368.971	mW/(m^2*nm)
brr_10:	753.37103	nm	7.4950004	nm	0.25775266		1230.9989	mW/(m^2*nm)
brr_12:	778.40906	nm	15.010001	nm	0.27536434		1144.6731	mW/(m^2*nm)
brr_13:	864.87604	nm	20.047	nm	0.30122972		931.8572	mW/(m^2*nm)
brr_14:	884.94403	nm	10.018001	nm	0.30576614		904.1002	mW/(m^2*nm)
ray_corr_flags:					0
schiller_cloud_value:					1.1104847
cloud_classif_flags:					8192

latitude:	12.0796175	deg
longitude:	-84.793724	deg
dem_alt:	160.08594	m
dem_rough:	37.80957	m
lat_corr:	1.7445849E-4	deg
lon_corr:	-7.6175784E-4	deg
sun_zenith:	24.538416	deg
sun_azimuth:	61.587795	deg
view_zenith:	28.118782	deg
view_azimuth:	283.23053	deg
zonal_wind:	3.4231446	m/s
merid_wind:	1.8701661	m/s
atm_press:	1010.703	hPa
ozone:	266.68054	DU
rel_hum:	85.69594	%

l1_flags.COSMETIC:	false
l1_flags.DUPLICATED:	false
l1_flags.GLINT_RISK:	false
l1_flags.SUSPECT:	false
l1_flags.LAND_OCEAN:	true
l1_flags.BRIGHT:	false
l1_flags.COASTLINE:	false
l1_flags.INVALID:	false

gas_flags.F_DO_CORRECT:	true
gas_flags.F_SUN70:	false
gas_flags.F_ORINP0:	false
gas_flags.F_OROUT0:	false

ray_corr_flags.F_NEGATIV_BRR_1:	false
ray_corr_flags.F_NEGATIV_BRR_2:	false
ray_corr_flags.F_NEGATIV_BRR_3:	false
ray_corr_flags.F_NEGATIV_BRR_4:	false
ray_corr_flags.F_NEGATIV_BRR_5:	false
ray_corr_flags.F_NEGATIV_BRR_6:	false
ray_corr_flags.F_NEGATIV_BRR_7:	false
ray_corr_flags.F_NEGATIV_BRR_8:	false
ray_corr_flags.F_NEGATIV_BRR_9:	false
ray_corr_flags.F_NEGATIV_BRR_10:	false
ray_corr_flags.F_NEGATIV_BRR_12:	false
ray_corr_flags.F_NEGATIV_BRR_13:	false
ray_corr_flags.F_NEGATIV_BRR_14:	false

cloud_classif_flags.F_CLOUD:	false
cloud_classif_flags.F_BRIGHT:	false
cloud_classif_flags.F_BRIGHT_RC:	false
cloud_classif_flags.F_LOW_PSCATT:	false
cloud_classif_flags.F_SLOPE_1:	false
cloud_classif_flags.F_SLOPE_2:	false
cloud_classif_flags.F_BRIGHT_TOA:	false
cloud_classif_flags.F_HIGH_MDSI:	false
cloud_classif_flags.F_SNOW_ICE:	false
cloud_classif_flags.F_GLINTRISK:	false
cloud_classif_flags.F_CLOUD_BUFFER:	false
cloud_classif_flags.F_CLOUD_SHADOW:	false
cloud_classif_flags.F_LAND:	true
cloud_classif_flags.F_COASTLINE:	false
cloud_classif_flags.F_CLOUD_AMBIGUOUS:	false
cloud_classif_flags.F_MIXED_PIXEL:	false

   */
}

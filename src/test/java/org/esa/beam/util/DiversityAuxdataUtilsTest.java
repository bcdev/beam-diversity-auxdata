package org.esa.beam.util;

import org.esa.beam.Constants;
import org.esa.beam.framework.datamodel.Product;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DiversityAuxdataUtilsTest {

    @Test
    public void testSortSmByMonth() {
        Product[] unsortedSmProducts = new Product[24];
        unsortedSmProducts[0] = new Product("DIVERSITY_SM_BIWEEKLY_jan_b", "bla", 0, 0);
        unsortedSmProducts[1] = new Product("DIVERSITY_SM_BIWEEKLY_mar_b", "bla", 0, 0);
        unsortedSmProducts[2] = new Product("DIVERSITY_SM_BIWEEKLY_oct_b", "bla", 0, 0);
        unsortedSmProducts[3] = new Product("DIVERSITY_SM_BIWEEKLY_jan_a", "bla", 0, 0);

        final Product[] sortedSmProducts = DiversityAuxdataUtils.sortProductsByMonth(unsortedSmProducts, null, 22, 26);
        assertNotNull(sortedSmProducts);
        assertEquals(4, sortedSmProducts.length);
        assertEquals("DIVERSITY_SM_BIWEEKLY_jan_a", sortedSmProducts[0].getName());
        assertEquals("DIVERSITY_SM_BIWEEKLY_jan_b", sortedSmProducts[1].getName());
        assertEquals("DIVERSITY_SM_BIWEEKLY_mar_b", sortedSmProducts[2].getName());
        assertEquals("DIVERSITY_SM_BIWEEKLY_oct_b", sortedSmProducts[3].getName());
    }

    @Test
    public void testSortTrmmByMonth() {
        Product[] unsortedSmProducts = new Product[24];
        unsortedSmProducts[0] = new Product("DIVERSITY_TRMM_BIWEEKLY_jan_b", "bla", 0, 0);
        unsortedSmProducts[1] = new Product("DIVERSITY_TRMM_BIWEEKLY_mar_b", "bla", 0, 0);
        unsortedSmProducts[2] = new Product("DIVERSITY_TRMM_BIWEEKLY_oct_b", "bla", 0, 0);
        unsortedSmProducts[3] = new Product("DIVERSITY_TRMM_BIWEEKLY_jan_a", "bla", 0, 0);

        final Product[] sortedSmProducts = DiversityAuxdataUtils.sortProductsByMonth(unsortedSmProducts, null, 24, 28);
        assertNotNull(sortedSmProducts);
        assertEquals(4, sortedSmProducts.length);
        assertEquals("DIVERSITY_TRMM_BIWEEKLY_jan_a", sortedSmProducts[0].getName());
        assertEquals("DIVERSITY_TRMM_BIWEEKLY_jan_b", sortedSmProducts[1].getName());
        assertEquals("DIVERSITY_TRMM_BIWEEKLY_mar_b", sortedSmProducts[2].getName());
        assertEquals("DIVERSITY_TRMM_BIWEEKLY_oct_b", sortedSmProducts[3].getName());
    }


    @Test
    public void testSortNdviByMonth() {
        Product[] unsortedNdviProducts = new Product[24];
        unsortedNdviProducts[0] = new Product("06apr15a.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[1] = new Product("06aug15a.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[2] = new Product("06jan15b.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[3] = new Product("06nov15a.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[4] = new Product("06mar15a.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[5] = new Product("06dec15b.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[6] = new Product("06mar15b.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[7] = new Product("06may15a.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[8] = new Product("06apr15b.n17-VIg_data", "bla", 0, 0);
        unsortedNdviProducts[9] = new Product("06jun15a.n17-VIg_data", "bla", 0, 0);

        final Product[] sortedNdviProducts = DiversityAuxdataUtils.sortProductsByMonth(unsortedNdviProducts, "_data", 2, 7);
        assertNotNull(sortedNdviProducts);
        assertEquals(10, sortedNdviProducts.length);
        assertEquals("06jan15b.n17-VIg_data", sortedNdviProducts[0].getName());
        assertEquals("06mar15a.n17-VIg_data", sortedNdviProducts[1].getName());
        assertEquals("06mar15b.n17-VIg_data", sortedNdviProducts[2].getName());
        assertEquals("06apr15a.n17-VIg_data", sortedNdviProducts[3].getName());
        assertEquals("06apr15b.n17-VIg_data", sortedNdviProducts[4].getName());
        assertEquals("06may15a.n17-VIg_data", sortedNdviProducts[5].getName());
        assertEquals("06jun15a.n17-VIg_data", sortedNdviProducts[6].getName());
        assertEquals("06aug15a.n17-VIg_data", sortedNdviProducts[7].getName());
        assertEquals("06nov15a.n17-VIg_data", sortedNdviProducts[8].getName());
        assertEquals("06dec15b.n17-VIg_data", sortedNdviProducts[9].getName());
    }

    @Test
    public void testGet8DayProductFractionsForBiweeklyPeriods_doubleOverlap() {
        String biweeklyStartDate = "20060116";
        String biweeklyEndDate = "20060131";

        BiweeklyProductFraction bpf = DiversityAuxdataUtils.getGet8DayProductFractionsForBiweeklyPeriods(biweeklyStartDate,
                                                                                                         biweeklyEndDate);
        assertNotNull(bpf);
        assertNotNull(bpf.getBiweeklyPeriodFractions());
        assertNotNull(bpf.getEightDayPeriodIdentifiers());
        assertEquals(3, bpf.getBiweeklyPeriodFractions().length);
        assertEquals(3, bpf.getEightDayPeriodIdentifiers().length);

        // we have the periods 009 (1 of 8 days: Jan 16), 017 (8 of 8 days), 025 (7 of 8 days, Jan 25-31)
        assertEquals("009", bpf.getEightDayPeriodIdentifiers()[0]);
        assertEquals("017", bpf.getEightDayPeriodIdentifiers()[1]);
        assertEquals("025", bpf.getEightDayPeriodIdentifiers()[2]);

        final double sumFrac = bpf.getBiweeklyPeriodFractions()[0] +
                bpf.getBiweeklyPeriodFractions()[1] +
                bpf.getBiweeklyPeriodFractions()[2];
        assertEquals(1.0, sumFrac, 1.E-6);
        assertEquals(0.0625, bpf.getBiweeklyPeriodFractions()[0], 1.E-6); // 1/16
        assertEquals(0.5, bpf.getBiweeklyPeriodFractions()[1], 1.E-6);    // 8/16
        assertEquals(0.4375, bpf.getBiweeklyPeriodFractions()[2], 1.E-6); // 7/16
    }

    @Test
    public void testGet8DayProductFractionsForBiweeklyPeriods_rightOverlap() {
        String biweeklyStartDate = "20060117";
        String biweeklyEndDate = "20060131";

        BiweeklyProductFraction bpf = DiversityAuxdataUtils.getGet8DayProductFractionsForBiweeklyPeriods(biweeklyStartDate,
                                                                                                         biweeklyEndDate);
        assertNotNull(bpf);
        assertNotNull(bpf.getBiweeklyPeriodFractions());
        assertNotNull(bpf.getEightDayPeriodIdentifiers());
        assertEquals(2, bpf.getBiweeklyPeriodFractions().length);
        assertEquals(2, bpf.getEightDayPeriodIdentifiers().length);

        // we have the periods 017 (8 of 8 days), 025 (7 of 8 days, Jan 25-31)
        assertEquals("017", bpf.getEightDayPeriodIdentifiers()[0]);
        assertEquals("025", bpf.getEightDayPeriodIdentifiers()[1]);

        final double sumFrac = bpf.getBiweeklyPeriodFractions()[0] + bpf.getBiweeklyPeriodFractions()[1];
        assertEquals(1.0, sumFrac, 1.E-6);
        assertEquals(0.533333, bpf.getBiweeklyPeriodFractions()[0], 1.E-6); // 8/15
        assertEquals(0.466667, bpf.getBiweeklyPeriodFractions()[1], 1.E-6);    // 7/15
    }

    @Test
    public void testGet8DayProductFractionsForBiweeklyPeriods_leftOverlap() {
        String biweeklyStartDate = "20060118";
        String biweeklyEndDate = "20060201";

        BiweeklyProductFraction bpf = DiversityAuxdataUtils.getGet8DayProductFractionsForBiweeklyPeriods(biweeklyStartDate,
                                                                                                         biweeklyEndDate);
        assertNotNull(bpf);
        assertNotNull(bpf.getBiweeklyPeriodFractions());
        assertNotNull(bpf.getEightDayPeriodIdentifiers());
        assertEquals(2, bpf.getBiweeklyPeriodFractions().length);
        assertEquals(2, bpf.getEightDayPeriodIdentifiers().length);

        // we have the periods 017 (7 of 8 days), 025 (8 of 8 days, Jan 25 - Feb 01)
        assertEquals("017", bpf.getEightDayPeriodIdentifiers()[0]);
        assertEquals("025", bpf.getEightDayPeriodIdentifiers()[1]);

        final double sumFrac = bpf.getBiweeklyPeriodFractions()[0] + bpf.getBiweeklyPeriodFractions()[1];
        assertEquals(1.0, sumFrac, 1.E-6);
        assertEquals(0.466667, bpf.getBiweeklyPeriodFractions()[0], 1.E-6);    // 7/15
        assertEquals(0.533333, bpf.getBiweeklyPeriodFractions()[1], 1.E-6); // 8/15
    }

    @Test
    public void testGet8DayProductFractionsForBiweeklyPeriods_exactlyFitting() {
        String biweeklyStartDate = "20060117";
        String biweeklyEndDate = "20060201";

        BiweeklyProductFraction bpf = DiversityAuxdataUtils.getGet8DayProductFractionsForBiweeklyPeriods(biweeklyStartDate,
                                                                                                         biweeklyEndDate);
        assertNotNull(bpf);
        assertNotNull(bpf.getBiweeklyPeriodFractions());
        assertNotNull(bpf.getEightDayPeriodIdentifiers());
        assertEquals(2, bpf.getBiweeklyPeriodFractions().length);
        assertEquals(2, bpf.getEightDayPeriodIdentifiers().length);

        // we have the periods 017 (8 of 8 days), 025 (8 of 8 days, Jan 25 - Feb 01)
        assertEquals("017", bpf.getEightDayPeriodIdentifiers()[0]);
        assertEquals("025", bpf.getEightDayPeriodIdentifiers()[1]);

        final double sumFrac = bpf.getBiweeklyPeriodFractions()[0] + bpf.getBiweeklyPeriodFractions()[1];
        assertEquals(1.0, sumFrac, 1.E-6);
        assertEquals(0.5, bpf.getBiweeklyPeriodFractions()[0], 1.E-6);    // 8/16
        assertEquals(0.5, bpf.getBiweeklyPeriodFractions()[1], 1.E-6); // 8/16
    }

}

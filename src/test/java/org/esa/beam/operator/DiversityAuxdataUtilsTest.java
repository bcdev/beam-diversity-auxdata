package org.esa.beam.operator;

import org.esa.beam.Constants;
import org.esa.beam.DiversityAuxdataUtils;
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
    public void testSortNdviByMonthaaaaa() {

        Product[] unsortedProducts = new Product[24];
        unsortedProducts[0] = new Product("06apr15a.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[1] = new Product("06aug15a.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[2] = new Product("06jan15b.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[3] = new Product("06nov15a.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[4] = new Product("06mar15a.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[5] = new Product("06dec15b.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[6] = new Product("06mar15b.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[7] = new Product("06may15a.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[8] = new Product("06apr15b.n17-VIg_data", "bla", 0, 0);
        unsortedProducts[9] = new Product("06jun15a.n17-VIg_data", "bla", 0, 0);

        final Product[] sortedProducts = DiversityAuxdataUtils.sortNdviProductsByMonth(unsortedProducts, "_data");
        assertNotNull(sortedProducts);
        assertEquals(10, sortedProducts.length);
        assertEquals("06jan15b.n17-VIg_data", sortedProducts[0].getName());
        assertEquals("06mar15a.n17-VIg_data", sortedProducts[1].getName());
        assertEquals("06mar15b.n17-VIg_data", sortedProducts[2].getName());
        assertEquals("06apr15a.n17-VIg_data", sortedProducts[3].getName());
        assertEquals("06apr15b.n17-VIg_data", sortedProducts[4].getName());
        assertEquals("06may15a.n17-VIg_data", sortedProducts[5].getName());
        assertEquals("06jun15a.n17-VIg_data", sortedProducts[6].getName());
        assertEquals("06aug15a.n17-VIg_data", sortedProducts[7].getName());
        assertEquals("06nov15a.n17-VIg_data", sortedProducts[8].getName());
        assertEquals("06dec15b.n17-VIg_data", sortedProducts[9].getName());
    }

//    @Test
//    public void testSortSmByMonth() {
//
//        Product[] unsortedProducts = new Product[24];
//        unsortedProducts[0] = new Product("DIVERSITY_SM_BIWEEKLY_jan_b", "bla", 0, 0);
//        unsortedProducts[1] = new Product("DIVERSITY_SM_BIWEEKLY_mar_b", "bla", 0, 0);
//        unsortedProducts[2] = new Product("DIVERSITY_SM_BIWEEKLY_oct_b", "bla", 0, 0);
//        unsortedProducts[3] = new Product("DIVERSITY_SM_BIWEEKLY_jan_a", "bla", 0, 0);
//
//        final Product[] sortedProducts = DiversityAuxdataUtils.sortSoilMoistureProductsByMonth(unsortedProducts, null);
//        assertNotNull(sortedProducts);
//        assertEquals(4, sortedProducts.length);
//        assertEquals("DIVERSITY_SM_BIWEEKLY_jan_a", sortedProducts[0].getName());
//        assertEquals("DIVERSITY_SM_BIWEEKLY_jan_b", sortedProducts[1].getName());
//        assertEquals("DIVERSITY_SM_BIWEEKLY_mar_b", sortedProducts[2].getName());
//        assertEquals("DIVERSITY_SM_BIWEEKLY_oct_b", sortedProducts[3].getName());
//    }

    @Test
    public void testGet8DayProductFractionsForBiweeklyPeriods() {
        String biweeklyStartDate = Constants.BIWEEKLY_START_DATES[1]; // 0116

        double[] fractions = DiversityAuxdataUtils.getGet8DayProductFractionsForBiweeklyPeriods(biweeklyStartDate);
        assertNotNull(fractions);

        // we have the periods 009 (1 of 8 days: Jan 16), 017 (8 of 8 days), 025 (7 of 8 days, Jan 25-31)
        // todo
//        assertEquals(3, fractions.length);
//        assertEquals(0.125, fractions[0]);
//        assertEquals(1.0, fractions[1]);
//        assertEquals(0.875, fractions[2]);
    }
}

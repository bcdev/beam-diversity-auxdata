package org.esa.beam.operator;

import org.esa.beam.DiversityAuxdataUtils;
import org.esa.beam.framework.datamodel.Product;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * todo: add comment
 * To change this template use File | Settings | File Templates.
 * Date: 29.01.13
 * Time: 10:49
 *
 * @author olafd
 */
public class DiversityAuxdataUtilsTest {

    @Test
    public void testSortNdviByMonth() {

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

    @Test
    public void testSortSmByMonth() {

        Product[] unsortedProducts = new Product[24];
        unsortedProducts[0] = new Product("DIVERSITY_SM_BIWEEKLY_jan_b", "bla", 0, 0);
        unsortedProducts[1] = new Product("DIVERSITY_SM_BIWEEKLY_mar_b", "bla", 0, 0);
        unsortedProducts[2] = new Product("DIVERSITY_SM_BIWEEKLY_oct_b", "bla", 0, 0);
        unsortedProducts[3] = new Product("DIVERSITY_SM_BIWEEKLY_jan_a", "bla", 0, 0);

        final Product[] sortedProducts = DiversityAuxdataUtils.sortSoilMoistureProductsByMonth(unsortedProducts, null);
        assertNotNull(sortedProducts);
        assertEquals(4, sortedProducts.length);
        assertEquals("DIVERSITY_SM_BIWEEKLY_jan_a", sortedProducts[0].getName());
        assertEquals("DIVERSITY_SM_BIWEEKLY_jan_b", sortedProducts[1].getName());
        assertEquals("DIVERSITY_SM_BIWEEKLY_mar_b", sortedProducts[2].getName());
        assertEquals("DIVERSITY_SM_BIWEEKLY_oct_b", sortedProducts[3].getName());
    }
}

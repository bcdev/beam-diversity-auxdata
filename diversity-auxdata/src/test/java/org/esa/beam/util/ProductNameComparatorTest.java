package org.esa.beam.util;

import org.esa.beam.framework.datamodel.Product;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProductNameComparatorTest {
    @Test
    public void testSortProductsByName() {
        Product[] products = new Product[4];
        products[0] = new Product("name2006156", "name2006156", 1, 1);
        products[1] = new Product("name2006056", "name2006056", 1, 1);
        products[2] = new Product("name2006336", "name2006336", 1, 1);
        products[3] = new Product("name2006181", "name2006181", 1, 1);

        Arrays.sort(products, new ProductNameComparator());
        assertNotNull(products);
        assertEquals(4, products.length);
        assertEquals("name2006056", products[0].getName());
        assertEquals("name2006156", products[1].getName());
        assertEquals("name2006181", products[2].getName());
        assertEquals("name2006336", products[3].getName());
    }
}

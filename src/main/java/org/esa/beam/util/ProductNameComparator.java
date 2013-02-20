package org.esa.beam.util;

import org.esa.beam.framework.datamodel.Product;

import java.util.Comparator;

/**
 * Comparator for product name comparison
 *
 * @author olafd
 */
public class ProductNameComparator implements Comparator<Product> {
    @Override
    public int compare(Product o1, Product o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

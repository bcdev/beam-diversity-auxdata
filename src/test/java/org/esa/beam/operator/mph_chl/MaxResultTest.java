package org.esa.beam.operator.mph_chl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MaxResultTest {

    private MaxResult maxResult;

    @Before
    public void setUp() {
        maxResult = new MaxResult();
    }

    @Test
    public void testSetGetReflectance() {
        final double refl_1 = 0.034;
        final double refl_2 = 0.67;

        maxResult.setReflectance(refl_1);
        assertEquals(refl_1, maxResult.getReflectance(), 1e-8);

        maxResult.setReflectance(refl_2);
        assertEquals(refl_2, maxResult.getReflectance(), 1e-8);
    }

    @Test
    public void testSetGetWavelength() {
        final double wl_1 = 478.9;
        final double wl_2 = 822.9;

        maxResult.setWavelength(wl_1);
        assertEquals(wl_1, maxResult.getWavelength(), 1e-8);

        maxResult.setWavelength(wl_2);
        assertEquals(wl_2, maxResult.getWavelength(), 1e-8);
    }
}

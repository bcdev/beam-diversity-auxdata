package org.esa.beam.util;

/**
 * Object holding an 8-day-period start day and corresponding fractions of overlapping biweekly periods
 *
 * @author olafd
 */
public class SubBiweeklyProductFraction {

    private String[] subPeriodStartDoys;
    private Double[] fractionsOfBiweeklyPeriod;

    public SubBiweeklyProductFraction() {
    }

    public SubBiweeklyProductFraction(String[] subPeriodStartDoys, Double[] eightDayPeriodFractions) {
        this.subPeriodStartDoys = subPeriodStartDoys;
        this.fractionsOfBiweeklyPeriod = eightDayPeriodFractions;
    }

    public String[] getSubPeriodStartDoys() {
        return subPeriodStartDoys;
    }

    public void setSubPeriodStartDoys(String[] subPeriodStartDoys) {
        this.subPeriodStartDoys = subPeriodStartDoys;
    }

    public Double[] getFractionsOfBiweeklyPeriod() {
        return fractionsOfBiweeklyPeriod;
    }

    public void setFractionsOfBiweeklyPeriod(Double[] fractionsOfBiweeklyPeriod) {
        this.fractionsOfBiweeklyPeriod = fractionsOfBiweeklyPeriod;
    }
}

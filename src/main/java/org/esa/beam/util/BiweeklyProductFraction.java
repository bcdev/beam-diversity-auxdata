package org.esa.beam.util;

/**
 * Object holding an 8-day-period start day and corresponding fractions of overlapping biweekly periods
 *
 * @author olafd
 */
public class BiweeklyProductFraction {

    private String[] eightDayPeriodIdentifiers;
    private Double[] biweeklyPeriodFractions;

    public BiweeklyProductFraction() {
    }

    public BiweeklyProductFraction(String[] eightDayPeriodIdentifiers, Double[] eightDayPeriodFractions) {
        this.eightDayPeriodIdentifiers = eightDayPeriodIdentifiers;
        this.biweeklyPeriodFractions = eightDayPeriodFractions;
    }

    public String[] getEightDayPeriodIdentifiers() {
        return eightDayPeriodIdentifiers;
    }

    public void setEightDayPeriodIdentifiers(String[] eightDayPeriodIdentifiers) {
        this.eightDayPeriodIdentifiers = eightDayPeriodIdentifiers;
    }

    public Double[] getBiweeklyPeriodFractions() {
        return biweeklyPeriodFractions;
    }

    public void setBiweeklyPeriodFractions(Double[] biweeklyPeriodFractions) {
        this.biweeklyPeriodFractions = biweeklyPeriodFractions;
    }
}

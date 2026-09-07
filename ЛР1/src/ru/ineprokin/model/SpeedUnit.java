package ru.ineprokin.model;

import java.math.BigDecimal;

public enum SpeedUnit {
    PER_MINUTE("паролей/мин", new BigDecimal("1440")),
    PER_HOUR("паролей/час", new BigDecimal("24")),
    PER_DAY("паролей/день", BigDecimal.ONE);

    private final String title;
    private final BigDecimal perDay;

    SpeedUnit(String title, BigDecimal perDay) {
        this.title = title;
        this.perDay = perDay;
    }

    public BigDecimal perDayFactor() {
        return perDay;
    }

    @Override
    public String toString() {
        return title;
    }
}

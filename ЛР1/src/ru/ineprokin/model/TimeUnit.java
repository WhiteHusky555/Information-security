package ru.ineprokin.model;

import java.math.BigDecimal;

public enum TimeUnit {
    DAYS("дней", BigDecimal.ONE),
    WEEKS("недель", new BigDecimal("7")),
    MONTHS("месяцев", new BigDecimal("30"));

    private final String title;
    private final BigDecimal days;

    TimeUnit(String title, BigDecimal days) {
        this.title = title;
        this.days = days;
    }

    public BigDecimal daysFactor() {
        return days;
    }

    @Override
    public String toString() {
        return title;
    }
}

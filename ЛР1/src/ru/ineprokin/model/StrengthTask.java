package ru.ineprokin.model;

import java.math.BigDecimal;

public record StrengthTask(BigDecimal p, BigDecimal speed, SpeedUnit speedUnit,
                           BigDecimal time, TimeUnit timeUnit) {

    public BigDecimal attempts() {
        return speed.multiply(speedUnit.perDayFactor())
                .multiply(time).multiply(timeUnit.daysFactor());
    }
}

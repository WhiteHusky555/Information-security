package ru.ineprokin.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record StrengthResult(BigDecimal attempts, BigInteger lowerBound, List<AlphabetOption> options) {
}

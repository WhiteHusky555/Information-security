package ru.ineprokin.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public record AlphabetOption(Alphabet alphabet, int length, BigInteger total, BigDecimal probability) {
}

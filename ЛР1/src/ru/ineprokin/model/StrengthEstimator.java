package ru.ineprokin.model;

import ru.ineprokin.Config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class StrengthEstimator {

    private StrengthEstimator() {
    }

    // S* = [ V * T / P ]
    public static BigInteger lowerBound(StrengthTask task) {
        if (task.p().signum() <= 0) {
            throw new IllegalArgumentException("Вероятность P должна быть больше нуля");
        }
        return task.attempts().divide(task.p(), 0, RoundingMode.CEILING).toBigIntegerExact();
    }

    // минимальное L, при котором A^L >= S*
    public static int minLength(BigInteger lowerBound, int power) {
        BigInteger total = BigInteger.ONE;
        int length = 0;
        while (total.compareTo(lowerBound) < 0) {
            total = total.multiply(BigInteger.valueOf(power));
            length++;
        }
        return Math.max(length, Config.MIN_LENGTH);
    }

    // P = V * T / A^L
    public static BigDecimal probability(BigDecimal attempts, BigInteger total) {
        return attempts.divide(new BigDecimal(total), new MathContext(Config.PROBABILITY_DIGITS));
    }

    public static StrengthResult evaluate(StrengthTask task) {
        BigDecimal attempts = task.attempts();
        BigInteger bound = lowerBound(task);
        List<AlphabetOption> options = new ArrayList<>();
        for (Alphabet alphabet : Alphabet.values()) {
            int length = minLength(bound, alphabet.power());
            BigInteger total = BigInteger.valueOf(alphabet.power()).pow(length);
            options.add(new AlphabetOption(alphabet, length, total, probability(attempts, total)));
        }
        return new StrengthResult(attempts, bound, options);
    }
}

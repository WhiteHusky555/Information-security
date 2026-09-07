package ru.ineprokin.util;

import ru.ineprokin.Config;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public final class Fmt {

    private Fmt() {
    }

    public static String sci(BigInteger value) {
        return sci(new BigDecimal(value));
    }

    public static String sci(BigDecimal value) {
        if (value.signum() == 0) {
            return "0";
        }
        BigDecimal rounded = value.round(new MathContext(Config.SIGNIFICANT_DIGITS)).stripTrailingZeros();
        BigDecimal abs = rounded.abs();
        String text = abs.compareTo(new BigDecimal("1E+10")) < 0 && abs.compareTo(new BigDecimal("1E-3")) >= 0
                ? rounded.toPlainString()
                : rounded.toString();
        return text.replace("E+", "·10^").replace("E-", "·10^-");
    }

    public static String group(BigInteger value) {
        String digits = value.toString();
        if (digits.length() > 21) {
            return sci(value);
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            sb.append(digits.charAt(i));
            if (++count % 3 == 0 && i > 0) {
                sb.append(' ');
            }
        }
        return sb.reverse().toString();
    }
}

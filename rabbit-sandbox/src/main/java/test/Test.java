package test;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static java.math.BigDecimal.ONE;

public class Test {

    static int num = 1;
    static int den = 1000000;

    public static void main(String[] args) {

        System.out.println(prob(price()));
    }

    private static BigDecimal price() {
        return new BigDecimal(num).divide(new BigDecimal(den), MathContext.DECIMAL64).add(BigDecimal.ONE);
    }

    private static BigDecimal prob(BigDecimal price) {
        return ONE.divide(price, 3, RoundingMode.HALF_UP);
    }
}

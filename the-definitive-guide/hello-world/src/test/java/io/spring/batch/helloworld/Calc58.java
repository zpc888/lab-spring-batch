
package io.spring.batch.helloworld;

import java.math.BigInteger;

public class Calc58 {
    public static void main(String[] args) {
        c14_14();
//        c58_20();
    }

    private static void c14_14() {
        BigInteger c = BigInteger.valueOf(0);
        for (int i=1; i<=14; i++) {
            c = c.add( combination(14, i) );
        }
        for (int i=1; i<=20; i++) {
            c = c.add( combination(30, i) );
        }
        for (int i=0; i<=20; i++) {
            c = c.add( combination(30, 30 - i) );
        }
       // final long SECONDS_PER_MINUTE = 60;
        final long MATCH_RUNS_PER_SECOND = 5_000_000L;
        System.out.printf("number of combinations: %d will take %d seconds to run%n",
                c, c.divide( BigInteger.valueOf(MATCH_RUNS_PER_SECOND)) );
    }

    private static void c58_20() {
        BigInteger c = BigInteger.valueOf(0);
        for (int i=1; i<=20; i++) {
            c = c.add( combination(58, i) );
        }
        for (int i=0; i<=20; i++) {
            c = c.add( combination(58, 58 - i) );
        }
        final long SECONDS_PER_DAY = 24L * 60 * 60;
        final long MATCH_RUNS_PER_SECOND = 5_000_000L;
        System.out.printf("number of combinations: %d will take %d days to run%n",
                c, c.divide( BigInteger.valueOf(SECONDS_PER_DAY * MATCH_RUNS_PER_SECOND)) );
    }

    private static BigInteger combination(int n, int r) {
        return factorial(n).divide( factorial(r).multiply( factorial(n - r) ) );
    }

    private static BigInteger factorial(int n) {
        BigInteger ret = BigInteger.valueOf(1);
        for (int i=1; i<=n; i++) {
            ret = ret.multiply( BigInteger.valueOf(i) );
        }
        return ret;
    }
}

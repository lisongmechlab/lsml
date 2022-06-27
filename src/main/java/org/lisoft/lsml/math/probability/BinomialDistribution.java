/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//@formatter:on
package org.lisoft.lsml.math.probability;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.lisoft.lsml.math.FastFactorial.factorial;

/**
 * This class models a binomial distribution
 *
 * @author Li Song
 */
public class BinomialDistribution implements Distribution {
    private final int n;
    private final double p;

    public BinomialDistribution(double aP, int aN) {
        p = aP;
        n = aN;
    }

    public static long nChooseK(int n, long aK) {
        if (n - aK < aK) {
            return nChooseK(n, n - aK);
        }
        long ans = 1;
        for (int kk = 0; kk < aK; ++kk) {
            ans = ans * (n - kk) / (kk + 1);
        }
        return ans;
    }

    public static BigInteger nChooseKLargeNumbers(int n, int aK) {
        if (n - aK < aK) {
            return nChooseKLargeNumbers(n, n - aK);
        }
        return factorial(n).divide(factorial(aK).multiply(factorial(n - aK)));

        /*
        BigInteger ans = BigInteger.valueOf(1);
        for (int kk = 0; kk < aK; ++kk) {
            ans = ans.multiply(BigInteger.valueOf(n - kk)).divide(BigInteger.valueOf(kk + 1));
        }
        return ans;*/
    }

    public static double pdf(int aK, int aN, double aP) {
        BigDecimal Pk = BigDecimal.valueOf(aP).pow(aK);
        BigDecimal PnotK = BigDecimal.valueOf(1.0 - aP).pow(aN - aK);
        BigDecimal permutations = new BigDecimal(nChooseKLargeNumbers(aN, aK));
        return permutations.multiply(Pk).multiply(PnotK).doubleValue();
    }

    @Override
    public double cdf(double aX) {
        double ans = 0;
        final long k = (long) (aX + Math.ulp(aX)); // Accept anything within truncation error of k as k.
        for (int i = 0; i <= k; ++i) {
            ans += pdf(i);
        }
        return ans;
    }

    @Override
    public double pdf(double aX) {
        long k = Math.round(aX);
        return nChooseK(n, k) * Math.pow(p, k) * Math.pow(1.0 - p, n - k);
    }

}

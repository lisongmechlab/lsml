/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.util;

/**
 * This class models a binomial distribution
 * 
 * @author Emily Björk
 */
public class BinomialDistribution implements Distribution {
    private final double p;
    private final int    n;

    public static long nChooseK(int n, long aK) {
        long ans = 1;
        for (int kk = 0; kk < aK; ++kk) {
            ans = ans * (n - kk) / (kk + 1);
        }
        return ans;
    }

    public BinomialDistribution(double aP, int aN) {
        p = aP;
        n = aN;
    }

    @Override
    public double pdf(double aX) {
        long k = Math.round(aX);
        return nChooseK(n, k) * Math.pow(p, k) * Math.pow(1 - p, n - k);
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

}

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

import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

/**
 * Models the gaussian distribution.
 * 
 * @author Li Song
 */
public class GaussianDistribution implements Distribution {

    private static final double PRECISION = 1E-7;

    @Override
    public double pdf(double x) {
        return exp(-x * x / 2) / sqrt(2 * PI);
    }

    @Override
    public double cdf(double x) {
        final double pdf = pdf(x);
        if (pdf < PRECISION)
            return x < 0 ? 0 : 1.0;

        // Calculate the CDF through integration by parts:
        //
        // phi(x) = 0.5 + e^(-x^2/2)/sqrt(2*pi)*{x + x^3/3 + x^5/15 + ... + x^(2n+1)/(2n+1)!!}
        // phi(x) = 0.5 + pdf*{x + x^3/3 + x^5/15 + ... + x^(2n+1)/(2n+1)!!}
        //
        // Where:
        //
        // n!! = product(k=0, m){n-2k} = n(n-2)(n-4)...
        //
        // also known as the double factorial.
        //
        // We'll denote the sum in the expression of phi as "S" and each term as "Sn":
        //
        // S = {x + x^3/3 + x^5/15 + ... + x^(2n+1)/(2n+1)!!}
        // S = sum(n=0, +Inf){Sn}
        // Sn = x^(2n+1) / (2n+1)!!
        //
        // Note that we can express Sn using Sn-1 (n>0):
        //
        // Sn-1 = x^(2(n-1)+1) / (2(n-1)+1)!! = {x^(2n+1) / (2n+1)!!} * {(2n+1) / (x^2)}
        // --> Sn = Sn-1 * x^2 / (2n+1) = Sn-1 * x^2 / a

        final double x2 = x * x;
        double s_n = x;
        double s = s_n;
        double a = 1.0;
        do {
            a += 2.0;
            s_n *= x2 / a;
            s += s_n;
        } while (s_n * s_n > PRECISION);
        return 0.5 + pdf * s;
    }

}

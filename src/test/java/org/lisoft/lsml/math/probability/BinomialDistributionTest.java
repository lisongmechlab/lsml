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

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 * A test suite for {@link BinomialDistribution}
 *
 * @author Li Song
 */
public class BinomialDistributionTest {
    @Test
    public void testCdf() {
        BinomialDistribution cut = new BinomialDistribution(0.3, 6);

        double ansAccError = 0.00006;
        double ans = 0.1176;
        assertEquals(ans, cut.cdf(0), 1 * ansAccError);
        ans += 0.3025;
        assertEquals(ans, cut.cdf(1), 2 * ansAccError);
        ans += 0.3241;
        assertEquals(ans, cut.cdf(2), 3 * ansAccError);
        ans += 0.1852;
        assertEquals(ans, cut.cdf(3), 4 * ansAccError);
        ans += 0.0595;
        assertEquals(ans, cut.cdf(4), 5 * ansAccError);
        ans += 0.0102;
        assertEquals(ans, cut.cdf(5), 6 * ansAccError);
        ans += 0.0007;
        assertEquals(ans, cut.cdf(6), 7 * ansAccError);
        assertEquals(1.0, ans, 8 * ansAccError);
    }

    @Test
    public void testNChooseKLargeNumbers() {
        BigInteger ans = BinomialDistribution.nChooseKLargeNumbers(6000, 40);
        BigInteger expected = new BigInteger("14382101870748934620631834207959481683111543" +
                                             "411430546791423827791182239239941230776614179961632102443350", 10);
        assertEquals(expected, ans);
    }

    @Test
    public void testPdf() {
        BinomialDistribution cut = new BinomialDistribution(0.3, 6);

        assertEquals(0.1176, cut.pdf(0), 0.00006);
        assertEquals(0.3025, cut.pdf(1), 0.00006);
        assertEquals(0.3241, cut.pdf(2), 0.00006);
        assertEquals(0.1852, cut.pdf(3), 0.00006);
        assertEquals(0.0595, cut.pdf(4), 0.00006);
        assertEquals(0.0102, cut.pdf(5), 0.00006);
        assertEquals(0.0007, cut.pdf(6), 0.00006);
    }
}

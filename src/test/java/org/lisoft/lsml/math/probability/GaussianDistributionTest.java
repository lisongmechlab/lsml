package org.lisoft.lsml.math.probability;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Li Song
 */
public class GaussianDistributionTest {

    @Test
    public void testCDF() {
        GaussianDistribution cut = new GaussianDistribution();

        double tolerance = 0.00001;
        assertEquals(0.5, cut.cdf(0), tolerance);

        // Values taken from standard tables
        assertEquals(0.00004, cut.cdf(-3.95), tolerance);
        assertEquals(0.00676, cut.cdf(-2.47), tolerance);
        assertEquals(0.10749, cut.cdf(-1.24), tolerance);
        assertEquals(0.73565, cut.cdf(0.63), tolerance);
        assertEquals(0.92647, cut.cdf(1.45), tolerance);
        assertEquals(0.99856, cut.cdf(2.98), tolerance);
    }
}

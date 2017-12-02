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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

/**
 * Test suite for {@link BurstHeat}.
 *
 * @author Li Song
 */
public class BurstHeatTest {
    @Test
    public void testCalculate() {
        final BurstDamageOverTime burstDoT = mock(BurstDamageOverTime.class);
        final HeatOverTime heatOverTime = mock(HeatOverTime.class);
        final Double time = 123.4; // Arbitrary non-zero value
        final Double heat = 6123.0; // Arbitrary non-zero value not equal to time
        when(burstDoT.getTime()).thenReturn(time);
        when(heatOverTime.calculate(time)).thenReturn(heat);

        assertEquals(heat, new BurstHeat(burstDoT, heatOverTime).calculate(), 0.0);
    }
}

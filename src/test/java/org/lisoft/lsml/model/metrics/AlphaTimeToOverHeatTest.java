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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * This is a test suite for {@link AlphaTimeToOverHeat}.
 *
 * @author Emily Björk
 */
public class AlphaTimeToOverHeatTest {

    @Test
    public void testCalculate() {
        final HeatCapacity capacity = Mockito.mock(HeatCapacity.class);
        final HeatOverTime generation = Mockito.mock(HeatOverTime.class);
        final HeatDissipation dissipation = Mockito.mock(HeatDissipation.class);

        Mockito.when(capacity.calculate()).thenReturn(60.0);
        Mockito.when(dissipation.calculate()).thenReturn(4.0);
        Mockito.when(generation.calculate(Matchers.anyDouble())).then(aInvocation -> {
            final double time = (Double) aInvocation.getArguments()[0];
            return 5 * time; // 5 heat per second generated
        });

        final AlphaTimeToOverHeat cut = new AlphaTimeToOverHeat(capacity, generation, dissipation);
        assertEquals(60.0, cut.calculate(), 0.6); // 1% tolerance
    }

    /**
     * Anything longer than 15 minutes is rounded up to infinity. As matches are only 15 minutes.
     */
    @Test
    public void testCalculate_longerThan15min() {
        final HeatCapacity capacity = Mockito.mock(HeatCapacity.class);
        final HeatOverTime generation = Mockito.mock(HeatOverTime.class);
        final HeatDissipation dissipation = Mockito.mock(HeatDissipation.class);

        Mockito.when(capacity.calculate()).thenReturn(60.0);
        Mockito.when(dissipation.calculate()).thenReturn(4.0);
        Mockito.when(generation.calculate(Matchers.anyDouble())).then(aInvocation -> {
            final double time = (Double) aInvocation.getArguments()[0];
            return 4.05 * time; // 4.05 heat per second generated -> 20min to overheat
        });

        final AlphaTimeToOverHeat cut = new AlphaTimeToOverHeat(capacity, generation, dissipation);
        assertTrue(Double.isInfinite(cut.calculate()));
    }

    /**
     * When heat is given as pulses the mech can cool down between pulses. But not to negative heat.
     */
    @Test
    public void testCalculate_Pulses() {
        final HeatCapacity capacity = Mockito.mock(HeatCapacity.class);
        final HeatOverTime generation = Mockito.mock(HeatOverTime.class);
        final HeatDissipation dissipation = Mockito.mock(HeatDissipation.class);

        Mockito.when(capacity.calculate()).thenReturn(60.0);
        Mockito.when(dissipation.calculate()).thenReturn(4.0);
        Mockito.when(generation.calculate(Matchers.anyDouble())).then(aInvocation -> {
            final double time = (Double) aInvocation.getArguments()[0];
            final int integerTime = (int) time;
            if (time <= 100) {
                return integerTime / 5 * 5; // 5 heat pulse every 5 seconds.
                // The cooling capacity is 20 in the same period which would cool the mech to negative temperature
                // acting as a cooling buffer which is not allowed. If this happens, the impulse after 100s will not
                // be enough to cause an over heat as expected.
            }
            return 100 + 61.0; // The above will have generated 100 heat in total, add an impulse of 61 to trigger
                               // over
                               // heat.
        });

        final AlphaTimeToOverHeat cut = new AlphaTimeToOverHeat(capacity, generation, dissipation);
        assertEquals(100.0, cut.calculate(), 1.0); // 1% tolerance
    }
}

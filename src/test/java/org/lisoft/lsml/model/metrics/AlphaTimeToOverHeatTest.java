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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This is a test suite for {@link AlphaTimeToOverHeat}.
 * 
 * @author Li Song
 */
public class AlphaTimeToOverHeatTest {

    @Test
    public void testCalculate() {
        HeatCapacity capacity = Mockito.mock(HeatCapacity.class);
        HeatOverTime generation = Mockito.mock(HeatOverTime.class);
        HeatDissipation dissipation = Mockito.mock(HeatDissipation.class);

        Mockito.when(capacity.calculate()).thenReturn(60.0);
        Mockito.when(dissipation.calculate()).thenReturn(4.0);
        Mockito.when(generation.calculate(Matchers.anyDouble())).then(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock aInvocation) throws Throwable {
                double time = (Double) aInvocation.getArguments()[0];
                return 5 * time; // 5 heat per second generated
            }
        });

        AlphaTimeToOverHeat cut = new AlphaTimeToOverHeat(capacity, generation, dissipation);
        assertEquals(60.0, cut.calculate(), 0.6); // 1% tolerance
    }

    /**
     * Anything longer than 15 minutes is rounded up to infinity. As matches are only 15 minutes.
     */
    @Test
    public void testCalculate_longerThan15min() {
        HeatCapacity capacity = Mockito.mock(HeatCapacity.class);
        HeatOverTime generation = Mockito.mock(HeatOverTime.class);
        HeatDissipation dissipation = Mockito.mock(HeatDissipation.class);

        Mockito.when(capacity.calculate()).thenReturn(60.0);
        Mockito.when(dissipation.calculate()).thenReturn(4.0);
        Mockito.when(generation.calculate(Matchers.anyDouble())).then(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock aInvocation) throws Throwable {
                double time = (Double) aInvocation.getArguments()[0];
                return 4.05 * time; // 4.05 heat per second generated -> 20min to overheat
            }
        });

        AlphaTimeToOverHeat cut = new AlphaTimeToOverHeat(capacity, generation, dissipation);
        assertTrue(Double.isInfinite(cut.calculate()));
    }

    /**
     * When heat is given as pulses the mech can cool down between pulses. But not to negative heat.
     */
    @Test
    public void testCalculate_Pulses() {
        HeatCapacity capacity = Mockito.mock(HeatCapacity.class);
        HeatOverTime generation = Mockito.mock(HeatOverTime.class);
        HeatDissipation dissipation = Mockito.mock(HeatDissipation.class);

        Mockito.when(capacity.calculate()).thenReturn(60.0);
        Mockito.when(dissipation.calculate()).thenReturn(4.0);
        Mockito.when(generation.calculate(Matchers.anyDouble())).then(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock aInvocation) throws Throwable {
                double time = (Double) aInvocation.getArguments()[0];
                int integerTime = (int) time;
                if (time <= 100) {
                    return (integerTime / 5) * 5.0; // 5 heat pulse every 5 seconds.
                    // The cooling capacity is 20 in the same period which would cool the mech to negative temperature
                    // acting as a cooling buffer which is not allowed. If this happens, the impulse after 100s will not
                    // be enough to cause an over heat as expected.
                }
                return 100 + 61.0; // The above will have generated 100 heat in total, add an impulse of 61 to trigger
                                   // over
                                   // heat.
            }
        });

        AlphaTimeToOverHeat cut = new AlphaTimeToOverHeat(capacity, generation, dissipation);
        assertEquals(100.0, cut.calculate(), 1.0); // 1% tolerance
    }
}

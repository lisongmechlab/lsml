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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.*;
import org.lisoft.lsml.model.upgrades.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link HeatDissipation}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class HeatDissipationTest {
    @Mock
    Engine engine;
    @Mock
    Loadout loadout;
    List<Modifier> modifiers = new ArrayList<>();
    @Mock
    Chassis chassis;
    @Mock
    HeatSink heatSinkType;
    @Mock
    HeatSinkUpgrade heatSinkUpgrade;
    @Mock
    Upgrades upgrades;

    final double dissipationFactor = 1.3;
    final int numExternalHs = 5;
    final int numInternalHs = 9;
    final double hsDissipation = 0.15;

    @Before
    public void setup() {
        when(loadout.getExternalHeatSinksCount()).thenReturn(numExternalHs);
        when(loadout.getAllModifiers()).thenReturn(modifiers);
        when(loadout.getChassis()).thenReturn(chassis);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.getUpgrades()).thenReturn(upgrades);
        when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);
        when(heatSinkUpgrade.getHeatSinkType()).thenReturn(heatSinkType);
        when(heatSinkType.getDissipation()).thenReturn(hsDissipation);
        when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
    }

    /**
     * The heat dissipation of a 'mech is dependent on the heat sink types. > For single heat sinks it is simply the
     * number of heat sinks multiplied by any modifier from the efficiencies. > For double heat sinks it each engine
     * internal heat sink counts as 0.2 and any other heat sinks count as 0.14.
     */
    @Test
    public void testCalculate() {

        final ModifierDescription description = mock(ModifierDescription.class);
        when(description.getOperation()).thenReturn(Operation.MUL);
        when(description.affects(any(Attribute.class))).then(aInvocation -> {
            final Attribute a = (Attribute) aInvocation.getArguments()[0];
            return a.getSelectors().containsAll(ModifierDescription.SEL_HEAT_DISSIPATION);
        });

        final Modifier heatDissipation = mock(Modifier.class);
        when(heatDissipation.getValue()).thenReturn(dissipationFactor - 1.0);
        when(heatDissipation.getDescription()).thenReturn(description);
        modifiers.add(heatDissipation);
        when(heatSinkUpgrade.isDouble()).thenReturn(true);
        when(heatSinkType.isDouble()).thenReturn(true);

        final Environment environment = mock(Environment.class);
        final double environmentHeat = 0.3;
        when(environment.getHeat(modifiers)).thenReturn(environmentHeat);

        final HeatDissipation cut = new HeatDissipation(loadout, environment);
        final double expectedDissipation = (numInternalHs + numExternalHs) * dissipationFactor * hsDissipation - environmentHeat;

        assertEquals(expectedDissipation, cut.calculate(), Math.ulp(expectedDissipation) * 4);
    }
}

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
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.Test;
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
 * Test suite for {@link HeatCapacity}
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class HeatCapacityTest {
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
    private final double basecapacity = 50.0;

    protected void setupMocks(int numInternalHs, int numExternalHs, double externalHsCapacity) {
        when(loadout.getHeatsinksCount()).thenReturn(numInternalHs + numExternalHs);
        when(loadout.getAllModifiers()).thenReturn(modifiers);
        when(loadout.getChassis()).thenReturn(chassis);
        when(loadout.getEngine()).thenReturn(engine);
        when(loadout.getUpgrades()).thenReturn(upgrades);
        when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);
        when(heatSinkUpgrade.getHeatSinkType()).thenReturn(heatSinkType);
        when(heatSinkType.getCapacity()).thenReturn(externalHsCapacity);
        when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
    }

    /**
     * Each 'mech has a base heat capacity of 30 heat. Each single heat sink adds 1 capacity. Each double heat sink adds
     * 1.4 capacity. Except for the ones counted as engine internal heat sinks which count as 2.0 (those in engine slots
     * count as 1.4 still). These values are also affected by the efficiency modifier.
     */
    @Test
    public void testCalculate() {
        final double heatContainmentSkill = 1.3;
        final int numExternalHs = 5;
        final int numInternalHs = 9;
        final double hsCapacity = 1.4;
        final double environmentHeat = 0.134;
        setupMocks(numInternalHs, numExternalHs, hsCapacity);

        final double expectedCapacity = basecapacity * heatContainmentSkill
                + (numExternalHs + numInternalHs - 10) * hsCapacity
                - (Engine.ENGINE_HEAT_FULL_THROTTLE + environmentHeat) * 10;

        final Modifier heatlimit = createHeatContainmentModifier(heatContainmentSkill);
        modifiers.add(heatlimit);

        final Environment environment = createEnvironment(environmentHeat);
        final HeatCapacity cut = new HeatCapacity(loadout, environment);

        assertEquals(expectedCapacity, cut.calculate(), 0.000000001);
    }

    private Environment createEnvironment(final double environmentHeat) {
        final Environment environment = mock(Environment.class);
        when(environment.getHeat(modifiers)).thenReturn(environmentHeat);
        return environment;
    }

    private Modifier createHeatContainmentModifier(double heatContainmentSkill) {
        final Modifier heatlimit = mock(Modifier.class);
        final ModifierDescription description = new ModifierDescription("", "", Operation.MUL,
                ModifierDescription.SEL_HEAT_LIMIT, null, ModifierType.POSITIVE_GOOD);
        when(heatlimit.getValue()).thenReturn(heatContainmentSkill - 1.0);
        when(heatlimit.getDescription()).thenReturn(description);
        return heatlimit;
    }
}

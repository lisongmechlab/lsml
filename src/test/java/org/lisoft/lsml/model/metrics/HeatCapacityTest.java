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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.database.ModifiersDB;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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

    final double heatContainmentSkill = 1.3;
    final int numExternalHs = 5;
    final int numInternalHs = 9;
    final double basecapacity = 30;
    final double externalHsCapacity = 1.4;
    final double internalHsCapacity = 2.0;
    final double environmentHeat = 0.134;

    @Before
    public void setup() {
        Mockito.when(loadout.getHeatsinksCount()).thenReturn(numInternalHs + numExternalHs);
        Mockito.when(loadout.getModifiers()).thenReturn(modifiers);
        Mockito.when(loadout.getChassis()).thenReturn(chassis);
        Mockito.when(loadout.getEngine()).thenReturn(engine);
        Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
        Mockito.when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);
        Mockito.when(heatSinkUpgrade.getHeatSinkType()).thenReturn(heatSinkType);
        Mockito.when(heatSinkType.getCapacity()).thenReturn(externalHsCapacity);
        Mockito.when(heatSinkType.getEngineDissipation()).thenReturn(internalHsCapacity / 10.0);
        Mockito.when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
    }

    /**
     * Each 'mech has a base heat capacity of 30 heat. Each single heat sink adds 1 capacity. Each double heat sink adds
     * 1.4 capacity. Except for the ones counted as engine internal heat sinks which count as 2.0 (those in engine slots
     * count as 1.4 still). These values are also affected by the efficiency modifier.
     */
    @Test
    public void testCalculate() {
        final double expectedCapacity = basecapacity * heatContainmentSkill + numInternalHs * internalHsCapacity
                + numExternalHs * externalHsCapacity - Engine.ENGINE_HEAT_FULL_THROTTLE * 10 - 10 * environmentHeat;

        // Create mock heat containment modifier
        final Modifier heatlimit = Mockito.mock(Modifier.class);
        final List<Modifier> heatContainment = new ArrayList<>(
                ModifiersDB.lookupEfficiencyModifiers(MechEfficiencyType.HEAT_CONTAINMENT, true));
        final ModifierDescription description = heatContainment.get(0).getDescription();
        Mockito.when(heatlimit.getValue()).thenReturn(heatContainmentSkill - 1.0);
        Mockito.when(heatlimit.getDescription()).thenReturn(description);
        modifiers.add(heatlimit);

        final Environment environment = mock(Environment.class);
        when(environment.getHeat(modifiers)).thenReturn(environmentHeat);

        final HeatCapacity cut = new HeatCapacity(loadout, environment);

        assertEquals(expectedCapacity, cut.calculate(), 0.000000001);
    }
}

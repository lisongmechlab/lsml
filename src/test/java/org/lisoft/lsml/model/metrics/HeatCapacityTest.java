/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.equipment.HeatSink;
import org.lisoft.mwo_data.equipment.HeatSinkUpgrade;
import org.lisoft.mwo_data.mechs.Chassis;
import org.lisoft.mwo_data.mechs.Upgrades;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.modifiers.ModifierType;
import org.lisoft.mwo_data.modifiers.Operation;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link HeatCapacity}
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class HeatCapacityTest {
  @Mock Chassis chassis;
  @Mock Engine engine;
  @Mock HeatSink heatSinkType;
  @Mock HeatSinkUpgrade heatSinkUpgrade;
  @Mock Loadout loadout;
  final List<Modifier> modifiers = new ArrayList<>();
  @Mock Upgrades upgrades;

  @Test
  public void testCalculate_BigEngine() {
    setupMocks(10, 10, 3, 1);
    final HeatCapacity cut = new HeatCapacity(loadout);
    assertEquals(70, cut.calculate(), 1e-9);
  }

  @Test
  public void testCalculate_NoHeatSinks() {
    setupMocks(0, 0, 10, 20);
    final HeatCapacity cut = new HeatCapacity(loadout);
    assertEquals(30, cut.calculate(), 1e-9);
  }

  @Test
  public void testCalculate_SmallEngine() {
    setupMocks(5, 10, 3, 1);
    final HeatCapacity cut = new HeatCapacity(loadout);
    assertEquals(65, cut.calculate(), 1e-9);
  }

  @Test
  public void testCalculate_WithModifier() {
    setupMocks(10, 10, 1, 1);
    modifiers.add(createHeatContainmentModifier(2.0));
    final HeatCapacity cut = new HeatCapacity(loadout);
    assertEquals(80, cut.calculate(), 1e-9);
  }

  protected void setupMocks(
      int numInternalHs, int numExternalHs, double internalHsCapacity, double externalHsCapacity) {
    when(loadout.getTotalHeatSinksCount()).thenReturn(numExternalHs + numInternalHs);
    when(loadout.getExternalHeatSinksCount()).thenReturn(numExternalHs);
    when(loadout.getAllModifiers()).thenReturn(modifiers);
    when(loadout.getChassis()).thenReturn(chassis);
    when(loadout.getEngine()).thenReturn(engine);
    when(loadout.getUpgrades()).thenReturn(upgrades);
    when(upgrades.getHeatSink()).thenReturn(heatSinkUpgrade);
    when(heatSinkUpgrade.getHeatSinkType()).thenReturn(heatSinkType);
    when(heatSinkType.getCapacity()).thenReturn(externalHsCapacity);
    when(heatSinkType.getEngineCapacity()).thenReturn(internalHsCapacity);
    when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
  }

  private Modifier createHeatContainmentModifier(double heatContainmentSkill) {
    final Modifier heatLimit = mock(Modifier.class);
    final ModifierDescription description =
        new ModifierDescription(
            "",
            "",
            Operation.MUL,
            ModifierDescription.SEL_HEAT_LIMIT,
            null,
            ModifierType.POSITIVE_GOOD);
    when(heatLimit.getValue()).thenReturn(heatContainmentSkill - 1.0);
    when(heatLimit.getDescription()).thenReturn(description);
    return heatLimit;
  }
}

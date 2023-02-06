/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.lsml.model.metrics.helpers;

import static org.junit.Assert.*;
import static org.lisoft.mwo_data.TestUtil.makeLaser;
import static org.lisoft.mwo_data.TestUtil.makePPC;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.TestUtil;
import org.lisoft.mwo_data.equipment.BallisticWeapon;
import org.lisoft.mwo_data.equipment.EnergyWeapon;
import org.lisoft.mwo_data.equipment.Engine;
import org.lisoft.mwo_data.modifiers.Attribute;
import org.lisoft.mwo_data.modifiers.ModifierDescription;

public class ExpectedHeatSignalTest {

  @Test
  public void testGetExpectedHeatEngine() {
    Attribute heat =
        new Attribute(
            Engine.ENGINE_HEAT_FULL_THROTTLE, ModifierDescription.SEL_HEAT_MOVEMENT, null);
    Engine engine =
        new Engine(
            "",
            "",
            "",
            0,
            6,
            0,
            1,
            Faction.INNERSPHERE,
            heat,
            300,
            Engine.EngineType.STD,
            5,
            5,
            0,
            0);

    IntegratedSignal cut = new ExpectedHeatSignal(engine, null);

    assertEquals(10 * Engine.ENGINE_HEAT_FULL_THROTTLE, cut.integrateFromZeroTo(10), 0.0);
  }

  @Test
  public void testGetExpectedHeatSignalAC5() {
    BallisticWeapon ballisticWeapon =
        TestUtil.makeAutoCannon(1, 2.0, 4.0, 5.0, 1.5, 250, 0.0, 0, 0, List.of("my_laser"));

    IntegratedSignal cut = new ExpectedHeatSignal(ballisticWeapon, null);

    assertEquals(4.0, cut.integrateFromZeroTo(1.5), 0.0);
    assertEquals(4.0, cut.integrateFromZeroTo(4.499), 0.0);
    assertEquals(8.0, cut.integrateFromZeroTo(5), 0.0);
  }

  @Test
  public void testGetExpectedHeatSignalLasers() {
    EnergyWeapon energyWeapon =
        makeLaser(1, 2.0, 4.0, 5.0, 1.5, 250, 500, Arrays.asList("my_laser"));

    IntegratedSignal cut = new ExpectedHeatSignal(energyWeapon, null);

    assertEquals(4.0, cut.integrateFromZeroTo(1.5), 0.0);
    assertEquals(4.0, cut.integrateFromZeroTo(6.499), 0.0);
    assertEquals(8.0, cut.integrateFromZeroTo(8), 0.0);
  }

  @Test
  public void testGetExpectedHeatSignalPPC() {
    EnergyWeapon energyWeapon = makePPC(1, 2.0, 5.0, 5.0, 1500, 250, 500, Arrays.asList("my_ppc"));

    IntegratedSignal cut = new ExpectedHeatSignal(energyWeapon, null);

    assertEquals(5.0, cut.integrateFromZeroTo(0.0), 0.0);
    assertEquals(5.0, cut.integrateFromZeroTo(4.99), 0.0);
    assertEquals(10.0, cut.integrateFromZeroTo(5.0), 0.0);
  }
}

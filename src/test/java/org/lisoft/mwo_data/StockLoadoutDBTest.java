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
package org.lisoft.mwo_data;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.lisoft.lsml.model.StockLoadoutDB;
import org.lisoft.mwo_data.mechs.Location;
import org.lisoft.mwo_data.mechs.StockLoadout;

/**
 * Test suite for {@link StockLoadoutDB}.
 *
 * @author Li Song
 */
public class StockLoadoutDBTest {

  /** Test that actuator state "Both" is correctly loaded for stock loadout. */
  @Test
  public void testLookup_Bug433_SCR_A() throws Exception {
    final StockLoadout stock = StockLoadoutDB.lookup(ChassisDB.lookup("SCR-A"));

    StockLoadout.StockComponent leftArm = null;
    StockLoadout.StockComponent rightArm = null;
    for (final StockLoadout.StockComponent component : stock.getComponents()) {
      if (component.getLocation() == Location.LeftArm) {
        leftArm = component;
      }
      if (component.getLocation() == Location.RightArm) {
        rightArm = component;
      }
    }
    assertNotNull(leftArm);
    assertNotNull(rightArm);
    Assert.assertEquals(StockLoadout.StockComponent.ActuatorState.BOTH, leftArm.getActuatorState());
    Assert.assertEquals(
        StockLoadout.StockComponent.ActuatorState.BOTH, rightArm.getActuatorState());
  }

  /** Test that actuator state "None" is correctly loaded for stock loadout. */
  @Test
  public void testLookup_Bug433_SCR_PRIME_S() throws Exception {
    final StockLoadout stock = StockLoadoutDB.lookup(ChassisDB.lookup("SCR-PRIME(S)"));

    StockLoadout.StockComponent leftArm = null;
    StockLoadout.StockComponent rightArm = null;
    for (final StockLoadout.StockComponent component : stock.getComponents()) {
      if (component.getLocation() == Location.LeftArm) {
        leftArm = component;
      }
      if (component.getLocation() == Location.RightArm) {
        rightArm = component;
      }
    }
    assertNotNull(leftArm);
    assertNotNull(rightArm);
    Assert.assertEquals(StockLoadout.StockComponent.ActuatorState.NONE, leftArm.getActuatorState());
    Assert.assertEquals(
        StockLoadout.StockComponent.ActuatorState.BOTH, rightArm.getActuatorState());
  }

  /** Test that actuator state "Only left arm" is correctly loaded for stock loadout. */
  @Test
  public void testLookup_Bug433_TBR_PRIME_I() throws Exception {
    final StockLoadout stock = StockLoadoutDB.lookup(ChassisDB.lookup("TBR-PRIME(I)"));

    StockLoadout.StockComponent leftArm = null;
    StockLoadout.StockComponent rightArm = null;
    for (final StockLoadout.StockComponent component : stock.getComponents()) {
      if (component.getLocation() == Location.LeftArm) {
        leftArm = component;
      }
      if (component.getLocation() == Location.RightArm) {
        rightArm = component;
      }
    }
    assertNotNull(leftArm);
    assertNotNull(rightArm);
    Assert.assertEquals(StockLoadout.StockComponent.ActuatorState.LAA, leftArm.getActuatorState());
    Assert.assertEquals(StockLoadout.StockComponent.ActuatorState.LAA, rightArm.getActuatorState());
  }
}

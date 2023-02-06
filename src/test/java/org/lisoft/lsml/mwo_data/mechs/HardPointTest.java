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
package org.lisoft.lsml.mwo_data.mechs;

import static org.junit.Assert.*;

import org.junit.Test;

public class HardPointTest {
  @Test
  public void testConstruction() {
    final HardPoint cut = new HardPoint(HardPointType.MISSILE, 3, true);

    assertEquals(3, cut.getNumMissileTubes());
    assertTrue(cut.hasMissileBayDoor());
    assertEquals(HardPointType.MISSILE, cut.getType());
  }

  @SuppressWarnings("unused")
  @Test(expected = IllegalArgumentException.class)
  public void testConstructionMissilesWithoutTubes() {
    new HardPoint(HardPointType.MISSILE);
  }

  @Test
  public void testConstructionNonMissile() {
    final HardPoint cut = new HardPoint(HardPointType.ENERGY);
    assertFalse(cut.hasMissileBayDoor());
    assertEquals(HardPointType.ENERGY, cut.getType());
    assertEquals(0, cut.getNumMissileTubes());
  }

  @SuppressWarnings("unused")
  @Test(expected = IllegalArgumentException.class)
  public void testConstructionNonMissileWithBayDoors() {
    new HardPoint(HardPointType.ENERGY, 0, true);
  }

  @SuppressWarnings("unused")
  @Test(expected = IllegalArgumentException.class)
  public void testConstructionNonMissileWithTubes() {
    new HardPoint(HardPointType.ENERGY, 3, true);
  }
}

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
package org.lisoft.mwo_data.equipment;

import static org.junit.Assert.assertEquals;
import static org.lisoft.mwo_data.TestUtil.makeLaser;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.modifiers.ModifierType;
import org.lisoft.mwo_data.modifiers.Operation;

/**
 * Test suite for {@link EnergyWeapon} class.
 *
 * @author Li Song
 */
public class EnergyWeaponTest {

  @SuppressWarnings("EqualsWithItself")
  @Test
  public void testCompare_flamers_flamers() throws Exception {
    final EnergyWeapon weapon = (EnergyWeapon) ItemDB.lookup("FLAMER");
    assertEquals(0, new ItemComparator(false).compare(weapon, weapon));
    assertEquals(0, new ItemComparator(true).compare(weapon, weapon));
  }

  @Test
  public void testGetDurationModified() {
    Collection<String> selectors = List.of("my_laser");
    ModifierDescription desc =
        new ModifierDescription(
            "",
            "",
            Operation.MUL,
            selectors,
            ModifierDescription.SPEC_WEAPON_DURATION,
            ModifierType.NEGATIVE_GOOD);
    Modifier modifier = new Modifier(desc, -0.5);
    EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 3.0, 250, 500, selectors);
    assertEquals(1.5, cut.getDuration(List.of(modifier)), 0.0);
  }

  @Test
  public void testGetDurationNoModifiers() {
    EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 1.0, 250, 500, List.of("my_laser"));
    assertEquals(1.0, cut.getDuration(null), 0.0);
  }

  @Test
  public void testRawFiringPeriodIncludesBurnTime() {
    EnergyWeapon cut = makeLaser(1, 2.0, 4.0, 5.0, 1.0, 250, 500, List.of("my_laser"));
    assertEquals(6.0, cut.getRawFiringPeriod(null), 0.0);
  }

  @Test
  public void testRawFiringPeriodInfiniteBurnTime() {
    // Some weapons such as flamers have infinite burn times (limited by player holding down the
    // button)
    // for those, only the cool down is returned.
    EnergyWeapon cut =
        makeLaser(1, 2.0, 4.0, 5.0, Double.POSITIVE_INFINITY, 250, 500, List.of("my_laser"));
    assertEquals(5.0, cut.getRawFiringPeriod(null), 0.0);
  }
}

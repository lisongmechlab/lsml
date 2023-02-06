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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lisoft.lsml.model.ChassisDB;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.equipment.TargetingComputer;
import org.lisoft.mwo_data.equipment.Weapon;
import org.lisoft.mwo_data.mechs.ChassisStandard;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.modifiers.ModifierType;
import org.lisoft.mwo_data.modifiers.Operation;

/**
 * This test suite tests that various parts of the data files are parsed in a correct manner. Mainly
 * regression tests.
 *
 * @author Li Song
 */
public class MwoDataImportTest {

  @Test
  public void testBallisticVelocityQuirk() throws Exception {
    // HBK-4G has 25% ballistic "velocity"
    final ChassisStandard chassis = (ChassisStandard) ChassisDB.lookup("HBK-4G");

    Modifier ballisticVelocity = null;
    for (final Modifier quirk : chassis.getQuirks()) {
      final String text = quirk.toString();
      if (text.contains("BALLISTIC") && text.contains("VELOCITY")) {
        ballisticVelocity = quirk;
        break;
      }
    }
    assertNotNull(ballisticVelocity);

    // Any IS ballistic weapon
    final Weapon weapon = (Weapon) ItemDB.lookup("GAUSS RIFLE");
    final double baseSpeed = weapon.getProjectileSpeed(null);
    final double quirkedSpeed = weapon.getProjectileSpeed(singletonList(ballisticVelocity));
    final double expectedSpeed = baseSpeed * 1.25;
    assertEquals("Base speed is: " + baseSpeed, expectedSpeed, quirkedSpeed, 0.0);
  }

  @Test
  public void testBug478() throws Exception {
    final TargetingComputer tc = (TargetingComputer) ItemDB.lookup("TARGETING COMP. MK VII");
    final Weapon ppc = (Weapon) ItemDB.lookup("C-ER PPC");

    final double raw = ppc.getProjectileSpeed(null);
    final double mod = ppc.getProjectileSpeed(tc.getModifiers());
    final double bonus = 0.35;
    final double expected = raw * (1 + bonus);
    assertEquals(expected, mod, 1E-15);
  }

  /** The ROF quirk should apply correctly to machine guns. */
  @Test
  public void testBug566() throws Exception {
    final ChassisStandard lct1v = (ChassisStandard) ChassisDB.lookup("LCT-1V");
    final Weapon mg = (Weapon) ItemDB.lookup("MACHINE GUN");

    final double cdRaw = mg.getCoolDown(null);
    final double cdModified = mg.getCoolDown(lct1v.getQuirks());

    // CD = 1/rof
    // CDquirk = 1 / (rof*(1+modifier)) = CD * 1/(1+modifier)
    // modifier = 30%
    assertEquals(cdRaw / 1.5, cdModified, 1E-15);
  }

  /** The cool down quirk needs to be changed to negative good. */
  @Test
  public void testCDNegativeGood() throws Exception {
    final Weapon llas = (Weapon) ItemDB.lookup("LARGE LASER");

    final ModifierDescription coolDownDesc =
        new ModifierDescription(
            "",
            "",
            Operation.MUL,
            ModifierDescription.SEL_ALL,
            ModifierDescription.SPEC_WEAPON_COOL_DOWN,
            ModifierType.NEGATIVE_GOOD);
    final Modifier coolDownModifier = new Modifier(coolDownDesc, -0.5);

    final double cdRaw = llas.getCoolDown(null);
    final double cdModified = llas.getCoolDown(singletonList(coolDownModifier));

    assertEquals(cdRaw * 0.5, cdModified, 0.0);
  }
}

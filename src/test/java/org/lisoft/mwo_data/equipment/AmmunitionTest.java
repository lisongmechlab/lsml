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

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.model.loadout.DefaultLoadoutFactory;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutFactory;
import org.lisoft.lsml.util.TestHelpers;
import org.lisoft.mwo_data.ChassisDB;
import org.lisoft.mwo_data.ItemDB;
import org.lisoft.mwo_data.mechs.HardPointType;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.ModifierDescription;

/**
 * A test suite for {@link Ammunition}.
 *
 * @author Li Song
 */
public class AmmunitionTest {

  @Test
  public void testAmmoCapacitiesApply() {
    LoadoutFactory loadoutFactory = new DefaultLoadoutFactory();
    List<Modifier> ammoModifiers =
        ChassisDB.lookupAll().stream()
            .flatMap(c -> loadoutFactory.produceEmpty(c).getAllModifiers().stream())
            .filter(
                m ->
                    m.getDescription()
                        .getSelectors()
                        .containsAll(ModifierDescription.SEL_AMMO_CAPACITY))
            .toList();
    List<Ammunition> allAmmo = ItemDB.lookup(Ammunition.class);
    assertFalse("No ammo quirks found!", ammoModifiers.isEmpty());

    for (Modifier m : ammoModifiers) {
      boolean appliesToSomeWeapon =
          allAmmo.stream()
              .anyMatch(a -> a.getNumRounds(null) < a.getNumRounds(Collections.singletonList(m)));
      assertTrue("Modifier " + m.toString() + " didn't apply to any item", appliesToSomeWeapon);
    }
  }

  @Test
  public void testAmmoCapacityQuirk() throws Exception {
    final Ammunition cut = (Ammunition) ItemDB.lookup("C-SRM AMMO");
    Loadout acw_p =
        TestHelpers.parse(
            "http://t.li-soft.org/?l=rwJvFSUDKBIsBCUDKBUKlIH30%2B%2BH38%2B8B96Pvx95Mw%3D%3D");

    int baseRounds = cut.getNumRounds(null);
    int actualRounds = cut.getNumRounds(acw_p.getQuirks());

    assertNotEquals(baseRounds, actualRounds);
  }

  @Test
  public void testAmmoCapacityQuirkHalfTon() throws Exception {
    final Ammunition full = (Ammunition) ItemDB.lookup("C-SRM AMMO");
    final Ammunition half = (Ammunition) ItemDB.lookup("C-SRM AMMO (1/2)");
    Loadout acw_p =
        TestHelpers.parse(
            "http://t.li-soft.org/?l=rwJvFSUDKBIsBCUDKBUKlIH30%2B%2BH38%2B8B96Pvx95Mw%3D%3D");

    int actualFullRounds = full.getNumRounds(acw_p.getQuirks());
    int actualHalfRounds = half.getNumRounds(acw_p.getQuirks());

    assertEquals(actualFullRounds / 2, actualHalfRounds);
  }

  @Test
  public void testBug693() throws Exception {
    final Ammunition cut = (Ammunition) ItemDB.lookup("C-ATM AMMO");

    assertEquals(HardPointType.MISSILE, cut.getWeaponHardPointType());
  }

  @Test
  public void testHalfTonAmmo() throws Exception {
    final Ammunition cut = (Ammunition) ItemDB.lookup(2233);

    assertEquals(0.5, cut.getMass(), 0.0);
    assertEquals(1, cut.getSlots());
    assertTrue(cut.getHealth() > 0.0);
  }
}

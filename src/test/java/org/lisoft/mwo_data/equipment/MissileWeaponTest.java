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

import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.model.UpgradeDB;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.ItemDB;

/**
 * This test suite test some properties of the {@link MissileWeapon}s read in from the game data
 * files to verify parsing.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
public class MissileWeaponTest {
  private static final List<MissileWeapon> ALL_MISSILE_WEAPONS = ItemDB.lookup(MissileWeapon.class);

  /**
   * Make sure {@link Weapon#getDamagePerShot()} returns the volley damage and not missile damage.
   *
   * @throws Exception
   */
  @Test
  public void testGetDamagePerShot_lrm20() throws Exception {
    final MissileWeapon lrm20 = (MissileWeapon) ItemDB.lookup("LRM 20");
    assertTrue(lrm20.getDamagePerShot() > 10);
  }

  /** The mass is affected by Artemis. */
  @Test
  public void testGetMass() throws Exception {
    final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
    final MissileWeapon srm6artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

    assertEquals(srm6.getMass() + 1.0, srm6artemis.getMass(), 0.0);
  }

  @Test
  public void testGetRangeEffectivity_SRM6() throws Exception {
    final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");

    final WeaponRangeProfile.Range opt = srm6.getRangeOptimal(null);
    assertEquals(1.0, srm6.getRangeEffectiveness(0, null), 0.0);
    assertEquals(
        0.6, srm6.getRangeEffectiveness(opt.maximum / 3, null), 0.05); // Spread taken into account.
    assertEquals(0.0, srm6.getRangeEffectiveness(Math.nextUp(opt.maximum), null), 0.0);
    assertEquals(0.0, srm6.getRangeEffectiveness(srm6.getRangeMax(null), null), 0.3);
  }

  @Test
  public void testGetRangeEffectivity_SRM6_Artemis() throws Exception {
    final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
    final MissileWeapon srm6Artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

    final double withoutArtemis = srm6.getRangeEffectiveness(90.0, null);
    final double withArtemis = srm6Artemis.getRangeEffectiveness(90.0, null);
    assertTrue(withArtemis > withoutArtemis * 1.1);
  }

  @Test
  public void testGetRangeEffectivity_lrm20() throws Exception {
    final MissileWeapon lrm20 = (MissileWeapon) ItemDB.lookup("LRM 20");
    final WeaponRangeProfile.Range opt = lrm20.getRangeOptimal(null);
    assertEquals(0.0, lrm20.getRangeEffectiveness(0, null), 0.0);
    assertEquals(0.0, lrm20.getRangeEffectiveness(Math.nextDown(opt.minimum), null), 0.0);
    assertEquals(1.0, lrm20.getRangeEffectiveness(opt.minimum, null), 0.0);
    assertEquals(1.0, lrm20.getRangeEffectiveness(opt.maximum, null), 0.0);
    assertEquals(0.0, lrm20.getRangeEffectiveness(Math.nextUp(opt.maximum), null), 0.0);

    assertEquals(1.0, lrm20.getRangeEffectiveness(lrm20.getRangeMax(null), null), 0.0);
    assertEquals(0.0, lrm20.getRangeEffectiveness(Math.nextUp(lrm20.getRangeMax(null)), null), 0.0);
  }

  /** All missiles do non-zero damage on the max range */
  @Test
  public void testGetRangeMax() {
    for (final MissileWeapon weapon : ALL_MISSILE_WEAPONS) {
      assertTrue(
          weapon.getName(), weapon.getRangeEffectiveness(weapon.getRangeMax(null), null) > 0.0);
    }
  }

  /** All missiles except Rocket Launcher & C-LRM have an instant fall off on the near range. */
  @Test
  public void testGetRangeZero() {
    for (final MissileWeapon weapon : ALL_MISSILE_WEAPONS) {
      if (weapon.getName().contains("LRM") && weapon.getFaction() == Faction.CLAN
          || weapon.getName().contains("ROCKET")) {
        continue;
      }

      final WeaponRangeProfile.Range opt = weapon.getRangeOptimal(null);

      assertEquals(
          weapon.getName(),
          0.0,
          weapon.getRangeEffectiveness(Math.nextDown(opt.minimum), null),
          0.0);
      assertEquals(weapon.getName(), 1.25, weapon.getRangeEffectiveness(opt.minimum, null), 0.3);
    }
  }

  @Test
  public void testGetSecondsPerShot_clrm20() throws Exception {
    final Weapon cut = (Weapon) ItemDB.lookup("C-LRM 20");
    final double expected = cut.getCoolDown(null) + 19 * 0.05;
    assertEquals(expected, cut.getExpectedFiringPeriod(null), 0.0);
  }

  @Test
  public void testGetSecondsPerShot_csrm6() throws Exception {
    final Weapon cut = (Weapon) ItemDB.lookup("C-SRM 6");
    final double expected = cut.getCoolDown(null);
    assertEquals(expected, cut.getExpectedFiringPeriod(null), 0.0);
  }

  @Test
  public void testGetSecondsPerShot_cssrm6() throws Exception {
    final Weapon cut = (Weapon) ItemDB.lookup("C-STREAK SRM 6");
    final double expected = cut.getCoolDown(null);
    assertEquals(expected, cut.getExpectedFiringPeriod(null), 0.0);
  }

  @Test
  public void testGetSecondsPerShot_srm6() throws Exception {
    final Weapon cut = (Weapon) ItemDB.lookup("SRM 6");
    final double expected = cut.getCoolDown(null);
    assertEquals(expected, cut.getExpectedFiringPeriod(null), 0.0);
  }

  @Test
  public void testGetShotsPerVolley_lrm10() throws Exception {
    final Weapon lrm10 = (Weapon) ItemDB.lookup("LRM 10");
    assertEquals(10, lrm10.getRoundsPerShot());
  }

  /** The number of critical slots is affected by Artemis. */
  @Test
  public void testGetSlots() throws Exception {
    final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
    final MissileWeapon srm6artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

    assertEquals(srm6artemis.getSlots(), srm6.getSlots() + 1);
  }

  @Test
  public void testGetSpread_Artemis() throws Exception {
    final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
    final MissileWeapon srm6Artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

    final GuidanceUpgrade artemis = UpgradeDB.ARTEMIS_IV;

    final double withoutArtemis = srm6.getRangeProfile().getSpread().value(null);
    final double withArtemis = srm6Artemis.getRangeProfile().getSpread().value(null);
    assertEquals(withArtemis, withoutArtemis * artemis.getSpreadFactor(), 0.0);
  }

  /** Only SRMs and LRMs are Artemis capable */
  @Test
  public void testIsArtemisCapable() {
    for (final MissileWeapon weapon : ALL_MISSILE_WEAPONS) {
      if (weapon.getName().contains("STREAK")
          || weapon.getName().contains("NARC")
          || weapon.getName().contains("ATM")
          || weapon.getName().contains("ROCKET")
          || weapon.getName().contains("MRM")) {
        assertFalse(weapon.getName(), weapon.isArtemisCapable());
      } else {
        assertTrue(weapon.getName(), weapon.isArtemisCapable());
      }
    }
  }

  @Test
  public void testNotArtemisMissiles() throws Exception {
    final MissileWeapon lrm = (MissileWeapon) ItemDB.lookup("LRM 20");
    assertFalse(lrm.getName().toLowerCase().contains("artemis"));
  }
}

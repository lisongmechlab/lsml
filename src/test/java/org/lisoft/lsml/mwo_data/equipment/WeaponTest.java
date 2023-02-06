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
package org.lisoft.lsml.mwo_data.equipment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.lisoft.lsml.mwo_data.*;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;
import org.lisoft.lsml.mwo_data.modifiers.*;

/**
 * Test suite for {@link Weapon}.
 *
 * @author Li Song
 */
public class WeaponTest {
  private static final String NON_EXISTENT_WEAPON_STAT = "x";

  @SuppressWarnings("unused")
  @Test
  public void testConstruction() {
    final String aName = "name";
    final String aDesc = "desc";
    final String aMwoName = "mwo";
    final int aMwoId = 10;
    final int aSlots = 11;
    final double aTons = 12;
    final HardPointType aHardPointType = HardPointType.AMS;
    final int aHP = 13;
    final Faction aFaction = Faction.CLAN;

    final List<String> selectors = List.of(aMwoName);
    final int heat = 30;
    final int coolDown = 31;
    final int rangeZero = 32;
    final int rangeMin = 33;
    final int rangeLong = 34;
    final int rangeMax = 35;
    final Attribute aHeat = new Attribute(heat, selectors, ModifierDescription.SPEC_WEAPON_HEAT);
    final Attribute aCoolDown =
        new Attribute(coolDown, selectors, ModifierDescription.SPEC_WEAPON_COOL_DOWN);

    final int aRoundsPerShot = 15;
    final double aDamagePerProjectile = 16;
    final int aProjectilesPerRound = 17;
    final int projectileSpeed = 36;
    final Attribute aProjectileSpeed =
        new Attribute(projectileSpeed, selectors, ModifierDescription.SPEC_WEAPON_PROJECTILE_SPEED);
    final int aGhostHeatGroupId = 18;
    final double aGhostHeatMultiplier = 19;
    final int ghostHeatMaxFreeAlpha = 20;
    final Attribute aGhostHeatMaxFreeAlpha =
        new Attribute(
            ghostHeatMaxFreeAlpha, selectors, ModifierDescription.SPEC_WEAPON_MAX_FREE_ALPHA);
    final double aVolleyDelay = 21;
    final double aImpulse = 22;
    final WeaponRangeProfile aRangeProfile = mock(WeaponRangeProfile.class);
    final Weapon cut =
        new Weapon(
            aName,
            aDesc,
            aMwoName,
            aMwoId,
            aSlots,
            aTons,
            aHardPointType,
            aHP,
            aFaction,
            aHeat,
            aCoolDown,
            aRangeProfile,
            aRoundsPerShot,
            aDamagePerProjectile,
            aProjectilesPerRound,
            aProjectileSpeed,
            aGhostHeatGroupId,
            aGhostHeatMultiplier,
            aGhostHeatMaxFreeAlpha,
            aImpulse);

    assertEquals(aName, cut.getName());
    assertEquals(aDesc, cut.getDescription());
    assertEquals(aMwoName, cut.getKey());
    assertEquals(aMwoId, cut.getId());
    assertEquals(aSlots, cut.getSlots());
    assertEquals(aTons, cut.getMass(), 0.0);
    assertEquals(aHardPointType, cut.getHardpointType());
    assertEquals(aFaction, cut.getFaction());

    assertEquals(heat, cut.getHeat(null), 0.0);
    assertEquals(coolDown, cut.getCoolDown(null), 0.0);
    assertSame(aRangeProfile, cut.getRangeProfile());

    assertEquals(aRoundsPerShot, cut.getRoundsPerShot());
    assertEquals(
        aDamagePerProjectile * aProjectilesPerRound * aRoundsPerShot, cut.getDamagePerShot(), 0.0);
    assertEquals(projectileSpeed, cut.getProjectileSpeed(null), 0.0);
    assertEquals(aGhostHeatGroupId, cut.getGhostHeatGroup());
    assertEquals(aGhostHeatMultiplier, cut.getGhostHeatMultiplier(), 0.0);
    assertEquals(ghostHeatMaxFreeAlpha, cut.getGhostHeatMaxFreeAlpha(null));
    assertEquals(
        coolDown, cut.getExpectedFiringPeriod(null), 0.0); // weapon without type only has coolDown.
    assertEquals(aImpulse, cut.getImpulse(), 0.0);

    try {
      new Weapon(
          aName,
          aDesc,
          aMwoName,
          aMwoId,
          aSlots,
          aTons,
          aHardPointType,
          aHP,
          aFaction,
          aHeat,
          aCoolDown,
          aRangeProfile,
          0,
          aDamagePerProjectile,
          aProjectilesPerRound,
          aProjectileSpeed,
          aGhostHeatGroupId,
          aGhostHeatMultiplier,
          aGhostHeatMaxFreeAlpha,
          aImpulse);
      fail("Expected exception");
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testGetDamagePerShot_gauss() throws Exception {
    final Weapon gauss = (Weapon) ItemDB.lookup("GAUSS RIFLE");
    assertTrue(gauss.getDamagePerShot() > 10);
  }

  /**
   * Make sure {@link Weapon#getDamagePerShot()} returns the volley damage and not projectile
   * damage.
   */
  @Test
  public void testGetDamagePerShot_lb10x() throws Exception {
    final Weapon lb10xac = (Weapon) ItemDB.lookup("LB 10-X AC");
    assertTrue(lb10xac.getDamagePerShot() > 5);
  }

  @Test
  public void testGetDamagePerShot_lpl() throws Exception {
    final Weapon weapon = (Weapon) ItemDB.lookup("LRG PULSE LASER");
    assertTrue(weapon.getDamagePerShot() > 8);
  }

  @Test
  public void testGetDamagePerShot_ml() throws Exception {
    final Weapon weapon = (Weapon) ItemDB.lookup("MEDIUM LASER");
    assertTrue(weapon.getDamagePerShot() > 4);
  }

  @Test
  public void testGetHeat_gauss() throws Exception {
    final Weapon gauss = (Weapon) ItemDB.lookup("GAUSS RIFLE");
    assertEquals(1.0, gauss.getHeat(null), 0.0);
  }

  @Test
  public void testGetRangeEffectivity_clrm() throws Exception {
    final MissileWeapon lrm = (MissileWeapon) ItemDB.lookup("C-LRM 20");
    final WeaponRangeProfile.Range opt = lrm.getRangeOptimal(null);
    assertEquals(0.0, lrm.getRangeEffectiveness(0.0, null), 0.0);
    assertEquals(0.444, lrm.getRangeEffectiveness(120, null), 0.001);
    assertEquals(1.0, lrm.getRangeEffectiveness(opt.minimum, null), 0.0);
    assertEquals(1.0, lrm.getRangeEffectiveness(opt.maximum, null), 0.0);
    assertEquals(0.0, lrm.getRangeEffectiveness(Math.nextUp(opt.maximum), null), 0.0);
  }

  @Test
  public void testGetRangeEffectivity_gaussRifle() throws Exception {
    final BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
    final WeaponRangeProfile.Range opt = gauss.getRangeOptimal(null);
    assertEquals(1.0, gauss.getRangeEffectiveness(0, null), 0.0);
    assertEquals(1.0, gauss.getRangeEffectiveness(opt.maximum, null), 0.0);
    assertEquals(
        0.5, gauss.getRangeEffectiveness((opt.maximum + gauss.getRangeMax(null)) / 2, null), 0.0);
    assertEquals(0.0, gauss.getRangeEffectiveness(gauss.getRangeMax(null), null), 0.0);

    assertTrue(gauss.getRangeEffectiveness(930, null) < 0.95);
    assertTrue(gauss.getRangeEffectiveness(930, null) > 0.8);
  }

  @Test
  public void testGetRangeEffectivity_mg() throws Exception {
    final BallisticWeapon mg = (BallisticWeapon) ItemDB.lookup("MACHINE GUN");
    final WeaponRangeProfile.Range opt = mg.getRangeOptimal(null);
    assertEquals(1.0, mg.getRangeEffectiveness(0, null), 0.0);
    assertEquals(1.0, mg.getRangeEffectiveness(opt.maximum, null), 0.1); // High spread on MG
    // Spread + falloff
    assertTrue(0.5 >= mg.getRangeEffectiveness((opt.maximum + mg.getRangeMax(null)) / 2, null));
    assertEquals(0.0, mg.getRangeEffectiveness(mg.getRangeMax(null), null), 0.0);
  }

  @Test
  public void testGetRangeMax_ppc() throws Exception {
    final Weapon ppc = (Weapon) ItemDB.lookup("PPC");
    assertEquals(1080.0, ppc.getRangeMax(null), 0.0);
  }

  @Test
  public void testGetRangeOptimal_ppc() throws Exception {
    final Weapon ppc = (Weapon) ItemDB.lookup("PPC");
    final WeaponRangeProfile.Range opt = ppc.getRangeOptimal(null);
    assertEquals(90.0, opt.minimum, 0.0);
    assertEquals(540.0, opt.maximum, 0.0);
  }

  @Test
  public void testGetSecondsPerShot_gauss() throws Exception {
    final Weapon gauss = (Weapon) ItemDB.lookup("GAUSS RIFLE");
    assertEquals(gauss.getCoolDown(null) + 0.75, gauss.getExpectedFiringPeriod(null), 0.0);
  }

  @Test
  public void testGetSecondsPerShot_mg() throws Exception {
    final Weapon mg = (Weapon) ItemDB.lookup("MACHINE GUN");
    assertTrue(mg.getExpectedFiringPeriod(null) > 0.05);
  }

  @Test
  public void testGetShotsPerVolley_lb10x() throws Exception {
    final Weapon lb10xac = (Weapon) ItemDB.lookup("LB 10-X AC");
    assertEquals(1, lb10xac.getRoundsPerShot());
  }

  @Test
  public void testGetStat() throws Exception {
    final Weapon wpn = (Weapon) ItemDB.lookup("ER PPC");
    assertEquals(wpn.getDamagePerShot() / wpn.getHeat(null), wpn.getStat("d/h", null), 0.0);
    assertEquals(wpn.getHeat(null) / wpn.getDamagePerShot(), wpn.getStat("h/d", null), 0.0);
    assertEquals(wpn.getExpectedFiringPeriod(null) / wpn.getMass(), wpn.getStat("s/t", null), 0.0);
    assertEquals(wpn.getMass() / wpn.getExpectedFiringPeriod(null), wpn.getStat("t/s", null), 0.0);
    assertEquals(wpn.getSlots(), wpn.getStat("c", null), 0.0);
    assertEquals(1.0, wpn.getStat("dsthc/dsthc", null), 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetStatFormatErrorDenominator() throws Exception {
    final Weapon wpn = (Weapon) ItemDB.lookup("ER PPC");
    wpn.getStat("/" + NON_EXISTENT_WEAPON_STAT, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetStatFormatErrorNominator() throws Exception {
    final Weapon wpn = (Weapon) ItemDB.lookup("ER PPC");
    wpn.getStat(NON_EXISTENT_WEAPON_STAT, null);
  }

  /**
   * When taking the quotient of two stats that are zero we're faced with interpreting 0/0. Although
   * not strictly mathematically correct, we will interpret x/y as 'x' per 'y' and if 'x' is zero we
   * will output zero if 'y' is also finite.a
   */
  @Test
  public void testGetStatZeroOverZero() {
    // AMS has no heat.
    assertEquals(0.0, ItemDB.AMS.getHeat(null), 0.0);
    assertEquals(0.0, ItemDB.AMS.getStat("h/h", null), 0.0);
  }

  /** Gauss has low heat test specially */
  @Test
  public void testGetStat_gauss() throws Exception {
    final BallisticWeapon gauss = (BallisticWeapon) ItemDB.lookup("GAUSS RIFLE");
    assertEquals(gauss.getDamagePerShot() / gauss.getHeat(null), gauss.getStat("d/h", null), 0.0);
  }

  @Test
  public void testInequality() throws Exception {
    final MissileWeapon lrm10 = (MissileWeapon) ItemDB.lookup("LRM 10");
    final MissileWeapon lrm15 = (MissileWeapon) ItemDB.lookup("LRM 15");
    assertNotEquals(lrm10, lrm15);
  }

  @Test
  public void testIsLargeBore() throws Exception {
    assertTrue(((Weapon) ItemDB.lookup("C-ER PPC")).isLargeBore());
    assertFalse(((Weapon) ItemDB.lookup("LARGE LASER")).isLargeBore());
    assertTrue(((Weapon) ItemDB.lookup("AC/10")).isLargeBore());
    assertTrue(((Weapon) ItemDB.lookup("LB 10-X AC")).isLargeBore());
    assertTrue(((Weapon) ItemDB.lookup("GAUSS RIFLE")).isLargeBore());
    assertTrue(((Weapon) ItemDB.lookup("C-LB5-X AC")).isLargeBore());
    assertFalse(((Weapon) ItemDB.lookup("MACHINE GUN")).isLargeBore());
    assertFalse(((Weapon) ItemDB.lookup("AMS")).isLargeBore());
  }

  @Test
  public void testIsOffensive() throws Exception {
    assertTrue(((Weapon) ItemDB.lookup("C-ER PPC")).isOffensive());
    assertFalse(((Weapon) ItemDB.lookup("AMS")).isOffensive());
    assertFalse(((Weapon) ItemDB.lookup("C-AMS")).isOffensive());
    assertFalse(((Weapon) ItemDB.lookup("C-LASER AMS")).isOffensive());
  }

  @Test
  public void testRangeModifiers() throws Exception {
    final Weapon llas = (Weapon) ItemDB.lookup("LARGE LASER");

    ModifierDescription desc1 =
        new ModifierDescription(
            "",
            "",
            Operation.MUL,
            List.of("islargelaser"),
            ModifierDescription.SPEC_WEAPON_RANGE,
            ModifierType.POSITIVE_GOOD);
    ModifierDescription desc2 =
        new ModifierDescription(
            "",
            "",
            Operation.ADD,
            List.of("islargelaser"),
            ModifierDescription.SPEC_WEAPON_RANGE,
            ModifierType.POSITIVE_GOOD);
    final Modifier range1 = new Modifier(desc1, 0.1);
    final Modifier range2 = new Modifier(desc2, 100);

    final List<Modifier> modifiers = new ArrayList<>();
    modifiers.add(range1);
    modifiers.add(range2);

    final WeaponRangeProfile.Range opt = llas.getRangeOptimal(null);
    final WeaponRangeProfile.Range optMod = llas.getRangeOptimal(modifiers);

    final double expectedLongRange = (opt.maximum + 100.0) * (1.0 + 0.1);
    assertEquals(expectedLongRange, optMod.maximum, 0.0);

    final double expectedMaxRange = (llas.getRangeMax(null) + 100.0) * (1.0 + 0.1);
    assertEquals(expectedMaxRange, llas.getRangeMax(modifiers), 0.0);
  }

  @Test
  public void testRawExpectedFiringPeriodAllWeapons() {
    final List<Weapon> weapons = ItemDB.lookup(Weapon.class);

    for (Weapon weapon : weapons) {
      final double rFP = weapon.getRawFiringPeriod(null);
      final double eFP = weapon.getExpectedFiringPeriod(null);

      // Jamming and multiple shots during cool down are the two reasons that raw damage does not
      // equal expected damage.
      // both situations currently are only in ballistic weapons and the values are only available
      // in the ballistic weapon class.
      if (weapon instanceof BallisticWeapon ballisticWeapon) {
        if ((ballisticWeapon.getJamProbability(null) == 0)
            && (ballisticWeapon.getShotsDuringCoolDown() == 0)) {
          // Only test weapons where rFP and eFP don't differ
          assertEquals(rFP, eFP, 0.0);
        }

      } else {
        // All others should be equal.
        assertEquals(rFP, eFP, 0.0);
      }
    }
  }
}

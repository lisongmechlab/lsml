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
package org.lisoft.lsml.view_fx.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import org.junit.Test;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.mwo_data.equipment.AmmoWeapon;
import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.NoSuchItemException;
import org.lisoft.mwo_data.modifiers.Modifier;

public class WeaponSummaryTest {

  private final AmmoWeapon ac10;
  private final Ammunition ac10ammo;
  private final AmmoWeapon ac20;
  private final Ammunition ac20ammo;
  private final Ammunition ac20ammoHalf;
  private final Item c_mg;
  private final Item llas;
  private final Item lrm20;
  private final Item mlas;
  private final List<Modifier> modifiers = new ArrayList<>();
  private final Item mrm40;
  private final AmmoWeapon rl20;
  private final AmmoWeapon srm2;
  private final AmmoWeapon srm2Artemis;
  private final AmmoWeapon srm4;
  private final AmmoWeapon srm4Artemis;
  private final AmmoWeapon srm6;
  private final AmmoWeapon srm6Artemis;
  private final Ammunition srmAmmo = (Ammunition) ItemDB.lookup("SRM AMMO");
  private final Supplier<Collection<Modifier>> supplier = () -> modifiers;

  public WeaponSummaryTest() throws NoSuchItemException {
    llas = ItemDB.lookup("LARGE LASER");
    lrm20 = ItemDB.lookup("LRM 20");
    mrm40 = ItemDB.lookup("MRM 40");
    rl20 = (AmmoWeapon) ItemDB.lookup("ROCKET LAUNCHER 20");
    ac20 = (AmmoWeapon) ItemDB.lookup("AC/20");
    ac20ammo = (Ammunition) ItemDB.lookup("AC/20 AMMO");
    ac20ammoHalf = (Ammunition) ItemDB.lookup("AC/20 AMMO (1/2)");
    ac10 = (AmmoWeapon) ItemDB.lookup("AC/10");
    ac10ammo = (Ammunition) ItemDB.lookup("AC/10 AMMO");
    c_mg = ItemDB.lookup("C-MACHINE GUN");
    srm6 = (AmmoWeapon) ItemDB.lookup("SRM 6");
    srm4 = (AmmoWeapon) ItemDB.lookup("SRM 4");
    srm2 = (AmmoWeapon) ItemDB.lookup("SRM 2");
    srm6Artemis = (AmmoWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");
    srm4Artemis = (AmmoWeapon) ItemDB.lookup("SRM 4 + ARTEMIS");
    srm2Artemis = (AmmoWeapon) ItemDB.lookup("SRM 2 + ARTEMIS");
    mlas = ItemDB.lookup("MEDIUM LASER");
  }

  @Test
  public void testBattleTime_AddRemove() {
    final WeaponSummary cut = new WeaponSummary(supplier, srmAmmo);
    cut.consume(srmAmmo);

    cut.consume(srm2);
    final double srm2BattleTime = cut.battleTimeProperty().get();
    cut.consume(srm6);
    cut.remove(srm6);
    assertEquals(srm2BattleTime, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testBattleTime_Complex() {
    final WeaponSummary cut = new WeaponSummary(supplier, srmAmmo);
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);

    final int rounds = 2 * srmAmmo.getNumRounds(supplier.get());

    cut.consume(srmAmmo);
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);

    cut.consume(srm2);
    assertEquals(srm2.getCoolDown(null) * rounds / 2, cut.battleTimeProperty().get(), 0.0);

    cut.consume(srm4);
    assertEquals(srm4.getCoolDown(null) * rounds / 6, cut.battleTimeProperty().get(), 0.0);

    cut.consume(srm6);
    assertEquals(srm6.getCoolDown(null) * rounds / 12, cut.battleTimeProperty().get(), 0.0);

    cut.consume(srm2);
    assertEquals(srm6.getCoolDown(null) * rounds / 14, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testBug495() throws Exception {
    final AmmoWeapon cuac10 = (AmmoWeapon) ItemDB.lookup("C-ULTRA AC/10");
    final Ammunition cuac10ammo = (Ammunition) ItemDB.lookup("C-U-AC/10 AMMO");

    final WeaponSummary cut = new WeaponSummary(supplier, cuac10);
    cut.consume(cuac10ammo);
    cut.consume(cuac10ammo);

    assertEquals(3, cut.volleySizeProperty().intValue());
    final double expectedTime =
        2 * cuac10ammo.getNumRounds(supplier.get()) / 3 * cuac10.getExpectedFiringPeriod(null);
    assertEquals(expectedTime, cut.battleTimeProperty().doubleValue(), 0.001);
  }

  @Test
  public void testBug550() throws Exception {
    final AmmoWeapon cuac10 = (AmmoWeapon) ItemDB.lookup("C-ULTRA AC/10");

    final WeaponSummary cut = new WeaponSummary(supplier, cuac10);
    cut.consume(cuac10);
    cut.remove(cuac10);

    assertEquals("C-UAC/10", cut.nameProperty().get());
  }

  @Test
  public void testConsume_Ammo2AmmoWeapon_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertTrue(cut.consume(ac20ammo));
    assertEquals(ac20ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertEquals(1, cut.volleySizeProperty().get());
    assertEquals(
        ac20.getCoolDown(null) * ac20ammo.getNumRounds(supplier.get()) / 1,
        cut.battleTimeProperty().get(),
        0.0);
  }

  @Test
  public void testConsume_Ammo2AmmoWeapon_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertFalse(cut.consume(ac10ammo));
    assertEquals(0, cut.roundsProperty().get(), 0.0);
    assertEquals(1, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_Ammo2Ammo_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertTrue(cut.consume(ac20ammo));
    assertEquals(ac20ammo.getNumRounds(supplier.get()) * 2, cut.roundsProperty().get(), 0.0);
    assertEquals(0, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_Ammo2Ammo_CorrectTypeHalfTon() {
    final int expectedRounds =
        ac20ammo.getNumRounds(supplier.get()) + ac20ammoHalf.getNumRounds(supplier.get());
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertTrue(cut.consume(ac20ammoHalf));
    assertEquals(expectedRounds, cut.roundsProperty().get(), 0.0);
    assertEquals(0, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_Ammo2Ammo_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertFalse(cut.consume(ac10ammo));
    assertEquals(0, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_Ammo2AmmolessWeapon_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, llas);
    assertFalse(cut.consume(ac10ammo));
    assertEquals(llas.getShortName(), cut.nameProperty().get());
    assertTrue(Double.isInfinite(cut.roundsProperty().get()));
    assertEquals(1, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_AmmoWeapon2AmmoWeapon_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertTrue(cut.consume(ac20));
    assertEquals("2x " + ac20.getShortName(), cut.nameProperty().get());
    assertEquals(2, cut.volleySizeProperty().get());
    assertTrue(cut.consume(ac20));
    assertEquals("3x " + ac20.getShortName(), cut.nameProperty().get());
    assertEquals(3, cut.volleySizeProperty().get());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testConsume_AmmoWeapon2AmmoWeapon_VariantType() {
    final WeaponSummary cut = new WeaponSummary(supplier, srm6);
    assertTrue(cut.consume(srm4));
    assertEquals(10, cut.volleySizeProperty().get());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testConsume_AmmoWeapon2AmmoWeapon_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertFalse(cut.consume(ac10));
    assertEquals(ac20.getShortName(), cut.nameProperty().get());
    assertEquals(1, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_AmmoWeapon2Ammo_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertTrue(cut.consume(ac20));
    assertEquals(ac20ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertEquals(ac20.getShortName(), cut.nameProperty().get());
    assertEquals(1, cut.volleySizeProperty().get());
    assertEquals(
        ac20.getCoolDown(null) * ac20ammo.getNumRounds(supplier.get()) / 1,
        cut.battleTimeProperty().get(),
        0.0);
  }

  @Test
  public void testConsume_AmmoWeapon2Ammo_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac10ammo);
    assertFalse(cut.consume(ac20));
    assertEquals(ac10ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertEquals(0, cut.volleySizeProperty().get());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testConsume_AmmolessWeapon2Ammo() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac10ammo);
    assertFalse(cut.consume(llas));
    assertEquals(ac10ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertEquals(0, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_AmmolessWeapon2AmmoWeapon() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertFalse(cut.consume(llas));
    assertEquals(ac20.getShortName(), cut.nameProperty().get());
    assertEquals(1, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_AmmolessWeapon2AmmolessWeapon_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, llas);
    assertTrue(cut.consume(llas));
    assertEquals("2x " + llas.getShortName(), cut.nameProperty().get());
    assertTrue(Double.isInfinite(cut.roundsProperty().get()));
    assertEquals(2, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_AmmolessWeapon2AmmolessWeapon_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, llas);
    assertFalse(cut.consume(mlas));
    assertEquals(llas.getShortName(), cut.nameProperty().get());
    assertTrue(Double.isInfinite(cut.roundsProperty().get()));
    assertEquals(1, cut.volleySizeProperty().get());
  }

  @Test
  public void testConsume_MissileNames() {
    final WeaponSummary cut = new WeaponSummary(supplier, srm2Artemis);
    assertTrue(cut.consume(srm4Artemis));
    assertTrue(cut.consume(srm6Artemis));
    assertEquals("SRM 12 + A.", cut.nameProperty().get());
    assertEquals(12, cut.volleySizeProperty().get());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testCreateAmmo() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertEquals(ac20ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertEquals(ac20ammo.getShortName(), cut.nameProperty().get());
    assertEquals(0, cut.volleySizeProperty().get());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testCreateAmmoWeapon() {
    final WeaponSummary cut = new WeaponSummary(supplier, c_mg);
    assertEquals(0, cut.roundsProperty().get(), 0.0);
    assertEquals(1, cut.volleySizeProperty().get());
    assertEquals(c_mg.getShortName(), cut.nameProperty().get());
    assertFalse(cut.empty());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testCreateAmmolessWeapon() {
    final WeaponSummary cut = new WeaponSummary(supplier, llas);
    assertTrue(Double.isInfinite(cut.roundsProperty().get()));
    assertEquals(llas.getShortName(), cut.nameProperty().get());
    assertEquals(1, cut.volleySizeProperty().get());
    assertFalse(cut.empty());
    assertEquals(Double.POSITIVE_INFINITY, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testCreateMissileWeapon() {
    final WeaponSummary cut = new WeaponSummary(supplier, lrm20);
    assertEquals(0.0, cut.roundsProperty().get(), 0.0);
    assertEquals(lrm20.getShortName(), cut.nameProperty().get());
    assertEquals(20, cut.volleySizeProperty().get());
    assertFalse(cut.empty());
    assertEquals(0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testRemove_AmmoFromAmmo_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertFalse(cut.remove(ac10ammo));
    assertFalse(cut.empty());
  }

  @Test
  public void testRemove_AmmoFromManyAmmo_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    cut.consume(ac20ammo);
    cut.consume(ac20ammo);

    assertTrue(cut.remove(ac20ammo));
    assertEquals(ac20ammo.getNumRounds(supplier.get()) * 2, cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());

    assertTrue(cut.remove(ac20ammo));
    assertEquals(ac20ammo.getNumRounds(supplier.get()) * 1, cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());

    assertTrue(cut.remove(ac20ammo));
    assertTrue(cut.empty());
  }

  @Test
  public void testRemove_AmmoWeaponFromAmmoAndAmmoWeapon_WrongType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    cut.consume(ac20);

    assertFalse(cut.remove(ac10));
    assertEquals(1, cut.volleySizeProperty().get(), 0.0);
    assertEquals(ac20ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());
    assertEquals(
        ac20.getCoolDown(null) * ac20ammo.getNumRounds(supplier.get()) / 1,
        cut.battleTimeProperty().get(),
        0.0);
  }

  @Test
  public void testRemove_AmmoWeaponFromManyAmmoAndAmmoWeapon_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    cut.consume(ac20ammo);
    cut.consume(ac20);
    cut.consume(ac20);

    assertTrue(cut.remove(ac20));
    assertEquals(1, cut.volleySizeProperty().get(), 0.0);
    assertEquals(ac20ammo.getNumRounds(supplier.get()) * 2, cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());
    assertEquals(
        ac20.getCoolDown(null) * 2 * ac20ammo.getNumRounds(supplier.get()) / 1,
        cut.battleTimeProperty().get(),
        0.0);
  }

  @Test
  public void testRemove_LastAmmoWeaponFromAmmoWeaponRemainingAmmo_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    cut.consume(ac20ammo);

    assertTrue(cut.remove(ac20));
    assertEquals(0.0, cut.battleTimeProperty().get(), 0.0);
    assertFalse(cut.empty());
  }

  @Test
  public void testRemove_LastAmmoWeaponFromAmmoWeapon_CorrectType() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertTrue(cut.remove(ac20));
    assertTrue(cut.empty());
    assertEquals(0.0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testRemove_MrmName() {
    final WeaponSummary cut = new WeaponSummary(supplier, mrm40);
    assertTrue(cut.consume(mrm40));
    assertEquals("MRM 80", cut.nameProperty().get());
    assertTrue(cut.remove(mrm40));
    assertEquals("MRM 40", cut.nameProperty().get());
    assertEquals(40, cut.volleySizeProperty().get(), 0.0);
    assertEquals(0, cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());
  }

  @Test
  public void testRemove_RocketLauncher_Bug700() {
    final WeaponSummary cut = new WeaponSummary(supplier, rl20);
    assertTrue(cut.remove(rl20));
    assertEquals(0, cut.volleySizeProperty().get(), 0.0);
    assertEquals(0, cut.roundsProperty().get(), 0.0);
    assertTrue(cut.empty());
  }

  @Test
  public void testRemove_SrmName() {
    final WeaponSummary cut = new WeaponSummary(supplier, srmAmmo);
    assertEquals("SRM AMMO", cut.nameProperty().get());
    assertTrue(cut.consume(srm6));
    assertEquals("SRM 6", cut.nameProperty().get());
    assertTrue(cut.remove(srm6));
    assertEquals("SRM AMMO", cut.nameProperty().get());
  }

  @Test
  public void testRemove_SrmName_Bug528() {
    final WeaponSummary cut = new WeaponSummary(supplier, srm6);
    assertTrue(cut.consume(srm6));
    assertEquals("SRM 12", cut.nameProperty().get());
    assertTrue(cut.remove(srm6));
    assertEquals("SRM 6", cut.nameProperty().get());
    assertEquals(6, cut.volleySizeProperty().get(), 0.0);
    assertEquals(0, cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());
  }

  @Test
  public void testRemove_Srm_Bug528() {
    final WeaponSummary cut = new WeaponSummary(supplier, srm6);
    assertTrue(cut.remove(srm6));
    assertEquals(0, cut.volleySizeProperty().get(), 0.0);
    assertEquals(0, cut.roundsProperty().get(), 0.0);
    assertTrue(cut.empty());
  }

  @Test
  public void testRemove_WeaponFromAmmoAndAmmoWeapon() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    cut.consume(ac20);

    assertFalse(cut.remove(llas));
    assertEquals(1, cut.volleySizeProperty().get(), 0.0);
    assertEquals(ac20ammo.getNumRounds(supplier.get()), cut.roundsProperty().get(), 0.0);
    assertFalse(cut.empty());
  }

  @Test
  public void testRemove_WeaponFromManyWeapon() {
    final WeaponSummary cut = new WeaponSummary(supplier, llas);
    cut.consume(llas);
    cut.consume(llas);

    assertTrue(cut.remove(llas));
    assertEquals(2, cut.volleySizeProperty().get(), 0.0);
    assertFalse(cut.empty());
    assertEquals(Double.POSITIVE_INFINITY, cut.battleTimeProperty().get(), 0.0);

    assertTrue(cut.remove(llas));
    assertEquals(1, cut.volleySizeProperty().get(), 0.0);
    assertFalse(cut.empty());
    assertEquals(Double.POSITIVE_INFINITY, cut.battleTimeProperty().get(), 0.0);

    assertTrue(cut.remove(llas));
    assertTrue(cut.empty());
    assertEquals(0.0, cut.battleTimeProperty().get(), 0.0);
  }

  @Test
  public void testRocketLauncherDamage() {
    final WeaponSummary cut = new WeaponSummary(supplier, rl20);
    assertTrue(cut.consume(rl20));
    assertTrue(cut.consume(rl20));
    assertEquals(60, cut.volleySizeProperty().get(), 0.0);
    assertEquals(60, cut.roundsProperty().get(), 0.0);
    assertEquals(rl20.getDamagePerShot() * 3, cut.totalDamageProperty().get(), 0.0);
    assertFalse(cut.empty());
  }

  @Test
  public void testTotalDamage_Ammoless() {
    final WeaponSummary cut = new WeaponSummary(supplier, mlas);
    assertEquals(Double.POSITIVE_INFINITY, cut.totalDamageProperty().get(), 0.0);
  }

  @Test
  public void testTotalDamage_BallisticNoAmmo() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    assertEquals(0.0, cut.totalDamageProperty().get(), 0.0);
  }

  @Test
  public void testTotalDamage_BallisticWithAmmo() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20);
    cut.consume(ac20ammo);
    cut.consume(ac20ammo);
    cut.consume(ac20ammoHalf);
    cut.consume(ac20);
    assertEquals(
        (ac20ammo.getNumRounds(supplier.get()) * 2 + ac20ammoHalf.getNumRounds(supplier.get()))
            * ac20.getDamagePerShot(),
        cut.totalDamageProperty().get(),
        0.0);
  }

  @Test
  public void testTotalDamage_Energy() {
    final WeaponSummary cut = new WeaponSummary(supplier, llas);
    assertTrue(Double.isInfinite(cut.totalDamageProperty().get()));
  }

  @Test
  public void testTotalDamage_NoDamageAmmoWeapon() {
    final AmmoWeapon ams = ItemDB.AMS;
    final WeaponSummary cut = new WeaponSummary(supplier, ams);
    assertEquals(0, cut.totalDamageProperty().get(), 0.0);
  }

  @Test
  public void testTotalDamage_NoDamageEnergyWeapon() throws NoSuchItemException {
    final Item tag = ItemDB.lookup("TAG");
    final WeaponSummary cut = new WeaponSummary(supplier, tag);
    assertEquals(0, cut.totalDamageProperty().get(), 0.0);
  }

  @Test
  public void testTotalDamage_OnlyAmmo() {
    final WeaponSummary cut = new WeaponSummary(supplier, ac20ammo);
    assertEquals(0, cut.totalDamageProperty().get(), 0.0);
  }
}

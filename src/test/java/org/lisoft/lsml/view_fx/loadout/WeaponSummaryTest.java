/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
//@formatter:on
package org.lisoft.lsml.view_fx.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.view_fx.util.WeaponSummary;
import org.mockito.Mockito;

public class WeaponSummaryTest {

    private final Item llas;
    private final Item lrm20;
    private final AmmoWeapon ac20;
    private final Ammunition ac20ammo;
    private final Ammunition ac20ammoHalf;
    private final AmmoWeapon ac10;
    private final Ammunition ac10ammo;
    private final Item c_mg;
    private final AmmoWeapon srm6;
    private final AmmoWeapon srm4;
    private final AmmoWeapon srm2;
    private final AmmoWeapon srm6Artemis;
    private final AmmoWeapon srm4Artemis;
    private final AmmoWeapon srm2Artemis;
    private final Item mllas;
    private final Loadout loadout = Mockito.mock(Loadout.class);

    public WeaponSummaryTest() throws NoSuchItemException {
        llas = ItemDB.lookup("LARGE LASER");
        lrm20 = ItemDB.lookup("LRM 20");
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
        mllas = ItemDB.lookup("MEDIUM LASER");
    }

    @Test
    public void testBattleTime_Complex() throws Exception {
        final Ammunition srmAmmo = (Ammunition) ItemDB.lookup("SRM AMMO");

        final WeaponSummary cut = new WeaponSummary(loadout, srmAmmo);
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);

        final int rounds = 2 * srmAmmo.getNumRounds();

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

        final WeaponSummary cut = new WeaponSummary(loadout, cuac10);
        cut.consume(cuac10ammo);
        cut.consume(cuac10ammo);

        assertEquals(3, cut.volleySizeProperty().intValue());
        final double expectedTime = 2 * cuac10ammo.getNumRounds() / 3 * cuac10.getSecondsPerShot(null);
        assertEquals(expectedTime, cut.battleTimeProperty().doubleValue(), 0.001);
    }

    @Test
    public void testBug550() throws Exception {
        final AmmoWeapon cuac10 = (AmmoWeapon) ItemDB.lookup("C-ULTRA AC/10");

        final WeaponSummary cut = new WeaponSummary(loadout, cuac10);
        cut.consume(cuac10);
        cut.remove(cuac10);

        assertEquals("C-UAC/10", cut.nameProperty().get());
    }

    @Test
    public void testConsume_Ammo2Ammo_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertTrue(cut.consume(ac20ammo));
        assertEquals(ac20ammo.getNumRounds() * 2, cut.roundsProperty().get(), 0.0);
        assertEquals(0, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_Ammo2Ammo_CorrectTypeHalfTon() {
        final int expectedRounds = ac20ammo.getNumRounds() + ac20ammoHalf.getNumRounds();
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertTrue(cut.consume(ac20ammoHalf));
        assertEquals(expectedRounds, cut.roundsProperty().get(), 0.0);
        assertEquals(0, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_Ammo2Ammo_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertFalse(cut.consume(ac10ammo));
        assertEquals(0, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_Ammo2AmmolessWeapon_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, llas);
        assertFalse(cut.consume(ac10ammo));
        assertEquals(llas.getShortName(), cut.nameProperty().get());
        assertTrue(Double.isInfinite(cut.roundsProperty().get()));
        assertEquals(1, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_Ammo2AmmoWeapon_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        assertTrue(cut.consume(ac20ammo));
        assertEquals(ac20ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertEquals(1, cut.volleySizeProperty().get());
        assertEquals(ac20.getCoolDown(null) * ac20ammo.getNumRounds() / 1, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testConsume_Ammo2AmmoWeapon_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        assertFalse(cut.consume(ac10ammo));
        assertEquals(0, cut.roundsProperty().get(), 0.0);
        assertEquals(1, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_AmmolessWeapon2Ammo() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac10ammo);
        assertFalse(cut.consume(llas));
        assertEquals(ac10ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertEquals(0, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_AmmolessWeapon2AmmolessWeapon_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, llas);
        assertTrue(cut.consume(llas));
        assertEquals("2x " + llas.getShortName(), cut.nameProperty().get());
        assertTrue(Double.isInfinite(cut.roundsProperty().get()));
        assertEquals(2, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_AmmolessWeapon2AmmolessWeapon_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, llas);
        assertFalse(cut.consume(mllas));
        assertEquals(llas.getShortName(), cut.nameProperty().get());
        assertTrue(Double.isInfinite(cut.roundsProperty().get()));
        assertEquals(1, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_AmmolessWeapon2AmmoWeapon() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        assertFalse(cut.consume(llas));
        assertEquals(ac20.getShortName(), cut.nameProperty().get());
        assertEquals(1, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_AmmoWeapon2Ammo_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertTrue(cut.consume(ac20));
        assertEquals(ac20ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertEquals(ac20.getShortName(), cut.nameProperty().get());
        assertEquals(1, cut.volleySizeProperty().get());
        assertEquals(ac20.getCoolDown(null) * ac20ammo.getNumRounds() / 1, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testConsume_AmmoWeapon2Ammo_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac10ammo);
        assertFalse(cut.consume(ac20));
        assertEquals(ac10ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertEquals(0, cut.volleySizeProperty().get());
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testConsume_AmmoWeapon2AmmoWeapon_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
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
        final WeaponSummary cut = new WeaponSummary(loadout, srm6);
        assertTrue(cut.consume(srm4));
        assertEquals(10, cut.volleySizeProperty().get());
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testConsume_AmmoWeapon2AmmoWeapon_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        assertFalse(cut.consume(ac10));
        assertEquals(ac20.getShortName(), cut.nameProperty().get());
        assertEquals(1, cut.volleySizeProperty().get());
    }

    @Test
    public void testConsume_MissileNames() {
        final WeaponSummary cut = new WeaponSummary(loadout, srm2Artemis);
        assertTrue(cut.consume(srm4Artemis));
        assertTrue(cut.consume(srm6Artemis));
        assertEquals("SRM 12 + A.", cut.nameProperty().get());
        assertEquals(12, cut.volleySizeProperty().get());
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testCreateAmmo() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertEquals(ac20ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertEquals(ac20ammo.getShortName(), cut.nameProperty().get());
        assertEquals(0, cut.volleySizeProperty().get());
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testCreateAmmolessWeapon() {
        final WeaponSummary cut = new WeaponSummary(loadout, llas);
        assertTrue(Double.isInfinite(cut.roundsProperty().get()));
        assertEquals(llas.getShortName(), cut.nameProperty().get());
        assertEquals(1, cut.volleySizeProperty().get());
        assertFalse(cut.empty());
        assertEquals(Double.POSITIVE_INFINITY, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testCreateAmmoWeapon() {
        final WeaponSummary cut = new WeaponSummary(loadout, c_mg);
        assertEquals(0, cut.roundsProperty().get(), 0.0);
        assertEquals(1, cut.volleySizeProperty().get());
        assertEquals(c_mg.getShortName(), cut.nameProperty().get());
        assertFalse(cut.empty());
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testCreateMissileWeapon() {
        final WeaponSummary cut = new WeaponSummary(loadout, lrm20);
        assertEquals(0.0, cut.roundsProperty().get(), 0.0);
        assertEquals(lrm20.getShortName(), cut.nameProperty().get());
        assertEquals(20, cut.volleySizeProperty().get());
        assertFalse(cut.empty());
        assertEquals(0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testRemove_AmmoFromAmmo_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertFalse(cut.remove(ac10ammo));
        assertFalse(cut.empty());
    }

    @Test
    public void testRemove_AmmoFromManyAmmo_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        cut.consume(ac20ammo);
        cut.consume(ac20ammo);

        assertTrue(cut.remove(ac20ammo));
        assertEquals(ac20ammo.getNumRounds() * 2, cut.roundsProperty().get(), 0.0);
        assertFalse(cut.empty());

        assertTrue(cut.remove(ac20ammo));
        assertEquals(ac20ammo.getNumRounds() * 1, cut.roundsProperty().get(), 0.0);
        assertFalse(cut.empty());

        assertTrue(cut.remove(ac20ammo));
        assertTrue(cut.empty());
    }

    @Test
    public void testRemove_AmmoWeaponFromAmmoAndAmmoWeapon_WrongType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        cut.consume(ac20);

        assertFalse(cut.remove(ac10));
        assertEquals(1, cut.volleySizeProperty().get(), 0.0);
        assertEquals(ac20ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertFalse(cut.empty());
        assertEquals(ac20.getCoolDown(null) * ac20ammo.getNumRounds() / 1, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testRemove_AmmoWeaponFromManyAmmoAndAmmoWeapon_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        cut.consume(ac20ammo);
        cut.consume(ac20);
        cut.consume(ac20);

        assertTrue(cut.remove(ac20));
        assertEquals(1, cut.volleySizeProperty().get(), 0.0);
        assertEquals(ac20ammo.getNumRounds() * 2, cut.roundsProperty().get(), 0.0);
        assertFalse(cut.empty());
        assertEquals(ac20.getCoolDown(null) * 2 * ac20ammo.getNumRounds() / 1, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testRemove_LastAmmoWeaponFromAmmoWeapon_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        assertTrue(cut.remove(ac20));
        assertTrue(cut.empty());
        assertEquals(0.0, cut.battleTimeProperty().get(), 0.0);
    }

    @Test
    public void testRemove_LastAmmoWeaponFromAmmoWeaponRemainingAmmo_CorrectType() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        cut.consume(ac20ammo);

        assertTrue(cut.remove(ac20));
        assertEquals(0.0, cut.battleTimeProperty().get(), 0.0);
        assertFalse(cut.empty());
    }

    @Test
    public void testRemove_Srm_Bug528() {
        final WeaponSummary cut = new WeaponSummary(loadout, srm6);
        assertTrue(cut.remove(srm6));
        assertEquals(0, cut.volleySizeProperty().get(), 0.0);
        assertEquals(0, cut.roundsProperty().get(), 0.0);
        assertTrue(cut.empty());
    }

    @Test
    public void testRemove_SrmName_Bug528() {
        final WeaponSummary cut = new WeaponSummary(loadout, srm6);
        assertTrue(cut.consume(srm6));
        assertEquals("SRM 12", cut.nameProperty().get());
        assertTrue(cut.remove(srm6));
        assertEquals("SRM 6", cut.nameProperty().get());
        assertEquals(6, cut.volleySizeProperty().get(), 0.0);
        assertEquals(0, cut.roundsProperty().get(), 0.0);
        assertFalse(cut.empty());
    }

    @Test
    public void testRemove_WeaponFromAmmoAndAmmoWeapon() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        cut.consume(ac20);

        assertFalse(cut.remove(llas));
        assertEquals(1, cut.volleySizeProperty().get(), 0.0);
        assertEquals(ac20ammo.getNumRounds(), cut.roundsProperty().get(), 0.0);
        assertFalse(cut.empty());
    }

    @Test
    public void testRemove_WeaponFromManyWeapon() {
        final WeaponSummary cut = new WeaponSummary(loadout, llas);
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
    public void testTotalDamage_BallisticNoAmmo() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        assertEquals(0.0, cut.totalDamageProperty().get(), 0.0);
    }

    @Test
    public void testTotalDamage_BallisticWithAmmo() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20);
        cut.consume(ac20ammo);
        cut.consume(ac20ammo);
        cut.consume(ac20);
        assertEquals(ac20ammo.getNumRounds() * 2 * ac20.getDamagePerShot(), cut.totalDamageProperty().get(), 0.0);
    }

    @Test
    public void testTotalDamage_Energy() {
        final WeaponSummary cut = new WeaponSummary(loadout, llas);
        assertTrue(Double.isInfinite(cut.totalDamageProperty().get()));
    }

    @Test
    public void testTotalDamage_OnlyAmmo() {
        final WeaponSummary cut = new WeaponSummary(loadout, ac20ammo);
        assertEquals(0, cut.totalDamageProperty().get(), 0.0);
    }
}

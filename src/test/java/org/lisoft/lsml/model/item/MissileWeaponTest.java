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
package org.lisoft.lsml.model.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.UpgradeDB;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;

@SuppressWarnings("javadoc")
public class MissileWeaponTest {

    private final List<MissileWeapon> allMissileWeapons = ItemDB.lookup(MissileWeapon.class);

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

    /**
     * The mass is affected by Artemis.
     */
    @Test
    public void testGetMass() {
        final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
        final MissileWeapon srm6artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

        assertEquals(srm6.getMass() + 1.0, srm6artemis.getMass(), 0.0);
    }

    @Test
    public void testGetRangeEffectivity_lrm20() throws Exception {
        final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("LRM 20");
        assertEquals(0.0, srm6.getRangeEffectiveness(0, null), 0.0);
        assertEquals(0.0, srm6.getRangeEffectiveness(
                srm6.getRangeMin(null) - Math.ulp(srm6.getRangeLong(null)) * Weapon.RANGE_ULP_FUZZ, null), 0.0);
        assertEquals(1.0, srm6.getRangeEffectiveness(srm6.getRangeMin(null), null), 0.0);
        assertEquals(1.0, srm6.getRangeEffectiveness(srm6.getRangeLong(null), null), 0.0);
        assertEquals(0.0,
                srm6.getRangeEffectiveness(
                        srm6.getRangeLong(null) + Math.ulp(srm6.getRangeLong(null)) * Weapon.RANGE_ULP_FUZZ, null),
                0.0);
        assertEquals(0.0, srm6.getRangeEffectiveness(srm6.getRangeMax(null), null), 0.0);
    }

    @Test
    public void testGetRangeEffectivity_SRM6() throws Exception {
        final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
        assertEquals(1.0, srm6.getRangeEffectiveness(0, null), 0.0);
        assertTrue(srm6.getRangeEffectiveness(srm6.getRangeLong(null), null) < 0.5); // Spread taken into account.
        assertEquals(0.0,
                srm6.getRangeEffectiveness(
                        srm6.getRangeLong(null) + Math.ulp(srm6.getRangeLong(null)) * Weapon.RANGE_ULP_FUZZ, null),
                0.0);
        assertEquals(0.0, srm6.getRangeEffectiveness(srm6.getRangeMax(null), null), 0.0);
    }

    @Test
    public void testGetRangeEffectivity_SRM6_Artemis() throws Exception {
        final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
        final MissileWeapon srm6Artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

        final double withoutArtemis = srm6.getRangeEffectiveness(90.0, null);
        final double withArtemis = srm6Artemis.getRangeEffectiveness(90.0, null);
        assertTrue(withArtemis > withoutArtemis * 1.1);
    }

    /**
     * All missiles have an instant fall off on the max range
     */
    @Test
    public void testGetRangeMax() {
        for (final MissileWeapon weapon : allMissileWeapons) {
            assertTrue(weapon.getRangeMax(null) - weapon.getRangeLong(null) < 0.0001);
        }
    }

    /**
     * All missiles except clan LRM have an instant fall off on the near range.
     */
    @Test
    public void testGetRangeZero() {
        for (final MissileWeapon weapon : allMissileWeapons) {
            if (weapon.getName().contains("LRM") && weapon.getFaction() == Faction.CLAN) {
                continue;
            }
            assertTrue(weapon.getRangeMin(null) - weapon.getRangeZero(null) < 0.0001);
        }
    }

    @Test
    public void testGetSecondsPerShot_clrm20() throws Exception {
        final Weapon cut = (Weapon) ItemDB.lookup("C-LRM 20");
        final double expected = cut.getCoolDown(null) + 19 * 0.05;
        assertEquals(expected, cut.getSecondsPerShot(null), 0.0);
    }

    @Test
    public void testGetSecondsPerShot_csrm6() throws Exception {
        final Weapon cut = (Weapon) ItemDB.lookup("C-SRM 6");
        final double expected = cut.getCoolDown(null);
        assertEquals(expected, cut.getSecondsPerShot(null), 0.0);
    }

    @Test
    public void testGetSecondsPerShot_cssrm6() throws Exception {
        final Weapon cut = (Weapon) ItemDB.lookup("C-STREAK SRM 6");
        final double expected = cut.getCoolDown(null);
        assertEquals(expected, cut.getSecondsPerShot(null), 0.0);
    }

    @Test
    public void testGetSecondsPerShot_srm6() throws Exception {
        final Weapon cut = (Weapon) ItemDB.lookup("SRM 6");
        final double expected = cut.getCoolDown(null);
        assertEquals(expected, cut.getSecondsPerShot(null), 0.0);
    }

    @Test
    public void testGetShotsPerVolley_lrm10() throws Exception {
        final Weapon lrm10 = (Weapon) ItemDB.lookup("LRM 10");
        assertEquals(10, lrm10.getAmmoPerPerShot());
    }

    /**
     * The number of critical slots is affected by Artemis.
     */
    @Test
    public void testGetSlots() {
        final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
        final MissileWeapon srm6artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

        assertEquals(srm6artemis.getSlots(), srm6.getSlots() + 1);
    }

    @Test
    public void testGetSpread_Artemis() throws Exception {
        final MissileWeapon srm6 = (MissileWeapon) ItemDB.lookup("SRM 6");
        final MissileWeapon srm6Artemis = (MissileWeapon) ItemDB.lookup("SRM 6 + ARTEMIS");

        final GuidanceUpgrade artemis = UpgradeDB.ARTEMIS_IV;

        final double withoutArtemis = srm6.getSpread(null);
        final double withArtemis = srm6Artemis.getSpread(null);
        assertEquals(withArtemis, withoutArtemis * artemis.getSpreadFactor(), 0.0);
    }

    /**
     * Only SRMs and LRMs are Artemis capable
     */
    @Test
    public void testIsArtemisCapable() {
        for (final MissileWeapon weapon : allMissileWeapons) {
            if (weapon.getName().contains("STREAK") || weapon.getName().contains("NARC")) {
                assertFalse(weapon.isArtemisCapable());
            }
            else {
                assertTrue(weapon.isArtemisCapable());
                assertTrue(weapon.isArtemisCapable());
            }
        }
    }

    @Test
    public void testNotArtemisMissiles() {
        final MissileWeapon lrm = (MissileWeapon) ItemDB.lookup("LRM 20");
        assertFalse(lrm.getName().toLowerCase().contains("artemis"));
    }
}

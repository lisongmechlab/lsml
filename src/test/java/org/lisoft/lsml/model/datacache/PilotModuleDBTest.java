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
package org.lisoft.lsml.model.datacache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * A test suite for {@link PilotModuleDB}. The primary purpose is to test lookup and correct parsing of
 * {@link PilotModule}s.
 *
 * @author Li Song
 */
public class PilotModuleDBTest {

    @Test
    public void testIssue502() {
        final WeaponModule erllasCd = (WeaponModule) PilotModuleDB.lookup("ERL LASER COOLDOWN 5");
        final WeaponModule erllasRange = (WeaponModule) PilotModuleDB.lookup("ERL-LASER RANGE 5");
        final WeaponModule llasCd = (WeaponModule) PilotModuleDB.lookup("L. LASER COOLDOWN 5");
        final WeaponModule llasRange = (WeaponModule) PilotModuleDB.lookup("LARGE LASER RANGE 5");
        final WeaponModule lplasCd = (WeaponModule) PilotModuleDB.lookup("LP LASER COOLDOWN 5");
        final WeaponModule lplasRange = (WeaponModule) PilotModuleDB.lookup("LP-LASER RANGE 5");

        final EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        final EnergyWeapon llas = (EnergyWeapon) ItemDB.lookup("LARGE LASER");
        final EnergyWeapon lplas = (EnergyWeapon) ItemDB.lookup("LRG PULSE LASER");

        assertTrue(erllasCd.affectsWeapon(erllas));
        assertFalse(erllasCd.affectsWeapon(llas));
        assertFalse(erllasCd.affectsWeapon(lplas));
        assertTrue(erllasRange.affectsWeapon(erllas));
        assertFalse(erllasRange.affectsWeapon(llas));
        assertFalse(erllasRange.affectsWeapon(lplas));

        assertFalse(llasCd.affectsWeapon(erllas));
        assertTrue(llasCd.affectsWeapon(llas));
        assertFalse(llasCd.affectsWeapon(lplas));
        assertFalse(llasRange.affectsWeapon(erllas));
        assertTrue(llasRange.affectsWeapon(llas));
        assertFalse(llasRange.affectsWeapon(lplas));

        assertFalse(lplasCd.affectsWeapon(erllas));
        assertFalse(lplasCd.affectsWeapon(llas));
        assertTrue(lplasCd.affectsWeapon(lplas));
        assertFalse(lplasRange.affectsWeapon(erllas));
        assertFalse(lplasRange.affectsWeapon(llas));
        assertTrue(lplasRange.affectsWeapon(lplas));

        assertEquals(1.1, erllas.getRangeLong(erllasRange.getModifiers()) / erllas.getRangeLong(null), 0.0);
        assertEquals(1.0, erllas.getRangeLong(llasRange.getModifiers()) / erllas.getRangeLong(null), 0.0);
        assertEquals(1.0, erllas.getRangeLong(lplasRange.getModifiers()) / erllas.getRangeLong(null), 0.0);

        assertEquals(1.0, llas.getRangeLong(erllasRange.getModifiers()) / llas.getRangeLong(null), 0.0);
        assertEquals(1.1, llas.getRangeLong(llasRange.getModifiers()) / llas.getRangeLong(null), 0.0);
        assertEquals(1.0, llas.getRangeLong(lplasRange.getModifiers()) / llas.getRangeLong(null), 0.0);

        assertEquals(1.0, lplas.getRangeLong(erllasRange.getModifiers()) / lplas.getRangeLong(null), 0.0);
        assertEquals(1.0, lplas.getRangeLong(llasRange.getModifiers()) / lplas.getRangeLong(null), 0.0);
        assertEquals(1.1, lplas.getRangeLong(lplasRange.getModifiers()) / lplas.getRangeLong(null), 0.0);
    }

    @Test
    public void testIssue531() {
        final WeaponModule enhancedNarc = (WeaponModule) PilotModuleDB.lookup(4043); // Enhanced NARC (Clan + IS)
        final WeaponModule enhancedNarcLtd = (WeaponModule) PilotModuleDB.lookup(4048); // Enhanced NARC LTD (Clan Only)
        final MissileWeapon narc = (MissileWeapon) ItemDB.lookup("NARC");
        final MissileWeapon cnarc = (MissileWeapon) ItemDB.lookup("C-NARC");

        assertTrue(enhancedNarc.affectsWeapon(narc));
        assertTrue(enhancedNarc.affectsWeapon(cnarc));
        assertFalse(enhancedNarcLtd.affectsWeapon(narc));
        assertTrue(enhancedNarcLtd.affectsWeapon(cnarc));
    }

    @Test
    public void testIssue531_AMS() {
        final WeaponModule amsOverload = (WeaponModule) PilotModuleDB.lookup(4039); // AMS OVERLOAD (Clan + IS)
        final WeaponModule amsOverloadLtd = (WeaponModule) PilotModuleDB.lookup(4044); // AMS OVERLOAD LTD (Clan Only)
        final AmmoWeapon ams = (AmmoWeapon) ItemDB.lookup("AMS");
        final AmmoWeapon cams = (AmmoWeapon) ItemDB.lookup("C-AMS");

        assertTrue(amsOverload.affectsWeapon(ams));
        assertTrue(amsOverload.affectsWeapon(cams));
        assertTrue(amsOverloadLtd.affectsWeapon(ams)); // Changed to true in data files
        assertTrue(amsOverloadLtd.affectsWeapon(cams));
    }

    @Test
    public void testLookup_ByID() {
        final WeaponModule module = (WeaponModule) PilotModuleDB.lookup(4234);

        final MissileWeapon srm2 = (MissileWeapon) ItemDB.lookup("SRM2");
        final MissileWeapon srm2artemis = (MissileWeapon) ItemDB.lookup("SRM2_Artemis");

        assertTrue(module.affectsWeapon(srm2));
        assertTrue(module.affectsWeapon(srm2artemis));

        for (final Modifier modifier : module.getModifiers()) {
            assertTrue(modifier.getValue() > 0.0);
        }
    }
}

/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * A test suite for {@link PilotModuleDB}. The primary purpose is to test lookup and correct parsing of
 * {@link PilotModule}s.
 * 
 * @author Emily Björk
 */
public class PilotModuleDBTest {

    @Test
    public void testLookup_ByID() {
        WeaponModule module = (WeaponModule) PilotModuleDB.lookup(4234);

        MissileWeapon srm2 = (MissileWeapon) ItemDB.lookup("SRM2");
        MissileWeapon srm2artemis = (MissileWeapon) ItemDB.lookup("SRM2_Artemis");

        assertTrue(module.affectsWeapon(srm2));
        assertTrue(module.affectsWeapon(srm2artemis));

        for (Modifier modifier : module.getModifiers()) {
            assertTrue(modifier.getValue() > 0.0);
        }
    }

    @Test
    public void testIssue502() {
        WeaponModule erllasCd = (WeaponModule) PilotModuleDB.lookup("ERL LASER COOLDOWN 5");
        WeaponModule erllasRange = (WeaponModule) PilotModuleDB.lookup("ERL-LASER RANGE 5");
        WeaponModule llasCd = (WeaponModule) PilotModuleDB.lookup("L. LASER COOLDOWN 5");
        WeaponModule llasRange = (WeaponModule) PilotModuleDB.lookup("LARGE LASER RANGE 5");
        WeaponModule lplasCd = (WeaponModule) PilotModuleDB.lookup("LP LASER COOLDOWN 5");
        WeaponModule lplasRange = (WeaponModule) PilotModuleDB.lookup("LP-LASER RANGE 5");

        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        EnergyWeapon llas = (EnergyWeapon) ItemDB.lookup("LARGE LASER");
        EnergyWeapon lplas = (EnergyWeapon) ItemDB.lookup("LRG PULSE LASER");

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
}

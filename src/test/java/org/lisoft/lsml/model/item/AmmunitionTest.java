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

import org.junit.Test;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.TestHelpers;

import static org.junit.Assert.*;

/**
 * A test suite for {@link Ammunition}.
 *
 * @author Li Song
 */
public class AmmunitionTest {

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

    @Test
    public void testAmmoCapacityQuirk() throws Exception {
        final Ammunition cut = (Ammunition) ItemDB.lookup("C-SRM AMMO");
        Loadout acw_p = TestHelpers.parse("http://t.li-soft.org/?l=rwJvFSUDKBIsBCUDKBUKlIH30%2B%2BH38%2B8B96Pvx95Mw%3D%3D");

        int baseRounds = cut.getNumRounds(null);
        int actualRounds = cut.getNumRounds(acw_p.getQuirks());

        assertNotEquals(baseRounds, actualRounds);
    }

}

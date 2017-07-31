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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;

/**
 * A test suite for {@link Ammunition}.
 *
 * @author Li Song
 */
public class AmmunitionTest {

    @Test
    public void testBug693() throws Exception {
        final Ammunition cut = (Ammunition) ItemDB.lookup("C-ATM AMMO");

        assertEquals(HardPointType.MISSILE, cut.getWeaponHardpointType());
    }

    @Test
    public void testHalfTonAmmo() throws Exception {
        final Ammunition cut = (Ammunition) ItemDB.lookup(2233);

        assertEquals(0.5, cut.getMass(), 0.0);
        assertEquals(1, cut.getSlots());
        assertTrue(cut.getHealth() > 0.0);
    }

}

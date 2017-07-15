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
package org.lisoft.lsml.model.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.lisoft.lsml.model.database.UpgradeDB;

/**
 * Test suite for {@link ArmourUpgrade}.
 *
 * @author Emily Björk
 */
public class ArmourUpgradeTest {
    /**
     * Test properties of Ferro-Fibrous armour
     */
    @Test
    public void testFerroFibrousArmour() throws Exception {
        final int ff_id = 2811;
        final ArmourUpgrade cut = (ArmourUpgrade) UpgradeDB.lookup(ff_id);

        assertNotNull(cut);
        assertEquals(ff_id, cut.getMwoId());
        assertEquals("FERRO FIBROUS ARMOUR", cut.getName());
        assertFalse(cut.getDescription().equals(""));
        assertEquals(14, cut.getExtraSlots());
        assertEquals(35.84, cut.getArmourPerTon(), 0.0);
        assertEquals(64.0 / 35.84, cut.getArmourMass(64), 0.0);
    }

    /**
     * Test properties of standard armour
     */
    @Test
    public void testStandardArmour() throws Exception {
        final int sa_id = 2810;
        final ArmourUpgrade cut = (ArmourUpgrade) UpgradeDB.lookup(sa_id);

        assertNotNull(cut);
        assertEquals(sa_id, cut.getMwoId());
        assertEquals("STANDARD ARMOUR", cut.getName());
        assertFalse(cut.getDescription().equals(""));
        assertEquals(0, cut.getExtraSlots());
        assertEquals(32.0, cut.getArmourPerTon(), 0.0);
        assertEquals(2.0, cut.getArmourMass(64), 0.0);
    }
}

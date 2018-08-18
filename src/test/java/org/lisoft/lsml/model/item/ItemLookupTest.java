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
package org.lisoft.lsml.model.item;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.Faction;

/**
 * Test suite for {@link ItemDB}.
 *
 * @author Emily Björk
 */
@SuppressWarnings("javadoc")
public class ItemLookupTest {

    /**
     * We have to be able to find items by MWO ID/MWO Key and Name.
     */
    @Test
    public void testLookup() throws Exception {
        // Setup
        final String name = "STD ENGINE 105";
        final Item expected = ItemDB.lookup(name);

        // Lookup by name
        assertNotNull(ItemDB.lookup(name));

        // Lookup by MWO ID
        assertSame(expected, ItemDB.lookup(3219));

        // Lookup by MWO name key
        assertSame(expected, ItemDB.lookup("Engine_Std_105"));

        // Lookup by MWO name key (ducked up case)
        assertSame(expected, ItemDB.lookup("EnGine_stD_105"));
    }

    @Test
    public void testGetEngine() throws Exception {
        // Setup
        final String name = "LIGHT ENGINE 105";
        final Item expected = ItemDB.lookup(name);

        // Lookup by name
        assertNotNull(ItemDB.lookup(name));

        // Lookup by ItemDb.getEngine().
        assertSame(expected, ItemDB.getEngine(105, EngineType.LE, Faction.INNERSPHERE));
    }

    @Test
    public void testLookupClass() {
        final Collection<EnergyWeapon> eweaps = ItemDB.lookup(EnergyWeapon.class);

        final Collection<Item> items = ItemDB.lookup(Item.class); // Should be all items

        assertTrue(items.containsAll(eweaps));

        for (final Item item : items) {
            if (item instanceof EnergyWeapon) {
                assertTrue(eweaps.contains(item));
            }
        }
    }

    @Test(expected = NoSuchItemException.class)
    public void testLookupFailBadName() throws Exception {
        ItemDB.lookup("HumbungaDingDong!");
    }

    @Test(expected = NoSuchItemException.class)
    public void testLookupFailNegativeId() throws Exception {
        ItemDB.lookup(-1);
    }

    @Test(expected = NoSuchItemException.class)
    public void testLookupFailNonExistentId() throws Exception {
        ItemDB.lookup(98751823);
    }
}

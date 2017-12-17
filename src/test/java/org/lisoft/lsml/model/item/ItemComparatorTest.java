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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;

public class ItemComparatorTest {

    @Test
    public void testByType() throws Exception {
        // Setup
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("ER PPC"));
        items.add(ItemDB.lookup("AC/20"));
        items.add(ItemDB.lookup("AC/20 AMMO"));
        items.add(ItemDB.lookup("LRM 20"));
        items.add(ItemDB.lookup("LRM AMMO"));
        items.add(ItemDB.lookup("AMS"));
        items.add(ItemDB.lookup("AMS AMMO"));
        items.add(ItemDB.lookup("GUARDIAN ECM"));
        items.add(ItemDB.lookup("COMMAND CONSOLE"));
        items.add(ItemDB.lookup("DOUBLE HEAT SINK"));
        items.add(ItemDB.lookup("JUMP JETS - CLASS V"));
        items.add(ItemDB.lookup("C.A.S.E."));
        items.add(ItemDB.lookup("STD ENGINE 300"));
        final ArrayList<Item> expected = new ArrayList<>(items);
        Collections.reverse(items);

        // Execute
        items.sort(new ItemComparator(false));

        // Verify
        assertEquals(expected.toString(), items.toString());
    }

    @Test
    public void testByTypePgi() throws Exception {
        // Setup
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("ER PPC"));
        items.add(ItemDB.lookup("AC/20"));
        items.add(ItemDB.lookup("AC/20 AMMO"));
        items.add(ItemDB.lookup("LRM 20"));
        items.add(ItemDB.lookup("LRM AMMO"));
        items.add(ItemDB.lookup("AMS"));
        items.add(ItemDB.lookup("AMS AMMO"));
        items.add(ItemDB.lookup("GUARDIAN ECM"));
        items.add(ItemDB.lookup("COMMAND CONSOLE"));
        items.add(ItemDB.lookup("DOUBLE HEAT SINK"));
        items.add(ItemDB.lookup("JUMP JETS - CLASS V"));
        items.add(ItemDB.lookup("C.A.S.E."));
        items.add(ItemDB.lookup("STD ENGINE 300"));
        final ArrayList<Item> expected = new ArrayList<>(items);
        Collections.reverse(items);

        // Execute
        items.sort(new ItemComparator(true));

        // Verify
        assertEquals(expected.toString(), items.toString());
    }

    @Test
    public void testCompareAmmoWeapon() throws Exception {
        final ItemComparator cut = new ItemComparator(false);
        assertTrue(cut.compare(ItemDB.lookup("AC/20 AMMO"), ItemDB.lookup("LRM 20")) < 0);
        assertTrue(cut.compare(ItemDB.lookup("AC/20"), ItemDB.lookup("AC/20 AMMO")) < 0);
        assertTrue(cut.compare(ItemDB.lookup("AC/20 AMMO"), ItemDB.lookup("C-AC/2")) < 0);
        assertTrue(cut.compare(ItemDB.lookup("C-ULTRA AC/20"), ItemDB.lookup("C-U-AC/20 AMMO")) < 0);
        assertTrue(cut.compare(ItemDB.lookup("LRM AMMO"), ItemDB.lookup("C-S-SRM AMMO")) < 0);
    }

    @Test
    public void testCompareAMS() throws Exception {
        final ItemComparator cut = new ItemComparator(false);
        assertTrue(cut.compare(ItemDB.lookup("C-LAS AMS"), ItemDB.lookup("LAS AMS")) < 0);
        assertTrue(cut.compare(ItemDB.lookup("LAS AMS"), ItemDB.lookup("C-AMS")) < 0);
        assertTrue(cut.compare(ItemDB.lookup("C-AMS"), ItemDB.lookup("AMS")) < 0);
    }

    @Test
    public void testCompareArtemis() throws Exception {
        final ItemComparator cut = new ItemComparator(false);
        assertTrue(cut.compare(ItemDB.lookup("SRM 6"), ItemDB.lookup("SRM 6 + ARTEMIS")) < 0);
    }

    @Test
    public void testCompareEngines() throws Exception {
        final ItemComparator cut = new ItemComparator(false);
        assertTrue(cut.compare(ItemDB.lookup("STD ENGINE 300"), ItemDB.lookup("XL ENGINE 200")) < 0);
    }

    @Test
    public void testCompareMissiles() throws Exception {
        final ItemComparator cut = new ItemComparator(false);
        assertTrue(cut.compare(ItemDB.lookup("C-SRM 6"), ItemDB.lookup("SRM 6")) < 0);
    }

    @Test
    public void testEngines() throws Exception {
        // Setup
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("CLAN XL ENGINE 300"));
        items.add(ItemDB.lookup("XL ENGINE 300"));
        items.add(ItemDB.lookup("LIGHT ENGINE 300"));
        items.add(ItemDB.lookup("STD ENGINE 300"));
        items.add(ItemDB.lookup("CLAN XL ENGINE 200"));
        items.add(ItemDB.lookup("XL ENGINE 200"));
        items.add(ItemDB.lookup("LIGHT ENGINE 200"));
        items.add(ItemDB.lookup("STD ENGINE 200"));
        final ArrayList<Item> expected = new ArrayList<>(items);
        Collections.reverse(items);

        // Execute
        items.sort(new ItemComparator(false));

        // Verify
        assertEquals(expected.toString(), items.toString());
    }

    @Test
    public void testEnginesPgi() throws Exception {
        // Setup
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("CLAN XL ENGINE 200"));
        items.add(ItemDB.lookup("XL ENGINE 200"));
        items.add(ItemDB.lookup("LIGHT ENGINE 200"));
        items.add(ItemDB.lookup("STD ENGINE 200"));
        items.add(ItemDB.lookup("CLAN XL ENGINE 300"));
        items.add(ItemDB.lookup("XL ENGINE 300"));
        items.add(ItemDB.lookup("LIGHT ENGINE 300"));
        items.add(ItemDB.lookup("STD ENGINE 300"));
        final ArrayList<Item> expected = new ArrayList<>(items);
        Collections.reverse(items);

        // Execute
        items.sort(new ItemComparator(true));

        // Verify
        assertEquals(expected.toString(), items.toString());
    }

    @Test
    public void testJumpJets() {
        // Setup
        final List<JumpJet> jumpJets = ItemDB.lookup(JumpJet.class);

        // Execute
        jumpJets.sort(new ItemComparator(false));

        // Verify
        for (int i = 1; i < jumpJets.size(); i++) {
            final double prevMinTons = jumpJets.get(i - 1).getMinTons();
            final double currMinTons = jumpJets.get(i).getMinTons();
            assertTrue("Min tons of previous: " + prevMinTons + " min tons of current: " + currMinTons,
                    prevMinTons <= currMinTons);
        }
    }

    @Test
    public void testJumpJetsPgi() {
        // Setup
        final List<JumpJet> jumpJets = ItemDB.lookup(JumpJet.class);

        // Execute
        jumpJets.sort(new ItemComparator(true));

        // Verify
        for (int i = 1; i < jumpJets.size(); i++) {
            final double prevMinTons = jumpJets.get(i - 1).getMinTons();
            final double currMinTons = jumpJets.get(i).getMinTons();
            assertTrue("Min tons of previous: " + prevMinTons + " min tons of current: " + currMinTons,
                    prevMinTons <= currMinTons);
        }
    }

    @Test
    public void testSortBallistics() throws Exception {
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("AC/20"));
        items.add(ItemDB.lookup("AC/20 AMMO"));
        items.add(ItemDB.lookup("AC/20 AMMO (1/2)"));

        items.add(ItemDB.lookup("C-AC/2"));
        items.add(ItemDB.lookup("C-AC/2 AMMO"));
        items.add(ItemDB.lookup("C-AC/2 AMMO (1/2)"));

        items.add(ItemDB.lookup("C-ULTRA AC/20"));
        items.add(ItemDB.lookup("C-U-AC/20 AMMO"));
        items.add(ItemDB.lookup("C-U-AC/20 AMMO (1/2)"));
        final ArrayList<Item> expected = new ArrayList<>(items);
        Collections.shuffle(items, new Random(0));

        items.sort(new ItemComparator(false));
        assertEquals(expected.toString(), items.toString());

        // PGI and LSML has same sort order
        items.sort(new ItemComparator(true));
        assertEquals(expected.toString(), items.toString());
    }

    @Test
    public void testSortMissiles() throws Exception {
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("LRM 20"));
        items.add(ItemDB.lookup("LRM AMMO"));
        items.add(ItemDB.lookup("LRM AMMO (1/2)"));

        items.add(ItemDB.lookup("C-SRM 6"));
        items.add(ItemDB.lookup("C-SRM 4"));
        items.add(ItemDB.lookup("C-SRM AMMO"));
        items.add(ItemDB.lookup("C-SRM AMMO (1/2)"));

        items.add(ItemDB.lookup("C-STREAK SRM 6"));
        items.add(ItemDB.lookup("C-STREAK SRM 4"));
        items.add(ItemDB.lookup("C-S-SRM AMMO"));
        items.add(ItemDB.lookup("C-S-SRM AMMO (1/2)"));
        final ArrayList<Item> expected = new ArrayList<>(items);
        Collections.shuffle(items, new Random(0));

        items.sort(new ItemComparator(false));
        assertEquals(expected.toString(), items.toString());

        items.sort(new ItemComparator(true)); // Same order for PGI and LSML sort
        assertEquals(expected.toString(), items.toString());
    }

    @Test
    public void testSortWeapons() throws Exception {
        // Setup
        final List<Item> weapons = new ArrayList<>();
        weapons.add(ItemDB.lookup("C-ER PPC"));
        weapons.add(ItemDB.lookup("ER PPC"));
        weapons.add(ItemDB.lookup("PPC"));
        weapons.add(ItemDB.lookup("C-LRG PULSE LASER"));
        weapons.add(ItemDB.lookup("LRG PULSE LASER"));
        weapons.add(ItemDB.lookup("C-MED PULSE LASER"));
        weapons.add(ItemDB.lookup("MED PULSE LASER"));
        weapons.add(ItemDB.lookup("C-SML PULSE LASER"));
        weapons.add(ItemDB.lookup("SML PULSE LASER"));
        weapons.add(ItemDB.lookup("C-ER LRG LASER"));
        weapons.add(ItemDB.lookup("ER LARGE LASER"));
        weapons.add(ItemDB.lookup("C-ER MED LASER"));
        weapons.add(ItemDB.lookup("C-ER SML LASER"));
        weapons.add(ItemDB.lookup("LARGE LASER"));
        weapons.add(ItemDB.lookup("MEDIUM LASER"));
        weapons.add(ItemDB.lookup("SMALL LASER"));
        weapons.add(ItemDB.lookup("C-FLAMER"));
        weapons.add(ItemDB.lookup("FLAMER"));
        weapons.add(ItemDB.lookup("C-TAG"));
        weapons.add(ItemDB.lookup("TAG"));

        weapons.add(ItemDB.lookup("C-GAUSS RIFLE"));
        weapons.add(ItemDB.lookup("GAUSS RIFLE"));
        weapons.add(ItemDB.lookup("C-AC/20"));
        weapons.add(ItemDB.lookup("AC/20"));
        weapons.add(ItemDB.lookup("C-AC/10"));
        weapons.add(ItemDB.lookup("AC/10"));
        weapons.add(ItemDB.lookup("C-AC/5"));
        weapons.add(ItemDB.lookup("AC/5"));
        weapons.add(ItemDB.lookup("C-AC/2"));
        weapons.add(ItemDB.lookup("AC/2"));
        weapons.add(ItemDB.lookup("C-ULTRA AC/20"));
        weapons.add(ItemDB.lookup("C-ULTRA AC/10"));
        weapons.add(ItemDB.lookup("C-ULTRA AC/5"));
        weapons.add(ItemDB.lookup("ULTRA AC/5"));
        weapons.add(ItemDB.lookup("C-ULTRA AC/2"));
        weapons.add(ItemDB.lookup("C-LB20-X AC"));
        weapons.add(ItemDB.lookup("C-LB10-X AC"));
        weapons.add(ItemDB.lookup("LB 10-X AC"));
        weapons.add(ItemDB.lookup("C-LB5-X AC"));
        weapons.add(ItemDB.lookup("C-LB2-X AC"));
        weapons.add(ItemDB.lookup("C-MACHINE GUN"));
        weapons.add(ItemDB.lookup("MACHINE GUN"));

        weapons.add(ItemDB.lookup("C-LRM 20"));
        weapons.add(ItemDB.lookup("C-LRM 20 + ARTEMIS"));
        weapons.add(ItemDB.lookup("LRM 20"));
        weapons.add(ItemDB.lookup("LRM 20 + ARTEMIS"));
        weapons.add(ItemDB.lookup("C-LRM 15"));
        weapons.add(ItemDB.lookup("C-LRM 15 + ARTEMIS"));
        weapons.add(ItemDB.lookup("LRM 15"));
        weapons.add(ItemDB.lookup("LRM 15 + ARTEMIS"));
        weapons.add(ItemDB.lookup("C-LRM 10"));
        weapons.add(ItemDB.lookup("C-LRM 10 + ARTEMIS"));
        weapons.add(ItemDB.lookup("LRM 10"));
        weapons.add(ItemDB.lookup("LRM 10 + ARTEMIS"));
        weapons.add(ItemDB.lookup("C-LRM 5"));
        weapons.add(ItemDB.lookup("C-LRM 5 + ARTEMIS"));
        weapons.add(ItemDB.lookup("LRM 5"));
        weapons.add(ItemDB.lookup("LRM 5 + ARTEMIS"));

        weapons.add(ItemDB.lookup("C-SRM 6"));
        weapons.add(ItemDB.lookup("C-SRM 6 + ARTEMIS"));
        weapons.add(ItemDB.lookup("SRM 6"));
        weapons.add(ItemDB.lookup("SRM 6 + ARTEMIS"));
        weapons.add(ItemDB.lookup("C-SRM 4"));
        weapons.add(ItemDB.lookup("C-SRM 4 + ARTEMIS"));
        weapons.add(ItemDB.lookup("SRM 4"));
        weapons.add(ItemDB.lookup("SRM 4 + ARTEMIS"));
        weapons.add(ItemDB.lookup("C-SRM 2"));
        weapons.add(ItemDB.lookup("C-SRM 2 + ARTEMIS"));
        weapons.add(ItemDB.lookup("SRM 2"));
        weapons.add(ItemDB.lookup("SRM 2 + ARTEMIS"));

        weapons.add(ItemDB.lookup("C-STREAK SRM 6"));
        weapons.add(ItemDB.lookup("C-STREAK SRM 4"));
        weapons.add(ItemDB.lookup("C-STREAK SRM 2"));
        weapons.add(ItemDB.lookup("STREAK SRM 2"));
        weapons.add(ItemDB.lookup("C-NARC"));
        weapons.add(ItemDB.lookup("NARC"));

        weapons.add(ItemDB.lookup("C-AMS"));
        weapons.add(ItemDB.lookup("AMS"));

        final List<Item> expected = new ArrayList<>(weapons);

        // Execute
        weapons.sort(new ItemComparator(false));

        // Verify
        assertEquals(expected.toString(), weapons.toString());

        // PGI and LSML sort has same order for weapons.
        weapons.sort(new ItemComparator(true));
        assertEquals(expected.toString(), weapons.toString());
    }

    @Test
    public void testSortWeaponsByRange() throws Exception {
        final List<Weapon> items = new ArrayList<>();
        items.add((Weapon) ItemDB.lookup("AC/20"));
        items.add((Weapon) ItemDB.lookup("LARGE LASER"));
        items.add((Weapon) ItemDB.lookup("C-AC/2"));
        items.add((Weapon) ItemDB.lookup("C-AC/2"));

        items.sort(ItemComparator.byRange(null));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LARGE LASER"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20"));
    }
}

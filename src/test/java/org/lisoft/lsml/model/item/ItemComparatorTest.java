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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.database.ItemDB;

public class ItemComparatorTest {

    private static final String RUBBISH = "foobAR";

    @Test
    public void testByType() {
        // Setup
        final Internal internal1 = new Internal("afoo", "bar", "int", 3, 0, 0, HardPointType.NONE, 0, Faction.ANY);
        final Internal internal2 = new Internal("bfoo", "bar", "int", 3, 0, 0, HardPointType.NONE, 0, Faction.ANY);
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
        items.add(ItemDB.lookup("STD ENGINE 300"));
        items.add(ItemDB.lookup("C.A.S.E."));
        items.add(internal1);
        items.add(internal2);
        Collections.shuffle(items, new Random(0));

        // Execute
        items.sort(ItemComparator.NATURAL_LSML);

        // Verify
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("ER PPC"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM 20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AMS"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AMS AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("GUARDIAN ECM"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("COMMAND CONSOLE"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("DOUBLE HEAT SINK"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C.A.S.E."));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("JUMP JETS - CLASS V"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("STD ENGINE 300"));
        assertSame(items.toString(), items.remove(0), internal1);
        assertSame(items.toString(), items.remove(0), internal2);
    }

    @Test
    public void testByTypePgi() {
        // Setup
        final Internal internal1 = new Internal("afoo", "bar", "int", 3, 0, 0, HardPointType.NONE, 0, Faction.ANY);
        final Internal internal2 = new Internal("bfoo", "bar", "int", 3, 0, 0, HardPointType.NONE, 0, Faction.ANY);
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
        items.add(ItemDB.lookup("STD ENGINE 300"));
        items.add(ItemDB.lookup("C.A.S.E."));
        items.add(internal1);
        items.add(internal2);
        Collections.shuffle(items, new Random(0));

        // Execute
        items.sort(ItemComparator.NATURAL_PGI);

        // Verify
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("ER PPC"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM 20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AMS"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AMS AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("GUARDIAN ECM"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("COMMAND CONSOLE"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("DOUBLE HEAT SINK"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C.A.S.E."));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("JUMP JETS - CLASS V"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("STD ENGINE 300"));
        assertSame(items.toString(), items.remove(0), internal1);
        assertSame(items.toString(), items.remove(0), internal2);
    }

    @Test
    public void testEngines() {
        // Setup
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("STD ENGINE 300"));
        items.add(ItemDB.lookup("STD ENGINE 200"));
        items.add(ItemDB.lookup("XL ENGINE 300"));
        items.add(ItemDB.lookup("XL ENGINE 200"));
        items.add(ItemDB.lookup("CLAN XL ENGINE 300"));
        items.add(ItemDB.lookup("CLAN XL ENGINE 200"));
        Collections.shuffle(items, new Random(0));

        // Execute
        items.sort(ItemComparator.NATURAL_LSML);

        // Verify
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("CLAN XL ENGINE 300"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("XL ENGINE 300"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("STD ENGINE 300"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("CLAN XL ENGINE 200"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("XL ENGINE 200"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("STD ENGINE 200"));
    }

    @Test
    public void testEnginesPgi() {
        // Setup
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("STD ENGINE 300"));
        items.add(ItemDB.lookup("STD ENGINE 200"));
        items.add(ItemDB.lookup("XL ENGINE 300"));
        items.add(ItemDB.lookup("XL ENGINE 200"));
        items.add(ItemDB.lookup("CLAN XL ENGINE 300"));
        items.add(ItemDB.lookup("CLAN XL ENGINE 200"));
        Collections.shuffle(items, new Random(0));

        // Execute
        items.sort(ItemComparator.NATURAL_PGI);

        // Verify
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("CLAN XL ENGINE 200"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("XL ENGINE 200"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("STD ENGINE 200"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("CLAN XL ENGINE 300"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("XL ENGINE 300"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("STD ENGINE 300"));
    }

    @Test
    public void testJumpJets() {
        // Setup
        final List<JumpJet> jumpJets = ItemDB.lookup(JumpJet.class);

        // Execute
        jumpJets.sort(ItemComparator.NATURAL_LSML);

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
        jumpJets.sort(ItemComparator.NATURAL_PGI);

        // Verify
        for (int i = 1; i < jumpJets.size(); i++) {
            final double prevMinTons = jumpJets.get(i - 1).getMinTons();
            final double currMinTons = jumpJets.get(i).getMinTons();
            assertTrue("Min tons of previous: " + prevMinTons + " min tons of current: " + currMinTons,
                    prevMinTons <= currMinTons);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLaserSizeRubbish() {
        ItemComparator.LaserSize.identify(RUBBISH);
    }

    @Test
    public void testSortAmmoAndWeapons() {
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("C-AC/2"));
        items.add(ItemDB.lookup("C-AC/2 AMMO (1/2)"));
        items.add(ItemDB.lookup("C-AC/2 AMMO"));

        items.add(ItemDB.lookup("AC/20"));
        items.add(ItemDB.lookup("AC/20 AMMO (1/2)"));
        items.add(ItemDB.lookup("AC/20 AMMO"));

        items.add(ItemDB.lookup("C-ULTRA AC/20"));
        items.add(ItemDB.lookup("C-U-AC/20 AMMO (1/2)"));
        items.add(ItemDB.lookup("C-U-AC/20 AMMO"));
        Collections.shuffle(items, new Random(0));

        items.sort(ItemComparator.NATURAL_LSML);

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20 AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2 AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-ULTRA AC/20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-U-AC/20 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-U-AC/20 AMMO (1/2)"));
    }

    @Test
    public void testSortAmmoAndWeaponsMissile() {
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("C-SRM 6"));
        items.add(ItemDB.lookup("C-SRM 4"));
        items.add(ItemDB.lookup("C-SRM AMMO (1/2)"));
        items.add(ItemDB.lookup("C-SRM AMMO"));

        items.add(ItemDB.lookup("LRM 20"));
        items.add(ItemDB.lookup("LRM AMMO (1/2)"));
        items.add(ItemDB.lookup("LRM AMMO"));

        items.add(ItemDB.lookup("C-STREAK SRM 6"));
        items.add(ItemDB.lookup("C-STREAK SRM 4"));
        items.add(ItemDB.lookup("C-S-SRM AMMO"));
        items.add(ItemDB.lookup("C-S-SRM AMMO (1/2)"));
        Collections.shuffle(items, new Random(0));

        items.sort(ItemComparator.NATURAL_LSML);

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM 20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM 6"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM 4"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-STREAK SRM 6"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-STREAK SRM 4"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-S-SRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-S-SRM AMMO (1/2)"));
    }

    @Test
    public void testSortAmmoAndWeaponsMissilePgi() {
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("C-SRM 6"));
        items.add(ItemDB.lookup("C-SRM 4"));
        items.add(ItemDB.lookup("C-SRM AMMO (1/2)"));
        items.add(ItemDB.lookup("C-SRM AMMO"));

        items.add(ItemDB.lookup("LRM 20"));
        items.add(ItemDB.lookup("LRM AMMO (1/2)"));
        items.add(ItemDB.lookup("LRM AMMO"));

        items.add(ItemDB.lookup("C-STREAK SRM 6"));
        items.add(ItemDB.lookup("C-STREAK SRM 4"));
        items.add(ItemDB.lookup("C-S-SRM AMMO"));
        items.add(ItemDB.lookup("C-S-SRM AMMO (1/2)"));
        Collections.shuffle(items, new Random(0));

        items.sort(ItemComparator.NATURAL_PGI);

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM 20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("LRM AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM 6"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM 4"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-SRM AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-STREAK SRM 6"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-STREAK SRM 4"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-S-SRM AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-S-SRM AMMO (1/2)"));
    }

    @Test
    public void testSortAmmoAndWeaponsPgi() {
        final List<Item> items = new ArrayList<>();
        items.add(ItemDB.lookup("C-AC/2"));
        items.add(ItemDB.lookup("C-AC/2 AMMO (1/2)"));
        items.add(ItemDB.lookup("C-AC/2 AMMO"));

        items.add(ItemDB.lookup("AC/20"));
        items.add(ItemDB.lookup("AC/20 AMMO (1/2)"));
        items.add(ItemDB.lookup("AC/20 AMMO"));

        items.add(ItemDB.lookup("C-ULTRA AC/20"));
        items.add(ItemDB.lookup("C-U-AC/20 AMMO (1/2)"));
        items.add(ItemDB.lookup("C-U-AC/20 AMMO"));
        Collections.shuffle(items, new Random(0));

        items.sort(ItemComparator.NATURAL_PGI);

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("AC/20 AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-AC/2 AMMO (1/2)"));

        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-ULTRA AC/20"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-U-AC/20 AMMO"));
        assertSame(items.toString(), items.remove(0), ItemDB.lookup("C-U-AC/20 AMMO (1/2)"));
    }

    @Test
    public void testSortWeapons() {
        // Setup
        final List<Weapon> weapons = ItemDB.lookup(Weapon.class);

        // Execute
        weapons.sort(ItemComparator.NATURAL_LSML);

        // Verify
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER PPC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("ER PPC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("PPC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRG PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRG PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-MED PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("MED PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SML PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SML PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER LRG LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("ER LARGE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER MED LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER SML LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LARGE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("MEDIUM LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SMALL LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-FLAMER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("FLAMER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-TAG"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("TAG"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-GAUSS RIFLE"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("GAUSS RIFLE"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("ULTRA AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB20-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB10-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LB 10-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB5-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB2-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-MACHINE GUN"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("MACHINE GUN"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 20 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 20 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 15"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 15 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 15"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 15 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 10 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 10 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 5 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 5 + ARTEMIS"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 6"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 6 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 6"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 6 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 4"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 4 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 4"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 4 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 2 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 2 + ARTEMIS"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-STREAK SRM 6"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-STREAK SRM 4"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-STREAK SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("STREAK SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-NARC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("NARC"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AMS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AMS"));
    }

    @Test
    public void testSortWeaponsByRange() {
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

    @Test
    public void testSortWeaponsPgi() {
        // Setup
        final List<Weapon> weapons = ItemDB.lookup(Weapon.class);

        // Execute
        weapons.sort(ItemComparator.NATURAL_PGI);

        // Verify
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER PPC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("ER PPC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("PPC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRG PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRG PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-MED PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("MED PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SML PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SML PULSE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER LRG LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("ER LARGE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER MED LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ER SML LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LARGE LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("MEDIUM LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SMALL LASER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-FLAMER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("FLAMER"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-TAG"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("TAG"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-GAUSS RIFLE"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("GAUSS RIFLE"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AC/2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AC/2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("ULTRA AC/5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-ULTRA AC/2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB20-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB10-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LB 10-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB5-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LB2-X AC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-MACHINE GUN"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("MACHINE GUN"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 20 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 20"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 20 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 15"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 15 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 15"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 15 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 10 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 10"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 10 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-LRM 5 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 5"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("LRM 5 + ARTEMIS"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 6"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 6 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 6"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 6 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 4"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 4 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 4"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 4 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-SRM 2 + ARTEMIS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("SRM 2 + ARTEMIS"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-STREAK SRM 6"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-STREAK SRM 4"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-STREAK SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("STREAK SRM 2"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-NARC"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("NARC"));

        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("C-AMS"));
        assertSame(weapons.toString(), weapons.remove(0), ItemDB.lookup("AMS"));
    }
}

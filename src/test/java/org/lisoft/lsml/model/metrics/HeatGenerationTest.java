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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSource;
import org.lisoft.lsml.model.item.Weapon;
import org.mockito.Mockito;

/**
 * Test suite for {@link HeatGeneration}.
 *
 * @author Li Song
 */
@SuppressWarnings("javadoc")
public class HeatGenerationTest {
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();

    /**
     * Heat generation shall include heat per second from all weapons as well as the base heat from the engine. But no
     * heat from the jump jets.
     */
    @Test
    public void testCalculate() throws Exception {
        // Setup
        final List<HeatSource> items = new ArrayList<>();
        final Weapon ppc = (Weapon) ItemDB.lookup("PPC");
        final Weapon ll = (Weapon) ItemDB.lookup("LARGE LASER");
        final Weapon lrm20 = (Weapon) ItemDB.lookup("LRM 20");
        final Weapon lb10x = (Weapon) ItemDB.lookup("LB 10-X AC");
        final Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
        // JumpJet jj = (JumpJet) ItemDB.lookup("JUMP JETS - CLASS V");
        items.add(ppc);
        items.add(ll);
        items.add(lrm20);
        items.add(lb10x);
        items.add(engine);
        // items.add(jj); // XXX: Should jump jets be included?
        when(mlc.loadout.items(HeatSource.class)).thenReturn(items);

        // Execute
        final HeatGeneration cut = new HeatGeneration(mlc.loadout);

        // Verify
        final double expected = ppc.getStat("h/s", null) + ll.getStat("h/s", null) + lrm20.getStat("h/s", null)
                + lb10x.getStat("h/s", null) + engine.getHeat(null);
        assertEquals(expected, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculate_WeaponGroups() throws Exception {
        // Setup

        final List<HeatSource> allItems = new ArrayList<>();
        final Weapon ppc = (Weapon) ItemDB.lookup("PPC");
        final Weapon ll = (Weapon) ItemDB.lookup("LARGE LASER");
        final Weapon lrm20 = (Weapon) ItemDB.lookup("LRM 20");
        final Weapon lb10x = (Weapon) ItemDB.lookup("LB 10-X AC");
        final Engine engine = (Engine) ItemDB.lookup("STD ENGINE 300");
        allItems.add(ppc);
        allItems.add(ll);
        allItems.add(lrm20);
        allItems.add(lb10x);
        allItems.add(engine);
        when(mlc.loadout.items(HeatSource.class)).thenReturn(allItems);

        final int group1 = 3;
        final Collection<Weapon> weaponsGroup1 = new ArrayList<>();
        weaponsGroup1.add(ppc);
        weaponsGroup1.add(ll);
        Mockito.when(mlc.weaponGroups.getWeapons(group1, mlc.loadout)).thenReturn(weaponsGroup1);

        final int group2 = 2;
        final Collection<Weapon> weaponsGroup2 = new ArrayList<>();
        weaponsGroup2.add(ppc); // PPC in both
        weaponsGroup2.add(lrm20);
        Mockito.when(mlc.weaponGroups.getWeapons(group2, mlc.loadout)).thenReturn(weaponsGroup2);

        // Execute
        final HeatGeneration cut = new HeatGeneration(mlc.loadout, group1);
        final double ans = cut.calculate();

        // Verify
        final double expected = ppc.getStat("h/s", null) + ll.getStat("h/s", null) + engine.getHeat(null);
        assertEquals(expected, ans, Math.ulp(expected) * 10);

    }

}

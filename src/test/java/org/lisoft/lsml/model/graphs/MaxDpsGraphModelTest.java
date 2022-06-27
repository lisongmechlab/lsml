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
package org.lisoft.lsml.model.graphs;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.util.TestHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Li Song
 */
public class MaxDpsGraphModelTest {
    private final Loadout loadout = mock(Loadout.class);
    private final List<Modifier> modifiers = new ArrayList<>();
    private final List<Weapon> weapons = new ArrayList<>();
    private MaxDpsGraphModel cut;

    @Before
    public void setUp() {
        when(loadout.getAllModifiers()).thenReturn(modifiers);
        when(loadout.items(Weapon.class)).thenReturn(weapons);
        cut = new MaxDpsGraphModel(loadout);
    }

    @Test
    public void testGetDataNotOffensive() {
        final Weapon nonOffensive = mock(Weapon.class);
        when(nonOffensive.isOffensive()).thenReturn(false);
        weapons.add(nonOffensive);

        assertTrue(cut.getData().isEmpty());
    }

    @Test
    public void testGetDataTwoWeapons() {
        final double zeroRange = 0.0;
        final double minRange = 1.0;
        final double longRange = 2.0;
        final double maxRange = 3.0;
        final double zeroRangeEff = 0.0;
        final double minRangeEff = 1.0;
        final double longRangeEff = 2.0;
        final double maxRangeEff = 3.0;
        final double dps = 4.0;
        final boolean isOffensive = true;

        final Weapon weapon = TestHelpers.makeWeapon(zeroRange, minRange, longRange, maxRange, zeroRangeEff,
                                                     minRangeEff, longRangeEff, maxRangeEff, isOffensive, dps, "SRM 6",
                                                     modifiers);
        weapons.add(weapon);
        weapons.add(weapon);

        final SortedMap<Weapon, List<Pair<Double, Double>>> ans = cut.getData();

        assertEquals(1, ans.size());
        final List<Pair<Double, Double>> series = ans.get(weapon);
        assertNotNull(series);
        assertEquals(6, series.size());
        assertEquals(zeroRange, series.get(0).first, 0.0);
        assertEquals(Math.nextDown(minRange), series.get(1).first, 0.0);
        assertEquals(minRange, series.get(2).first, 0.0);
        assertEquals(longRange, series.get(3).first, 0.0);
        assertEquals(maxRange, series.get(4).first, 0.0);
        assertEquals(Math.nextUp(maxRange), series.get(5).first, 0.0);

        for (final Pair<Double, Double> point : series) {
            final double range = point.first;
            final double damage = point.second;
            assertEquals(weapons.size() * dps * weapon.getRangeEffectiveness(range, modifiers), damage, 0.0);
        }
    }

    @Test
    public void testGetTitle() {
        assertEquals("Maximal DPS", cut.getTitle());
    }

    @Test
    public void testGetXAxisLabel() {
        assertEquals("Range [m]", cut.getXAxisLabel());
    }

    @Test
    public void testGetYAxisLabel() {
        assertEquals("DPS", cut.getYAxisLabel());
    }
}

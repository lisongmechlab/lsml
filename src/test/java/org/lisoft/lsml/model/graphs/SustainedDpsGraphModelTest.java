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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.MaxSustainedDPS;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.Pair;

/**
 * Test suite for {@link SustainedDpsGraphModel}.
 *
 * @author Li Song
 */
public class SustainedDpsGraphModelTest {
    private final List<Weapon> weapons = new ArrayList<>();
    private final List<Modifier> modifiers = new ArrayList<>();
    private final Loadout loadout = mock(Loadout.class);
    private final MaxSustainedDPS maxSustDPS = mock(MaxSustainedDPS.class);
    private SustainedDpsGraphModel cut;

    @Before
    public void setUp() {
        when(loadout.getModifiers()).thenReturn(modifiers);
        when(loadout.items(Weapon.class)).thenReturn(weapons);
        cut = new SustainedDpsGraphModel(maxSustDPS, loadout);
    }

    @Test
    public void testGetDataTwoWeapons() {
        final Weapon nonOffensive = makeWeapon(0, 1, 2, 3, false, 2.0, "x");
        final double dps1 = 10.2;
        final double dps2 = 3.1;
        final Weapon w1 = makeWeapon(10, 20, 30, 40, true, dps1, "y");
        final Weapon w2 = makeWeapon(11, 21, 31, 41, true, dps2, "z");

        final Function<Double, Double> rangeEff1 = x -> 3.0 + x * 0.1;
        final Function<Double, Double> rangeEff2 = x -> 300.0 + x * 0.1;

        when(w1.getRangeEffectiveness(anyDouble(), same(modifiers)))
                .thenAnswer(aInvocation -> rangeEff1.apply(aInvocation.getArgumentAt(0, Double.class)));
        when(w2.getRangeEffectiveness(anyDouble(), same(modifiers)))
                .thenAnswer(aInvocation -> rangeEff2.apply(aInvocation.getArgumentAt(0, Double.class)));

        weapons.add(nonOffensive);
        weapons.add(w1);
        weapons.add(w2);

        final Map<Weapon, Double> ratios = new HashMap<>();
        ratios.put(w1, 2.0);
        ratios.put(w2, 1.4);
        when(maxSustDPS.getWeaponRatios(anyDouble())).thenReturn(ratios);

        final SortedMap<Weapon, List<Pair<Double, Double>>> ans = cut.getData();

        for (final Weapon weapon : weapons) {
            if (weapon == nonOffensive) {
                assertFalse(ans.containsKey(weapon));
            }
            else {
                assertTrue(ans.containsKey(weapon));
                final List<Pair<Double, Double>> series = ans.get(weapon);
                assertNotNull(series);
                assertEquals(9, series.size());

                // Non-offensive weapon shall not contribute to ranges.
                final double tolerance = 0.0000001;
                assertEquals(0, series.get(0).first, tolerance);
                assertEquals(10, series.get(1).first, tolerance);
                assertEquals(11, series.get(2).first, tolerance);
                assertEquals(20, series.get(3).first, tolerance);
                assertEquals(21, series.get(4).first, tolerance);
                assertEquals(30, series.get(5).first, tolerance);
                assertEquals(31, series.get(6).first, tolerance);
                assertEquals(40, series.get(7).first, tolerance);
                assertEquals(41, series.get(8).first, tolerance);

                final Function<Double, Double> rangeEff = weapon == w1 ? rangeEff1 : rangeEff2;
                final double dps = weapon == w1 ? dps1 : dps2;

                assertEquals(rangeEff.apply(0.0) * dps * ratios.get(weapon), series.get(0).second, tolerance);
                assertEquals(rangeEff.apply(10.0) * dps * ratios.get(weapon), series.get(1).second, tolerance);
                assertEquals(rangeEff.apply(11.0) * dps * ratios.get(weapon), series.get(2).second, tolerance);
                assertEquals(rangeEff.apply(20.0) * dps * ratios.get(weapon), series.get(3).second, tolerance);
                assertEquals(rangeEff.apply(21.0) * dps * ratios.get(weapon), series.get(4).second, tolerance);
                assertEquals(rangeEff.apply(30.0) * dps * ratios.get(weapon), series.get(5).second, tolerance);
                assertEquals(rangeEff.apply(31.0) * dps * ratios.get(weapon), series.get(6).second, tolerance);
                assertEquals(rangeEff.apply(40.0) * dps * ratios.get(weapon), series.get(7).second, tolerance);
                assertEquals(rangeEff.apply(41.0) * dps * ratios.get(weapon), series.get(8).second, tolerance);
            }
        }
        verify(maxSustDPS, times(1)).getWeaponRatios(0);
        verify(maxSustDPS, times(1)).getWeaponRatios(10);
        verify(maxSustDPS, times(1)).getWeaponRatios(11);
        verify(maxSustDPS, times(1)).getWeaponRatios(20);
        verify(maxSustDPS, times(1)).getWeaponRatios(21);
        verify(maxSustDPS, times(1)).getWeaponRatios(30);
        verify(maxSustDPS, times(1)).getWeaponRatios(31);
        verify(maxSustDPS, times(1)).getWeaponRatios(40);
        verify(maxSustDPS, times(1)).getWeaponRatios(41);
        verifyNoMoreInteractions(maxSustDPS);
    }

    @Test
    public void testGetTitle() {
        assertEquals("Sustained DPS", cut.getTitle());
    }

    @Test
    public void testGetXAxisLabel() {
        assertEquals("Range [m]", cut.getXAxisLabel());
    }

    @Test
    public void testGetYAxisLabel() {
        assertEquals("DPS", cut.getYAxisLabel());
    }

    private Weapon makeWeapon(final double zeroRange, final double minRange, final double longRange,
            final double maxRange, final boolean isOffensive, double dps, String aName) {
        final Weapon weapon = mock(Weapon.class);
        when(weapon.getName()).thenReturn(aName);
        when(weapon.isOffensive()).thenReturn(isOffensive);
        when(weapon.getRangeZero(modifiers)).thenReturn(zeroRange);
        when(weapon.getRangeMin(modifiers)).thenReturn(minRange);
        when(weapon.getRangeLong(modifiers)).thenReturn(longRange);
        when(weapon.getRangeMax(modifiers)).thenReturn(maxRange);
        when(weapon.getStat("d/s", modifiers)).thenReturn(dps);
        return weapon;
    }
}

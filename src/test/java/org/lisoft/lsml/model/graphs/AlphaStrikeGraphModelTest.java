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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
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
import org.lisoft.lsml.model.metrics.AlphaStrike;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.Pair;

/**
 * Test suite for {@link AlphaStrikeGraphModel}.
 *
 * @author Li Song
 */
public class AlphaStrikeGraphModelTest {
    private final List<Weapon> weapons = new ArrayList<>();
    private final List<Modifier> modifiers = new ArrayList<>();
    private final Loadout loadout = mock(Loadout.class);
    private final AlphaStrike alphaStrike = mock(AlphaStrike.class);
    private AlphaStrikeGraphModel cut;

    @Before
    public void setUp() {
        when(loadout.getModifiers()).thenReturn(modifiers);
        when(loadout.items(Weapon.class)).thenReturn(weapons);
        cut = new AlphaStrikeGraphModel(alphaStrike, loadout);
    }

    @Test
    public void testGetDataTwoWeapons() {
        final Weapon nonOffensive = makeWeapon(0, 1, 2, 3, false, 2.0, "x");
        final double dps1 = 10.2;
        final double dps2 = 3.1;
        final Weapon w1 = makeWeapon(10, 20, 30, 40, true, dps1, "y");
        final Weapon w2 = makeWeapon(11, 21, 31, 41, true, dps2, "z");

        weapons.add(nonOffensive);
        weapons.add(w1);
        weapons.add(w2);

        // Arbitrary, senseless but unique values
        final Function<Double, Double> alpha1 = x -> 10 + x * 0.3;
        final Function<Double, Double> alpha2 = x -> 100 + x * 1;

        when(alphaStrike.getWeaponRatios(anyDouble())).thenAnswer(i -> {
            final Map<Weapon, Double> ratio = new HashMap<>();
            ratio.put(w1, alpha1.apply(i.getArgumentAt(0, Double.class)));
            ratio.put(w2, alpha2.apply(i.getArgumentAt(0, Double.class)));
            return ratio;
        });

        final SortedMap<Weapon, List<Pair<Double, Double>>> ans = cut.getData();

        assertNotNull(ans);
        assertEquals(2, ans.size());
        assertTrue(ans.containsKey(w1));
        assertTrue(ans.containsKey(w2));

        for (final Weapon w : weapons) {
            if (w == nonOffensive) {
                continue;
            }
            final List<Pair<Double, Double>> series = ans.get(w);
            assertNotNull(series);
            assertEquals(9, series.size());

            // Correct ranges
            assertEquals(0.0, series.get(0).first, 0.0);
            assertEquals(10.0, series.get(1).first, 0.0);
            assertEquals(11.0, series.get(2).first, 0.0);
            assertEquals(20.0, series.get(3).first, 0.0);
            assertEquals(21.0, series.get(4).first, 0.0);
            assertEquals(30.0, series.get(5).first, 0.0);
            assertEquals(31.0, series.get(6).first, 0.0);
            assertEquals(40.0, series.get(7).first, 0.0);
            assertEquals(41.0, series.get(8).first, 0.0);

            // Correct values
            final Function<Double, Double> alpha = w == w1 ? alpha1 : alpha2;

            assertEquals(alpha.apply(0.0), series.get(0).second, 0.0);
            assertEquals(alpha.apply(10.0), series.get(1).second, 0.0);
            assertEquals(alpha.apply(11.0), series.get(2).second, 0.0);
            assertEquals(alpha.apply(20.0), series.get(3).second, 0.0);
            assertEquals(alpha.apply(21.0), series.get(4).second, 0.0);
            assertEquals(alpha.apply(30.0), series.get(5).second, 0.0);
            assertEquals(alpha.apply(31.0), series.get(6).second, 0.0);
            assertEquals(alpha.apply(40.0), series.get(7).second, 0.0);
            assertEquals(alpha.apply(41.0), series.get(8).second, 0.0);
        }
    }

    @Test
    public void testGetTitle() {
        assertEquals("Alpha Strike Damage", cut.getTitle());
    }

    @Test
    public void testGetXAxisLabel() {
        assertEquals("Range [m]", cut.getXAxisLabel());
    }

    @Test
    public void testGetYAxisLabel() {
        assertEquals("Damage", cut.getYAxisLabel());
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

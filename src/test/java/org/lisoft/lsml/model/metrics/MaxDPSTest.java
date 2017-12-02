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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * Test suite for k Metric}.
 *
 * @author Li Song
 */
@SuppressWarnings("unchecked")
public class MaxDPSTest {
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();
    private MaxDPS cut;
    private final List<Weapon> items = new ArrayList<>();
    private Collection<Modifier> modifiers;

    @Before
    public void setup() {
        modifiers = mock(Collection.class);
        when(mlc.loadout.items(Weapon.class)).thenReturn(items);
        when(mlc.loadout.getAllModifiers()).thenReturn(modifiers);
        cut = new MaxDPS(mlc.loadout);
    }

    /**
     * {@link MaxDPS#calculate(double)} shall calculate the maximal DPS at a given range.
     */
    @Test
    public void testCalculate() {
        final double range = 300;

        final Weapon weapon1 = mock(Weapon.class);
        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRangeEffectiveness(range, modifiers)).thenReturn(0.8);
        when(weapon1.getStat("d/s", modifiers)).thenReturn(1.0);

        final Weapon weapon2 = mock(Weapon.class);
        when(weapon2.isOffensive()).thenReturn(true);
        when(weapon2.getRangeEffectiveness(range, modifiers)).thenReturn(1.0);
        when(weapon2.getStat("d/s", modifiers)).thenReturn(3.0);

        final Weapon weapon3 = mock(Weapon.class);
        when(weapon3.isOffensive()).thenReturn(true);
        when(weapon3.getRangeEffectiveness(range, modifiers)).thenReturn(0.9);
        when(weapon3.getStat("d/s", modifiers)).thenReturn(5.0);

        items.add(weapon1);
        items.add(weapon2);
        items.add(weapon3);

        final double dps1 = 0.8 * 1.0;
        final double dps2 = 1.0 * 3.0;
        final double dps3 = 0.9 * 5.0;

        assertEquals(dps1 + dps2 + dps3, cut.calculate(range), 0.0);
    }

    /**
     * No weapons should return 0.
     */
    @Test
    public void testCalculate_NoItems() {
        assertEquals(0.0, cut.calculate(0), 0.0);
    }

    /**
     * Non-Offensive weapons should not contribute to DPS.
     */
    @Test
    public void testCalculate_NonOffensive() {
        final Weapon weapon = mock(Weapon.class);
        when(weapon.isOffensive()).thenReturn(false);
        when(weapon.getRangeEffectiveness(anyDouble(), anyCollection())).thenReturn(1.0);
        when(weapon.getStat(anyString(), anyCollection())).thenReturn(100.0);

        items.add(weapon);
        assertEquals(0.0, cut.calculate(0), 0.0);
    }

    /**
     * {@link MaxDPS#calculate(double)} shall calculate the maximal DPS at a given range.
     */
    @Test
    public void testCalculate_WeaponGroups() {
        final double range = 300;

        final Weapon weapon1 = mock(Weapon.class);
        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRangeEffectiveness(range, modifiers)).thenReturn(0.8);
        when(weapon1.getStat("d/s", modifiers)).thenReturn(1.0);

        final Weapon weapon2 = mock(Weapon.class);
        when(weapon2.isOffensive()).thenReturn(true);
        when(weapon2.getRangeEffectiveness(range, modifiers)).thenReturn(1.0);
        when(weapon2.getStat("d/s", modifiers)).thenReturn(3.0);

        final Weapon weapon3 = mock(Weapon.class);
        when(weapon3.isOffensive()).thenReturn(true);
        when(weapon3.getRangeEffectiveness(range, modifiers)).thenReturn(0.9);
        when(weapon3.getStat("d/s", modifiers)).thenReturn(5.0);

        items.add(weapon1);
        items.add(weapon2);
        items.add(weapon3);

        final double dps2 = 1.0 * 3.0;
        final double dps3 = 0.9 * 5.0;

        final int group = 0;
        final Collection<Weapon> groupWeapons = new ArrayList<>();
        groupWeapons.add(weapon2);
        groupWeapons.add(weapon3);
        when(mlc.weaponGroups.getWeapons(group, mlc.loadout)).thenReturn(groupWeapons);

        cut = new MaxDPS(mlc.loadout, group);

        assertEquals(dps2 + dps3, cut.calculate(range), 0.0);
    }

}

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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.mockito.Mockito;

/**
 * A test suite for the {@link AlphaHeat} class.
 *
 * @author Li Song
 */
@SuppressWarnings("unchecked")
public class AlphaHeatTest {
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();

    private AlphaHeat cut;
    private final List<Weapon> items = new ArrayList<>();
    private Collection<Modifier> modifiers;

    @Before
    public void setup() {
        modifiers = Mockito.mock(Collection.class);
        when(mlc.loadout.items(Weapon.class)).thenReturn(items);
        when(mlc.loadout.getModifiers()).thenReturn(modifiers);

        cut = new AlphaHeat(mlc.loadout);
    }

    /**
     * Calculate shall sum up the heat of all weapons.
     */
    @Test
    public void testCalculate() {
        final Weapon weapon1 = Mockito.mock(Weapon.class);
        Mockito.when(weapon1.isOffensive()).thenReturn(true);
        Mockito.when(weapon1.getHeat(modifiers)).thenReturn(1.0);

        final Weapon weapon2 = Mockito.mock(Weapon.class);
        Mockito.when(weapon2.isOffensive()).thenReturn(true);
        Mockito.when(weapon2.getHeat(modifiers)).thenReturn(3.0);

        final Weapon weapon3 = Mockito.mock(Weapon.class);
        Mockito.when(weapon3.isOffensive()).thenReturn(true);
        Mockito.when(weapon3.getHeat(modifiers)).thenReturn(5.0);

        items.add(weapon1);
        items.add(weapon2);
        items.add(weapon3);

        assertEquals(9.0, cut.calculate(), 0.0);
    }

    /**
     * No weapons should return zero.
     */
    @Test
    public void testCalculate_noItems() {
        assertEquals(0.0, cut.calculate(), 0.0);
    }

    /**
     * Non-Offensive weapons should not be counted into the result.
     */
    @Test
    public void testCalculate_NonOffensive() {
        final Weapon weapon = Mockito.mock(Weapon.class);
        Mockito.when(weapon.isOffensive()).thenReturn(false);
        Mockito.when(weapon.getRangeEffectiveness(anyDouble(), anyCollection())).thenReturn(1.0);
        Mockito.when(weapon.getStat(anyString(), anyCollection())).thenReturn(100.0);

        items.add(weapon);
        assertEquals(0.0, cut.calculate(), 0.0);
    }

    /**
     * Only use the weapons in the current weapon group.
     */
    @Test
    public void testCalculate_WeaponGroup() {
        final Weapon weapon1 = Mockito.mock(Weapon.class);
        Mockito.when(weapon1.isOffensive()).thenReturn(true);
        Mockito.when(weapon1.getHeat(modifiers)).thenReturn(0.8);

        final Weapon weapon2 = Mockito.mock(Weapon.class);
        Mockito.when(weapon2.isOffensive()).thenReturn(true);
        Mockito.when(weapon2.getHeat(modifiers)).thenReturn(1.0);

        final Weapon weapon3 = Mockito.mock(Weapon.class);
        Mockito.when(weapon3.isOffensive()).thenReturn(true);
        Mockito.when(weapon3.getHeat(modifiers)).thenReturn(0.9);

        items.add(weapon1);
        items.add(weapon2);
        items.add(weapon3);

        final int group = 0;
        final Collection<Weapon> groupWeapons = new ArrayList<>();
        groupWeapons.add(weapon2);
        groupWeapons.add(weapon3);
        Mockito.when(mlc.weaponGroups.getWeapons(group, mlc.loadout)).thenReturn(groupWeapons);

        cut = new AlphaHeat(mlc.loadout, group);

        assertEquals(1.9, cut.calculate(), 0.0);
    }
}

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.modifiers.Modifier;

public class AlphaHeatPercentTest {

    private final AlphaHeat alphaHeat = mock(AlphaHeat.class);
    private final GhostHeat ghostHeat = mock(GhostHeat.class);
    private final HeatDissipation heatDissipation = mock(HeatDissipation.class);
    private final HeatCapacity heatCapacity = mock(HeatCapacity.class);
    private final Loadout loadout = mock(Loadout.class);
    private final List<EnergyWeapon> energyWeapons = new ArrayList<>();
    private final List<Modifier> modifiers = new ArrayList<>();

    @Before
    public void setup() {
        when(loadout.items(EnergyWeapon.class)).thenReturn(energyWeapons);
        when(loadout.getModifiers()).thenReturn(modifiers);
    }

    @Test
    public void testCalculate() {
        final EnergyWeapon shortDuration = mock(EnergyWeapon.class);
        final EnergyWeapon longDuration = mock(EnergyWeapon.class);
        when(shortDuration.getDuration(modifiers)).thenReturn(3.0);
        when(longDuration.getDuration(modifiers)).thenReturn(6.0);
        energyWeapons.add(shortDuration);
        energyWeapons.add(longDuration);

        when(alphaHeat.calculate()).thenReturn(8.0);
        when(ghostHeat.calculate()).thenReturn(2.0);
        when(heatDissipation.calculate()).thenReturn(3.13);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final double expected = (alphaHeat.calculate() + ghostHeat.calculate() - 6 * heatDissipation.calculate())
                / heatCapacity.calculate();

        final AlphaHeatPercent cut = new AlphaHeatPercent(alphaHeat, ghostHeat, heatDissipation, heatCapacity, loadout);

        assertEquals(expected, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculate_NoEnergyWeapons() {
        when(alphaHeat.calculate()).thenReturn(8.0);
        when(ghostHeat.calculate()).thenReturn(2.0);
        when(heatDissipation.calculate()).thenReturn(3.13);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final double expected = (alphaHeat.calculate() + ghostHeat.calculate()) / heatCapacity.calculate();

        final AlphaHeatPercent cut = new AlphaHeatPercent(alphaHeat, ghostHeat, heatDissipation, heatCapacity, loadout);

        assertEquals(expected, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateWeaponGroup() {
        final EnergyWeapon shortDuration = mock(EnergyWeapon.class);
        final EnergyWeapon longDuration = mock(EnergyWeapon.class);
        when(shortDuration.getDuration(modifiers)).thenReturn(3.0);
        when(longDuration.getDuration(modifiers)).thenReturn(6.0);

        final int group = 3;
        final Collection<Weapon> groupWeapons = new ArrayList<>();
        final WeaponGroups weaponGroups = mock(WeaponGroups.class);
        when(weaponGroups.getWeapons(group, loadout)).thenReturn(groupWeapons);
        when(loadout.getWeaponGroups()).thenReturn(weaponGroups);

        energyWeapons.add(shortDuration);
        energyWeapons.add(longDuration);
        groupWeapons.add(shortDuration);

        when(alphaHeat.calculate()).thenReturn(8.0);
        when(ghostHeat.calculate()).thenReturn(2.0);
        when(heatDissipation.calculate()).thenReturn(3.13);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final double expected = (alphaHeat.calculate() + ghostHeat.calculate() - 3 * heatDissipation.calculate())
                / heatCapacity.calculate();

        final AlphaHeatPercent cut = new AlphaHeatPercent(alphaHeat, ghostHeat, heatDissipation, heatCapacity, loadout,
                group);

        assertEquals(expected, cut.calculate(), 0.0);
    }
}

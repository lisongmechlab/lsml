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

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.WeaponGroups;
import org.lisoft.lsml.model.metrics.helpers.IntegratedConstantSignal;
import org.lisoft.lsml.model.metrics.helpers.IntegratedImpulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedPulseTrain;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Li Song
 */
public class AlphaHeatPercentTest {
    private final GhostHeat ghostHeat = mock(GhostHeat.class);
    private final HeatCapacity heatCapacity = mock(HeatCapacity.class);
    private final HeatDissipation heatDissipation = mock(HeatDissipation.class);
    private final Loadout loadout = mock(Loadout.class);
    private final List<Modifier> modifiers = new ArrayList<>();
    private final WeaponGroups weaponGroups = mock(WeaponGroups.class);
    private final List<Weapon> weapons = new ArrayList<>();

    @Before
    public void setup() {
        when(loadout.items(Weapon.class)).thenReturn(weapons);
        when(loadout.getAllModifiers()).thenReturn(modifiers);
        when(loadout.getWeaponGroups()).thenReturn(weaponGroups);
    }

    @Test
    public void testCalculateEngine() {
        Engine engine = mock(Engine.class);
        when(engine.getExpectedHeatSignal(any())).thenReturn(new IntegratedConstantSignal(0.1));
        when(loadout.getEngine()).thenReturn(engine);

        final Weapon weapon1 = mock(Weapon.class);
        weapons.add(weapon1);
        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(4.0);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedPulseTrain(4.0, 2.0, 5.0));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(1.0); // Engine heat cancels dissipation
        when(heatCapacity.calculate()).thenReturn(50.0);

        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals((4.1 * 2.0) / 50.0, cut.calculate(), 0.0);
    }

    // Ghost heat applies immediately, even if the weapon has a duration.
    @Test
    public void testCalculateGhostHeat() {
        final double duration = 2.0;
        final double amplitude = 0.5;
        final double period = 4.0;

        final Weapon weapon1 = mock(Weapon.class);
        weapons.add(weapon1);
        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(period);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedPulseTrain(period, duration, amplitude));

        when(ghostHeat.calculate()).thenReturn(10.0);
        when(heatDissipation.calculate()).thenReturn(1.0);
        when(heatCapacity.calculate()).thenReturn(10.0);

        final double expected = 10;
        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals(expected / 10.0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateLaserBurnDuration() {
        final double duration = 2.0;
        final double amplitude = 5.0;
        final double period = 4.0;

        final Weapon weapon1 = mock(Weapon.class);
        weapons.add(weapon1);
        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(period);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedPulseTrain(period, duration, amplitude));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(1.0);
        when(heatCapacity.calculate()).thenReturn(10.0);

        final double expected = duration * (amplitude - 1);
        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals(expected / 10.0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateNonOffensive() {
        final Weapon weapon1 = mock(Weapon.class);
        weapons.add(weapon1);

        when(weapon1.isOffensive()).thenReturn(false);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(4.0);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedImpulseTrain(4.0, 5.0));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(1.0);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals(0.0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateOneWeapon() {
        final Weapon weapon1 = mock(Weapon.class);
        weapons.add(weapon1);

        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(4.0);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedImpulseTrain(4.0, 10.0));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(1.0);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals(10.0 / 50.0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateOnlyOnePeriodPerWeapon() {
        final Weapon weapon1 = mock(Weapon.class);
        final Weapon weapon2 = mock(Weapon.class);
        weapons.add(weapon1);
        weapons.add(weapon2);

        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon2.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(4.0);
        when(weapon2.getRawFiringPeriod(any())).thenReturn(1.0);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedImpulseTrain(4.0, 5.0));
        when(weapon2.getExpectedHeatSignal(any())).thenReturn(new IntegratedImpulseTrain(1.0, 5.0));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(1.0);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals(10.0 / 50.0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateRocketLauncher_Issue787() throws NoSuchItemException {
        final Weapon weapon1 = mock(Weapon.class);
        weapons.add(weapon1);

        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(Double.POSITIVE_INFINITY);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(
            new IntegratedImpulseTrain(Double.POSITIVE_INFINITY, 5.0));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(2.0);
        when(heatCapacity.calculate()).thenReturn(30.0);

        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout);
        assertEquals(5.0 / 30.0, cut.calculate(), 0.0);
    }

    @Test
    public void testCalculateWeaponGroup() {
        final List<Weapon> groupedWeapons = new ArrayList<>();
        final Weapon weapon1 = mock(Weapon.class);
        final Weapon weapon2 = mock(Weapon.class);
        groupedWeapons.add(weapon1);
        groupedWeapons.add(weapon2);
        when(weaponGroups.getWeapons(3, loadout)).thenReturn(groupedWeapons);

        when(weapon1.isOffensive()).thenReturn(true);
        when(weapon2.isOffensive()).thenReturn(true);
        when(weapon1.getRawFiringPeriod(any())).thenReturn(4.0);
        when(weapon2.getRawFiringPeriod(any())).thenReturn(1.0);
        when(weapon1.getExpectedHeatSignal(any())).thenReturn(new IntegratedImpulseTrain(4.0, 5.0));
        when(weapon2.getExpectedHeatSignal(any())).thenReturn(new IntegratedImpulseTrain(1.0, 5.0));

        when(ghostHeat.calculate()).thenReturn(0.0);
        when(heatDissipation.calculate()).thenReturn(1.0);
        when(heatCapacity.calculate()).thenReturn(50.0);

        final AlphaHeatPercent cut = new AlphaHeatPercent(ghostHeat, heatDissipation, heatCapacity, loadout, 3);
        assertEquals(10.0 / 50.0, cut.calculate(), 0.0);
    }
}

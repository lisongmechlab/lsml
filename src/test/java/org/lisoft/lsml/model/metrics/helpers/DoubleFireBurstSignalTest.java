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
package org.lisoft.lsml.model.metrics.helpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.lisoft.lsml.model.NoSuchItemException;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This class implements a test suite for {@link DoubleFireBurstSignal}.
 *
 * @author Li Song
 */
public class DoubleFireBurstSignalTest {

    /**
     * Only weapons that can double fire are supported.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidWeapon() {
        final BallisticWeapon weapon = mock(BallisticWeapon.class);
        when(weapon.canDoubleFire()).thenReturn(false);

        new DoubleFireBurstSignal(weapon, null, 0);
    }

    @Test
    public void testOnlyJams() { // Oooof
        final double p_jam = 1.0;
        final double t_jam = 5.0;
        final double t_cycle = 2.0;
        final double range = 400;
        final double range_eff = 0.9;
        final double damage = 5.0;

        final Collection<Modifier> modifiers = mock(Collection.class);
        final BallisticWeapon weapon = mock(BallisticWeapon.class);
        when(weapon.canDoubleFire()).thenReturn(true);
        when(weapon.getJamProbability(modifiers)).thenReturn(p_jam);
        when(weapon.getJamTime(modifiers)).thenReturn(t_jam);
        when(weapon.getRawFiringPeriod(modifiers)).thenReturn(t_cycle);
        when(weapon.getRangeEffectiveness(range, modifiers)).thenReturn(range_eff);
        when(weapon.getDamagePerShot()).thenReturn(damage);
        when(weapon.getShotsDuringCooldown()).thenReturn(1);
        when(weapon.getRampUpTime(modifiers)).thenReturn(0.0);

        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, range);

        final int cycles = 100;
        final double expected = Math.ceil(t_cycle * cycles/(t_jam+t_cycle)) * damage * range_eff;
        // We run the signal for one shot, this means we don't test the recursion.
        assertEquals(expected, cut.integrateFromZeroTo(t_cycle*cycles), 0.0);
    }

    @Test
    public void testNoJams() {
        final double p_jam = 0.0;
        final double t_jam = 5.0;
        final double t_cycle = 2.0;
        final double range = 400;
        final double range_eff = 0.9;
        final double damage = 5.0;

        final Collection<Modifier> modifiers = mock(Collection.class);
        final BallisticWeapon weapon = mock(BallisticWeapon.class);
        when(weapon.canDoubleFire()).thenReturn(true);
        when(weapon.getJamProbability(modifiers)).thenReturn(p_jam);
        when(weapon.getJamTime(modifiers)).thenReturn(t_jam);
        when(weapon.getRawFiringPeriod(modifiers)).thenReturn(t_cycle);
        when(weapon.getRangeEffectiveness(range, modifiers)).thenReturn(range_eff);
        when(weapon.getDamagePerShot()).thenReturn(damage);
        when(weapon.getShotsDuringCooldown()).thenReturn(1);

        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, range);

        final int cycles = 100;
        final double expected = cycles * damage * range_eff * 2;
        final double epsilon = 0.0001;
        // We run the signal for one shot, this means we don't test the recursion.
        assertEquals(expected, cut.integrateFromZeroTo(t_cycle*cycles-epsilon), 0.0);
    }

    @Test
    public void testOneCooldown() {
        final double p_jam = 0.2;
        final double t_jam = 5.0;
        final double t_cycle = 2.0;
        final double range = 400;
        final double range_eff = 0.9;
        final double damage = 5.0;

        final Collection<Modifier> modifiers = mock(Collection.class);
        final BallisticWeapon weapon = mock(BallisticWeapon.class);
        when(weapon.canDoubleFire()).thenReturn(true);
        when(weapon.getJamProbability(modifiers)).thenReturn(p_jam);
        when(weapon.getJamTime(modifiers)).thenReturn(t_jam);
        when(weapon.getRawFiringPeriod(modifiers)).thenReturn(t_cycle);
        when(weapon.getRangeEffectiveness(range, modifiers)).thenReturn(range_eff);
        when(weapon.getDamagePerShot()).thenReturn(damage);
        when(weapon.getShotsDuringCooldown()).thenReturn(1);
        when(weapon.getRampUpTime(modifiers)).thenReturn(0.0);

        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, range);

        final double expected = (p_jam*1 + (1 - p_jam) * 2) * damage * range_eff;
        // We run the signal for one shot, this means we don't test the recursion.
        assertEquals(expected, cut.integrateFromZeroTo(t_cycle), 0.0);
    }

    @Test
    public void testAsymptoticUAC5() throws NoSuchItemException {

        BallisticWeapon weapon = (BallisticWeapon) ItemDB.lookup("ULTRA AC/5");
        final Collection<Modifier> modifiers = Collections.emptyList();
        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, 0);

        // We run the signal for one shot, this means we don't test the recursion.
        double longTime=60*60;
        assertEquals(weapon.getStat("d/s", modifiers), cut.integrateFromZeroTo(longTime)/longTime, 0.05);
        assertEquals(1.0, cut.getProbabilityMass(), 0.0);
    }

    @Test
    public void testAsymptoticUAC20() throws NoSuchItemException {

        BallisticWeapon weapon = (BallisticWeapon) ItemDB.lookup("ULTRA AC/20");
        final Collection<Modifier> modifiers = Collections.emptyList();
        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, 0);

        // We run the signal for one shot, this means we don't test the recursion.
        double longTime=60*60;
        assertEquals(weapon.getStat("d/s", modifiers), cut.integrateFromZeroTo(longTime)/longTime, 0.5);
        assertEquals(1.0, cut.getProbabilityMass(), 0.0);
    }

    @Test
    public void testAsymptoticRAC2() throws NoSuchItemException {
        BallisticWeapon weapon = (BallisticWeapon) ItemDB.lookup("ROTARY AC/2");
        final Collection<Modifier> modifiers = Collections.emptyList();
        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, 0);

        // We run the signal for one shot, this means we don't test the recursion.
        double longTime=10*60;
        assertEquals(weapon.getStat("d/s", modifiers), cut.integrateFromZeroTo(longTime)/longTime, 0.3);
        assertEquals(1.0, cut.getProbabilityMass(), 0.0);
    }

    @Test
    public void testAsymptoticRAC5() throws NoSuchItemException {
        BallisticWeapon weapon = (BallisticWeapon) ItemDB.lookup("ROTARY AC/5");
        final Collection<Modifier> modifiers = Collections.emptyList();
        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, 0);

        // We run the signal for one shot, this means we don't test the recursion.
        double longTime=10*60;
        assertEquals(weapon.getStat("d/s", modifiers), cut.integrateFromZeroTo(longTime)/longTime, 0.3);
        assertEquals(1.0, cut.getProbabilityMass(), 0.0);
    }

    @Test
    public void testJamFreeTimeRAC5() throws NoSuchItemException {
        BallisticWeapon weapon = (BallisticWeapon) ItemDB.lookup("ROTARY AC/5");
        final Collection<Modifier> modifiers = Collections.emptyList();
        final DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, 0);

        // We run the signal for one shot, this means we don't test the recursion.
        double jamFreeTime = weapon.getJamRampUpTime(modifiers) - weapon.getRampUpTime(modifiers);
        double expectedDamage = weapon.getDamagePerShot()/weapon.getRawFiringPeriod(modifiers)*jamFreeTime;
        assertEquals(expectedDamage, cut.integrateFromZeroTo(jamFreeTime), weapon.getDamagePerShot());
        assertEquals(1.0, cut.getProbabilityMass(), 0.0);
    }
}

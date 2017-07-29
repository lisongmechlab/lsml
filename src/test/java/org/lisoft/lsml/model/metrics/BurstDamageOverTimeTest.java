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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.util.Pair;

/**
 * Test suite for {@link BurstDamageOverTime}.
 *
 * @author Li Song
 */
public class BurstDamageOverTimeTest {
    private final MessageXBar aXBar = mock(MessageXBar.class);
    private final List<Weapon> items = new ArrayList<>();
    private final MockLoadoutContainer mlc = new MockLoadoutContainer();

    @Before
    public void setup() {
        when(mlc.loadout.items(Weapon.class)).thenReturn(items);
    }

    @Test
    public void testBurstDamageOverTime() {
        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);

        // Verify
        verify(aXBar).attach(cut);
        verifyNoMoreInteractions(aXBar);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall calculate the result correctly taking range falloff
     * and weapon cool downs into account.
     */
    @Test
    public final void testCalculate() throws Exception {
        // Setup
        final Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        final EnergyWeapon erppc = (EnergyWeapon) ItemDB.lookup("ER PPC");
        final EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(ac20);
        items.add(erllas);
        items.add(erppc);
        final double time = erllas.getSecondsPerShot(null) * 3 + erllas.getDuration(null) / 2; // 3.5 ER LLAS

        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        final double burst = cut.calculate(500, time);

        // Verify
        double expected = erllas.getDamagePerShot() * 3.5;
        expected += (int) (time / ac20.getSecondsPerShot(null) + 1) * ac20.getDamagePerShot()
                * ac20.getRangeEffectiveness(500, null);
        expected += (int) (time / erppc.getSecondsPerShot(null) + 1) * erppc.getDamagePerShot()
                * erppc.getRangeEffectiveness(500, null);
        assertEquals(expected, burst, 1E-6);
    }

    /**
     * The implementation caches partial results. So even if we change parameters, the result shall be calculated for
     * the correct parameters.
     */
    @Test
    public final void testCalculate_Cacheupdate() throws Exception {
        // Setup
        final EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(erllas);

        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        cut.calculate(123, 321); // Dummy just make sure it's different from below

        final double time = erllas.getSecondsPerShot(null) * 3 + erllas.getDuration(null) / 2; // 3.5 ER LLAS
        final double burst = cut.calculate(500, time);

        // Verify
        final double expected = erllas.getDamagePerShot() * 3.5;
        assertEquals(expected, burst, 1E-6);
    }

    /**
     * Test that a hypothetical double fire capable ballistic weapon with a minimum range is correctly calculated for
     * automatic range.
     */
    @Test
    public final void testCalculate_MinRangeBallisticDoubleFire() {
        final BallisticWeapon weapon = mock(BallisticWeapon.class);
        final Pair<Double, Double> optimal = new Pair<>(100.0, 200.0);
        when(weapon.isOffensive()).thenReturn(true);
        when(weapon.canDoubleFire()).thenReturn(true);
        when(weapon.getRangeOptimal(any())).thenReturn(optimal);
        when(weapon.getJamProbability(any())).thenReturn(0.4);
        when(weapon.getJamTime(any())).thenReturn(5.0);
        when(weapon.getRawSecondsPerShot(any())).thenReturn(2.0);
        when(weapon.getDamagePerShot()).thenReturn(10.0);
        when(weapon.getRangeEffectiveness(anyDouble(), any())).thenAnswer(aInvocation -> {
            final double x = aInvocation.<Double> getArgument(0).doubleValue();
            if (x < optimal.first || x > optimal.second) {
                return 0.0;
            }
            return 1.0;
        });

        // Setup
        items.add(weapon);

        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        final double burst = cut.calculate(-1, 5);

        // Verify
        assertTrue(burst > 0);

    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall not include AMS!!
     */
    @Test
    public final void testCalculate_NoAMS() {
        // Setup
        items.add(ItemDB.AMS);

        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        final double burst = cut.calculate(0, 500);

        // Verify
        assertEquals(0.0, burst, 0.0);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall return 0 for no weapons.
     */
    @Test
    public final void testCalculate_NoWeapons() {
        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        final double burst = cut.calculate(500, 500);

        // Verify
        assertEquals(0.0, burst, 0.0);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall only calculate for the weapons in the selected weapon
     * group.
     */
    @Test
    public final void testCalculate_WeaponGroups() throws Exception {
        // Setup
        final Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        final EnergyWeapon erppc = (EnergyWeapon) ItemDB.lookup("ER PPC");
        final EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(ac20);
        items.add(erllas);
        items.add(erppc);
        final double time = erllas.getSecondsPerShot(null) * 3 + erllas.getDuration(null) / 2; // 3.5 ER LLAS

        final int group = 3;
        final Collection<Weapon> groupWeapons = new ArrayList<>();
        groupWeapons.add(ac20);
        groupWeapons.add(erllas);
        when(mlc.weaponGroups.getWeapons(group, mlc.loadout)).thenReturn(groupWeapons);

        // Execute
        final BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar, group);
        final double burst = cut.calculate(500, time);

        // Verify
        double expected = erllas.getDamagePerShot() * 3.5;
        expected += (int) (time / ac20.getSecondsPerShot(null) + 1) * ac20.getDamagePerShot()
                * ac20.getRangeEffectiveness(500, null);
        assertEquals(expected, burst, 1E-6);
    }
}

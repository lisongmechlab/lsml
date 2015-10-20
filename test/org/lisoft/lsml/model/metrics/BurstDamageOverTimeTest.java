/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.lisoft.lsml.messages.MessageXBar;
import org.lisoft.lsml.model.datacache.ItemDB;
import org.lisoft.lsml.model.helpers.MockLoadoutContainer;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Weapon;
import org.mockito.Mockito;

/**
 * Test suite for {@link BurstDamageOverTime}.
 * 
 * @author Emily Björk
 */
public class BurstDamageOverTimeTest {
    private final MessageXBar          aXBar = Mockito.mock(MessageXBar.class);
    private final List<Weapon>         items = new ArrayList<>();
    private final MockLoadoutContainer mlc   = new MockLoadoutContainer();

    @Before
    public void setup() {
        Mockito.when(mlc.loadout.items(Weapon.class)).thenReturn(items);
    }

    @Test
    public void testBurstDamageOverTime() {
        // Execute
        BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);

        // Verify
        Mockito.verify(aXBar).attach(cut);
        Mockito.verifyNoMoreInteractions(aXBar);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall calculate the result correctly taking range falloff
     * and weapon cool downs into account.
     */
    @Test
    public final void testCalculate() {
        // Setup
        Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        EnergyWeapon erppc = (EnergyWeapon) ItemDB.lookup("ER PPC");
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(ac20);
        items.add(erllas);
        items.add(erppc);
        final double time = erllas.getSecondsPerShot(null) * 3 + erllas.getDuration(null) / 2; // 3.5 ER LLAS

        // Execute
        BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        double burst = cut.calculate(500, time);

        // Verify
        double expected = erllas.getDamagePerShot() * 3.5;
        expected += ((int) (time / ac20.getSecondsPerShot(null) + 1)) * ac20.getDamagePerShot()
                * ac20.getRangeEffectivity(500, null);
        expected += ((int) (time / erppc.getSecondsPerShot(null) + 1)) * erppc.getDamagePerShot()
                * erppc.getRangeEffectivity(500, null);
        assertEquals(expected, burst, 0.0);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall only calculate for the weapons in the selected weapon
     * group.
     */
    @Test
    public final void testCalculate_WeaponGroups() {
        // Setup
        Weapon ac20 = (Weapon) ItemDB.lookup("AC/20");
        EnergyWeapon erppc = (EnergyWeapon) ItemDB.lookup("ER PPC");
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(ac20);
        items.add(erllas);
        items.add(erppc);
        final double time = erllas.getSecondsPerShot(null) * 3 + erllas.getDuration(null) / 2; // 3.5 ER LLAS

        int group = 3;
        Collection<Weapon> groupWeapons = new ArrayList<>();
        groupWeapons.add(ac20);
        groupWeapons.add(erllas);
        Mockito.when(mlc.weaponGroups.getWeapons(group, mlc.loadout)).thenReturn(groupWeapons);
        
        // Execute
        BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar, group);
        double burst = cut.calculate(500, time);

        // Verify
        double expected = erllas.getDamagePerShot() * 3.5;
        expected += ((int) (time / ac20.getSecondsPerShot(null) + 1)) * ac20.getDamagePerShot()
                * ac20.getRangeEffectivity(500, null);
        assertEquals(expected, burst, 0.0);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall return 0 for no weapons.
     */
    @Test
    public final void testCalculate_NoWeapons() {
        // Execute
        BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        double burst = cut.calculate(500, 500);

        // Verify
        assertEquals(0.0, burst, 0.0);
    }

    /**
     * {@link BurstDamageOverTime#calculate(double, double)} shall not include AMS!!
     */
    @Test
    public final void testCalculate_NoAMS() {
        // Setup
        items.add(ItemDB.AMS);

        // Execute
        BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        double burst = cut.calculate(0, 500);

        // Verify
        assertEquals(0.0, burst, 0.0);
    }

    /**
     * The implementation caches partial results. So even if we change parameters, the result shall be calculated for
     * the correct parameters.
     */
    @Test
    public final void testCalculate_Cacheupdate() {
        // Setup
        EnergyWeapon erllas = (EnergyWeapon) ItemDB.lookup("ER LARGE LASER");
        items.add(erllas);

        // Execute
        BurstDamageOverTime cut = new BurstDamageOverTime(mlc.loadout, aXBar);
        cut.calculate(123, 321); // Dummy just make sure it's different from below

        double time = erllas.getSecondsPerShot(null) * 3 + erllas.getDuration(null) / 2; // 3.5 ER LLAS
        double burst = cut.calculate(500, time);

        // Verify
        double expected = erllas.getDamagePerShot() * 3.5;
        assertEquals(expected, burst, 0.0);
    }
}

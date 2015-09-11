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
package lisong_mechlab.model.metrics.helpers;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.modifiers.Modifier;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * This class implements a test suite for {@link DoubleFireBurstSignal}.
 * 
 * @author Li Song
 */
public class DoubleFireBurstSignalTest {

    /**
     * Only weapons that can double fire are supported.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidWeapon() {
        BallisticWeapon weapon = Mockito.mock(BallisticWeapon.class);
        Mockito.when(weapon.canDoubleFire()).thenReturn(false);

        @SuppressWarnings("unused")
        DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, null, 0);
    }

    @Test
    public void testOneCooldown() {
        final double p_jam = 0.2;
        final double t_jam = 5.0;
        final double t_cycle = 2.0;
        final double range = 400;
        final double range_eff = 0.9;
        final double damage = 5.0;

        Collection<Modifier> modifiers = Mockito.mock(Collection.class);
        BallisticWeapon weapon = Mockito.mock(BallisticWeapon.class);
        Mockito.when(weapon.canDoubleFire()).thenReturn(true);
        Mockito.when(weapon.getJamProbability(modifiers)).thenReturn(p_jam);
        Mockito.when(weapon.getJamTime(modifiers)).thenReturn(t_jam);
        Mockito.when(weapon.getRawSecondsPerShot(modifiers)).thenReturn(t_cycle);
        Mockito.when(weapon.getRangeEffectivity(range, modifiers)).thenReturn(range_eff);
        Mockito.when(weapon.getDamagePerShot()).thenReturn(damage);

        DoubleFireBurstSignal cut = new DoubleFireBurstSignal(weapon, modifiers, range);

        double expected = (p_jam + (1 - p_jam) * 2) * damage * range_eff;
        assertEquals(expected, cut.integrateFromZeroTo(t_cycle / 2), 0.0);
    }
}

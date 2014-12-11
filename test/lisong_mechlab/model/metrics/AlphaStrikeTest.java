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
package lisong_mechlab.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.modifiers.Modifier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link AlphaStrike}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class AlphaStrikeTest {
    @Mock
    LoadoutStandard      loadout;
    @InjectMocks
    private AlphaStrike  cut;
    private List<Weapon> items = new ArrayList<>();
    @Mock
    private Collection<Modifier> modifiers; 
    
    @Before
    public void setup() {
        when(loadout.items(Weapon.class)).thenReturn(items);
        when(loadout.getModifiers()).thenReturn(modifiers);
    }

    /**
     * Non-Offensive weapons should not be counted into the result.
     */
    @Test
    public void testCalculate_NonOffensive() {
        Weapon weapon = Mockito.mock(Weapon.class);
        Mockito.when(weapon.isOffensive()).thenReturn(false);
        Mockito.when(weapon.getRangeEffectivity(Matchers.anyDouble(), Matchers.anyCollection())).thenReturn(1.0);
        Mockito.when(weapon.getStat(Matchers.anyString(), Matchers.anyCollection())).thenReturn(100.0);

        items.add(weapon);
        assertEquals(0.0, cut.calculate(0), 0.0);
    }

    /**
     * No weapons should return zero.
     */
    @Test
    public void testCalculate_noItems() {
        assertEquals(0.0, cut.calculate(0), 0.0);
    }

    /**
     * Calculate shall sum up the per volley damage of all weapons at the given range.
     */
    @Test
    public void testCalculate() {
        final double range = 300;
        
        Weapon weapon1 = Mockito.mock(Weapon.class);
        Mockito.when(weapon1.isOffensive()).thenReturn(true);
        Mockito.when(weapon1.getRangeEffectivity(range, modifiers)).thenReturn(0.8);
        Mockito.when(weapon1.getDamagePerShot()).thenReturn(1.0);
        
        Weapon weapon2 = Mockito.mock(Weapon.class);
        Mockito.when(weapon2.isOffensive()).thenReturn(true);
        Mockito.when(weapon2.getRangeEffectivity(range, modifiers)).thenReturn(1.0);
        Mockito.when(weapon2.getDamagePerShot()).thenReturn(3.0);
        
        Weapon weapon3 = Mockito.mock(Weapon.class);
        Mockito.when(weapon3.isOffensive()).thenReturn(true);
        Mockito.when(weapon3.getRangeEffectivity(range, modifiers)).thenReturn(0.9);
        Mockito.when(weapon3.getDamagePerShot()).thenReturn(5.0);
        
        items.add(weapon1);
        items.add(weapon2);
        items.add(weapon3);

        final double alpha1 = 0.8*1.0;
        final double alpha2 = 1.0*3.0;
        final double alpha3 = 0.9*5.0;
        
        assertEquals(alpha1 + alpha2 + alpha3, cut.calculate(range), 0.0);
    }
}

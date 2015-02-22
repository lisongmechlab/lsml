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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.WeaponGroups.FiringMode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A Unit Test suite for {@link WeaponGroups}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class WeaponGroupsTest {
    @Mock
    private LoadoutBase<?> loadout;
    private List<Weapon>   weapons = new ArrayList<>();
    private WeaponGroups   cut;

    @Before
    public void setup() {
        Mockito.when(loadout.items(Weapon.class)).thenReturn(weapons);
        cut = new WeaponGroups(loadout);
    }

    /**
     * Test the default constructed state.
     */
    @Test
    public final void testConstructor() {
        for (int i = 0; i < WeaponGroups.MAX_GROUPS; ++i) {
            assertEquals(WeaponGroups.FiringMode.Optimal, cut.getFiringMode(i));

            for (int j = 0; j < WeaponGroups.MAX_WEAPONS; ++j) {
                assertFalse(cut.isInGroup(i, i));
            }
        }
    }
    
    /**
     * Test the copy constructed state.
     */
    @Test
    public final void testCopyConstructor() {
        LoadoutBase<?> loadout2 = Mockito.mock(LoadoutBase.class);
        List<Weapon>   weapons2 = new ArrayList<>();
        Mockito.when(loadout2.items(Weapon.class)).thenReturn(weapons2);
        
        cut.setFiringMode(2, FiringMode.ChainFire);
        cut.setFiringMode(5, FiringMode.AlphaStrike);        
        cut.setGroup(0, 0, true);
        cut.setGroup(5, 15, true);
        
        Weapon w0 = Mockito.mock(Weapon.class);
        Weapon w1 = Mockito.mock(Weapon.class);
        Mockito.when(w0.isOffensive()).thenReturn(true);
        Mockito.when(w1.isOffensive()).thenReturn(true);
        weapons.add(w0);
        weapons2.add(w1);
        
        WeaponGroups copy = new WeaponGroups(cut, loadout2);
        
        // Using weapons from new loadout
        assertEquals(Arrays.asList(w1), copy.getWeaponOrder());
        
        // Firing mode is copied
        assertEquals(FiringMode.Optimal, copy.getFiringMode(1)); // Implicitly optimal
        assertEquals(FiringMode.ChainFire, copy.getFiringMode(2));
        assertEquals(FiringMode.AlphaStrike, copy.getFiringMode(5));
        
        // Groups are copied
        assertTrue(copy.isInGroup(0, 0));
        assertTrue(copy.isInGroup(5, 15));
        assertFalse(copy.isInGroup(0, 1)); // Implicitly false.
    }

    /**
     * Test that groups can have different firing modes.
     */
    @Test
    public final void testFiringMode() {
        cut.setFiringMode(0, FiringMode.Optimal);
        cut.setFiringMode(1, FiringMode.ChainFire);
        cut.setFiringMode(2, FiringMode.AlphaStrike);

        assertEquals(FiringMode.Optimal, cut.getFiringMode(0));
        assertEquals(FiringMode.ChainFire, cut.getFiringMode(1));
        assertEquals(FiringMode.AlphaStrike, cut.getFiringMode(2));
    }

    /**
     * Test that a weapon can be in many groups and any group can have multiple weapons.
     */
    @Test
    public final void testGroup() {
        cut.setGroup(0, 0, true);
        cut.setGroup(1, 0, true);

        cut.setGroup(5, 15, true);
        cut.setGroup(5, 3, true);

        assertTrue(cut.isInGroup(0, 0));
        assertTrue(cut.isInGroup(1, 0));

        assertTrue(cut.isInGroup(5, 15));
        assertTrue(cut.isInGroup(5, 3));

        assertFalse(cut.isInGroup(4, 4));
    }

    /**
     * Test that weapons are returned in the order of the iterator of loadout.item(weapon) and that only offensive
     * weapons are included.
     */
    @Test
    public final void testWeaponOrder() {
        Weapon w0 = Mockito.mock(Weapon.class);
        Weapon w1 = Mockito.mock(Weapon.class);
        Weapon w2 = Mockito.mock(Weapon.class);

        Mockito.when(w0.isOffensive()).thenReturn(true);
        Mockito.when(w2.isOffensive()).thenReturn(true);

        weapons.add(w0);
        weapons.add(w1);
        weapons.add(w2);

        List<Weapon> ans = cut.getWeaponOrder();

        assertEquals(Arrays.asList(w0, w2), ans);
    }
    

    /**
     * Test that the correct weapons are returned for each group.
     */
    @Test
    public final void testGroupWeapons() {
        Weapon w0 = Mockito.mock(Weapon.class);
        Weapon w1 = Mockito.mock(Weapon.class);
        Weapon w2 = Mockito.mock(Weapon.class);
        Weapon w3 = Mockito.mock(Weapon.class);
        Weapon w4 = Mockito.mock(Weapon.class);

        Mockito.when(w0.toString()).thenReturn("w0");
        Mockito.when(w1.toString()).thenReturn("w1");
        Mockito.when(w2.toString()).thenReturn("w2");
        Mockito.when(w3.toString()).thenReturn("w3");
        Mockito.when(w4.toString()).thenReturn("w4");
        
        Mockito.when(w0.isOffensive()).thenReturn(true);
        Mockito.when(w2.isOffensive()).thenReturn(true);
        Mockito.when(w3.isOffensive()).thenReturn(true);
        Mockito.when(w4.isOffensive()).thenReturn(true);

        weapons.add(w0);
        weapons.add(w1);
        weapons.add(w2);
        weapons.add(w3);
        weapons.add(w4);

        cut.setGroup(0, 0, true);
        cut.setGroup(0, 1, true); // w1 is not offensive, so it is not enumerated.
        cut.setGroup(5, 2, true);
        cut.setGroup(5, 3, true);
        
        List<Weapon> ans0 = (List<Weapon>) cut.getWeapons(0);
        assertEquals(Arrays.asList(w0, w2), ans0);
        
        List<Weapon> ans1 = (List<Weapon>) cut.getWeapons(5);
        assertEquals(Arrays.asList(w3, w4), ans1);
    }
}

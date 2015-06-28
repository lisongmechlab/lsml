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
package lisong_mechlab.model.item;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * Test suite for ballistic weapons.
 * 
 * @author Li Song
 */
public class BallisticWeaponTest {

    @Test
    public void testCUAC10(){
        BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup(1206);
        
        assertTrue(cut.getName().contains("C-ULTRA AC/10"));
        assertEquals(4, cut.getAmmoPerPerShot());
        assertEquals(10.0, cut.getDamagePerShot(), 0.0);
        
        double expectedSecondsPerShot = cut.getCoolDown(null) + 0.11 * 4;
        
        assertEquals(expectedSecondsPerShot, cut.getRawSecondsPerShot(null), 0.0);
    }
    
    @Test
    public void testLB10X_Damage(){
        BallisticWeapon cut = (BallisticWeapon) ItemDB.lookup(1023);
        
        assertTrue(cut.getName().contains("LB 10-X AC"));
        assertEquals(1, cut.getAmmoPerPerShot());
        assertEquals(10.0, cut.getDamagePerShot(), 0.0);
    }
}

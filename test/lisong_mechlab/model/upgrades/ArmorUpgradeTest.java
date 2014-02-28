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
package lisong_mechlab.model.upgrades;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test suite for {@link ArmorUpgrade}.
 * 
 * @author Li Song
 */
public class ArmorUpgradeTest{
   /**
    * Test properties of standard armor
    */
   @Test
   public void testStandardArmor(){
      final int sa_id = 2810;
      ArmorUpgrade cut = (ArmorUpgrade)UpgradeDB.lookup(sa_id);

      assertNotNull(cut);
      assertEquals(sa_id, cut.getMwoId());
      assertEquals("STANDARD ARMOR", cut.getName());
      assertFalse(cut.getDescription().equals(""));
      assertEquals(0, cut.getExtraSlots());
      assertEquals(32.0, cut.getArmorPerTon(), 0.0);
      assertEquals(2.0, cut.getArmorMass(64), 0.0);
   }

   /**
    * Test properties of Ferro-Fibrous armor
    */
   @Test
   public void testFerroFibrousArmor(){
      final int ff_id = 2811;
      ArmorUpgrade cut = (ArmorUpgrade)UpgradeDB.lookup(ff_id);

      assertNotNull(cut);
      assertEquals(ff_id, cut.getMwoId());
      assertEquals("FERRO FIBROUS ARMOR", cut.getName());
      assertFalse(cut.getDescription().equals(""));
      assertEquals(14, cut.getExtraSlots());
      assertEquals(35.84, cut.getArmorPerTon(), 0.0);
      assertEquals(64.0 / 35.84, cut.getArmorMass(64), 0.0);
   }
}

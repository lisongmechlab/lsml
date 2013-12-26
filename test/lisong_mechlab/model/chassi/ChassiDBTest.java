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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class ChassiDBTest{

   @Test(expected = IllegalArgumentException.class)
   public void testLookupFailed(){
      // Successful lookup is tested by ChassiTest.java
      ChassiDB.lookup("Nonexistent mech");
   }

   @Test
   public void testLookupByChassiSeries(){
      List<Chassi> cataphracts = ChassiDB.lookupSeries("CATAphract");
      List<Chassi> cataphracts1 = ChassiDB.lookupSeries("CTF");

      assertEquals(cataphracts, cataphracts1);

      assertTrue(cataphracts.remove(ChassiDB.lookup("ILYA MUROMETS")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-1X")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-2X")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-3D")));
      assertTrue(cataphracts.remove(ChassiDB.lookup("CTF-4X")));
      assertTrue(cataphracts.isEmpty());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testLookupByChassiSeriesFail(){
      ChassiDB.lookupSeries("No such series");
   }

   @Test
   public void testLookupByChassiClass(){
      List<Chassi> heavies = ChassiDB.lookup(ChassiClass.HEAVY);

      assertTrue(heavies.contains(ChassiDB.lookup("ILYA MUROMETS")));
      assertTrue(heavies.contains(ChassiDB.lookup("JM6-DD")));
      assertTrue(heavies.contains(ChassiDB.lookup("CPLT-C4")));
      assertTrue(heavies.contains(ChassiDB.lookup("FLAME")));
      assertTrue(heavies.contains(ChassiDB.lookup("PROTECTOR")));

      for(Chassi chassi : heavies){
         assertEquals(ChassiClass.HEAVY, chassi.getChassiClass());
      }
   }

   /**
    * Test that the assault group contains the right mechs
    */
   @Test
   public void testLookupByChassiClass_Assault(){
      List<Chassi> heavies = ChassiDB.lookup(ChassiClass.ASSAULT);

      assertTrue(heavies.contains(ChassiDB.lookup("PRETTY BABY")));
      assertTrue(heavies.contains(ChassiDB.lookup("DRAGON SLAYER")));
      assertTrue(heavies.contains(ChassiDB.lookup("MISERY")));
      assertTrue(heavies.contains(ChassiDB.lookup("AS7-D-DC")));

      for(Chassi chassi : heavies){
         assertEquals(ChassiClass.ASSAULT, chassi.getChassiClass());
      }
   }

}

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

import java.util.Collection;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ChassiDBTest{

   @Test(expected = IllegalArgumentException.class)
   public void testLookupFailed(){
      // Successful lookup is tested by ChassiTest.java
      ChassisDB.lookup("Nonexistent mech");
   }

   @Test
   public void testLookupByChassiSeries(){
      Collection<Chassis> cataphracts = ChassisDB.lookupSeries("CATAphract");
      Collection<Chassis> cataphracts1 = ChassisDB.lookupSeries("CTF");

      assertEquals(cataphracts, cataphracts1);

      assertTrue(cataphracts.remove(ChassisDB.lookup("ILYA MUROMETS")));
      assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-1X")));
      assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-2X")));
      assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-3D")));
      assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-3D(C)")));
      assertTrue(cataphracts.remove(ChassisDB.lookup("CTF-4X")));
      assertTrue(cataphracts.isEmpty());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testLookupByChassiSeriesFail(){
      ChassisDB.lookupSeries("No such series");
   }

   @Test
   public void testLookupByChassiClass(){
      Collection<Chassis> heavies = ChassisDB.lookup(ChassisClass.HEAVY);

      assertTrue(heavies.contains(ChassisDB.lookup("ILYA MUROMETS")));
      assertTrue(heavies.contains(ChassisDB.lookup("JM6-DD")));
      assertTrue(heavies.contains(ChassisDB.lookup("CPLT-C4")));
      assertTrue(heavies.contains(ChassisDB.lookup("FLAME")));
      assertTrue(heavies.contains(ChassisDB.lookup("PROTECTOR")));

      for(Chassis chassi : heavies){
         assertEquals(ChassisClass.HEAVY, chassi.getChassiClass());
      }
   }

   /**
    * Test that the assault group contains the right mechs
    */
   @Test
   public void testLookupByChassiClass_Assault(){
      Collection<Chassis> heavies = ChassisDB.lookup(ChassisClass.ASSAULT);

      assertTrue(heavies.contains(ChassisDB.lookup("PRETTY BABY")));
      assertTrue(heavies.contains(ChassisDB.lookup("DRAGON SLAYER")));
      assertTrue(heavies.contains(ChassisDB.lookup("MISERY")));
      assertTrue(heavies.contains(ChassisDB.lookup("AS7-D-DC")));

      for(Chassis chassi : heavies){
         assertEquals(ChassisClass.ASSAULT, chassi.getChassiClass());
      }
   }

   /**
    * {@link ChassisDB#lookupVariations(Chassis)} shall return a list of all chassis variations for the given chassis
    * (including the chassis given as argument).
    * 
    * @param aLookup
    *           The chassis name to use as a lookup.
    * @param aExpected
    *           The expected chassis in addition to the lookup.
    */
   @Parameters({"SDR-5K, SDR-5K(C)", "SDR-5K(C), SDR-5K", "HBK-4P, HBK-4P(C)", "HBK-4P(C), HBK-4P", "CTF-3D, CTF-3D(C)", "CTF-3D(C), CTF-3D(C)", "TDR-5S(P), TDR-5S", "TDR-5S, TDR-5S(P)"})
   @Test
   public void testLookupVariations_LookupFromNormal(String aLookup, String aExpected){
      Collection<Chassis> ans = ChassisDB.lookupVariations(ChassisDB.lookup(aLookup));
      assertTrue(ans.contains(ChassisDB.lookup(aLookup)));
      assertTrue(ans.contains(ChassisDB.lookup(aExpected)));
      assertEquals(2, ans.size());
   }

}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test suite for {@link Chassi}.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class ChassiTest{

   @Test(expected = UnsupportedOperationException.class)
   public void getParts_NoMod(){
      Chassi cut = ChassiDB.lookup("Ilya Muromets");
      cut.getInternalParts().add(null);
   }

   @Parameters({"HBK-4J, HBK-4P","CTF-3D, Ilya Muromets"})
   @Test
   public void testIsSameSeries(String aChassiA, String aChassiB){
      assertTrue(ChassiDB.lookup(aChassiA).isSameSeries(ChassiDB.lookup(aChassiB)));
   }
   
   @Parameters({"HBK-4J, CTF-3D","EMBER, Ilya Muromets"})
   @Test
   public void testIsNotSameSeries(String aChassiA, String aChassiB){
      assertFalse(ChassiDB.lookup(aChassiA).isSameSeries(ChassiDB.lookup(aChassiB)));
   }
   
   @Parameters({"SDR-5K(C)", "JR7-D(S)", "CDA-2A(C)"})
   @Test
   public void testIsSpecialVariant(String aChassiA){
      assertTrue(ChassiDB.lookup(aChassiA).isSpecialVariant());
   }
   
   @Parameters({"SDR-5K", "JR7-D", "CDA-2A"})
   @Test
   public void testIsNotSpecialVariant(String aChassiA){
      assertFalse(ChassiDB.lookup(aChassiA).isSpecialVariant());
   }
   
   @Test
   public void testLoadHeroMech(){
      Chassi cut = ChassiDB.lookup("Ilya Muromets");

      assertEquals(140, cut.getEngineMin());
      assertEquals(340, cut.getEngineMax());

      assertEquals("ILYA MUROMETS", cut.getName());
      assertEquals("CTF-IM", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("ctf-im", cut.getMwoName());

      assertEquals(7.0, cut.getInternalMass(), 0.0);
      assertEquals(70.0, cut.getMassMax(), 0.0);

      assertEquals(434, cut.getArmorMax());

      assertEquals(16.2, cut.getSpeedFactor(), 0.0);

      assertSame(ChassiClass.HEAVY, cut.getChassiClass());
      assertEquals(0, cut.getMaxJumpJets());
      assertEquals(0, cut.getHardpointsCount(HardpointType.ECM));

      // Do a through test only on the Ilyas components
      {
         InternalPart pt = cut.getInternalPart(Part.Head);

         assertEquals(18, pt.getArmorMax());
         assertEquals(15.0, pt.getHitpoints(), 0.0);
         assertEquals(6, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.Head, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());

         assertEquals(3, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.RightArm);
         assertEquals(44, pt.getArmorMax());
         assertEquals(22.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(1, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(1, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.RightArm, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(3, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.LeftArm);
         assertEquals(44, pt.getArmorMax());
         assertEquals(22.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(1, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.LeftArm, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.RightTorso);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(1, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(1, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.RightTorso, pt.getType());
         assertTrue(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(0, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.LeftTorso);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(1, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(1, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.LeftTorso, pt.getType());
         assertTrue(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(0, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.CenterTorso);
         assertEquals(88, pt.getArmorMax());
         assertEquals(44.0, pt.getHitpoints(), 0.0);
         assertEquals(12, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.CenterTorso, pt.getType());
         assertTrue(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(1, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.RightLeg);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(6, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.RightLeg, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }

      {
         InternalPart pt = cut.getInternalPart(Part.LeftLeg);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitpoints(), 0.0);
         assertEquals(6, pt.getNumCriticalslots());
         assertEquals(0, pt.getNumHardpoints(HardpointType.ENERGY));
         assertEquals(0, pt.getNumHardpoints(HardpointType.BALLISTIC));
         assertEquals(0, pt.getNumHardpoints(HardpointType.AMS));
         assertEquals(0, pt.getNumHardpoints(HardpointType.MISSILE));
         assertEquals(Part.LeftLeg, pt.getType());
         assertFalse(pt.getType().isTwoSided());
         assertEquals(pt.getType().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }
   }

   @Test
   public void testLoadHasECM(){
      Chassi cut = ChassiDB.lookup("AS7-D-DC");

      assertEquals(200, cut.getEngineMin());
      assertEquals(360, cut.getEngineMax());

      assertEquals("ATLAS AS7-D-DC", cut.getName());
      assertEquals("AS7-D-DC", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("as7-d-dc", cut.getMwoName());

      assertEquals(10.0, cut.getInternalMass(), 0.0);
      assertEquals(100.0, cut.getMassMax(), 0.0);

      assertEquals(614, cut.getArmorMax());

      assertSame(ChassiClass.ASSAULT, cut.getChassiClass());
      assertEquals(0, cut.getMaxJumpJets());
      assertEquals(1, cut.getHardpointsCount(HardpointType.ECM));

      assertEquals(3, cut.getInternalPart(Part.Head).getInternalItems().size());

      assertEquals(4, cut.getInternalPart(Part.RightArm).getInternalItems().size());
      assertEquals(4, cut.getInternalPart(Part.LeftArm).getInternalItems().size());

      assertEquals(4, cut.getInternalPart(Part.RightLeg).getInternalItems().size());
      assertEquals(4, cut.getInternalPart(Part.LeftLeg).getInternalItems().size());

      assertEquals(1, cut.getInternalPart(Part.RightArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(1, cut.getInternalPart(Part.LeftArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(1, cut.getInternalPart(Part.LeftArm).getNumHardpoints(HardpointType.AMS));
      assertEquals(3, cut.getInternalPart(Part.LeftTorso).getNumHardpoints(HardpointType.MISSILE));
      assertEquals(2, cut.getInternalPart(Part.RightTorso).getNumHardpoints(HardpointType.BALLISTIC));
   }

   @Test
   public void testLoadHasJJ(){
      Chassi cut = ChassiDB.lookup("Jenner JR7-F");

      assertEquals(70, cut.getEngineMin()); // However no such engine exists :)
      assertEquals(300, cut.getEngineMax());

      assertEquals("JENNER JR7-F", cut.getName());
      assertEquals("JR7-F", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("jr7-f", cut.getMwoName());

      assertEquals(3.5, cut.getInternalMass(), 0.0);
      assertEquals(35.0, cut.getMassMax(), 0.0);

      assertEquals(238, cut.getArmorMax());

      assertSame(ChassiClass.LIGHT, cut.getChassiClass());
      assertEquals(5, cut.getMaxJumpJets());
      assertEquals(0, cut.getHardpointsCount(HardpointType.ECM));

      assertEquals(3, cut.getInternalPart(Part.Head).getInternalItems().size());

      assertEquals(2, cut.getInternalPart(Part.RightArm).getInternalItems().size());
      assertEquals(2, cut.getInternalPart(Part.LeftArm).getInternalItems().size());

      assertEquals(4, cut.getInternalPart(Part.RightLeg).getInternalItems().size());
      assertEquals(4, cut.getInternalPart(Part.LeftLeg).getInternalItems().size());

      assertEquals(3, cut.getInternalPart(Part.RightArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(3, cut.getInternalPart(Part.LeftArm).getNumHardpoints(HardpointType.ENERGY));
      assertEquals(1, cut.getInternalPart(Part.LeftTorso).getNumHardpoints(HardpointType.AMS));
   }
}

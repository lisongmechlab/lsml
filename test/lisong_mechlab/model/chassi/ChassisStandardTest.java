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
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test suite for {@link ChassisStandard}.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class ChassisStandardTest{

   /**
    * Internal parts list can not be modified.
    */
   @Test(expected = UnsupportedOperationException.class)
   public void getParts_NoMod(){
      // Setup
      ChassisStandard cut = ChassisDB.lookup("Ilya Muromets");

      // Execute
      cut.getComponents().add(null);
   }

   @Test
   public void testIsHero(){
      ChassisStandard ilya = ChassisDB.lookup("Ilya Muromets");
      assertEquals(ChassisVariant.HERO, ilya.getVariantType());

      ChassisStandard ctf3d = ChassisDB.lookup("CTF-3D");
      assertEquals(ChassisVariant.NORMAL, ctf3d.getVariantType());
   }

   @Parameters({"HBK-4J, HBK-4P", "CTF-3D, Ilya Muromets"})
   @Test
   public void testIsSameSeries(String aChassiA, String aChassiB){
      assertTrue(ChassisDB.lookup(aChassiA).isSameSeries(ChassisDB.lookup(aChassiB)));
   }

   @Parameters({"HBK-4J, CTF-3D", "EMBER, Ilya Muromets"})
   @Test
   public void testIsNotSameSeries(String aChassiA, String aChassiB){
      assertFalse(ChassisDB.lookup(aChassiA).isSameSeries(ChassisDB.lookup(aChassiB)));
   }

   @Parameters({"SDR-5K(C)", "JR7-D(S)", "CDA-2A(C)"})
   @Test
   public void testIsSpecialVariant(String aChassiA){
      assertTrue(ChassisDB.lookup(aChassiA).getVariantType().isVariation());
   }

   @Parameters({"SDR-5K", "JR7-D", "CDA-2A"})
   @Test
   public void testIsNotSpecialVariant(String aChassiA){
      assertFalse(ChassisDB.lookup(aChassiA).getVariantType().isVariation());
   }

   @Test
   public void testLoadHeroMech(){
      ChassisStandard cut = ChassisDB.lookup("Ilya Muromets");

      assertEquals(140, cut.getEngineMin());
      assertEquals(340, cut.getEngineMax());

      assertEquals("ILYA MUROMETS", cut.getName());
      assertEquals("CTF-IM", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("ctf-im", cut.getMwoName());

      assertEquals(70.0, cut.getMassMax(), 0.0);

      assertEquals(434, cut.getArmorMax());

      assertEquals(16.2, cut.getMovementProfile().getMaxMovementSpeed(), 0.0);

      assertSame(ChassisClass.HEAVY, cut.getChassiClass());
      assertEquals(0, cut.getJumpJetsMax());
      assertEquals(0, cut.getHardpointsCount(HardPointType.ECM));

      // Do a through test only on the Ilyas components
      {
         InternalComponent pt = cut.getComponent(Location.Head);

         assertEquals(18, pt.getArmorMax());
         assertEquals(15.0, pt.getHitPoints(), 0.0);
         assertEquals(6, pt.getSlots());
         assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.Head, pt.getLocation());
         assertFalse(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());

         assertEquals(3, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.RightArm);
         assertEquals(44, pt.getArmorMax());
         assertEquals(22.0, pt.getHitPoints(), 0.0);
         assertEquals(12, pt.getSlots());
         assertEquals(1, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(1, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.RightArm, pt.getLocation());
         assertFalse(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(3, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.LeftArm);
         assertEquals(44, pt.getArmorMax());
         assertEquals(22.0, pt.getHitPoints(), 0.0);
         assertEquals(12, pt.getSlots());
         assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(1, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.LeftArm, pt.getLocation());
         assertFalse(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.RightTorso);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitPoints(), 0.0);
         assertEquals(12, pt.getSlots());
         assertEquals(1, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(1, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.RightTorso, pt.getLocation());
         assertTrue(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(0, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.LeftTorso);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitPoints(), 0.0);
         assertEquals(12, pt.getSlots());
         assertEquals(1, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(1, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.LeftTorso, pt.getLocation());
         assertTrue(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(0, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.CenterTorso);
         assertEquals(88, pt.getArmorMax());
         assertEquals(44.0, pt.getHitPoints(), 0.0);
         assertEquals(12, pt.getSlots());
         assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.CenterTorso, pt.getLocation());
         assertTrue(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(1, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.RightLeg);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitPoints(), 0.0);
         assertEquals(6, pt.getSlots());
         assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.RightLeg, pt.getLocation());
         assertFalse(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }

      {
         InternalComponent pt = cut.getComponent(Location.LeftLeg);
         assertEquals(60, pt.getArmorMax());
         assertEquals(30.0, pt.getHitPoints(), 0.0);
         assertEquals(6, pt.getSlots());
         assertEquals(0, pt.getHardPointCount(HardPointType.ENERGY));
         assertEquals(0, pt.getHardPointCount(HardPointType.BALLISTIC));
         assertEquals(0, pt.getHardPointCount(HardPointType.AMS));
         assertEquals(0, pt.getHardPointCount(HardPointType.MISSILE));
         assertEquals(Location.LeftLeg, pt.getLocation());
         assertFalse(pt.getLocation().isTwoSided());
         assertEquals(pt.getLocation().toString(), pt.toString());
         assertEquals(4, pt.getInternalItems().size());
      }
   }

   @Test
   public void testLoadHasECM(){
      ChassisStandard cut = ChassisDB.lookup("AS7-D-DC");

      assertEquals(200, cut.getEngineMin());
      assertEquals(360, cut.getEngineMax());

      assertEquals("ATLAS AS7-D-DC", cut.getName());
      assertEquals("AS7-D-DC", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("as7-d-dc", cut.getMwoName());

      assertEquals(100.0, cut.getMassMax(), 0.0);

      assertEquals(614, cut.getArmorMax());

      assertSame(ChassisClass.ASSAULT, cut.getChassiClass());
      assertEquals(0, cut.getJumpJetsMax());
      assertEquals(1, cut.getHardpointsCount(HardPointType.ECM));

      assertEquals(3, cut.getComponent(Location.Head).getInternalItems().size());

      assertEquals(4, cut.getComponent(Location.RightArm).getInternalItems().size());
      assertEquals(4, cut.getComponent(Location.LeftArm).getInternalItems().size());

      assertEquals(4, cut.getComponent(Location.RightLeg).getInternalItems().size());
      assertEquals(4, cut.getComponent(Location.LeftLeg).getInternalItems().size());

      assertEquals(1, cut.getComponent(Location.RightArm).getHardPointCount(HardPointType.ENERGY));
      assertEquals(1, cut.getComponent(Location.LeftArm).getHardPointCount(HardPointType.ENERGY));
      assertEquals(1, cut.getComponent(Location.LeftArm).getHardPointCount(HardPointType.AMS));
      assertEquals(3, cut.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.MISSILE));
      assertEquals(2, cut.getComponent(Location.RightTorso).getHardPointCount(HardPointType.BALLISTIC));
   }

   @Test
   public void testLoadHasJJ(){
      ChassisStandard cut = ChassisDB.lookup("Jenner JR7-F");

      assertEquals(70, cut.getEngineMin()); // However no such engine exists :)
      assertEquals(300, cut.getEngineMax());

      assertEquals("JENNER JR7-F", cut.getName());
      assertEquals("JR7-F", cut.getNameShort());
      assertEquals(cut.getNameShort(), cut.toString());
      assertEquals("jr7-f", cut.getMwoName());

      assertEquals(35.0, cut.getMassMax(), 0.0);

      assertEquals(238, cut.getArmorMax());

      assertSame(ChassisClass.LIGHT, cut.getChassiClass());
      assertEquals(5, cut.getJumpJetsMax());
      assertEquals(0, cut.getHardpointsCount(HardPointType.ECM));

      assertEquals(3, cut.getComponent(Location.Head).getInternalItems().size());

      assertEquals(2, cut.getComponent(Location.RightArm).getInternalItems().size());
      assertEquals(2, cut.getComponent(Location.LeftArm).getInternalItems().size());

      assertEquals(4, cut.getComponent(Location.RightLeg).getInternalItems().size());
      assertEquals(4, cut.getComponent(Location.LeftLeg).getInternalItems().size());

      assertEquals(3, cut.getComponent(Location.RightArm).getHardPointCount(HardPointType.ENERGY));
      assertEquals(3, cut.getComponent(Location.LeftArm).getHardPointCount(HardPointType.ENERGY));
      assertEquals(1, cut.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.AMS));
   }

   @Test
   public void testIsAllowed_JJ(){
      ChassisStandard jj55tons = ChassisDB.lookup("WVR-6R");
      ChassisStandard jj70tons = ChassisDB.lookup("QKD-4G");
      ChassisStandard nojj55tons = ChassisDB.lookup("KTO-18");

      Item classIV = ItemDB.lookup("JUMP JETS - CLASS IV");
      Item classIII = ItemDB.lookup("JUMP JETS - CLASS III");

      assertFalse(nojj55tons.isAllowed(classIV));
      assertFalse(nojj55tons.isAllowed(classIII));
      assertTrue(jj55tons.isAllowed(classIV));
      assertFalse(jj55tons.isAllowed(classIII));
      assertFalse(jj70tons.isAllowed(classIV));
      assertTrue(jj70tons.isAllowed(classIII));
   }

   @Test
   public void testIsAllowed_Engine(){
      ChassisStandard cut = ChassisDB.lookup("ILYA MUROMETS");

      Item tooSmall = ItemDB.lookup("STD ENGINE 135");
      Item tooLarge = ItemDB.lookup("STD ENGINE 345");
      Item smallest = ItemDB.lookup("STD ENGINE 140");
      Item largest = ItemDB.lookup("STD ENGINE 340");

      assertFalse(cut.isAllowed(tooSmall));
      assertTrue(cut.isAllowed(smallest));
      assertTrue(cut.isAllowed(largest));
      assertFalse(cut.isAllowed(tooLarge));
   }

   @Test
   public void testIsAllowed_Hardpoints(){
      ChassisStandard cut = ChassisDB.lookup("ILYA MUROMETS");

      Item lrm20 = ItemDB.lookup("LRM 20");
      Item ac20 = ItemDB.lookup("AC/20");

      assertFalse(cut.isAllowed(lrm20));
      assertTrue(cut.isAllowed(ac20));
   }
}

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
package lisong_mechlab.model.chassi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class InternalPartTest{

   Chassis chassi = ChassiDB.lookup("TDR-5S");

   @Test(expected = UnsupportedOperationException.class)
   public void testGetHardpoints_Immutable() throws Exception{
      InternalPart cut = chassi.getInternalPart(Part.CenterTorso);
      cut.getHardpoints().add(new HardPoint(HardPointType.ENERGY));
   }

   @Test
   public void testGetHardpoints() throws Exception{
      Collection<HardPoint> hardpoints = chassi.getInternalPart(Part.RightTorso).getHardpoints();
      assertEquals(3, hardpoints.size());

      List<HardPoint> hps = new ArrayList<>(hardpoints);
      boolean foundAms = false;
      boolean foundLrm10 = false;
      boolean foundLrm20 = false;
      for(HardPoint hardpoint : hps){
         if( hardpoint.getType() == HardPointType.AMS ){
            if( foundAms )
               fail("Two ams when only one expected!");
            foundAms = true;
         }
         else if( hardpoint.getType() == HardPointType.MISSILE ){
            if( hardpoint.getNumMissileTubes() == 20 ){
               if( foundLrm20 )
                  fail("Expected only one 20-tuber!");
               foundLrm20 = true;
            }
            else if( hardpoint.getNumMissileTubes() == 10 ){
               if( foundLrm10 )
                  fail("Expected only one 10-tuber!");
               foundLrm10 = true;
            }
            else
               fail("Unexpected tube count!");
         }
         else
            fail("Unexpected hardpoint!");

      }

      assertTrue(foundAms);
      assertTrue(foundLrm10);
      assertTrue(foundLrm20);
   }

   @Test
   public void testGetType() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         assertSame(part, cut.getType());
      }
   }

   @Test
   public void testGetArmorMax() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         if( part == Part.Head ){
            assertEquals(18, cut.getArmorMax());
         }
         else{
            assertEquals((int)cut.getHitpoints() * 2, cut.getArmorMax());
         }
      }
   }

   @Test
   public void testGetNumCriticalslots() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         if( part == Part.Head || part == Part.RightLeg || part == Part.LeftLeg ){
            assertEquals(6, cut.getNumCriticalslots());
         }
         else{
            assertEquals(12, cut.getNumCriticalslots());
         }
      }
   }

   @Test
   public void testGetNumHardpoints() throws Exception{
      assertEquals(3, chassi.getInternalPart(Part.LeftTorso).getNumHardpoints(HardPointType.ENERGY));
      assertEquals(0, chassi.getInternalPart(Part.LeftTorso).getNumHardpoints(HardPointType.BALLISTIC));

      assertEquals(1, chassi.getInternalPart(Part.RightTorso).getNumHardpoints(HardPointType.AMS));
      assertEquals(2, chassi.getInternalPart(Part.RightTorso).getNumHardpoints(HardPointType.MISSILE));
   }

   @Test
   public void testGetInternalItems() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         switch( part ){
            case Head:
               assertEquals(3, cut.getInternalItems().size());
               break;
            case RightLeg:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case RightArm:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case LeftArm:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case LeftLeg:
               assertEquals(4, cut.getInternalItems().size());
               break;
            case CenterTorso:
               assertEquals(1, cut.getInternalItems().size());
               break;
            default:
               break;
         }
      }
   }

   @Test(expected = UnsupportedOperationException.class)
   public void testGetInternalItems_Immutable() throws Exception{
      InternalPart cut = chassi.getInternalPart(Part.LeftLeg);
      cut.getInternalItems().add(LoadoutPart.ENGINE_INTERNAL);
   }

   @Test
   public void testGetHitpoints() throws Exception{
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);

         switch( part ){
            case RightArm:
               assertEquals(20, cut.getHitpoints(), 0.0);
               break;
            case RightTorso:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case RightLeg:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case Head:
               assertEquals(15, cut.getHitpoints(), 0.0);
               break;
            case CenterTorso:
               assertEquals(42, cut.getHitpoints(), 0.0);
               break;
            case LeftTorso:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case LeftLeg:
               assertEquals(30, cut.getHitpoints(), 0.0);
               break;
            case LeftArm:
               assertEquals(20, cut.getHitpoints(), 0.0);
               break;
            default:
               break;
         }
      }
   }

   @Test
   public void testIsAllowed_Internals(){
      Internal internal = Mockito.mock(Internal.class);
      Mockito.when(internal.getNumCriticalSlots(Matchers.any(Upgrades.class))).thenReturn(1);
      Mockito.when(internal.getMass(Matchers.any(Upgrades.class))).thenReturn(0.0);
      Mockito.when(internal.getHardpointType()).thenReturn(HardPointType.NONE);
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);
         assertFalse(cut.isAllowed(internal));
      }
   }

   @Test
   public void testIsAllowed_Engine(){
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 200");
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);
         if( part == Part.CenterTorso ){
            assertTrue(cut.isAllowed(engine));
         }
         else{
            assertFalse(cut.isAllowed(engine));
         }
      }
   }

   @Test
   public void testIsAllowed_Jumpjets(){
      JumpJet jj = (JumpJet)ItemDB.lookup("JUMP JETS - CLASS III");
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);
         if( part == Part.CenterTorso || part == Part.RightTorso || part == Part.LeftTorso || part == Part.LeftLeg || part == Part.RightLeg ){
            assertTrue(cut.isAllowed(jj));
         }
         else{
            assertFalse(cut.isAllowed(jj));
         }
      }
   }

   @Test
   public void testIsAllowed_Case(){
      Item case_module = ItemDB.CASE;
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);
         if( part == Part.RightTorso || part == Part.LeftTorso ){
            assertTrue(cut.isAllowed(case_module));
         }
         else{
            assertFalse(cut.isAllowed(case_module));
         }
      }
   }

   @Test
   public void testIsAllowed_Hardpoints(){
      assertFalse(chassi.getInternalPart(Part.LeftArm).isAllowed(ItemDB.lookup("PPC")));
      assertFalse(chassi.getInternalPart(Part.LeftArm).isAllowed(ItemDB.lookup("LRM 20")));
      assertFalse(chassi.getInternalPart(Part.LeftArm).isAllowed(ItemDB.lookup("AMS")));
      assertFalse(chassi.getInternalPart(Part.LeftArm).isAllowed(ItemDB.lookup("GUARDIAN ECM")));
      assertTrue(chassi.getInternalPart(Part.LeftArm).isAllowed(ItemDB.lookup("AC/2")));
   }

   @Test
   public void testIsAllowed_Size(){
      assertFalse(chassi.getInternalPart(Part.LeftArm).isAllowed(ItemDB.lookup("AC/20")));
      
      assertFalse(chassi.getInternalPart(Part.LeftLeg).isAllowed(ItemDB.DHS));
      assertFalse(chassi.getInternalPart(Part.Head).isAllowed(ItemDB.DHS));
   }
   
   @Test
   public void testIsAllowed_Modules(){
      for(Part part : Part.values()){
         InternalPart cut = chassi.getInternalPart(part);
         assertTrue(cut.isAllowed(ItemDB.SHS));
      }
   }
}

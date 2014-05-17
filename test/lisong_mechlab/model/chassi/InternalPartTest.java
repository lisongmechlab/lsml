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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class InternalPartTest{

   ChassisStandard chassi = ChassisDB.lookup("TDR-5S");

   @Test(expected = UnsupportedOperationException.class)
   public void testGetHardpoints_Immutable() throws Exception{
      InternalComponent cut = chassi.getComponent(Location.CenterTorso);
      cut.getHardPoints().add(new HardPoint(HardPointType.ENERGY));
   }

   @Test
   public void testGetHardpoints() throws Exception{
      Collection<HardPoint> hardpoints = chassi.getComponent(Location.RightTorso).getHardPoints();
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
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);

         assertSame(part, cut.getLocation());
      }
   }

   @Test
   public void testGetArmorMax() throws Exception{
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);

         if( part == Location.Head ){
            assertEquals(18, cut.getArmorMax());
         }
         else{
            assertEquals((int)cut.getHitPoints() * 2, cut.getArmorMax());
         }
      }
   }

   @Test
   public void testGetNumCriticalslots() throws Exception{
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);

         if( part == Location.Head || part == Location.RightLeg || part == Location.LeftLeg ){
            assertEquals(6, cut.getSlots());
         }
         else{
            assertEquals(12, cut.getSlots());
         }
      }
   }

   @Test
   public void testGetNumHardpoints() throws Exception{
      assertEquals(3, chassi.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.ENERGY));
      assertEquals(0, chassi.getComponent(Location.LeftTorso).getHardPointCount(HardPointType.BALLISTIC));

      assertEquals(1, chassi.getComponent(Location.RightTorso).getHardPointCount(HardPointType.AMS));
      assertEquals(2, chassi.getComponent(Location.RightTorso).getHardPointCount(HardPointType.MISSILE));
   }

   @Test
   public void testGetInternalItems() throws Exception{
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);

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
      InternalComponent cut = chassi.getComponent(Location.LeftLeg);
      cut.getInternalItems().add(ConfiguredComponent.ENGINE_INTERNAL);
   }

   @Test
   public void testGetHitpoints() throws Exception{
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);

         switch( part ){
            case RightArm:
               assertEquals(20, cut.getHitPoints(), 0.0);
               break;
            case RightTorso:
               assertEquals(30, cut.getHitPoints(), 0.0);
               break;
            case RightLeg:
               assertEquals(30, cut.getHitPoints(), 0.0);
               break;
            case Head:
               assertEquals(15, cut.getHitPoints(), 0.0);
               break;
            case CenterTorso:
               assertEquals(42, cut.getHitPoints(), 0.0);
               break;
            case LeftTorso:
               assertEquals(30, cut.getHitPoints(), 0.0);
               break;
            case LeftLeg:
               assertEquals(30, cut.getHitPoints(), 0.0);
               break;
            case LeftArm:
               assertEquals(20, cut.getHitPoints(), 0.0);
               break;
            default:
               break;
         }
      }
   }

   @Test
   public void testIsAllowed_Internals(){
      Internal internal = Mockito.mock(Internal.class);
      Mockito.when(internal.getNumCriticalSlots()).thenReturn(1);
      Mockito.when(internal.getMass()).thenReturn(0.0);
      Mockito.when(internal.getHardpointType()).thenReturn(HardPointType.NONE);
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);
         assertFalse(cut.isAllowed(internal));
      }
   }

   @Test
   public void testIsAllowed_Engine(){
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 200");
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);
         if( part == Location.CenterTorso ){
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
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);
         if( part == Location.CenterTorso || part == Location.RightTorso || part == Location.LeftTorso || part == Location.LeftLeg
             || part == Location.RightLeg ){
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
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);
         if( part == Location.RightTorso || part == Location.LeftTorso ){
            assertTrue(cut.isAllowed(case_module));
         }
         else{
            assertFalse(cut.isAllowed(case_module));
         }
      }
   }

   @Test
   public void testIsAllowed_Hardpoints(){
      assertFalse(chassi.getComponent(Location.LeftArm).isAllowed(ItemDB.lookup("PPC")));
      assertFalse(chassi.getComponent(Location.LeftArm).isAllowed(ItemDB.lookup("LRM 20")));
      assertFalse(chassi.getComponent(Location.LeftArm).isAllowed(ItemDB.lookup("AMS")));
      assertFalse(chassi.getComponent(Location.LeftArm).isAllowed(ItemDB.lookup("GUARDIAN ECM")));
      assertTrue(chassi.getComponent(Location.LeftArm).isAllowed(ItemDB.lookup("AC/2")));
   }

   @Test
   public void testIsAllowed_Size(){
      assertFalse(chassi.getComponent(Location.LeftArm).isAllowed(ItemDB.lookup("AC/20")));

      assertFalse(chassi.getComponent(Location.LeftLeg).isAllowed(ItemDB.DHS));
      assertFalse(chassi.getComponent(Location.Head).isAllowed(ItemDB.DHS));
   }

   @Test
   public void testIsAllowed_Modules(){
      for(Location part : Location.values()){
         InternalComponent cut = chassi.getComponent(part);
         assertTrue(cut.isAllowed(ItemDB.SHS));
      }
   }
}

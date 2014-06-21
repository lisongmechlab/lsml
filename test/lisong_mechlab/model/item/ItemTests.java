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
package lisong_mechlab.model.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ItemTests{
   @Mock
   MessageXBar xBar;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
   }

   /**
    * According to: <a href=
    * "http://mwomercs.com/forums/topic/147990-paging-karl-bergkarl-berg-please-pick-up-the-white-courtesy-phone/page__view__findpost__p__3484591"
    * >here</a> C.A.S.E. can not be critically hit and should not be a part of the calculations.
    */
   @Test
   public void testIsCrittable_Case(){
      assertFalse(ItemDB.CASE.isCrittable());
   }

   /**
    * XL engine sides do affect the critical hit rolls.
    */
   @Test
   public void testIsCrittable_EngineSides(){
      assertTrue(ConfiguredComponentBase.ENGINE_INTERNAL.isCrittable());
      assertTrue(ConfiguredComponentBase.ENGINE_INTERNAL_CLAN.isCrittable());
   }

   @Test
   public void testJumpJets(){
      JumpJet jj = (JumpJet)ItemDB.lookup(1503); // Class IV JJ

      assertEquals(3.75, jj.getDuration(), 0);
      assertEquals(0.1, jj.getJumpHeat(), 0);
      assertEquals(39.3, jj.getForce(), 0);

      assertTrue(jj.getMinTons() > 0);
      assertTrue(jj.getMaxTons() > 0);
      assertTrue(jj.getMaxTons() > jj.getMinTons());
   }

   @Test
   public void testEngines() throws Exception{
      Engine std175 = (Engine)ItemDB.lookup("STD ENGINE 175");
      Engine std180 = (Engine)ItemDB.lookup("STD ENGINE 180");
      Engine xl330 = (Engine)ItemDB.lookup("XL ENGINE 330");
      Engine xl335 = (Engine)ItemDB.lookup("XL ENGINE 335");

      assertEquals(6, std175.getNumCriticalSlots());
      assertEquals(6, std180.getNumCriticalSlots());
      assertEquals(6, xl330.getNumCriticalSlots());
      assertEquals(6, xl335.getNumCriticalSlots());

      assertEquals(9.0, std175.getMass(), 0.0);
      assertEquals(9.0, std180.getMass(), 0.0);
      assertEquals(19.5, xl330.getMass(), 0.0);
      assertEquals(20.0, xl335.getMass(), 0.0);

      // Engines have a base heat of the dissipation equal to 2 standard heat sinks when using 100% throttle.
      assertEquals(0.2, std175.getHeat(null), 0.0);
      assertEquals(0.2, std180.getHeat(null), 0.0);
      assertEquals(0.2, xl330.getHeat(null), 0.0);
      assertEquals(0.2, xl335.getHeat(null), 0.0);

      assertEquals(EngineType.STD, std175.getType());
      assertEquals(EngineType.STD, std180.getType());
      assertEquals(EngineType.XL, xl330.getType());
      assertEquals(EngineType.XL, xl335.getType());
   }

   /**
    * 
    */
   @Test
   public void testAMS(){
      AmmoWeapon ams = (AmmoWeapon)ItemDB.lookup("AMS");
      assertSame(ams, ItemDB.AMS);
      assertEquals(1, ams.getNumCriticalSlots());
      assertEquals(0.5, ams.getMass(), 0.0);
      assertEquals(HardPointType.AMS, ams.getHardpointType());
   }

   /**
    * ECM/BAP/CC/CASE etc should exist and only be equippable on the correct mechs
    * 
    * @throws Exception
    */
   @Test
   public void testModules() throws Exception{

      Item ECM = ItemDB.lookup("GUARDIAN ECM");
      Item CC = ItemDB.lookup("COMMAND CONSOLE");
      Item BAP = ItemDB.lookup("BEAGLE ACTIVE PROBE");
      Item Case = ItemDB.lookup("C.A.S.E.");

      Item JJC3 = ItemDB.lookup("Jump Jets - Class III");
      Item JJC4 = ItemDB.lookup("Jump Jets - Class IV");
      Item JJC5 = ItemDB.lookup("Jump Jets - Class V");

      assertEquals(2, ECM.getNumCriticalSlots());
      assertEquals(1, CC.getNumCriticalSlots());
      assertEquals(2, BAP.getNumCriticalSlots());
      assertEquals(1, Case.getNumCriticalSlots());
      assertEquals(1, JJC3.getNumCriticalSlots());
      assertEquals(1, JJC4.getNumCriticalSlots());
      assertEquals(1, JJC5.getNumCriticalSlots());

      assertEquals(1.5, ECM.getMass(), 0.0);
      assertEquals(3, CC.getMass(), 0.0);
      assertEquals(1.5, BAP.getMass(), 0.0);
      assertEquals(0.5, Case.getMass(), 0.0);
      assertEquals(1, JJC3.getMass(), 0.0);
      assertEquals(0.5, JJC4.getMass(), 0.0);
      assertEquals(0.5, JJC5.getMass(), 0.0);

      assertEquals(HardPointType.ECM, ECM.getHardpointType());
   }

   /**
    * No item must exist twice in the database!
    */
   @Test
   public void testNoDoubles(){
      Collection<Item> items = ItemDB.lookup(Item.class);

      List<Item> found = new ArrayList<Item>();
      for(Item item : items){
         if( !found.contains(item) )
            found.add(item);
         else
            fail();
      }
   }

   /**
    * There must be heat sinks in the item database
    */
   @Test
   public void testHeatsinks(){
      Collection<HeatSink> heatsinks = ItemDB.lookup(HeatSink.class);

      // Should contain at least double and standard (+ typically clan versions)
      assertTrue(heatsinks.size() >= 2);

      // All parameters should be positive
      HeatSink shs = ItemDB.SHS;
      HeatSink dhs = ItemDB.DHS;
      for(HeatSink heatSink : heatsinks){
         assertTrue(heatSink.getDissipation() > 0);
         assertTrue(heatSink.getCapacity() > 0);
      }

      assertNotNull(dhs);
      assertNotNull(shs);

      // Double should have higher values than single
      assertTrue(dhs.getDissipation() > shs.getDissipation());
      assertTrue(dhs.getCapacity() > shs.getCapacity());

      assertEquals(3, dhs.getNumCriticalSlots());
      assertEquals(1, shs.getNumCriticalSlots());
   }
}

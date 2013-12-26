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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ItemTests{
   @Mock
   MessageXBar xBar;
   @Mock
   UndoStack   undoStack;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
   }

   @Test
   public void testJumpJets(){
      JumpJet jj = (JumpJet)ItemDB.lookup(1503); // Class IV JJ
      Chassi chassi_39t = mock(Chassi.class);
      Chassi chassi_40t = mock(Chassi.class);
      Chassi chassi_59t = mock(Chassi.class);
      Chassi chassi_60t = mock(Chassi.class);
      when(chassi_39t.getMassMax()).thenReturn(39);
      when(chassi_40t.getMassMax()).thenReturn(40);
      when(chassi_59t.getMassMax()).thenReturn(59);
      when(chassi_60t.getMassMax()).thenReturn(60);
      when(chassi_39t.getMaxJumpJets()).thenReturn(1);
      when(chassi_40t.getMaxJumpJets()).thenReturn(1);
      when(chassi_59t.getMaxJumpJets()).thenReturn(1);
      when(chassi_60t.getMaxJumpJets()).thenReturn(1);
      Loadout loadout_39t = mock(Loadout.class);
      Loadout loadout_40t = mock(Loadout.class);
      Loadout loadout_59t = mock(Loadout.class);
      Loadout loadout_60t = mock(Loadout.class);
      when(loadout_39t.getChassi()).thenReturn(chassi_39t);
      when(loadout_40t.getChassi()).thenReturn(chassi_40t);
      when(loadout_59t.getChassi()).thenReturn(chassi_59t);
      when(loadout_60t.getChassi()).thenReturn(chassi_60t);

      assertFalse(jj.isEquippableOn(loadout_39t)); // 39 tons
      assertTrue(jj.isEquippableOn(loadout_40t)); // 40 tons
      assertTrue(jj.isEquippableOn(loadout_59t)); // 59 tons
      assertFalse(jj.isEquippableOn(loadout_60t)); // 60 tons
      assertEquals(3.75, jj.getDuration(), 0);
      assertEquals(0.1, jj.getJumpHeat(), 0);
      assertEquals(39.3, jj.getForce(), 0);
   }

   @Test
   public void testEngines() throws Exception{
      Loadout hm = new Loadout(ChassiDB.lookup("Heavy Metal"), xBar, undoStack);

      Engine std175 = (Engine)ItemDB.lookup("STD ENGINE 175");
      Engine std180 = (Engine)ItemDB.lookup("STD ENGINE 180");
      Engine xl330 = (Engine)ItemDB.lookup("XL ENGINE 330");
      Engine xl335 = (Engine)ItemDB.lookup("XL ENGINE 335");

      assertEquals(6, std175.getNumCriticalSlots(hm.getUpgrades()));
      assertEquals(6, std180.getNumCriticalSlots(hm.getUpgrades()));
      // assertEquals(12, xl330.getNumCriticalSlots());
      // assertEquals(12, xl335.getNumCriticalSlots());

      assertEquals(9.0, std175.getMass(hm.getUpgrades()), 0.0);
      assertEquals(9.0, std180.getMass(hm.getUpgrades()), 0.0);
      assertEquals(19.5, xl330.getMass(hm.getUpgrades()), 0.0);
      assertEquals(20.0, xl335.getMass(hm.getUpgrades()), 0.0);

      // Heavy Metal can equip 180-330 engines
      assertFalse(std175.isEquippableOn(hm));
      assertTrue(std180.isEquippableOn(hm));
      assertTrue(xl330.isEquippableOn(hm));
      assertFalse(xl335.isEquippableOn(hm));

      // Engines have a base heat of the dissipation equal to 2 standard heat sinks when using 100% throttle.
      assertEquals(0.2, std175.getHeat(), 0.0);
      assertEquals(0.2, std180.getHeat(), 0.0);
      assertEquals(0.2, xl330.getHeat(), 0.0);
      assertEquals(0.2, xl335.getHeat(), 0.0);
   }

   /**
    * 
    */
   @Test
   public void testAMS(){
      AmmoWeapon ams = (AmmoWeapon)ItemDB.lookup("ANTI-MISSILE SYSTEM");
      assertSame(ams, ItemDB.AMS);
      assertEquals(1, ams.getNumCriticalSlots(null));
      assertEquals(0.5, ams.getMass(null), 0.0);
      assertEquals(HardpointType.AMS, ams.getHardpointType());
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

      assertEquals(2, ECM.getNumCriticalSlots(null));
      assertEquals(1, CC.getNumCriticalSlots(null));
      assertEquals(2, BAP.getNumCriticalSlots(null));
      assertEquals(1, Case.getNumCriticalSlots(null));
      assertEquals(1, JJC3.getNumCriticalSlots(null));
      assertEquals(1, JJC4.getNumCriticalSlots(null));
      assertEquals(1, JJC5.getNumCriticalSlots(null));

      assertEquals(1.5, ECM.getMass(null), 0.0);
      assertEquals(3, CC.getMass(null), 0.0);
      assertEquals(1.5, BAP.getMass(null), 0.0);
      assertEquals(0.5, Case.getMass(null), 0.0);
      assertEquals(1, JJC3.getMass(null), 0.0);
      assertEquals(0.5, JJC4.getMass(null), 0.0);
      assertEquals(0.5, JJC5.getMass(null), 0.0);

      assertTrue(ECM.isEquippableOn(new Loadout("AS7-D-DC", xBar, undoStack)));
      assertFalse(ECM.isEquippableOn(new Loadout("JR7-K", xBar, undoStack)));

      assertTrue(JJC5.isEquippableOn(new Loadout("JR7-F", xBar, undoStack)));
      assertFalse(JJC5.isEquippableOn(new Loadout("RVN-3L", xBar, undoStack)));
      assertFalse(JJC5.isEquippableOn(new Loadout("TBT-5J", xBar, undoStack)));
      assertFalse(JJC5.isEquippableOn(new Loadout("CTF-3D", xBar, undoStack)));

      assertFalse(JJC4.isEquippableOn(new Loadout("JR7-F", xBar, undoStack)));
      assertFalse(JJC4.isEquippableOn(new Loadout("hbk-4j", xBar, undoStack)));
      assertTrue(JJC4.isEquippableOn(new Loadout("TBT-5J", xBar, undoStack)));
      assertFalse(JJC4.isEquippableOn(new Loadout("CTF-3D", xBar, undoStack)));

      assertFalse(JJC3.isEquippableOn(new Loadout("JR7-F", xBar, undoStack)));
      assertFalse(JJC3.isEquippableOn(new Loadout("ILYA MUROMETS", xBar, undoStack)));
      assertFalse(JJC3.isEquippableOn(new Loadout("TBT-5J", xBar, undoStack)));
      assertTrue(JJC3.isEquippableOn(new Loadout("CTF-3D", xBar, undoStack)));
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
    * All weapons with ammo must have a valid ammo item and all ammo items are valid
    */
   @Test
   public void testWeaponsHaveAmmo(){
      Collection<AmmoWeapon> items = ItemDB.lookup(AmmoWeapon.class);

      for(AmmoWeapon item : items){
         Ammunition ammunition = item.getAmmoType(null);
         assertNotNull(ammunition);

         assertEquals(1.0, ammunition.getMass(null), 0.0); // All ammo weigh 1 ton!
         assertNotNull(ammunition.getName()); // All ammo must have a name!

         assertEquals(1, ammunition.getNumCriticalSlots(null));
         assertTrue(ammunition.getShotsPerTon() > 0);

         // The name of the ammo must be traceable to the weapon
         String weaponPart = item.getName();
         if( weaponPart.indexOf(" ") != -1 ){
            weaponPart = weaponPart.substring(0, weaponPart.indexOf(" "));
         }

         if( item == ItemDB.AMS ){
            assertTrue(ammunition.getName().equals("AMS AMMO"));
         }
         else{
            assertTrue(ammunition.getName().contains(weaponPart)); // The ammo name must contain part of the weapon
                                                                   // name!
         }
      }
   }

   /**
    * There must be heat sinks in the item database
    */
   @SuppressWarnings("null")
   @Test
   public void testHeatsinks(){
      Collection<HeatSink> heatsinks = ItemDB.lookup(HeatSink.class);

      // Should contain double and standard
      assertEquals(2, heatsinks.size());

      // All parameters should be positive
      HeatSink shs = null;
      HeatSink dhs = null;
      for(HeatSink heatSink : heatsinks){
         assertTrue(heatSink.getDissipation() > 0);
         assertTrue(heatSink.getCapacity() > 0);

         // Determine which is double/single
         if( null == shs ){
            shs = heatSink;
         }
         else{
            if( heatSink.getDissipation() > shs.getDissipation() ){
               dhs = heatSink;
            }
            else{
               dhs = shs;
               shs = heatSink;
            }
         }
      }

      assertNotNull(dhs);
      assertNotNull(shs);

      // Double should have higher values than single
      assertTrue(dhs.getDissipation() > shs.getDissipation());
      assertTrue(dhs.getCapacity() > shs.getCapacity());

      assertEquals(3, dhs.getNumCriticalSlots(null));
      assertEquals(1, shs.getNumCriticalSlots(null));
   }
}

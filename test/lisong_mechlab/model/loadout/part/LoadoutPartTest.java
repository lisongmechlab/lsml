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
package lisong_mechlab.model.loadout.part;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Test suite for {@link LoadoutPart}.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class LoadoutPartTest{
   @Mock
   MessageXBar          xBar;
   @Mock
   OperationStack       undoStack;
   MockLoadoutContainer mlc = new MockLoadoutContainer();

   @Mock
   InternalPart         part;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);

      Mockito.when(mlc.loadout.getNumCriticalSlotsFree()).thenReturn(20);
      Mockito.when(mlc.chassi.getMassMax()).thenReturn(100);
   }

   @After
   public void tearDown(){
      // We do not allow spurious messages on the crossbar!
      Mockito.verifyNoMoreInteractions(xBar);
      Mockito.verifyNoMoreInteractions(undoStack);
   }

   /**
    * Constructing a new {@link LoadoutPart} shall initialize the internal components and set armor to 0 on all sides.
    * 
    * @param aPart
    *           The part to test for
    */
   @Test
   @Parameters({"LeftArm", "RightTorso"})
   public void testLoadoutPart(Part aPart){
      // Setup
      List<Item> internals = new ArrayList<>();
      internals.add(mlc.makeInternal(2));
      internals.add(mlc.makeInternal(1));
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(aPart);
      Loadout loadout = Mockito.mock(Loadout.class);

      // Execute
      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Verify
      assertEquals(internals, cut.getItems());
      if( aPart.isTwoSided() ){
         assertEquals(0, cut.getArmor(ArmorSide.FRONT));
         assertEquals(0, cut.getArmor(ArmorSide.BACK));

      }
      else{
         assertEquals(0, cut.getArmor(ArmorSide.ONLY));
      }
      assertEquals(0, cut.getArmorTotal());
      assertSame(part, cut.getInternalPart());
      assertSame(loadout, cut.getLoadout());
   }

   @Test
   @Parameters({"LeftArm", "RightTorso"})
   public void testSetGetArmorAndTotalArmor(Part aPart){
      // Setup
      List<Item> internals = new ArrayList<>();
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(aPart);
      LoadoutPart cut = new LoadoutPart(null, part);
      int anAmount = 10;

      // Execute
      if( aPart.isTwoSided() ){
         cut.setArmor(ArmorSide.FRONT, anAmount);
         cut.setArmor(ArmorSide.BACK, 2 * anAmount);
      }
      else{
         cut.setArmor(ArmorSide.ONLY, anAmount);
      }

      // Execute & Verify
      if( aPart.isTwoSided() ){
         assertEquals(anAmount, cut.getArmor(ArmorSide.FRONT));
         assertEquals(2 * anAmount, cut.getArmor(ArmorSide.BACK));
         assertEquals(3 * anAmount, cut.getArmorTotal());
      }
      else{
         assertEquals(anAmount, cut.getArmor(ArmorSide.ONLY));
         assertEquals(anAmount, cut.getArmorTotal());
      }
   }

   @Test
   @Parameters({"LeftArm", "RightTorso"})
   public void testGetArmorMax(Part aPart){
      // Setup
      final int maxArmor = 10;
      List<Item> internals = new ArrayList<>();
      Mockito.when(part.getArmorMax()).thenReturn(maxArmor);
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(aPart);
      LoadoutPart cut = new LoadoutPart(null, part);

      if( aPart.isTwoSided() ){
         int front = 4;
         int back = 2;
         cut.setArmor(ArmorSide.FRONT, front);
         cut.setArmor(ArmorSide.BACK, back);

         // Execute & Verify
         assertEquals(maxArmor - back, cut.getArmorMax(ArmorSide.FRONT));
         assertEquals(maxArmor - front, cut.getArmorMax(ArmorSide.BACK));
      }
      else{
         // Execute & Verify
         assertEquals(maxArmor, cut.getArmorMax(ArmorSide.ONLY));
      }
   }

   @Test
   public void testGetItemMass(){
      // Setup
      List<Item> internals = new ArrayList<>();
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(Part.LeftTorso);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      LoadoutPart cut = new LoadoutPart(loadout, part);

      Item item0 = Mockito.mock(Item.class);
      Item item1 = Mockito.mock(Item.class);
      Mockito.when(item0.getMass(upgrades)).thenReturn(4.0);
      Mockito.when(item1.getMass(upgrades)).thenReturn(1.5);
      cut.addItem(item0);
      cut.addItem(item1);

      // Execute & Verify
      assertEquals(5.5, cut.getItemMass(), 0.0);
   }

   @Test
   public void testGetItemMass_engineHs(){
      // Setup
      final int engineHs = 2;
      List<Item> internals = new ArrayList<>();
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(Part.LeftTorso);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      LoadoutPart cut = new LoadoutPart(loadout, part);

      Item engine = ItemDB.lookup("STD ENGINE 400");
      cut.addItem(engine);
      for(int i = 0; i < engineHs; ++i)
         cut.addItem(ItemDB.SHS);

      // Execute & Verify
      assertEquals(engineHs * ItemDB.SHS.getMass(upgrades) + engine.getMass(upgrades), cut.getItemMass(), 0.0);
   }

   @Test
   public void testGetItems(){
      // Setup
      final int engineHs = 2;
      List<Item> internals = new ArrayList<>();
      Internal internal = mlc.makeInternal(2);
      internals.add(internal);
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(Part.LeftTorso);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      LoadoutPart cut = new LoadoutPart(loadout, part);

      Item engine = ItemDB.lookup("STD ENGINE 400");
      cut.addItem(engine);
      for(int i = 0; i < engineHs; ++i)
         cut.addItem(ItemDB.SHS);

      // Execute
      List<Item> ans = new ArrayList<>(cut.getItems());

      // Verify
      assertTrue(ans.remove(internal));
      assertTrue(ans.remove(engine));
      for(int i = 0; i < engineHs; ++i)
         assertTrue(ans.remove(ItemDB.SHS));
      assertTrue(ans.isEmpty());
   }

   @Test
   public void testGetNumCriticalSlotsUsedFree() throws Exception{
      // Setup
      MissileWeapon srm = (MissileWeapon)ItemDB.lookup("STREAK SRM 2");
      MissileWeapon lrm_artemis = (MissileWeapon)ItemDB.lookup("LRM 5 + ARTEMIS");
      final int critSlots = 12;

      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(part.getType()).thenReturn(Part.LeftLeg);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      LoadoutPart cut = new LoadoutPart(loadout, part);
      cut.addItem(lrm_artemis);
      cut.addItem(srm);

      // Execute & verify
      assertEquals(3, cut.getNumCriticalSlotsUsed());
      assertEquals(critSlots - 3, cut.getNumCriticalSlotsFree());
   }

   @Test
   public void testGetNumCriticalSlotsUsedFree_engineHs() throws Exception{
      // Setup
      final int critSlots = 12;
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      Item engine = ItemDB.lookup("STD ENGINE 400");
      final int engineHs = ((Engine)engine).getNumHeatsinkSlots() + 1;

      LoadoutPart cut = new LoadoutPart(loadout, part);
      cut.addItem(engine);
      for(int i = 0; i < engineHs; ++i)
         cut.addItem(ItemDB.SHS);

      // Execute & verify
      assertEquals(engine.getNumCriticalSlots(upgrades) + 1, cut.getNumCriticalSlotsUsed());
      assertEquals(critSlots - engine.getNumCriticalSlots(upgrades) - 1, cut.getNumCriticalSlotsFree());
   }

   @Test
   public void testGetNumEngineHs(){
      final int critSlots = 12;
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      assertEquals(0, cut.getNumEngineHeatsinks());

      cut.addItem(ItemDB.SHS);
      assertEquals(0, cut.getNumEngineHeatsinks()); // Not added to engine...

      cut.addItem(ItemDB.lookup("STD ENGINE 300"));
      assertEquals(1, cut.getNumEngineHeatsinks()); // Now it is

      cut.addItem(ItemDB.SHS);
      assertEquals(2, cut.getNumEngineHeatsinks()); // Next one is too

      cut.addItem(ItemDB.SHS);
      assertEquals(2, cut.getNumEngineHeatsinks()); // No more room in engine, overflow
   }

   @Test
   public void testGetNumEngineHSMax(){
      // Setup
      final int critSlots = 12;
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Execute & Verify (no engine)
      assertEquals(0, cut.getNumEngineHeatsinksMax());

      // Setup (engine)
      cut.addItem(ItemDB.lookup("STD ENGINE 300"));

      // Execute & Verify (engine)
      assertEquals(2, cut.getNumEngineHeatsinksMax());
   }

   /**
    * Jump jets can only be added to legs and torsos.
    */
   @Test
   public void testCanEquip_jumpJets(){
      for(Part p : Part.values()){
         // Setup
         Item jumpjet = ItemDB.lookup("JUMP JETS - CLASS I");
         int critSlots = 12;

         Chassis chassi = Mockito.mock(Chassis.class);
         Mockito.when(chassi.getMassMax()).thenReturn(90);
         Mockito.when(chassi.getMaxJumpJets()).thenReturn(5);

         Loadout loadout = Mockito.mock(Loadout.class);
         Mockito.when(loadout.getJumpJetCount()).thenReturn(0);
         Mockito.when(loadout.getChassi()).thenReturn(chassi);
         Mockito.when(loadout.getFreeMass()).thenReturn(90.0);
         Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(100);

         Mockito.when(part.getType()).thenReturn(p);
         Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

         LoadoutPart cut = new LoadoutPart(loadout, part);

         // Execute & Verify
         if( p == Part.RightLeg || p == Part.LeftLeg || p == Part.RightTorso || p == Part.LeftTorso || p == Part.CenterTorso )
            assertTrue(cut.canEquip(jumpjet));
         else
            assertFalse(cut.canEquip(jumpjet));
      }
   }

   /**
    * Jump jets can not be added if there is not enough free tonnage
    * 
    * @param globalSlots
    *           The number of globally available slots in the mech.
    * @param localSlots
    *           The number of locally available slots in the part.
    * @param freeMass
    *           The amount of free mass in the mech.
    * @param jjCapacity
    *           The total capacity of jump jets
    * @param jjOccupacy
    *           The total number of jump jets in the mech.
    */
   @Test
   @Parameters({"100, 100, 0.2, 5, 0", // Too heavy
         "100, 0, 10.0, 5, 0", // No local slots
         "0, 100, 10.0, 5, 0", // No global slots
         "100, 100, 10.0, 0, 0", // No capacity at all
         "100, 100, 10.0, 2, 2" // No capacity left
   })
   public void testCanEquip_jumpJets_Failures(int globalSlots, int localSlots, double freeMass, int jjCapacity, int jjOccupacy){
      for(Part p : Part.values()){
         // Setup
         Item jumpjet = ItemDB.lookup("JUMP JETS - CLASS V");
         Chassis chassi = Mockito.mock(Chassis.class);
         Mockito.when(chassi.getMaxJumpJets()).thenReturn(jjCapacity);

         Loadout loadout = Mockito.mock(Loadout.class);
         Mockito.when(loadout.getJumpJetCount()).thenReturn(jjOccupacy);
         Mockito.when(loadout.getChassi()).thenReturn(chassi);
         Mockito.when(loadout.getFreeMass()).thenReturn(freeMass);
         Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(globalSlots);

         Mockito.when(part.getType()).thenReturn(p);
         Mockito.when(part.getNumCriticalslots()).thenReturn(localSlots);

         LoadoutPart cut = new LoadoutPart(loadout, part);

         // Execute & Verify
         assertFalse(cut.canEquip(jumpjet));
      }
   }

   @Test
   @Parameters({"LeftTorso", "RightTorso", "CenterTorso", "Head", "LeftLeg", "LeftArm", "RightLeg", "RightArm"})
   public void testCanEquip_CASE(Part aPart) throws Exception{

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(100);

      Mockito.when(part.getType()).thenReturn(aPart);
      Mockito.when(part.getNumCriticalslots()).thenReturn(10);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Execute & Verify
      if( aPart == Part.LeftTorso || aPart == Part.RightTorso ){
         assertTrue(cut.canEquip(ItemDB.lookup("C.A.S.E.")));
      }
      else{
         assertFalse(cut.canEquip(ItemDB.lookup("C.A.S.E.")));
      }
   }

   @Test
   @Parameters({"CenterTorso, 6, 6, 100.0, 300, 300", "LeftTorso, 6, 6, 100.0, 300, 300", "RightTorso, 6, 6, 100.0, 300, 300",})
   public void testCanAddEngine(Part aPart, int ctFreeSlots, int globalSlots, double freeMass, int engineMin, int engineMax){
      // Setup
      Chassis chassi = Mockito.mock(Chassis.class);
      Mockito.when(chassi.getEngineMax()).thenReturn(engineMax);
      Mockito.when(chassi.getEngineMin()).thenReturn(engineMin);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeMass);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(globalSlots);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);

      Mockito.when(part.getType()).thenReturn(aPart);
      Mockito.when(part.getNumCriticalslots()).thenReturn(ctFreeSlots);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Execute & Verify
      if( aPart == Part.CenterTorso ){
         assertTrue(cut.canEquip(ItemDB.lookup("STD ENGINE 300")));
      }
      else{
         assertFalse(cut.canEquip(ItemDB.lookup("STD ENGINE 300")));
      }
   }

   @Test
   @Parameters({"4, 4, 10,  8, 100.0, 200, 400", // Too few global slots
         "2, 4, 10, 20, 100.0, 200, 400", // Too few LT slots
         "4, 2, 10, 20, 100.0, 200, 400", // Too few RT slots
         "4, 4,  5, 20, 100.0, 200, 400", // Too few CT slots
         "4, 4, 10, 20,   0.1, 200, 400", // Too little free mass
         "4, 4, 10, 20,   0.1, 350, 400", // Too small engine
         "4, 4, 10, 20,   0.1, 200, 250", // Too big engine
   })
   public void testCanEquip_xlEngineFailures(int ltFreeSlots, int rtFreeSlots, int ctFreeSlots, int globalSlots, double freeMass, Integer engineMin,
                                             Integer engineMax) throws Exception{
      // Setup
      LoadoutPart rt = Mockito.mock(LoadoutPart.class);
      Mockito.when(rt.getNumCriticalSlotsFree()).thenReturn(rtFreeSlots);

      LoadoutPart lt = Mockito.mock(LoadoutPart.class);
      Mockito.when(lt.getNumCriticalSlotsFree()).thenReturn(ltFreeSlots);

      Chassis chassi = Mockito.mock(Chassis.class);
      Mockito.when(chassi.getEngineMax()).thenReturn(engineMax);
      Mockito.when(chassi.getEngineMin()).thenReturn(engineMin);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeMass);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(globalSlots);
      Mockito.when(loadout.getPart(Part.LeftTorso)).thenReturn(lt);
      Mockito.when(loadout.getPart(Part.RightTorso)).thenReturn(rt);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(ctFreeSlots);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Execute & Verify
      assertFalse(cut.canEquip(ItemDB.lookup("XL ENGINE 300")));
   }

   @Test
   @Parameters({"10, 5, 100, 200, 400", // Too few global slots
         "5, 20, 100, 200, 400", // Too few CT slots
         "10, 20, 0.1, 200, 400", // Too little free mass
         "10, 20, 0.1, 350, 400", // Too small engine
         "10, 20, 0.1, 200, 250", // Too big engine
   })
   public void testCanEquip_stdEngineFailures(int ctFreeSlots, int globalSlots, double freeMass, Integer engineMin, Integer engineMax)
                                                                                                                                      throws Exception{
      // Setup
      Chassis chassi = Mockito.mock(Chassis.class);
      Mockito.when(chassi.getEngineMax()).thenReturn(engineMax);
      Mockito.when(chassi.getEngineMin()).thenReturn(engineMin);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeMass);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(globalSlots);
      Mockito.when(loadout.getChassi()).thenReturn(chassi);

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(ctFreeSlots);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Execute & Verify
      assertFalse(cut.canEquip(ItemDB.lookup("STD ENGINE 300")));
   }

   /**
    * It is not possible to add internals to a loadout part.
    */
   @Test
   public void testCanEquip_internal(){
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(8);

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(8);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      Internal internal = Mockito.mock(Internal.class);
      Mockito.when(internal.getHardpointType()).thenReturn(HardpointType.NONE);
      Mockito.when(internal.getMass(Matchers.any(Upgrades.class))).thenReturn(0.0);
      Mockito.when(internal.getNumCriticalSlots(Matchers.any(Upgrades.class))).thenReturn(1);
      assertFalse(cut.canEquip(internal));
   }

   /**
    * {@link LoadoutPart#canEquip(Item)} shall return false if the {@link Loadout} doesn't have enough free slots.
    */
   @Test
   public void testCanEquip_TooFewSlots(){
      // Setup
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(ItemDB.BAP.getNumCriticalSlots(null) - 1);

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(8);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      // Execute & Verify
      assertFalse(cut.canEquip(ItemDB.BAP));
   }

   /**
    * Adding an Artemis enabled launcher to a space where it would fit without Artemis shall not work
    */
   @Test
   public void testCanEquip_Artemis(){
      Item srm6 = ItemDB.lookup("SRM 6");

      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(srm6.getNumCriticalSlots(null)); // Enough space
                                                                                                  // without artemis

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(8);
      Mockito.when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(1);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      assertFalse(cut.canEquip(ItemDB.lookup("SRM 6")));
   }

   /**
    * Engine heat sinks of the correct type can be added if there is enough space in the engine, even though there is
    * not enough space in the component.
    * 
    * @param aDHS
    *           <code>true</code> if testing with DHS enabled.
    * @param freeMass
    *           The amount of free mass in the loadout.
    */
   @Test
   @Parameters({"true, 10.0", "false, 10.0", "true, 0.0", "false, 0.0"})
   public void testCanEquip_engineHs(boolean aDHS, double freeMass){
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getHeatSink()).thenReturn(aDHS ? UpgradeDB.DOUBLE_HEATSINKS : UpgradeDB.STANDARD_HEATSINKS);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      Mockito.when(loadout.getFreeMass()).thenReturn(freeMass);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(0);

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(0);

      Engine engine = Mockito.mock(Engine.class);
      Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);
      LoadoutPart cut = new LoadoutPart(loadout, part);
      cut.addItem(engine);

      // Only allow heat sinks of correct type.
      if( aDHS ){
         if( freeMass >= ItemDB.DHS.getMass(upgrades) )
            assertTrue(cut.canEquip(ItemDB.DHS));
         else
            assertFalse(cut.canEquip(ItemDB.DHS));
         assertFalse(cut.canEquip(ItemDB.SHS));
      }
      else{
         if( freeMass >= ItemDB.SHS.getMass(upgrades) )
            assertTrue(cut.canEquip(ItemDB.SHS));
         else
            assertFalse(cut.canEquip(ItemDB.SHS));
         assertFalse(cut.canEquip(ItemDB.DHS));
      }

      cut.addItem(aDHS ? ItemDB.DHS : ItemDB.SHS);
      cut.addItem(aDHS ? ItemDB.DHS : ItemDB.SHS);

      assertEquals(2, cut.getNumEngineHeatsinksMax());
      assertEquals(2, cut.getNumEngineHeatsinks());

      // No more space in engine
      assertFalse(cut.canEquip(ItemDB.DHS));
      assertFalse(cut.canEquip(ItemDB.SHS));
   }

   /**
    * Items that require hard points can not be added unless there is a free hard point.
    */
   @Test
   public void testCanEquip_noHardpoints(){
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(0);

      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(0);
      Mockito.when(part.getNumHardpoints(HardpointType.ENERGY)).thenReturn(3);
      Mockito.when(part.getNumHardpoints(HardpointType.BALLISTIC)).thenReturn(1);

      Item item = Mockito.mock(Item.class);
      //Mockito.when(item.isCompatible(loadout)).thenReturn(true);
      Mockito.when(item.getHardpointType()).thenReturn(HardpointType.ENERGY);

      Item otherItem = Mockito.mock(Item.class);
      //Mockito.when(otherItem.isCompatible(loadout)).thenReturn(true);
      Mockito.when(otherItem.getHardpointType()).thenReturn(HardpointType.BALLISTIC);

      LoadoutPart cut = new LoadoutPart(loadout, part);

      cut.addItem(otherItem);
      assertFalse(cut.canEquip(otherItem));

      assertTrue(cut.canEquip(item));
      cut.addItem(item);
      assertTrue(cut.canEquip(item));
      cut.addItem(item);
      assertTrue(cut.canEquip(item));
      cut.addItem(item);
      assertFalse(cut.canEquip(item));
   }

   @Test
   public void testRemoveItem_nosuchitem() throws Exception{
      Mockito.when(part.getType()).thenReturn(Part.CenterTorso);
      LoadoutPart cut = new LoadoutPart(null, part);
      Item item = ItemDB.lookup("AC/20 AMMO");

      assertFalse(cut.removeItem(item));
   }
}

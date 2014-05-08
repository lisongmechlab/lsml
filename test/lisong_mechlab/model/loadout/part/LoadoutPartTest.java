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
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.Location;
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
 * Test suite for {@link ConfiguredComponent}.
 * 
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public class LoadoutPartTest{
   @Mock
   MessageXBar          xBar;
   @Mock
   OperationStack       undoStack;
   MockLoadoutContainer mlc = new MockLoadoutContainer();

   @Mock
   InternalComponent         part;

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
    * Constructing a new {@link ConfiguredComponent} shall initialize the internal components and set armor to 0 on all sides.
    * 
    * @param aPart
    *           The part to test for
    */
   @Test
   @Parameters({"LeftArm", "RightTorso"})
   public void testLoadoutPart(Location aPart){
      // Setup
      List<Item> internals = new ArrayList<>();
      internals.add(mlc.makeInternal(2));
      internals.add(mlc.makeInternal(1));
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getLocation()).thenReturn(aPart);
      Loadout loadout = Mockito.mock(Loadout.class);

      // Execute
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

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
   public void testSetArmorAllowedAutomagic(Location aPart){
      // Setup
      List<Item> internals = new ArrayList<>();
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getLocation()).thenReturn(aPart);
      ConfiguredComponent cut = new ConfiguredComponent(null, part, false);
      int anAmount = 10;

      // Execute
      if( aPart.isTwoSided() ){
         cut.setArmor(ArmorSide.FRONT, anAmount, true);
      }
      else{
         cut.setArmor(ArmorSide.ONLY, anAmount, true);
      }

      assertTrue(cut.allowAutomaticArmor());
   }
   
   @Test
   @Parameters({"LeftArm", "RightTorso"})
   public void testSetGetArmorAndTotalArmor(Location aPart){
      // Setup
      List<Item> internals = new ArrayList<>();
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getLocation()).thenReturn(aPart);
      ConfiguredComponent cut = new ConfiguredComponent(null, part, false);
      int anAmount = 10;

      // Execute
      if( aPart.isTwoSided() ){
         cut.setArmor(ArmorSide.FRONT, anAmount, false);
         cut.setArmor(ArmorSide.BACK, 2 * anAmount, false);
      }
      else{
         cut.setArmor(ArmorSide.ONLY, anAmount, false);
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
   public void testGetArmorMax(Location aPart){
      // Setup
      final int maxArmor = 10;
      List<Item> internals = new ArrayList<>();
      Mockito.when(part.getArmorMax()).thenReturn(maxArmor);
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getLocation()).thenReturn(aPart);
      ConfiguredComponent cut = new ConfiguredComponent(null, part, false);

      if( aPart.isTwoSided() ){
         int front = 4;
         int back = 2;
         cut.setArmor(ArmorSide.FRONT, front, false);
         cut.setArmor(ArmorSide.BACK, back, false);

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
      Mockito.when(part.getLocation()).thenReturn(Location.LeftTorso);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

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
      Mockito.when(part.getLocation()).thenReturn(Location.LeftTorso);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

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
      Mockito.when(part.getLocation()).thenReturn(Location.LeftTorso);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

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
      Mockito.when(part.getLocation()).thenReturn(Location.LeftLeg);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);
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
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      Item engine = ItemDB.lookup("STD ENGINE 400");
      final int engineHs = ((Engine)engine).getNumHeatsinkSlots() + 1;

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);
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
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

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
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(critSlots);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      // Execute & Verify (no engine)
      assertEquals(0, cut.getNumEngineHeatsinksMax());

      // Setup (engine)
      cut.addItem(ItemDB.lookup("STD ENGINE 300"));

      // Execute & Verify (engine)
      assertEquals(2, cut.getNumEngineHeatsinksMax());
   }

   /**
    * Items that are not supported by the {@link InternalComponent} are not addable.
    */
   @Test
   public void testCanEquip_NoSupport(){
      Item jumpjet = ItemDB.lookup("JUMP JETS - CLASS V");

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(false);
      Mockito.when(part.getNumCriticalslots()).thenReturn(10);
      Mockito.when(part.getLocation()).thenReturn(Location.Head);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      // Execute & Verify
      assertFalse(cut.canEquip(jumpjet));
   }

   /**
    * It is not possible to add internals to a loadout part.
    */
   @Test
   public void testCanEquip_Internal(){
      Internal internal = Mockito.mock(Internal.class);
      Mockito.when(internal.getHardpointType()).thenReturn(HardPointType.NONE);
      Mockito.when(internal.getMass(Matchers.any(Upgrades.class))).thenReturn(0.0);
      Mockito.when(internal.getNumCriticalSlots(Matchers.any(Upgrades.class))).thenReturn(1);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getFreeMass()).thenReturn(100.0);
      Mockito.when(loadout.getNumCriticalSlotsFree()).thenReturn(8);

      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(8);
      Mockito.when(part.isAllowed(internal)).thenReturn(false);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);
      assertFalse(cut.canEquip(internal));
   }

   /**
    * {@link ConfiguredComponent#canEquip(Item)} shall return false if the {@link Loadout} doesn't have enough free slots.
    */
   @Test
   public void testCanEquip_TooFewSlots(){
      // Setup
      Loadout loadout = Mockito.mock(Loadout.class);

      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(ItemDB.BAP.getNumCriticalSlots(null) - 1);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      // Execute & Verify
      assertFalse(cut.canEquip(ItemDB.BAP));
   }
   
   /**
    * {@link ConfiguredComponent#canEquip(Item)} shall return false if the LoadoutPart already has a C.A.S.E.
    */
   @Test
   public void testCanEquip_HasCase(){
      // Setup
      Loadout loadout = Mockito.mock(Loadout.class);

      Mockito.when(part.getLocation()).thenReturn(Location.RightTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(4);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);
      cut.addItem(ItemDB.CASE);

      // Execute & Verify
      assertFalse(cut.canEquip(ItemDB.CASE));
   }


   /**
    * Adding an Artemis enabled launcher to a space where it would fit without Artemis shall not work
    */
   @Test
   public void testCanEquip_Artemis(){
      Item srm6 = ItemDB.lookup("SRM 6 + ARTEMIS");

      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getGuidance()).thenReturn(UpgradeDB.ARTEMIS_IV);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);

      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(2);
      Mockito.when(part.getNumHardpoints(HardPointType.MISSILE)).thenReturn(1);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);

      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      assertFalse(cut.canEquip(srm6));
   }

   /**
    * The {@link ConfiguredComponent} will allow items that don't require hard points if they are:
    * <ul>
    * <li>Supported by the {@link InternalComponent}.</li>
    * <li>There is enough space in the component locally.</li>
    * </ul>
    * @param aDHS <code>true</code> if DHS should be used.
    */
   @Test
   @Parameters({"true", "false"})
   public void testCanEquip_EngineHs(boolean aDHS){
      Upgrades upgrades = Mockito.mock(Upgrades.class);
      Mockito.when(upgrades.getHeatSink()).thenReturn(aDHS ? UpgradeDB.DOUBLE_HEATSINKS : UpgradeDB.STANDARD_HEATSINKS);

      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);

      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(0);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);

      Engine engine = Mockito.mock(Engine.class);
      Mockito.when(engine.getNumHeatsinkSlots()).thenReturn(2);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);
      cut.addItem(engine);

      // Only test heat sinks of correct type. Wrong types are handled by loadout
      if( aDHS ){
         assertTrue(cut.canEquip(ItemDB.DHS));
      }
      else{
         assertTrue(cut.canEquip(ItemDB.SHS));
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
    * The {@link ConfiguredComponent} will allow items that don't require hard points if they are:
    * <ul>
    * <li>Supported by the {@link InternalComponent}.</li>
    * <li>There is enough space in the component locally.</li>
    * </ul>
    */
   @Test
   public void testCanEquip_Module(){
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(10);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);
      Item module = Mockito.mock(Item.class);
      Mockito.when(module.getHardpointType()).thenReturn(HardPointType.NONE);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      assertTrue(cut.canEquip(module));
   }

   /**
    * The {@link ConfiguredComponent} will allow items that require hard points if they are:
    * <ul>
    * <li>Supported by the {@link InternalComponent}.</li>
    * <li>There is enough space in the component locally.</li>
    * <li>There are free hard points to use.</li>
    * </ul>
    */
   @Test
   public void testCanEquip_EnoughHardpoints(){
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(10);
      Mockito.when(part.getNumHardpoints(HardPointType.BALLISTIC)).thenReturn(1);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);
      Item ballistic = Mockito.mock(Item.class);
      Mockito.when(ballistic.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      assertTrue(cut.canEquip(ballistic));
   }

   /**
    * Items that require hard points can not be added unless there is a free hard point.
    */
   @Test
   public void testCanEquip_NoHardpoints(){
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(10);
      Mockito.when(part.getNumHardpoints(HardPointType.BALLISTIC)).thenReturn(0);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);
      Item ballistic = Mockito.mock(Item.class);
      Mockito.when(ballistic.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);

      assertFalse(cut.canEquip(ballistic));
   }

   /**
    * Items that require hard points can not be added unless there is a free hard point.
    */
   @Test
   public void testCanEquip_NoFreeHardpoints(){
      Loadout loadout = Mockito.mock(Loadout.class);
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      Mockito.when(part.getNumCriticalslots()).thenReturn(10);
      Mockito.when(part.getNumHardpoints(HardPointType.BALLISTIC)).thenReturn(1);
      Mockito.when(part.isAllowed(Matchers.any(Item.class))).thenReturn(true);
      Item ballistic = Mockito.mock(Item.class);
      Mockito.when(ballistic.getHardpointType()).thenReturn(HardPointType.BALLISTIC);
      ConfiguredComponent cut = new ConfiguredComponent(loadout, part, false);
      cut.addItem(ballistic);

      assertFalse(cut.canEquip(ballistic));
   }

   @Test
   public void testRemoveItem_NoSuchItem() throws Exception{
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      ConfiguredComponent cut = new ConfiguredComponent(null, part, false);
      Item item = ItemDB.lookup("AC/20 AMMO");

      assertFalse(cut.removeItem(item));
   }
   
   @Test
   public void testEquals_ArmorStatus() throws Exception{
      Mockito.when(part.getLocation()).thenReturn(Location.CenterTorso);
      
      ConfiguredComponent cut = new ConfiguredComponent(null, part, false);
      cut.setArmor(ArmorSide.FRONT, 0, false);
      
      ConfiguredComponent cut1 = new ConfiguredComponent(cut, null);
      cut1.setArmor(ArmorSide.FRONT, 0, true);

      assertFalse(cut.equals(cut1));
   }
}

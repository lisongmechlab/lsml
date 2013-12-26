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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;
import lisong_mechlab.model.loadout.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LoadoutPartTest{
   @Mock
   MessageXBar          xBar;
   @Mock
   UndoStack            undoStack;
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

   private LoadoutPart makeCUT(int max_armor, Part type, int numCritSlots){
      List<Item> internals = new ArrayList<>();
      return makeCUT(internals, max_armor, type, numCritSlots);
   }

   /**
    * Creates a {@link LoadoutPart} and verifies the initial state.
    * 
    * @param internals
    * @param max_armor
    * @param type
    * @param numCritSlots
    * @return
    */
   private LoadoutPart makeCUT(List<Item> internals, int max_armor, Part type, int numCritSlots){
      Mockito.when(part.getNumCriticalslots()).thenReturn(numCritSlots);
      Mockito.when(part.getArmorMax()).thenReturn(max_armor);
      Mockito.when(part.getInternalItems()).thenReturn(internals);
      Mockito.when(part.getType()).thenReturn(type);

      int usedCrits = 0;
      for(Item i : internals){
         usedCrits += i.getNumCriticalSlots(mlc.upgrades);
      }

      // Execute
      LoadoutPart cut = new LoadoutPart(mlc.loadout, part, xBar, undoStack);
      Mockito.verify(xBar, Mockito.atLeast(1)).attach(cut);

      // Verify default state
      assertSame(part, cut.getInternalPart());
      assertEquals(numCritSlots - usedCrits, cut.getNumCriticalSlotsFree());
      assertEquals(usedCrits, cut.getNumCriticalSlotsUsed());
      if( type.isTwoSided() ){
         assertEquals(0, cut.getArmor(ArmorSide.FRONT));
         assertEquals(0, cut.getArmor(ArmorSide.BACK));
         assertEquals(max_armor, cut.getArmorMax(ArmorSide.FRONT));
         assertEquals(max_armor, cut.getArmorMax(ArmorSide.BACK));
      }
      else{
         assertEquals(0, cut.getArmor(ArmorSide.ONLY));
         assertEquals(max_armor, cut.getArmorMax(ArmorSide.ONLY));
      }
      assertEquals(0, cut.getArmorTotal());
      assertEquals(internals, cut.getItems());
      assertEquals(0.0, cut.getItemMass(), 0.0);
      assertEquals(0, cut.getNumEngineHeatsinksMax());

      return cut;
   }

   /**
    * Tests construction of a loadout part for CT. (Double sided armor and some internals)
    */
   @Test
   public void testLoadoutPart_CT(){
      // Setup
      Internal gyro = mlc.makeInternal(4);
      makeCUT(Arrays.asList((Item)gyro), 31, Part.CenterTorso, 12);
   }

   /**
    * Tests construction of a loadout part for CT. (Double sided armor and some internals)
    */
   @Test
   public void testLoadoutPart_LL(){
      // Setup
      Internal hip = mlc.makeInternal(1);
      Internal ula = mlc.makeInternal(1);
      Internal lla = mlc.makeInternal(1);
      Internal fa = mlc.makeInternal(1);
      List<Item> internals = Arrays.asList((Item)hip, (Item)ula, (Item)lla, (Item)fa);

      makeCUT(internals, 31, Part.LeftLeg, 12);
   }

   @Test
   public void testGetNumEngineHs(){
      LoadoutPart cut = makeCUT(0, Part.CenterTorso, 7);
      Mockito.when(mlc.chassi.getEngineMax()).thenReturn(400);
      Mockito.when(mlc.chassi.getEngineMin()).thenReturn(0);

      assertEquals(0, cut.getNumEngineHeatsinks());

      cut.addItem(ItemDB.SHS, false);
      assertEquals(0, cut.getNumEngineHeatsinks());

      cut.addItem(ItemDB.lookup("STD ENGINE 300"), false);
      assertEquals(1, cut.getNumEngineHeatsinks());

      cut.addItem(ItemDB.SHS, false);
      assertEquals(2, cut.getNumEngineHeatsinks());

      cut.addItem(ItemDB.SHS, false);
      assertEquals(2, cut.getNumEngineHeatsinks());

      Mockito.verify(xBar, Mockito.times(4)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
   }

   @Test
   public void testGetNumEngineHs_notCT(){
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 7);
      cut.addItem(ItemDB.SHS, false);
      assertEquals(0, cut.getNumEngineHeatsinks());
      Mockito.verify(xBar, Mockito.times(1)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
   }

   @Test
   public void testGetNumCriticalSlotsUsed() throws Exception{
      // Setup
      MissileWeapon srm = (MissileWeapon)ItemDB.lookup("STREAK SRM 2");
      MissileWeapon lrm_artemis = (MissileWeapon)ItemDB.lookup("LRM5");

      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      Mockito.when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(2);

      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      cut.addItem(lrm_artemis, false);
      cut.addItem(srm, false);
      Mockito.verify(xBar, Mockito.times(2)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      // Execute & verify
      assertEquals(3, cut.getNumCriticalSlotsUsed());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAddItem_fail_nomessage() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);

      cut.addItem("AC/20", false);
   }

   @Test
   public void testAddItem_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);

      cut.addItem("AC/20 AMMO", false);

      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      assertTrue(cut.getItems().contains(ItemDB.lookup("AC/20 AMMO")));
   }

   @Test
   public void testAddItem_undo() throws Exception{
      // Setup
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Loadout dummyLoadout = Mockito.mock(Loadout.class);
      Item item = ItemDB.lookup("LRM5");
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      Mockito.when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(1);

      // Execute
      cut.addItem(item, true);

      // Verify an undo action was created
      ArgumentCaptor<UndoAction> argument = ArgumentCaptor.forClass(UndoAction.class);
      Mockito.verify(undoStack, Mockito.only()).pushAction(argument.capture());
      assertEquals("Undo add " + item.getName(mlc.upgrades) + " to " + part.getType().toString(), argument.getValue().describe());
      assertTrue(argument.getValue().affects(mlc.loadout));
      assertFalse(argument.getValue().affects(dummyLoadout));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      // Execute undo action
      argument.getValue().undo();

      // Verify action was undone.
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemRemoved));
      assertFalse(cut.getItems().contains(item));
   }

   @Test
   public void testAddItem_jumpJetsBadPart() throws Exception{
      for(Part testPart : new Part[] {Part.LeftArm, Part.RightArm, Part.Head}){
         LoadoutPart cut = makeCUT(0, testPart, 12);
         Mockito.when(mlc.loadout.getJumpJetCount()).thenReturn(0);
         Mockito.when(mlc.chassi.getMaxJumpJets()).thenReturn(5);
         try{
            cut.addItem("JUMP JETS - CLASS V", false);
            fail("Expected exception!");
         }
         catch( Exception e ){
            // success
         }
      }
   }

   /**
    * Engine heat sinks will behave like regular items added to the component with the exception that their slots will
    * not count towards the critical slots used by the component.
    */
   @Test
   public void testAddItem_EngineHeatsink() throws Exception{
      Item[] items = new Item[] {ItemDB.SHS, ItemDB.DHS};
      for(Item i : items){
         Internal gyro = mlc.makeInternal(4);
         LoadoutPart cut = makeCUT(Arrays.asList((Item)gyro), 0, Part.CenterTorso, 12);
         Mockito.when(mlc.upgrades.hasDoubleHeatSinks()).thenReturn(i == ItemDB.DHS);
         Mockito.when(mlc.chassi.getEngineMax()).thenReturn(400);
         Mockito.when(mlc.chassi.getEngineMin()).thenReturn(100);
         cut.addItem("STD ENGINE 400", false);

         // Execute
         cut.addItem(i, false);

         // Verify
         assertEquals(1, cut.getNumEngineHeatsinks());
         assertTrue(cut.getItems().contains(i));
         assertEquals(10, cut.getNumCriticalSlotsUsed());

         // Execute
         cut.addItem(i, false);
         cut.addItem(i, false);
         cut.addItem(i, false);
         cut.addItem(i, false);
         cut.addItem(i, false);

         // Verify
         assertEquals(6, cut.getNumEngineHeatsinks());
         assertEquals(10, cut.getNumCriticalSlotsUsed());

         if( i == ItemDB.DHS ){
            // Execute
            try{
               cut.addItem(i, false);
               fail();
            }
            catch( Exception e ){
               // Success
            }

            // Verify (internal slots all occupied)
            assertEquals(6, cut.getNumEngineHeatsinks());
            assertEquals(10, cut.getNumCriticalSlotsUsed());

            Mockito.verify(xBar, Mockito.times(7)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         }
         else{
            // Execute
            cut.addItem(i, false);

            // Verify (internal slots all occupied)
            assertEquals(6, cut.getNumEngineHeatsinks());
            assertEquals(11, cut.getNumCriticalSlotsUsed());

            Mockito.verify(xBar, Mockito.times(8)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         }
      }
   }

   @Test
   public void testAddItem_CASE_side_torso() throws Exception{
      for(Part testPart : new Part[] {Part.LeftTorso, Part.RightTorso}){
         LoadoutPart cut = makeCUT(0, testPart, 12);

         cut.addItem("C.A.S.E.", false);

         Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
         assertTrue(cut.getItems().contains(ItemDB.lookup("C.A.S.E.")));
      }
   }

   @Test
   public void testAddItem_CASE_invalid() throws Exception{
      for(Part testPart : new Part[] {Part.LeftArm, Part.LeftLeg, Part.CenterTorso, Part.Head, Part.RightArm, Part.RightLeg}){

         LoadoutPart cut = makeCUT(0, testPart, 12);

         try{
            cut.addItem("C.A.S.E.", false);
            fail(); // No hardpoints
         }
         catch( Exception e ){
            // Success
         }
         assertFalse(cut.getItems().contains(ItemDB.lookup("C.A.S.E.")));
      }
   }

   @Test
   public void testCanAddItem_xlEngineTooFewSlots() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.CenterTorso, 8);
      Mockito.when(mlc.chassi.getEngineMax()).thenReturn(400);
      Mockito.when(mlc.chassi.getEngineMin()).thenReturn(100);
      Mockito.when(mlc.lt.getNumCriticalSlotsFree()).thenReturn(3);
      Mockito.when(mlc.loadout.getNumCriticalSlotsFree()).thenReturn(8);
      Mockito.when(mlc.rt.getNumCriticalSlotsFree()).thenReturn(3);
      assertFalse(cut.canAddItem(ItemDB.lookup("XL ENGINE 100")));
   }

   /**
    * It is not possible to add internals to a loadout part.
    */
   @Test
   public void testCanAddItem_internal() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.CenterTorso, 8);
      Mockito.when(mlc.loadout.getNumCriticalSlotsFree()).thenReturn(8);
      Internal internal = Mockito.mock(Internal.class);
      Mockito.when(internal.getHardpointType()).thenReturn(HardpointType.NONE);
      Mockito.when(internal.getMass(Matchers.any(Upgrades.class))).thenReturn(0.0);
      Mockito.when(internal.getNumCriticalSlots(Matchers.any(Upgrades.class))).thenReturn(1);
      assertFalse(cut.canAddItem(internal));
   }

   /**
    * {@link LoadoutPart#canAddItem(Item)} shall return false if the {@link Loadout} doesn't have enough free slots.
    * 
    * @throws Exception
    */
   @Test
   public void testCanAddItem_TooFewSlots() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Mockito.when(mlc.loadout.getNumCriticalSlotsFree()).thenReturn(ItemDB.BAP.getNumCriticalSlots(null) - 1);

      assertFalse(cut.canAddItem(ItemDB.BAP));
   }

   /**
    * Adding an Artemis enabled SRM6 launcher to CT while there is an engine there shouldn't work.
    * 
    * @throws Exception
    */
   @Test
   public void testCanAddItem_ArtemisSRM6CT() throws Exception{
      Internal gyro = mlc.makeInternal(4);
      LoadoutPart cut = makeCUT(Arrays.asList((Item)gyro), 31, Part.CenterTorso, 12);
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      Mockito.when(mlc.chassi.getEngineMax()).thenReturn(400);
      Mockito.when(mlc.chassi.getEngineMin()).thenReturn(100);
      Mockito.when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(1);
      cut.addItem(ItemDB.lookup("STD ENGINE 100"), false);
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      assertFalse(cut.canAddItem(ItemDB.lookup("SRM 6")));
   }

   @Test
   public void testRemoveItem_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Item item = ItemDB.lookup("AC/20 AMMO");

      cut.addItem(item, false);
      cut.removeItem(item, false);

      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemRemoved));
   }

   @Test
   public void testRemoveItem_undo() throws Exception{
      // Setup
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Loadout dummyLoadout = Mockito.mock(Loadout.class);
      Item item = ItemDB.lookup("LRM5");
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      Mockito.when(part.getNumHardpoints(HardpointType.MISSILE)).thenReturn(1);
      cut.addItem(item, false);
      Mockito.reset(xBar);
      Mockito.reset(undoStack);

      // Execute
      cut.removeItem(item, true);

      // Verify an undo action was created
      ArgumentCaptor<UndoAction> argument = ArgumentCaptor.forClass(UndoAction.class);
      Mockito.verify(undoStack, Mockito.only()).pushAction(argument.capture());
      assertEquals("Undo remove " + item.getName(mlc.upgrades) + " from " + part.getType().toString(), argument.getValue().describe());
      assertTrue(argument.getValue().affects(mlc.loadout));
      assertFalse(argument.getValue().affects(dummyLoadout));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemRemoved));

      // Execute undo action
      argument.getValue().undo();

      // Verify action was undone.
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      assertTrue(cut.getItems().contains(item));
   }

   @Test
   public void testRemoveAll_success() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Item item = ItemDB.lookup("AC/20 AMMO");

      cut.addItem(item, false);
      cut.removeAllItems();

      assertTrue(cut.getItems().isEmpty());
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemRemoved));
   }

   @Test
   public void testRemoveAll_noitems() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      cut.removeAllItems();

      assertTrue(cut.getItems().isEmpty());
      Mockito.verifyNoMoreInteractions(xBar);
   }

   /**
    * Removing an engine shall remove all heat sinks in the engine but none of the external heat sinks.
    * 
    * @throws Exception
    */
   @Test
   public void testRemoveItem_EngineWithHS() throws Exception{
      Internal gyro = mlc.makeInternal(4);
      LoadoutPart cut = makeCUT(Arrays.asList((Item)gyro), 0, Part.CenterTorso, 12);
      Mockito.when(mlc.chassi.getEngineMax()).thenReturn(400);
      Mockito.when(mlc.chassi.getEngineMin()).thenReturn(100);
      cut.addItem(ItemDB.lookup("STD ENGINE 300"), false); // 2 slots
      cut.addItem(ItemDB.SHS, false);
      cut.addItem(ItemDB.SHS, false);
      cut.addItem(ItemDB.SHS, false);
      cut.addItem(ItemDB.SHS, false);

      cut.removeItem(ItemDB.lookup("STD ENGINE 300"), false);

      assertEquals(3, cut.getItems().size());
      assertSame(gyro, cut.getItems().get(0));
      assertSame(ItemDB.SHS, cut.getItems().get(1));
      assertSame(ItemDB.SHS, cut.getItems().get(2));
      Mockito.verify(xBar, Mockito.times(5)).post(new LoadoutPart.Message(cut, Type.ItemAdded));
      Mockito.verify(xBar, Mockito.times(3)).post(new LoadoutPart.Message(cut, Type.ItemRemoved));
   }

   @Test
   public void testRemoveItem_nosuchitem() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Item item = ItemDB.lookup("AC/20 AMMO");

      cut.removeItem(item, false);
   }

   @Test
   public void testSetArmor() throws Exception{
      LoadoutPart cut = makeCUT(0, Part.LeftTorso, 12);
      Mockito.when(part.getArmorMax()).thenReturn(20);

      cut.setArmor(ArmorSide.FRONT, 10);

      assertEquals(10, cut.getArmor(ArmorSide.FRONT));
      assertEquals(0, cut.getArmor(ArmorSide.BACK));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }

   /**
    * {@link LoadoutPart#setArmor(ArmorSide, int)} shall throw an {@link IllegalArgumentException} if the armor amount
    * is above the max for the component.
    * 
    * @throws Exception
    */
   @Test(expected = IllegalArgumentException.class)
   public void testSetArmor_tooMuch() throws Exception{
      int max = 10;
      LoadoutPart cut = makeCUT(max, Part.LeftTorso, 12);

      cut.setArmor(ArmorSide.FRONT, max + 1);
   }

   /**
    * {@link LoadoutPart#setArmor(ArmorSide, int)} shall successfully change the armor value if called with an armor
    * amount less than the current amount and the 'mech is over-tonnage.
    * 
    * @throws Exception
    */
   @Test
   public void testSetArmor_reduceWhenOverTonnage() throws Exception{
      int max = 64;
      LoadoutPart cut = makeCUT(max, Part.LeftTorso, 12);
      cut.setArmor(ArmorSide.FRONT, max);
      Mockito.when(mlc.loadout.getFreeMass()).thenReturn(-0.1);

      cut.setArmor(ArmorSide.FRONT, max - 1);
      Mockito.verify(xBar, Mockito.times(2)).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }

   /**
    * {@link LoadoutPart#setArmor(ArmorSide, int)} shall throw an {@link IllegalArgumentException} if there is not
    * enough free tons to add that amount of armor.
    * 
    * @throws Exception
    */
   @Test(expected = IllegalArgumentException.class)
   public void testSetArmor_notEnoughTons() throws Exception{
      int maxArmor = 20;
      LoadoutPart cut = makeCUT(maxArmor + 1, Part.LeftTorso, 12);
      Mockito.when(mlc.loadout.getFreeMass()).thenReturn(-1.0);

      cut.setArmor(ArmorSide.FRONT, maxArmor + 1);
   }

   /**
    * Setting a armor value shall not throw regardless of previous value if new value is valid.
    */
   @Test
   public void testSetArmor_alreadyMax() throws Exception{
      int maxArmor = 20;
      LoadoutPart cut = makeCUT(maxArmor, Part.LeftTorso, 12);

      cut.setArmor(ArmorSide.FRONT, 5);
      cut.setArmor(ArmorSide.BACK, 10);
      cut.setArmor(ArmorSide.FRONT, 10);

      assertEquals(10, cut.getArmor(ArmorSide.FRONT));
      assertEquals(10, cut.getArmor(ArmorSide.BACK));
      Mockito.verify(xBar, Mockito.times(3)).post(new LoadoutPart.Message(cut, Type.ArmorChanged));
   }

   /**
    * When the Artemis status for the loadout changes, the ammo which is affected should be replaced with the correct
    * type.
    */
   @Test
   public void testReceive_artemisEnabled(){
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(false);

      LoadoutPart cut = makeCUT(100, Part.LeftTorso, 12);
      cut.addItem("SRM AMMO", false);
      cut.addItem("LRM AMMO", false);
      cut.addItem("STREAK SRM AMMO", false);
      cut.addItem("NARC AMMO", false);
      Mockito.verify(xBar, Mockito.times(4)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      cut.receive(new Upgrades.Message(ChangeMsg.GUIDANCE, mlc.upgrades));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemsChanged));

      List<Item> items = new ArrayList<>(cut.getItems());
      assertTrue(items.remove(ItemDB.lookup("SRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("NARC AMMO")));
      assertTrue(items.isEmpty());
   }

   /**
    * When the Artemis status for the loadout changes, the ammo which is affected should be replaced with the correct
    * type.
    */
   @Test
   public void testReceive_artemisDisabled(){
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);

      LoadoutPart cut = makeCUT(100, Part.LeftTorso, 12);
      cut.addItem("SRM AMMO + ARTEMIS IV", false);
      cut.addItem("LRM AMMO + ARTEMIS IV", false);
      cut.addItem("STREAK SRM AMMO", false);
      cut.addItem("NARC AMMO", false);
      Mockito.verify(xBar, Mockito.times(4)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(false);
      cut.receive(new Upgrades.Message(ChangeMsg.GUIDANCE, mlc.upgrades));
      Mockito.verify(xBar).post(new LoadoutPart.Message(cut, Type.ItemsChanged));

      List<Item> items = new ArrayList<>(cut.getItems());
      assertTrue(items.remove(ItemDB.lookup("SRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("LRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("NARC AMMO")));
      assertTrue(items.isEmpty());
   }

   /**
    * When the Artemis status for the loadout changes, the ammo which is affected should be replaced with the correct
    * type.
    */
   @Test
   public void testReceive_artemisNoChange(){
      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);

      LoadoutPart cut = makeCUT(100, Part.LeftTorso, 12);
      cut.addItem("SRM AMMO + ARTEMIS IV", false);
      cut.addItem("LRM AMMO + ARTEMIS IV", false);
      cut.addItem("STREAK SRM AMMO", false);
      cut.addItem("NARC AMMO", false);
      Mockito.verify(xBar, Mockito.times(4)).post(new LoadoutPart.Message(cut, Type.ItemAdded));

      Mockito.when(mlc.upgrades.hasArtemis()).thenReturn(true);
      cut.receive(new Upgrades.Message(ChangeMsg.GUIDANCE, mlc.upgrades));

      List<Item> items = new ArrayList<>(cut.getItems());
      assertTrue(items.remove(ItemDB.lookup("SRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
      assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));
      assertTrue(items.remove(ItemDB.lookup("NARC AMMO")));
      assertTrue(items.isEmpty());
   }
}

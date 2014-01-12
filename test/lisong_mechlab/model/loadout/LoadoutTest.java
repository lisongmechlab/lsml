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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.LoadoutPart.Message.Type;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class LoadoutTest{
   @Spy
   MessageXBar xBar;

   @Mock
   UndoStack   undoStack;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
   }

   // -------------------------------------------------------------------------
   //
   // Jump jet related tests
   //
   // -------------------------------------------------------------------------
   @Test
   public void testGetJumpJetCount_noJJCapability() throws Exception{
      Loadout cut = new Loadout("HBK-4J", xBar, undoStack);
      assertEquals(0, cut.getJumpJetCount());
   }

   @Test
   public void testGetJumpJetCount_noJJEquipped() throws Exception{
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar, undoStack);
      assertEquals(0, cut.getJumpJetCount());
   }

   @Test
   public void testGetJumpJetCount() throws Exception{
      Loadout cut = new Loadout("SDR-5D", xBar, undoStack);
      assertEquals(8, cut.getJumpJetCount()); // 8 stock
   }

   @Test
   public void testGetJumpJetType_noJJCapability() throws Exception{
      Loadout cut = new Loadout("HBK-4J", xBar, undoStack);
      assertNull(cut.getJumpJetType());
   }

   @Test
   public void testGetJumpJetType_noJJEquipped() throws Exception{
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar, undoStack);
      assertNull(cut.getJumpJetType());
   }

   @Test
   public void testGetJumpJetType() throws Exception{
      Loadout cut = new Loadout("SDR-5D", xBar, undoStack);
      assertSame(ItemDB.lookup("JUMP JETS - CLASS V"), cut.getJumpJetType());
   }

   @Test(expected = IllegalArgumentException.class)
   public void testAddItem_JJTooMany(){
      Loadout cut = null;
      JumpJet jjv = null;
      try{
         cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar, undoStack);
         jjv = (JumpJet)ItemDB.lookup("JUMP JETS - CLASS V");

         Part parts[] = new Part[] {Part.RightTorso, Part.LeftTorso, Part.CenterTorso, Part.LeftLeg, Part.RightLeg};
         for(int i = 0; i < cut.getChassi().getMaxJumpJets(); ++i){
            cut.getPart(parts[i % parts.length]).addItem(jjv, false);
         }
      }
      catch( Exception e ){
         fail("Premature throw!");
         return;
      }

      cut.getPart(Part.RightTorso).addItem(jjv, false);
   }

   // -------------------------------------------------------------------------
   //
   // Unsorted tests
   //
   // -------------------------------------------------------------------------

   /**
    * Will create a new, empty loadout
    * 
    * @throws Exception
    */
   @Test
   public void testLoadout_empty() throws Exception{
      Loadout cut = new Loadout(ChassiDB.lookup("HBK-4J"), xBar, undoStack);

      assertEquals(0, cut.getArmor());
      assertEquals(ChassiDB.lookup("hbk-4j"), cut.getChassi());
      assertEquals(5.0, cut.getMass(), 0.0);
      assertEquals(53, cut.getNumCriticalSlotsFree());
      assertEquals(5 * 12 + 3 * 6 - 53, cut.getNumCriticalSlotsUsed());

      verify(xBar).attach(cut);
      verify(xBar).post(new Loadout.Message(cut, Loadout.Message.Type.CREATE));
   }

   /**
    * We can rename loadouts.
    */
   @Test
   public void testRename(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("HBK-4J"), xBar, undoStack);
      assertEquals("HBK-4J", cut.getName());

      // Execute
      cut.rename("Test");

      // Verify
      assertEquals("Test", cut.getName());
      assertEquals("Test (HBK-4J)", cut.toString());
      verify(xBar).post(new Loadout.Message(cut, Loadout.Message.Type.RENAME));
   }

   /**
    * Stripping a loadout shall remove all upgrades, items and armor.
    * 
    * @throws Exception
    */
   @Test
   public void testStrip() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("HBK-4J"), xBar, undoStack);
      cut.loadStock();

      // Execute
      cut.strip();

      // Verify
      for(LoadoutPart loadoutPart : cut.getPartLoadOuts()){
         assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
         assertEquals(0, loadoutPart.getArmorTotal());
      }
      assertFalse(cut.getUpgrades().hasArtemis());
      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());
      assertFalse(cut.getUpgrades().hasFerroFibrous());
      assertFalse(cut.getUpgrades().hasEndoSteel());
   }

   /**
    * Enabling DHS when it was disabled shall remove all heat sinks. TODO: Make it convert all heat sinks but not now.
    */
   @Test
   public void testDHSToggleOn(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar, undoStack);
      cut.getPart(Part.RightTorso).addItem(ItemDB.SHS, false);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(true);

      // Verify
      assertTrue(cut.getUpgrades().hasDoubleHeatSinks());
      verify(xBar).post(new Upgrades.Message(Upgrades.Message.ChangeMsg.HEATSINKS, cut.getUpgrades()));
      assertFalse(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.SHS));

   }

   /**
    * Disabling DHS when it was enabled shall remove all heat sinks. TODO: Make it convert all heat sinks but not now.
    */
   @Test
   public void testDHSToggleOff(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar, undoStack);
      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getPart(Part.RightTorso).addItem(ItemDB.DHS, false);
      reset(xBar);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(false);

      // Verify
      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());
      verify(xBar).post(new Upgrades.Message(Upgrades.Message.ChangeMsg.HEATSINKS, cut.getUpgrades()));
      assertFalse(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.DHS));
   }

   /**
    * Setting the DHS status to the previous value shall not affect the heat sink status.
    */
   @Test
   public void testDHSNoChange_enable_enabled(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar, undoStack);
      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getPart(Part.RightTorso).addItem(ItemDB.DHS, false);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(true);

      // Verify
      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.DHS));
   }

   /**
    * Setting the DHS status to the previous value shall not affect the heat sink status.
    */
   @Test
   public void testDHSNoChange_disable_disabled(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar, undoStack);
      cut.getUpgrades().setDoubleHeatSinks(false);
      cut.getPart(Part.RightTorso).addItem(ItemDB.SHS, false);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(false);

      // Verify
      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.SHS));
   }

   @Test
   public void testFFEnableDisable() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar, undoStack);
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
      cut.getPart(Part.LeftArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);

      double armorTons = cut.getArmor() / 32.0;
      double mass = cut.getMass();
      int freeslots = cut.getNumCriticalSlotsFree();

      cut.getUpgrades().setFerroFibrous(true);
      assertEquals(freeslots - 14, cut.getNumCriticalSlotsFree());
      assertEquals(mass - armorTons * (1 - 1 / 1.12), cut.getMass(), 0.0);

      cut.getUpgrades().setFerroFibrous(false);
      assertEquals(freeslots, cut.getNumCriticalSlotsFree());
      assertEquals(mass, cut.getMass(), 0.0);
   }

   @Test
   public void testFFEnableNotEnoughSlots() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar, undoStack);

      // Execute (13 free slots, failure)
      assertEquals(13, cut.getNumCriticalSlotsFree());
      try{
         cut.getUpgrades().setFerroFibrous(true);
         fail("Expected exception!");
      }
      catch( IllegalArgumentException e ){
         // Success!
      }
      assertFalse(cut.getUpgrades().hasFerroFibrous());

      // Execute (14 free slots, success)
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
      assertEquals(14, cut.getNumCriticalSlotsFree());
      cut.getUpgrades().setFerroFibrous(true);
      assertTrue(cut.getUpgrades().hasFerroFibrous());
   }

   @Test
   public void testESEnableDisable() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar, undoStack);
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
      cut.getPart(Part.LeftArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);

      double intMass = cut.getChassi().getInternalMass();
      double mass = cut.getMass();
      int freeslots = cut.getNumCriticalSlotsFree();

      cut.getUpgrades().setEndoSteel(true);
      assertEquals(freeslots - 14, cut.getNumCriticalSlotsFree());
      assertEquals(mass - intMass * (0.5), cut.getMass(), 0.0);

      cut.getUpgrades().setEndoSteel(false);
      assertEquals(freeslots, cut.getNumCriticalSlotsFree());
      assertEquals(mass - intMass * (0.0), cut.getMass(), 0.0);
   }

   /**
    * Endo-Steel weight savings is rounded up to the closest half ton. This only applies for mechs with weights
    * divisible by 5 tons. Such as the JR7-F
    * 
    * @throws Exception
    */
   @Test
   public void testES_oddtonnage() throws Exception{
      Loadout cut = new Loadout(ChassiDB.lookup("JR7-F"), xBar, undoStack);
      cut.getUpgrades().setEndoSteel(true);
      assertEquals(2.0, cut.getMass(), 0.0);
   }

   @Test
   public void testESEnableNotEnoughSlots() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar, undoStack);

      // Execute (13 free slots, failure)
      assertEquals(13, cut.getNumCriticalSlotsFree());
      try{
         cut.getUpgrades().setEndoSteel(true);
         fail("Expected exception!");
      }
      catch( IllegalArgumentException e ){
         // Success!
      }
      assertFalse(cut.getUpgrades().hasEndoSteel());

      // Execute (14 free slots, success)
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
      assertEquals(14, cut.getNumCriticalSlotsFree());
      cut.getUpgrades().setEndoSteel(true);
      assertTrue(cut.getUpgrades().hasEndoSteel());
   }

   @Test
   public void testArtemisEnable(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);

      cut.getPart(Part.RightTorso).addItem("LRM AMMO", false);
      cut.getPart(Part.RightTorso).addItem("SRM AMMO", false);
      cut.getPart(Part.LeftTorso).addItem("SRM 6", false);
      cut.getPart(Part.LeftTorso).addItem("SRM 2", false);
      cut.getPart(Part.LeftTorso).addItem("LRM 20", false);

      double tons = cut.getMass();
      int slots = cut.getNumCriticalSlotsFree();

      // Execute
      cut.getUpgrades().setArtemis(true);

      // Verify
      assertEquals(tons + 3, cut.getMass(), 0.0);
      assertEquals(slots - 3, cut.getNumCriticalSlotsFree());

      List<Item> itemsRt = new ArrayList<>(cut.getPart(Part.RightTorso).getItems());
      assertTrue(itemsRt.remove(ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
      assertTrue(itemsRt.remove(ItemDB.lookup("SRM AMMO + ARTEMIS IV")));
      /*
       * assertEquals("SRM 6 + ARTEMIS", cut.getPart(Part.LeftTorso).getItems().get(0).getName());
       * assertEquals("SRM 2 + ARTEMIS", cut.getPart(Part.LeftTorso).getItems().get(1).getName());
       * assertEquals("LRM 20 + ARTEMIS", cut.getPart(Part.LeftTorso).getItems().get(2).getName());
       * assertSame(ItemDB.lookup("SRM AMMO + ARTEMIS IV"), cut.getPart(Part.RightTorso).getItems().get(0));
       * assertSame(ItemDB.lookup("LRM AMMO + ARTEMIS IV"), cut.getPart(Part.RightTorso).getItems().get(1));
       */
   }

   @Test
   public void testMaxArmor(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);
      final double front_back_ratio = 3.0 / 2.0;
      final int tolerance = 1;

      // Execute
      cut.setMaxArmor(front_back_ratio);

      // Verify
      // All parts have max armor
      for(InternalPart part : cut.getChassi().getInternalParts()){
         assertEquals(part.getArmorMax(), cut.getPart(part.getType()).getArmorTotal());

         // Double sided parts have a ratio of 3 : 2 armor between front and back.
         if( part.getType().isTwoSided() ){
            int front = cut.getPart(part.getType()).getArmor(ArmorSide.FRONT);
            int back = cut.getPart(part.getType()).getArmor(ArmorSide.BACK);

            double lb = (double)(front - tolerance) / (back + tolerance);
            double ub = (double)(front + tolerance) / (back - tolerance);

            assertTrue(lb < front_back_ratio);
            assertTrue(ub > front_back_ratio);

            verify(xBar, atLeast(2)).post(new LoadoutPart.Message(cut.getPart(part.getType()), LoadoutPart.Message.Type.ArmorChanged));
         }
         else
            verify(xBar).post(new LoadoutPart.Message(cut.getPart(part.getType()), LoadoutPart.Message.Type.ArmorChanged));
      }
   }

   @Test
   public void testMaxArmor_twice(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);
      final double front_back_ratio = 3.0 / 2.0;
      final int tolerance = 1;

      // Execute
      cut.setMaxArmor(1.0);
      reset(xBar);
      cut.setMaxArmor(front_back_ratio);

      // Verify
      // All parts have max armor
      for(InternalPart part : cut.getChassi().getInternalParts()){
         assertEquals(part.getArmorMax(), cut.getPart(part.getType()).getArmorTotal());

         // Double sided parts have a ratio of 3 : 2 armor between front and back.
         if( part.getType().isTwoSided() ){
            int front = cut.getPart(part.getType()).getArmor(ArmorSide.FRONT);
            int back = cut.getPart(part.getType()).getArmor(ArmorSide.BACK);

            double lb = (double)(front - tolerance) / (back + tolerance);
            double ub = (double)(front + tolerance) / (back - tolerance);

            assertTrue(lb < front_back_ratio);
            assertTrue(ub > front_back_ratio);

            verify(xBar, atLeast(2)).post(new LoadoutPart.Message(cut.getPart(part.getType()), LoadoutPart.Message.Type.ArmorChanged));
         }
         else
            verify(xBar).post(new LoadoutPart.Message(cut.getPart(part.getType()), LoadoutPart.Message.Type.ArmorChanged));
      }
   }

   @Test
   public void testFreeMass(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);

      // Verify
      assertEquals(90, cut.getFreeMass(), 0.0);
   }

   @Ignore
   // This test has been superceded
   @Test
   public void testCheckArtemisAdditionLegal(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("COM-2D"), xBar, undoStack);
      Loadout anotherCut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);
      anotherCut.getPart(Part.LeftTorso).addItem("SRM 6", false);
      try{
         cut.loadStock();
      }
      catch( Exception e ){
         fail("Unexpected exception when loading stock loadout!");
      }
      // Verify
      try{
         // cut.checkArtemisAdditionLegal();
         cut.getUpgrades().setArtemis(true);
         fail("Exception expected!");
      }
      catch( Exception e ){
         // Success!
      }
      try{
         anotherCut.getUpgrades().setArtemis(true);
      }
      catch( Exception e ){
         fail("Should not throw exception!");
      }
   }

   // -------------------------------------------------------------------------
   //
   // Integration tests
   //
   // -------------------------------------------------------------------------
   @Test
   public void testBuild_jr7f(){
      Loadout cut = new Loadout(ChassiDB.lookup("JR7-F"), xBar, undoStack);

      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getUpgrades().setFerroFibrous(true);
      cut.getUpgrades().setEndoSteel(true);

      cut.getPart(Part.LeftArm).setArmor(ArmorSide.ONLY, 24);
      cut.getPart(Part.LeftArm).addItem("MEDIUM LASER", false);
      cut.getPart(Part.LeftArm).addItem("MEDIUM LASER", false);
      cut.getPart(Part.LeftArm).addItem("MEDIUM LASER", false);

      cut.getPart(Part.RightArm).setArmor(ArmorSide.ONLY, 24);
      cut.getPart(Part.RightArm).addItem("MEDIUM LASER", false);
      cut.getPart(Part.RightArm).addItem("MEDIUM LASER", false);
      cut.getPart(Part.RightArm).addItem("MEDIUM LASER", false);

      cut.getPart(Part.LeftTorso).setArmor(ArmorSide.FRONT, 23);
      cut.getPart(Part.LeftTorso).setArmor(ArmorSide.BACK, 9);
      cut.getPart(Part.LeftTorso).addItem("JUMP JETS - CLASS V", false);
      cut.getPart(Part.LeftTorso).addItem("JUMP JETS - CLASS V", false);

      cut.getPart(Part.RightTorso).setArmor(ArmorSide.FRONT, 23);
      cut.getPart(Part.RightTorso).setArmor(ArmorSide.BACK, 9);
      cut.getPart(Part.RightTorso).addItem("JUMP JETS - CLASS V", false);
      cut.getPart(Part.RightTorso).addItem("JUMP JETS - CLASS V", false);
      cut.getPart(Part.RightTorso).addItem("DOUBLE HEAT SINK", false);

      cut.getPart(Part.Head).setArmor(ArmorSide.ONLY, 12);

      cut.getPart(Part.CenterTorso).setArmor(ArmorSide.FRONT, 39);
      cut.getPart(Part.CenterTorso).setArmor(ArmorSide.BACK, 5);
      cut.getPart(Part.CenterTorso).addItem("XL ENGINE 300", false);
      cut.getPart(Part.CenterTorso).addItem("DOUBLE HEAT SINK", false);
      cut.getPart(Part.CenterTorso).addItem("DOUBLE HEAT SINK", false);

      cut.getPart(Part.LeftLeg).setArmor(ArmorSide.ONLY, 32);

      cut.getPart(Part.RightLeg).setArmor(ArmorSide.ONLY, 32);

      // Verification against in-game mech lab
      assertEquals(232, cut.getArmor());
      assertTrue("mass = " + cut.getMass(), cut.getMass() > 35.0 - 1.0 / 32.0);
      assertTrue("mass = " + cut.getMass(), cut.getMass() <= 35.0);
   }

   /**
    * {@link Loadout#addItem()} shall add an item to the first applicable slot in this loadout. Order the items are
    * added is: RA, RT, RL, HD, CT, LT, LL, LA
    */
   @Test
   public void testAddItem(){
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);

      cut.getUpgrades().setDoubleHeatSinks(true);

      cut.addItem("MEDIUM LASER", false);
      assertTrue(cut.getPart(Part.RightArm).getItems().contains(ItemDB.lookup("MEDIUM LASER")));

      cut.addItem(ItemDB.lookup("MEDIUM LASER"), false);
      assertTrue(cut.getPart(Part.LeftArm).getItems().contains(ItemDB.lookup("MEDIUM LASER")));

      cut.addItem(ItemDB.lookup("AC/20"), false);
      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.lookup("AC/20")));

      cut.addItem("LRM 5", false);
      assertTrue(cut.getPart(Part.LeftTorso).getItems().contains(ItemDB.lookup("LRM 5")));

      cut.addItem("LRM 15", false);
      assertTrue(cut.getPart(Part.LeftTorso).getItems().contains(ItemDB.lookup("LRM 15")));

      cut.addItem("STD ENGINE 250", false);
      assertTrue(cut.getPart(Part.CenterTorso).getItems().contains(ItemDB.lookup("STD ENGINE 250")));

      // Fill right arm
      cut.addItem(ItemDB.DHS, false);
      cut.addItem(ItemDB.DHS, false);
      assertTrue(cut.getPart(Part.RightArm).getItems().contains(ItemDB.DHS));
      verify(xBar, times(1 + 2)).post(new LoadoutPart.Message(cut.getPart(Part.RightArm), Type.ItemAdded));

      // Skips RA, RT, RL, HD, CT (too few slots) and places the item in LT
      cut.addItem(ItemDB.DHS, false);
      assertTrue(cut.getPart(Part.LeftTorso).getItems().contains(ItemDB.DHS));

      // Skips RA (too few slots) and places the item in RT
      cut.addItem(ItemDB.BAP, false);
      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.BAP));
   }

   /**
    * {@link Loadout#addItem()} shall prioritize engine slots for heat sinks
    */
   @Test
   public void testAddItem_engineHS(){
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar, undoStack);

      cut.addItem("STD ENGINE 300", false);
      assertTrue(cut.getPart(Part.CenterTorso).getItems().contains(ItemDB.lookup("STD ENGINE 300")));

      cut.addItem(ItemDB.SHS, false); // Engine HS slot 1
      cut.addItem(ItemDB.SHS, false); // Engine HS slot 2
      cut.addItem(ItemDB.SHS, false); // Right arm
      verify(xBar, times(1 + 2)).post(new LoadoutPart.Message(cut.getPart(Part.CenterTorso), Type.ItemAdded));
      assertTrue(cut.getPart(Part.CenterTorso).getItems().contains(ItemDB.SHS)); // 1 remaining
      assertTrue(cut.getPart(Part.RightArm).getItems().contains(ItemDB.SHS));
   }
}

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
import static org.mockito.Mockito.verify;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.part.AddItemOperation;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.SetArmorTypeOperation;
import lisong_mechlab.model.upgrades.SetHeatSinkTypeOperation;
import lisong_mechlab.model.upgrades.SetStructureTypeOperation;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.thoughtworks.xstream.XStream;

public class LoadoutTest{
   @Spy
   MessageXBar    xBar;

   @Mock
   OperationStack undoStack;

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
      Loadout cut = new Loadout("HBK-4J", xBar);
      assertEquals(0, cut.getJumpJetCount());
   }

   @Test
   public void testGetJumpJetCount_noJJEquipped() throws Exception{
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      assertEquals(0, cut.getJumpJetCount());
   }

   @Test
   public void testGetJumpJetCount() throws Exception{
      Loadout cut = new Loadout("SDR-5D", xBar);
      assertEquals(8, cut.getJumpJetCount()); // 8 stock
   }

   @Test
   public void testGetJumpJetType_noJJCapability() throws Exception{
      Loadout cut = new Loadout("HBK-4J", xBar);
      assertNull(cut.getJumpJetType());
   }

   @Test
   public void testGetJumpJetType_noJJEquipped() throws Exception{
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      assertNull(cut.getJumpJetType());
   }

   @Test
   public void testGetJumpJetType() throws Exception{
      Loadout cut = new Loadout("SDR-5D", xBar);
      assertSame(ItemDB.lookup("JUMP JETS - CLASS V"), cut.getJumpJetType());
   }

   /**
    * Even if DHS are serialized before the Engine, they should be added as engine heat sinks.
    */
   @Test
   public void testUnMarshalDhsBeforeEngine(){
      String xml = "<?xml version=\"1.0\" ?><loadout name=\"AS7-BH\" chassi=\"AS7-BH\"><upgrades version=\"2\"><armor>2810</armor><structure>3100</structure><guidance>3051</guidance><heatsinks>3002</heatsinks></upgrades><efficiencies><speedTweak>false</speedTweak><coolRun>false</coolRun><heatContainment>false</heatContainment><anchorTurn>false</anchorTurn><doubleBasics>false</doubleBasics><fastfire>false</fastfire></efficiencies><component part=\"Head\" armor=\"0\" /><component part=\"LeftArm\" armor=\"0\" /><component part=\"LeftLeg\" armor=\"0\" /><component part=\"LeftTorso\" armor=\"0/0\" /><component part=\"CenterTorso\" armor=\"0/0\"><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3001</item><item>3278</item></component><component part=\"RightTorso\" armor=\"0/0\" /><component part=\"RightLeg\" armor=\"0\" /><component part=\"RightArm\" armor=\"0\" /></loadout>";

      XStream stream = Loadout.loadoutXstream(xBar);
      Loadout loadout = (Loadout)stream.fromXML(xml);

      assertEquals(6, loadout.getPart(Part.CenterTorso).getNumEngineHeatsinks());
   }

   @Test
   public void testCanEquip_NoInternals() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);

      // Execute + Verify
      assertFalse(cut.canEquip(LoadoutPart.ENGINE_INTERNAL));
   }
   
   @Test
   public void testCanEquip_TooHeavy() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.CenterTorso), ItemDB.lookup("STD ENGINE 190")));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.lookup("PPC")));
      assertTrue(cut.getFreeMass() < 2.0); // Should be 1.5 tons free

      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.lookup("PPC")));
   }

   @Test
   public void testCanEquip_NotSupportedByChassi() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);

      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.ECM));
   }

   @Test
   public void testCanEquip_CompatibleUpgrades() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      
      // Execute + Verify
      assertTrue(cut.canEquip(ItemDB.DHS));
   }
   
   @Test
   public void testCanEquip_NotCompatibleUpgrades() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);

      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.DHS));
   }

   @Test
   public void testCanEquip_TooFewSlots() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("LCT-3M"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new SetArmorTypeOperation(null, cut, UpgradeDB.FERRO_FIBROUS_ARMOR));
      stack.pushAndApply(new SetStructureTypeOperation(null, cut, UpgradeDB.ENDO_STEEL_STRUCTURE));
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.CenterTorso), ItemDB.lookup("XL ENGINE 100")));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftArm), ItemDB.DHS));
      assertTrue(cut.getFreeMass() > 1.5); // Should be 13.5 tons free

      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.DHS));
   }

   @Test
   public void testCanEquip_JJ() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      Item jj = ItemDB.lookup("JUMP JETS - CLASS V");
      
      // Execute + Verify
      assertTrue(cut.canEquip(jj));
   }
   
   @Test
   public void testCanEquip_TooManyJJ() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      Item jj = ItemDB.lookup("JUMP JETS - CLASS V");
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), jj));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), jj));
      // Make sure test won't fail on wrong condition
      assertTrue(cut.getFreeMass() > 1.5);
      assertTrue(cut.getNumCriticalSlotsFree() > 1);
      assertEquals(cut.getChassi().getMaxJumpJets(), cut.getJumpJetCount());
      assertTrue(cut.getChassi().isAllowed(jj));
      
      // Execute + Verify
      assertFalse(cut.canEquip(jj));
   }
   
   @Test
   public void testCanEquip_TooManyEngine() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.CenterTorso), ItemDB.lookup("XL ENGINE 100")));
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.lookup("XL ENGINE 100")));
   }
  
   @Test
   public void testCanEquip_XLEngineNoSpaceLeftTorso() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.DHS));
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.lookup("XL ENGINE 100")));
   }
   
   @Test
   public void testCanEquip_XLEngineNoSpaceRightTorso() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightTorso), ItemDB.DHS));
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.lookup("XL ENGINE 100")));
   }
   
   @Test
   public void testCanEquip_XLEngineNoSpaceCentreTorso() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.CenterTorso), ItemDB.DHS));
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.lookup("XL ENGINE 100")));
   }
   
   @Test
   public void testCanEquip_XLEngine12SlotsFree() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new SetStructureTypeOperation(null, cut, UpgradeDB.ENDO_STEEL_STRUCTURE));
      stack.pushAndApply(new SetArmorTypeOperation(null, cut, UpgradeDB.FERRO_FIBROUS_ARMOR));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.CASE));
      assertEquals(12, cut.getNumCriticalSlotsFree());
      
      // Execute + Verify
      assertTrue(cut.canEquip(ItemDB.lookup("XL ENGINE 100")));
   }
   
   @Test
   public void testCanEquip_XLEngine11SlotsFree() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new SetHeatSinkTypeOperation(null, cut, UpgradeDB.DOUBLE_HEATSINKS));
      stack.pushAndApply(new SetStructureTypeOperation(null, cut, UpgradeDB.ENDO_STEEL_STRUCTURE));
      stack.pushAndApply(new SetArmorTypeOperation(null, cut, UpgradeDB.FERRO_FIBROUS_ARMOR));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.RightArm), ItemDB.DHS));
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.ECM));
      assertEquals(11, cut.getNumCriticalSlotsFree());
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.lookup("XL ENGINE 100")));
   }
   
   @Test
   public void testCanEquip_NoHardpoints() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5V"), xBar);
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.ECM));
   }
   
   @Test
   public void testCanEquip_NotEnoughHardpoints() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
      OperationStack stack = new OperationStack(0);
      stack.pushAndApply(new AddItemOperation(null, cut.getPart(Part.LeftTorso), ItemDB.ECM));
      
      // Execute + Verify
      assertFalse(cut.canEquip(ItemDB.ECM));
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
      Loadout cut = new Loadout(ChassiDB.lookup("HBK-4J"), xBar);

      assertEquals(0, cut.getArmor());
      assertEquals(ChassiDB.lookup("hbk-4j"), cut.getChassi());
      assertEquals(5.0, cut.getMass(), 0.0);
      assertEquals(53, cut.getNumCriticalSlotsFree());
      assertEquals(5 * 12 + 3 * 6 - 53, cut.getNumCriticalSlotsUsed());

      verify(xBar).post(new Loadout.Message(cut, Loadout.Message.Type.CREATE));
   }

//@formatter:off
//   /**
//    * Enabling DHS when it was disabled shall remove all heat sinks. TODO: Make it convert all heat sinks but not now.
//    */
//   @Test
//   public void testDHSToggleOn(){
//      // Setup
//      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
//      cut.getPart(Part.RightTorso).addItem(ItemDB.SHS, false);
//
//      // Execute
//      cut.getUpgrades().setDoubleHeatSinks(true);
//
//      // Verify
//      assertTrue(cut.getUpgrades().hasDoubleHeatSinks());
//      verify(xBar).post(new Upgrades.Message(Upgrades.Message.ChangeMsg.HEATSINKS, cut.getUpgrades()));
//      assertFalse(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.SHS));
//
//   }
//
//   /**
//    * Disabling DHS when it was enabled shall remove all heat sinks. TODO: Make it convert all heat sinks but not now.
//    */
//   @Test
//   public void testDHSToggleOff(){
//      // Setup
//      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
//      cut.getUpgrades().setDoubleHeatSinks(true);
//      cut.getPart(Part.RightTorso).addItem(ItemDB.DHS, false);
//      reset(xBar);
//
//      // Execute
//      cut.getUpgrades().setDoubleHeatSinks(false);
//
//      // Verify
//      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());
//      verify(xBar).post(new Upgrades.Message(Upgrades.Message.ChangeMsg.HEATSINKS, cut.getUpgrades()));
//      assertFalse(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.DHS));
//   }
//
//   /**
//    * Setting the DHS status to the previous value shall not affect the heat sink status.
//    */
//   @Test
//   public void testDHSNoChange_enable_enabled(){
//      // Setup
//      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
//      cut.getUpgrades().setDoubleHeatSinks(true);
//      cut.getPart(Part.RightTorso).addItem(ItemDB.DHS, false);
//
//      // Execute
//      cut.getUpgrades().setDoubleHeatSinks(true);
//
//      // Verify
//      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.DHS));
//   }
//
//   /**
//    * Setting the DHS status to the previous value shall not affect the heat sink status.
//    */
//   @Test
//   public void testDHSNoChange_disable_disabled(){
//      // Setup
//      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
//      cut.getUpgrades().setDoubleHeatSinks(false);
//      cut.getPart(Part.RightTorso).addItem(ItemDB.SHS, false);
//
//      // Execute
//      cut.getUpgrades().setDoubleHeatSinks(false);
//
//      // Verify
//      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.SHS));
//   }
//
//   @Test
//   public void testFFEnableDisable() throws Exception{
//      Loadout cut = new Loadout("as7-d", xBar);
//      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
//      cut.getPart(Part.LeftArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
//
//      double armorTons = cut.getArmor() / 32.0;
//      double mass = cut.getMass();
//      int freeslots = cut.getNumCriticalSlotsFree();
//
//      cut.getUpgrades().setFerroFibrous(true);
//      assertEquals(freeslots - 14, cut.getNumCriticalSlotsFree());
//      assertEquals(mass - armorTons * (1 - 1 / 1.12), cut.getMass(), 0.0);
//
//      cut.getUpgrades().setFerroFibrous(false);
//      assertEquals(freeslots, cut.getNumCriticalSlotsFree());
//      assertEquals(mass, cut.getMass(), 0.0);
//   }
//
//   @Test
//   public void testFFEnableNotEnoughSlots() throws Exception{
//      Loadout cut = new Loadout("as7-d", xBar);
//
//      // Execute (13 free slots, failure)
//      assertEquals(13, cut.getNumCriticalSlotsFree());
//      try{
//         cut.getUpgrades().setFerroFibrous(true);
//         fail("Expected exception!");
//      }
//      catch( IllegalArgumentException e ){
//         // Success!
//      }
//      assertFalse(cut.getUpgrades().hasFerroFibrous());
//
//      // Execute (14 free slots, success)
//      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
//      assertEquals(14, cut.getNumCriticalSlotsFree());
//      cut.getUpgrades().setFerroFibrous(true);
//      assertTrue(cut.getUpgrades().hasFerroFibrous());
//   }
//
//   @Test
//   public void testESEnableDisable() throws Exception{
//      Loadout cut = new Loadout("as7-d", xBar);
//      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
//      cut.getPart(Part.LeftArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
//
//      double intMass = cut.getChassi().getInternalMass();
//      double mass = cut.getMass();
//      int freeslots = cut.getNumCriticalSlotsFree();
//
//      cut.getUpgrades().setEndoSteel(true);
//      assertEquals(freeslots - 14, cut.getNumCriticalSlotsFree());
//      assertEquals(mass - intMass * (0.5), cut.getMass(), 0.0);
//
//      cut.getUpgrades().setEndoSteel(false);
//      assertEquals(freeslots, cut.getNumCriticalSlotsFree());
//      assertEquals(mass - intMass * (0.0), cut.getMass(), 0.0);
//   }
//
//   /**
//    * Endo-Steel weight savings is rounded up to the closest half ton. This only applies for mechs with weights
//    * divisible by 5 tons. Such as the JR7-F
//    * 
//    * @throws Exception
//    */
//   @Test
//   public void testES_oddtonnage() throws Exception{
//      Loadout cut = new Loadout(ChassiDB.lookup("JR7-F"), xBar);
//      cut.getUpgrades().setEndoSteel(true);
//      assertEquals(2.0, cut.getMass(), 0.0);
//   }
//
//   @Test
//   public void testESEnableNotEnoughSlots() throws Exception{
//      Loadout cut = new Loadout("as7-d", xBar);
//
//      // Execute (13 free slots, failure)
//      assertEquals(13, cut.getNumCriticalSlotsFree());
//      try{
//         cut.getUpgrades().setEndoSteel(true);
//         fail("Expected exception!");
//      }
//      catch( IllegalArgumentException e ){
//         // Success!
//      }
//      assertFalse(cut.getUpgrades().hasEndoSteel());
//
//      // Execute (14 free slots, success)
//      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"), false);
//      assertEquals(14, cut.getNumCriticalSlotsFree());
//      cut.getUpgrades().setEndoSteel(true);
//      assertTrue(cut.getUpgrades().hasEndoSteel());
//   }
//
//   @Test
//   public void testArtemisEnable(){
//      // Setup
//      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
//
//      cut.getPart(Part.RightTorso).addItem("LRM AMMO", false);
//      cut.getPart(Part.RightTorso).addItem("SRM AMMO", false);
//      cut.getPart(Part.LeftTorso).addItem("SRM 6", false);
//      cut.getPart(Part.LeftTorso).addItem("SRM 2", false);
//      cut.getPart(Part.LeftTorso).addItem("LRM 20", false);
//
//      double tons = cut.getMass();
//      int slots = cut.getNumCriticalSlotsFree();
//
//      // Execute
//      cut.getUpgrades().setArtemis(true);
//
//      // Verify
//      assertEquals(tons + 3, cut.getMass(), 0.0);
//      assertEquals(slots - 3, cut.getNumCriticalSlotsFree());
//
//      List<Item> itemsRt = new ArrayList<>(cut.getPart(Part.RightTorso).getItems());
//      assertTrue(itemsRt.remove(ItemDB.lookup("LRM AMMO + ARTEMIS IV")));
//      assertTrue(itemsRt.remove(ItemDB.lookup("SRM AMMO + ARTEMIS IV")));
//   }
//@formatter:on

   @Test
   public void testFreeMass(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);

      // Verify
      assertEquals(90, cut.getFreeMass(), 0.0);
   }
}

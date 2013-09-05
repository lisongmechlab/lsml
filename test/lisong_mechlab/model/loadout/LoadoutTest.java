package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.JumpJet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class LoadoutTest{
   @Spy
   MessageXBar xBar;

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

   @Test(expected = IllegalArgumentException.class)
   public void testAddItem_JJTooMany(){
      Loadout cut = null;
      JumpJet jjv = null;
      try{
         cut = new Loadout(ChassiDB.lookup("SDR-5D"), xBar);
         jjv = (JumpJet)ItemDB.lookup("JUMP JETS - CLASS V");

         Part parts[] = new Part[] {Part.RightTorso, Part.LeftTorso, Part.CenterTorso, Part.LeftLeg, Part.RightLeg};
         for(int i = 0; i < cut.getChassi().getMaxJumpJets(); ++i){
            cut.getPart(parts[i % parts.length]).addItem(jjv);
         }
      }
      catch( Exception e ){
         fail("Premature throw!");
      }

      cut.getPart(Part.RightTorso).addItem(jjv);
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

      verify(xBar).attach(cut);
      verify(xBar).post(new Loadout.Message(cut, Loadout.Message.Type.CREATE));
   }

   /**
    * We can rename loadouts.
    */
   @Test
   public void testRename(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("HBK-4J"), xBar);
      assertEquals("HBK-4J", cut.getName());

      // Execute
      cut.rename("Test");

      // Verify
      assertEquals("Test", cut.getName());
      assertEquals("Test (HBK-4J)", cut.toString());
      verify(xBar).post(new Loadout.Message(cut, Loadout.Message.Type.RENAME));
   }

   /**
    * Enabling DHS when it was disabled shall remove all heat sinks. TODO: Make it convert all heat sinks but not now.
    */
   @Test
   public void testDHSToggleOn(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
      cut.getPart(Part.RightTorso).addItem(ItemDB.SHS);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(true);

      // Verify
      assertTrue(cut.getUpgrades().hasDoubleHeatSinks());
      verify(xBar).post(new Upgrades.Message(Upgrades.ChangeMsg.HEATSINKS, cut.getUpgrades()));
      assertFalse(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.SHS));

   }

   /**
    * Disabling DHS when it was enabled shall remove all heat sinks. TODO: Make it convert all heat sinks but not now.
    */
   @Test
   public void testDHSToggleOff(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getPart(Part.RightTorso).addItem(ItemDB.DHS);
      reset(xBar);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(false);

      // Verify
      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());
      verify(xBar).post(new Upgrades.Message(Upgrades.ChangeMsg.HEATSINKS, cut.getUpgrades()));
      assertFalse(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.DHS));
   }

   /**
    * Setting the DHS status to the previous value shall not affect the heat sink status.
    */
   @Test
   public void testDHSNoChange_enable_enabled(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getPart(Part.RightTorso).addItem(ItemDB.DHS);

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
      Loadout cut = new Loadout(ChassiDB.lookup("as7-d"), xBar);
      cut.getUpgrades().setDoubleHeatSinks(false);
      cut.getPart(Part.RightTorso).addItem(ItemDB.SHS);

      // Execute
      cut.getUpgrades().setDoubleHeatSinks(false);

      // Verify
      assertTrue(cut.getPart(Part.RightTorso).getItems().contains(ItemDB.SHS));
   }

   @Test
   public void testFFEnableDisable() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar);
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"));
      cut.getPart(Part.LeftArm).removeItem(ItemDB.lookup("MEDIUM LASER"));

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
      Loadout cut = new Loadout("as7-d", xBar);

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
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"));
      assertEquals(14, cut.getNumCriticalSlotsFree());
      cut.getUpgrades().setFerroFibrous(true);
      assertTrue(cut.getUpgrades().hasFerroFibrous());
   }

   @Test
   public void testESEnableDisable() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar);
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"));
      cut.getPart(Part.LeftArm).removeItem(ItemDB.lookup("MEDIUM LASER"));

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
      Loadout cut = new Loadout(ChassiDB.lookup("JR7-F"), xBar);
      cut.getUpgrades().setEndoSteel(true);
      assertEquals(2.0, cut.getMass(), 0.0);
   }

   @Test
   public void testESEnableNotEnoughSlots() throws Exception{
      Loadout cut = new Loadout("as7-d", xBar);

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
      cut.getPart(Part.RightArm).removeItem(ItemDB.lookup("MEDIUM LASER"));
      assertEquals(14, cut.getNumCriticalSlotsFree());
      cut.getUpgrades().setEndoSteel(true);
      assertTrue(cut.getUpgrades().hasEndoSteel());
   }

   @Test
   public void testArtemisEnable(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);

      cut.getPart(Part.RightTorso).addItem("LRM AMMO");
      cut.getPart(Part.RightTorso).addItem("SRM AMMO");
      cut.getPart(Part.LeftTorso).addItem("SRM 6");
      cut.getPart(Part.LeftTorso).addItem("SRM 2");
      cut.getPart(Part.LeftTorso).addItem("LRM 20");

      double tons = cut.getMass();
      int slots = cut.getNumCriticalSlotsFree();

      // Execute
      cut.getUpgrades().setArtemis(true);

      // Verify
      assertEquals(tons + 3, cut.getMass(), 0.0);
      assertEquals(slots - 3, cut.getNumCriticalSlotsFree());
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
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
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
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
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
      Loadout cut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
      
// Verify
      assertEquals(90, cut.getFreeMass(), 0.0);
   }
   
   @Test
   public void testCheckArtemisAdditionLegal(){
      // Setup
         Loadout cut = new Loadout(ChassiDB.lookup("COM-2D"), xBar);
         Loadout anotherCut = new Loadout(ChassiDB.lookup("AS7-D-DC"), xBar);
         anotherCut.getPart(Part.LeftTorso).addItem("SRM 6");
         try{
            cut.loadStock();
         }
         catch( Exception e ){
            fail("Unexpected exception when loading stock loadout!");
         }
      //Verify
         try{
//            cut.checkArtemisAdditionLegal();
            cut.getUpgrades().setArtemis(true);
            fail("Exception expected!");
         }
         catch( Exception e ){
            //Success!
         }
         try{
            anotherCut.getUpgrades().setArtemis(true);
         }
         catch(Exception e){
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
      Loadout cut = new Loadout(ChassiDB.lookup("JR7-F"), xBar);

      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getUpgrades().setFerroFibrous(true);
      cut.getUpgrades().setEndoSteel(true);

      cut.getPart(Part.LeftArm).setArmor(ArmorSide.ONLY, 24);
      cut.getPart(Part.LeftArm).addItem("MEDIUM LASER");
      cut.getPart(Part.LeftArm).addItem("MEDIUM LASER");
      cut.getPart(Part.LeftArm).addItem("MEDIUM LASER");

      cut.getPart(Part.RightArm).setArmor(ArmorSide.ONLY, 24);
      cut.getPart(Part.RightArm).addItem("MEDIUM LASER");
      cut.getPart(Part.RightArm).addItem("MEDIUM LASER");
      cut.getPart(Part.RightArm).addItem("MEDIUM LASER");

      cut.getPart(Part.LeftTorso).setArmor(ArmorSide.FRONT, 23);
      cut.getPart(Part.LeftTorso).setArmor(ArmorSide.BACK, 9);
      cut.getPart(Part.LeftTorso).addItem("JUMP JETS - CLASS V");
      cut.getPart(Part.LeftTorso).addItem("JUMP JETS - CLASS V");

      cut.getPart(Part.RightTorso).setArmor(ArmorSide.FRONT, 23);
      cut.getPart(Part.RightTorso).setArmor(ArmorSide.BACK, 9);
      cut.getPart(Part.RightTorso).addItem("JUMP JETS - CLASS V");
      cut.getPart(Part.RightTorso).addItem("JUMP JETS - CLASS V");
      cut.getPart(Part.RightTorso).addItem("DOUBLE HEAT SINK");

      cut.getPart(Part.Head).setArmor(ArmorSide.ONLY, 12);

      cut.getPart(Part.CenterTorso).setArmor(ArmorSide.FRONT, 39);
      cut.getPart(Part.CenterTorso).setArmor(ArmorSide.BACK, 5);
      cut.getPart(Part.CenterTorso).addItem("XL ENGINE 300");
      cut.getPart(Part.CenterTorso).addItem("DOUBLE HEAT SINK");
      cut.getPart(Part.CenterTorso).addItem("DOUBLE HEAT SINK");

      cut.getPart(Part.LeftLeg).setArmor(ArmorSide.ONLY, 32);

      cut.getPart(Part.RightLeg).setArmor(ArmorSide.ONLY, 32);

      // Verification against in-game mech lab
      assertEquals(232, cut.getArmor());
      assertTrue("mass = " + cut.getMass(), cut.getMass() > 35.0 - 1.0 / 32.0);
      assertTrue("mass = " + cut.getMass(), cut.getMass() <= 35.0);
   }
}

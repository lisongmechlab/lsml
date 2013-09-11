package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.MessageXBar;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.LoadoutPart;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LoadoutSerializationTest{
   @Mock
   MessageXBar xBar;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
   }

   /**
    * We can save and load loadouts.
    * 
    * @throws IOException
    */
   @Test
   public void testSaveLoad() throws IOException{
      // Setup
      Chassi chassi = ChassiDB.lookup("RVN-3L");
      Loadout cut = new Loadout(chassi, xBar);

      cut.rename("ecraven");
      cut.getUpgrades().setDoubleHeatSinks(true);
      cut.getUpgrades().setEndoSteel(true);

      for(Part part : Part.values()){
         if( part.isTwoSided() ){
            cut.getPart(part).setArmor(ArmorSide.FRONT, 10);
            cut.getPart(part).setArmor(ArmorSide.BACK, 10);
         }
         else{
            cut.getPart(part).setArmor(ArmorSide.ONLY, 10);
         }
      }

      cut.getPart(Part.CenterTorso).addItem(ItemDB.lookup("XL ENGINE 290"));
      cut.getPart(Part.LeftTorso).addItem(ItemDB.lookup("GUARDIAN ECM"));
      cut.getPart(Part.CenterTorso).addItem(ItemDB.DHS);

      cut.getPart(Part.RightArm).addItem(ItemDB.lookup("MED PULSE LASER"));
      cut.getPart(Part.RightArm).addItem(ItemDB.lookup("MED PULSE LASER"));

      cut.getPart(Part.RightTorso).addItem(ItemDB.lookup("TAG"));
      cut.getPart(Part.RightTorso).addItem(ItemDB.lookup("STREAK SRM 2"));

      cut.getPart(Part.LeftTorso).addItem(ItemDB.AMS);
      cut.getPart(Part.LeftTorso).addItem(ItemDB.lookup("AMS AMMO"));
      cut.getPart(Part.LeftTorso).addItem(ItemDB.lookup("STREAK SRM AMMO"));
      cut.getPart(Part.LeftTorso).addItem(ItemDB.lookup("STREAK SRM AMMO"));

      // cut.getInternalPartLoadout(InternalPartType.LeftArm).addItem(ItemDB.lookup("STREAK SRM 2")); // TODO: Add when
      // we can handle endo steel/ferrofib

      // Execute
      File testFile = new File("test.xml");
      cut.save(testFile);
      Loadout loaded = Loadout.load(testFile, xBar);

      // Verify
      assertEquals("ecraven", loaded.getName());
      assertTrue(loaded.getUpgrades().hasDoubleHeatSinks());
      assertTrue(loaded.getUpgrades().hasEndoSteel());

      for(Part part : Part.values()){
         if( part.isTwoSided() ){
            assertEquals(10, loaded.getPart(part).getArmor(ArmorSide.FRONT));
            assertEquals(10, loaded.getPart(part).getArmor(ArmorSide.BACK));
         }
         else{
            assertEquals(10, loaded.getPart(part).getArmor(ArmorSide.ONLY));
         }
      }

      // CT:
      {
         LoadoutPart part = loaded.getPart(Part.CenterTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("XL ENGINE 290")));
         assertTrue(items.remove(ItemDB.DHS));

         assertEquals(1, part.getNumEngineHeatsinks());

         assertOnlyInternals(items);
         assertEquals(1, items.size());
      }

      // RA:
      {
         LoadoutPart part = loaded.getPart(Part.RightArm);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("MED PULSE LASER")));
         assertTrue(items.remove(ItemDB.lookup("MED PULSE LASER")));

         assertOnlyInternals(items);
         assertEquals(2, items.size());
      }

      // LA:
      {
         LoadoutPart part = loaded.getPart(Part.LeftArm);
         List<Item> items = new ArrayList<Item>(part.getItems());

         // assertTrue(items.remove(ItemDB.lookup("STREAK SRM 2")));

         assertOnlyInternals(items);
         assertEquals(2, items.size());
      }

      // RT:
      {
         LoadoutPart part = loaded.getPart(Part.RightTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("TAG")));
         assertTrue(items.remove(ItemDB.lookup("STREAK SRM 2")));

         assertOnlyInternals(items);
         assertEquals(1, items.size()); // xl engine
      }

      // LT:
      {
         LoadoutPart part = loaded.getPart(Part.LeftTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("GUARDIAN ECM")));
         assertTrue(items.remove(ItemDB.AMS));
         assertTrue(items.remove(ItemDB.lookup("AMS AMMO")));
         assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));
         assertTrue(items.remove(ItemDB.lookup("STREAK SRM AMMO")));

         assertOnlyInternals(items);
         assertEquals(1, items.size()); // xl engine
      }
   }

   /**
    * We can make empty loadouts.
    */
   @Test
   public void testEmptyLoadout(){
      Chassi chassi = ChassiDB.lookup("CPLT-K2");
      Loadout cut = new Loadout(chassi, xBar);

      assertEquals(0, cut.getArmor());
      assertSame(chassi, cut.getChassi());
      assertNull(cut.getEngine());
      assertEquals(0, cut.getHeatsinksCount());
      assertEquals(chassi.getInternalMass(), cut.getMass(), 0.0);
      assertEquals(chassi.getNameShort(), cut.getName());
      assertEquals(21, cut.getNumCriticalSlotsUsed()); // 21 for empty K2
      assertEquals(57, cut.getNumCriticalSlotsFree()); // 57 for empty K2
      assertEquals(8, cut.getPartLoadOuts().size());

      Efficiencies efficiencies = cut.getEfficiencies();
      assertFalse(efficiencies.hasCoolRun());
      assertFalse(efficiencies.hasDoubleBasics());
      assertFalse(efficiencies.hasHeatContainment());
      assertFalse(efficiencies.hasSpeedTweak());

      assertFalse(cut.getUpgrades().hasArtemis());
      assertFalse(cut.getUpgrades().hasEndoSteel());
      assertFalse(cut.getUpgrades().hasFerroFibrous());
      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());
   }

   @Test
   public void testLoadSavedLoadout() throws Exception{
      Loadout cut = new Loadout("AS7-D-DC", xBar);

      File aFile = new File("test_Ddc7.xml");
      aFile.deleteOnExit();
      cut.save(aFile);

      Loadout.load(aFile, xBar); // Does not throw

      // TODO: Check that the same loadout was loaded
   }

   /**
    * Loading stock configuration shall succeed even if the loadout isn't empty to start with.
    * 
    * @throws Exception
    */
   @Test
   public void testLoadStockTwice() throws Exception{
      Loadout cut = new Loadout("JR7-F", xBar);
      cut.loadStock();
   }

   /**
    * All stock builds should be loadable without error!
    * 
    * @throws Exception
    */
   @Test
   public void testStockLoadout() throws Exception{
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));

      for(Chassi chassi : chassii){
         Loadout cut = new Loadout(chassi, xBar);
         cut.loadStock();
      }
   }

   /**
    * {@link Loadout#Loadout(String, MessageXBar)} works even for the special names.
    * 
    * @throws Exception
    */
   @Test
   public void testStockLoadoutIlya() throws Exception{
      Loadout cut = new Loadout("Ilya Muromets", xBar);
      cut.loadStock();
   }

   /**
    * We can load stock loadouts. (AS7-D)
    * 
    * @throws Exception
    */
   @Test
   public void testStockLoadoutAS7D() throws Exception{
      Chassi chassi = ChassiDB.lookup("AS7-D");
      Loadout cut = new Loadout("AS7-D", xBar);

      assertEquals(608, cut.getArmor());
      assertSame(chassi, cut.getChassi());
      assertSame(ItemDB.lookup("STD ENGINE 300"), cut.getEngine());
      assertEquals(20, cut.getHeatsinksCount());
      assertEquals(100.0, cut.getMass(), 0.0);
      assertEquals(chassi.getNameShort(), cut.getName());
      assertEquals(5 * 12 + 3 * 6 - 13, cut.getNumCriticalSlotsUsed()); // 21 for empty K2
      assertEquals(13, cut.getNumCriticalSlotsFree()); // 13 for stock AS7-D
      assertEquals(8, cut.getPartLoadOuts().size());

      assertFalse(cut.getUpgrades().hasArtemis());
      assertFalse(cut.getUpgrades().hasEndoSteel());
      assertFalse(cut.getUpgrades().hasFerroFibrous());
      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());

      // Right leg:
      {
         LoadoutPart part = cut.getPart(Part.RightLeg);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.SHS));
         assertTrue(items.remove(ItemDB.SHS));
         assertEquals(82, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Left leg:
      {
         LoadoutPart part = cut.getPart(Part.LeftLeg);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.SHS));
         assertTrue(items.remove(ItemDB.SHS));
         assertEquals(82, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }
      // Right arm:
      {
         LoadoutPart part = cut.getPart(Part.RightArm);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.SHS));
         assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
         assertEquals(68, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Left arm:
      {
         LoadoutPart part = cut.getPart(Part.LeftArm);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.SHS));
         assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
         assertEquals(68, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Right torso:
      {
         LoadoutPart part = cut.getPart(Part.RightTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("AC/20")));
         assertTrue(items.remove(ItemDB.lookup("AC/20 AMMO")));
         assertTrue(items.remove(ItemDB.lookup("AC/20 AMMO")));
         assertEquals(64, part.getArmor(ArmorSide.FRONT));
         assertEquals(20, part.getArmor(ArmorSide.BACK));

         assertOnlyInternals(items);
         assertEquals(0, items.size());
      }

      // Left torso:
      {
         LoadoutPart part = cut.getPart(Part.LeftTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("LRM 20")));
         assertTrue(items.remove(ItemDB.lookup("SRM6")));
         assertTrue(items.remove(ItemDB.lookup("LRM AMMO")));
         assertTrue(items.remove(ItemDB.lookup("LRM AMMO")));
         assertTrue(items.remove(ItemDB.lookup("SRM AMMO")));
         assertTrue(items.remove(ItemDB.SHS));
         assertEquals(64, part.getArmor(ArmorSide.FRONT));
         assertEquals(20, part.getArmor(ArmorSide.BACK));

         assertOnlyInternals(items);
         assertEquals(0, items.size());
      }

      // Center torso:
      {
         LoadoutPart part = cut.getPart(Part.CenterTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("STD ENGINE 300")));
         assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
         assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
         assertTrue(items.remove(ItemDB.SHS));
         assertTrue(items.remove(ItemDB.SHS));
         assertEquals(94, part.getArmor(ArmorSide.FRONT));
         assertEquals(28, part.getArmor(ArmorSide.BACK));

         assertEquals(2, part.getNumEngineHeatsinks());

         assertOnlyInternals(items);
         assertEquals(1, items.size());
      }

      // Head:
      {
         LoadoutPart part = cut.getPart(Part.Head);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.SHS));
         assertEquals(18, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(3, items.size());
      }

      Efficiencies efficiencies = cut.getEfficiencies();
      assertFalse(efficiencies.hasCoolRun());
      assertFalse(efficiencies.hasDoubleBasics());
      assertFalse(efficiencies.hasHeatContainment());
      assertFalse(efficiencies.hasSpeedTweak());
   }

   /**
    * We can load stock loadouts. (SDR-5V)
    * 
    * @throws Exception
    */
   @Test
   public void testStockLoadoutSDR5V() throws Exception{
      Chassi chassi = ChassiDB.lookup("SDR-5V");
      Loadout cut = new Loadout("SDR-5V", xBar);

      assertEquals(112, cut.getArmor());
      assertSame(chassi, cut.getChassi());
      assertSame(ItemDB.lookup("STD ENGINE 240"), cut.getEngine());
      assertEquals(10, cut.getHeatsinksCount());
      assertEquals(30.0, cut.getMass(), 0.0);
      assertEquals(chassi.getNameShort(), cut.getName());
      assertEquals(5 * 12 + 3 * 6 - 36, cut.getNumCriticalSlotsUsed());
      assertEquals(36, cut.getNumCriticalSlotsFree());
      assertEquals(8, cut.getPartLoadOuts().size());

      assertFalse(cut.getUpgrades().hasArtemis());
      assertFalse(cut.getUpgrades().hasEndoSteel());
      assertFalse(cut.getUpgrades().hasFerroFibrous());
      assertFalse(cut.getUpgrades().hasDoubleHeatSinks());

      // Right leg:
      {
         LoadoutPart part = cut.getPart(Part.RightLeg);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertEquals(12, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Left leg:
      {
         LoadoutPart part = cut.getPart(Part.LeftLeg);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertEquals(12, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Right arm:
      {
         LoadoutPart part = cut.getPart(Part.RightArm);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertEquals(10, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Left arm:
      {
         LoadoutPart part = cut.getPart(Part.LeftArm);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertEquals(10, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(4, items.size());
      }

      // Right torso:
      {
         LoadoutPart part = cut.getPart(Part.RightTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertEquals(12, part.getArmor(ArmorSide.FRONT));
         assertEquals(4, part.getArmor(ArmorSide.BACK));

         assertOnlyInternals(items);
         assertEquals(0, items.size());
      }

      // Left torso:
      {
         LoadoutPart part = cut.getPart(Part.LeftTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertTrue(items.remove(ItemDB.lookup("JUMP JETS - CLASS V")));
         assertEquals(12, part.getArmor(ArmorSide.FRONT));
         assertEquals(4, part.getArmor(ArmorSide.BACK));

         assertOnlyInternals(items);
         assertEquals(0, items.size());
      }

      // Center torso:
      {
         LoadoutPart part = cut.getPart(Part.CenterTorso);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.lookup("STD ENGINE 240")));
         assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
         assertTrue(items.remove(ItemDB.lookup("MEDIUM LASER")));
         assertEquals(16, part.getArmor(ArmorSide.FRONT));
         assertEquals(8, part.getArmor(ArmorSide.BACK));

         assertEquals(0, part.getNumEngineHeatsinks());

         assertOnlyInternals(items);
         assertEquals(1, items.size());
      }

      // Head:
      {
         LoadoutPart part = cut.getPart(Part.Head);
         List<Item> items = new ArrayList<Item>(part.getItems());

         assertTrue(items.remove(ItemDB.SHS));
         assertEquals(12, part.getArmor(ArmorSide.ONLY));

         assertOnlyInternals(items);
         assertEquals(3, items.size());
      }

      Efficiencies efficiencies = cut.getEfficiencies();
      assertFalse(efficiencies.hasCoolRun());
      assertFalse(efficiencies.hasDoubleBasics());
      assertFalse(efficiencies.hasHeatContainment());
      assertFalse(efficiencies.hasSpeedTweak());
   }

   // TODO: Move these to statistics test!
   /*
    * @Test public void testHeatSHS_SmallEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 210"));
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS); assertEquals(9,
    * cut.getHeatsinksCount()); assertEquals(9.0, cut.getHeatCapacity(), 0.0); assertEquals(0.9,
    * cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(9, cut.getHeatsinksCount());
    * assertEquals(9.0, cut.getHeatCapacity(), 0.0); assertEquals(0.9 * 1.075, cut.getHeatDissapation(), 0.0);
    * cut.getEfficiencies().setHeatContainment(true); cut.getEfficiencies().setCoolRun(false); assertEquals(9,
    * cut.getHeatsinksCount()); assertEquals(9.0 * 1.075, cut.getHeatCapacity(), 0.0); assertEquals(0.9,
    * cut.getHeatDissapation(), 0.0); }
    * @Test public void testHeatDHS_SmallEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
    * cut.getUpgrades().setDoubleHeatSinks(true);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 210")); // 8 internal
    * cut.getInternalPartLoadout(InternalPartType.LeftArm).addItem(ItemDB.DHS); assertEquals(9,
    * cut.getHeatsinksCount()); assertEquals(8 * 2 + 1.4 * 1, cut.getHeatCapacity(), 0.0); assertEquals((8 * 2 + 1.4 *
    * 1) / 10.0, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(9,
    * cut.getHeatsinksCount()); assertEquals((8 * 2 + 1.4 * 1), cut.getHeatCapacity(), 0.0); assertEquals((8 * 2 + 1.4 *
    * 1) / 10.0 * 1.075, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setHeatContainment(true);
    * cut.getEfficiencies().setCoolRun(false); assertEquals(9, cut.getHeatsinksCount()); assertEquals((8 * 2 + 1.4 * 1)
    * * 1.075, cut.getHeatCapacity(), 0.0); assertEquals((8 * 2 + 1.4 * 1) / 10.0, cut.getHeatDissapation(), 0.0); }
    * @Test public void testHeatSHS_BigEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 355"));
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.SHS); assertEquals(16,
    * cut.getHeatsinksCount()); assertEquals(16.0, cut.getHeatCapacity(), 0.0); assertEquals(1.6,
    * cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(16, cut.getHeatsinksCount());
    * assertEquals(16.0, cut.getHeatCapacity(), 0.0); assertEquals(1.6 * 1.075, cut.getHeatDissapation(), 0.0);
    * cut.getEfficiencies().setHeatContainment(true); cut.getEfficiencies().setCoolRun(false); assertEquals(16,
    * cut.getHeatsinksCount()); assertEquals(16.0 * 1.075, cut.getHeatCapacity(), 0.0); assertEquals(1.6,
    * cut.getHeatDissapation(), 0.0); }
    * @Test public void testHeatDHS_BigEngine(){ Loadout cut = new Loadout(ChassiDB.lookup("ATLAS", "as7-k"));
    * cut.getUpgrades().setDoubleHeatSinks(true);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.lookup("STD ENGINE 355"));
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
    * cut.getInternalPartLoadout(InternalPartType.CenterTorso).addItem(ItemDB.DHS);
    * cut.getInternalPartLoadout(InternalPartType.LeftTorso).addItem(ItemDB.DHS);
    * cut.getInternalPartLoadout(InternalPartType.LeftTorso).addItem(ItemDB.DHS); assertEquals(16,
    * cut.getHeatsinksCount()); assertEquals((2 * 10 + 1.4 * 6), cut.getHeatCapacity(), 0.0); assertEquals((2 * 10 + 1.4
    * * 6) / 10.0, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setCoolRun(true); assertEquals(16,
    * cut.getHeatsinksCount()); assertEquals((2 * 10 + 1.4 * 6), cut.getHeatCapacity(), 0.0); assertEquals((2 * 10 + 1.4
    * * 6) / 10.0 * 1.075, cut.getHeatDissapation(), 0.0); cut.getEfficiencies().setHeatContainment(true);
    * cut.getEfficiencies().setCoolRun(false); assertEquals(16, cut.getHeatsinksCount()); assertEquals((2 * 10 + 1.4 *
    * 6) * 1.075, cut.getHeatCapacity(), 0.0); assertEquals((2 * 10 + 1.4 * 6) / 10.0, cut.getHeatDissapation(), 0.0); }
    */

   private void assertOnlyInternals(List<Item> aList){
      for(Item item : aList){
         assertTrue(item instanceof Internal);
      }
   }

}

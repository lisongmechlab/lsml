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
package lisong_mechlab.model.loadout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lisong_mechlab.model.chassi.ArmorSide;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test suite for {@link LoadoutSerializationTest}.
 * 
 * @author Li Song
 */
@RunWith(JUnitParamsRunner.class)
public class LoadoutSerializationTest{
   @Mock
   MessageXBar    xBar;
   @Mock
   OperationStack undoStack;

   @Before
   public void setup(){
      MockitoAnnotations.initMocks(this);
   }

   @SuppressWarnings("unused")
   private Object[] allChassis(){
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));
      return chassii.toArray();
   }

   /**
    * We can save and load all stock Loadouts.
    * 
    * @param aChassi
    * @throws Exception
    */
   @Test
   @Parameters(method = "allChassis")
   public void testSaveLoad(Chassi aChassi) throws Exception{
      Loadout cut = new Loadout(aChassi.getNameShort(), xBar);
      cut.rename(cut.getName() + "x");

      File testFile = new File(cut.getName() + ".xml");
      testFile.deleteOnExit();

      cut.save(testFile);

      Loadout loaded = Loadout.load(testFile, xBar);

      assertEquals(cut, loaded);
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

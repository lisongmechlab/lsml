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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.model.upgrades.Upgrades.Message.ChangeMsg;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Test suite for {@link OpLoadStock}.
 * 
 * @author Emily Björk
 */
@RunWith(JUnitParamsRunner.class)
public class LoadStockOperationTest{
   private MessageXBar xBar;

   @Before
   public void setup(){
      xBar = Mockito.mock(MessageXBar.class);
   }

   /**
    * Loading stock configuration shall succeed even if the loadout isn't empty to start with.
    * 
    * @throws Exception
    */
   @Test
   public void testNotEmpty() throws Exception{
      // Setup
      ChassisStandard chassi = (ChassisStandard)ChassisDB.lookup("JR7-F");
      LoadoutStandard loadout = new LoadoutStandard(chassi, xBar);
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new OpLoadStock(chassi, loadout, xBar));

      assertTrue(loadout.getMass() > 34.9);

      // Execute
      opstack.pushAndApply(new OpLoadStock(chassi, loadout, xBar));
   }

   public Object[] allChassis(){
      List<ChassisBase> chassii = new ArrayList<>();
      chassii.addAll(ChassisDB.lookup(ChassisClass.LIGHT));
      chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
      chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
      chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
      return chassii.toArray();
   }

   /**
    * Loading stock configuration shall produce a complete loadout for all chassis
    * 
    * @param aChassis
    *           Chassis to test on.
    * @throws Exception
    */
   @Test
   @Parameters(method = "allChassis")
   public void testApply(ChassisStandard aChassis) throws Exception{
      // Setup
      LoadoutStandard loadout = new LoadoutStandard(aChassis, xBar);

      // Execute
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new OpLoadStock(aChassis, loadout, xBar));

      // Verify (What the hell is up with the misery's stock loadout with almost one ton free mass and not full armor?!)
      assertTrue(loadout.getFreeMass() < 0.5 || (loadout.getName().contains("STK-M") && loadout.getFreeMass() < 1));
      for(ConfiguredComponentBase part : loadout.getComponents()){
         Mockito.verify(xBar, Mockito.atLeast(1)).post(new ConfiguredComponentBase.Message(part, Type.ArmorChanged));
      }
      Mockito.verify(xBar, Mockito.atLeast(1)).post(new ConfiguredComponentBase.Message(Matchers.any(ConfiguredComponentBase.class), Type.ItemAdded));
      Mockito.verify(xBar, Mockito.atLeast(1)).post(new Upgrades.Message(Matchers.any(ChangeMsg.class), loadout.getUpgrades()));
   }

   /**
    * Loading stock shall handle artemis changes on February 4th patch.
    * 
    * @throws Exception
    */
   @Test
   public void testApply_artemisFeb4() throws Exception{
      // Setup
      LoadoutStandard loadout = new LoadoutStandard((ChassisStandard)ChassisDB.lookup("CN9-D"), xBar);

      // Execute
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new OpLoadStock(loadout.getChassis(), loadout, xBar));

      assertTrue(loadout.getComponent(Location.LeftTorso).getItemsAll().contains(ItemDB.lookup("LRM 10 + ARTEMIS")));
   }

   /**
    * Undoing load stock shall produce previous loadout.
    * 
    * @throws Exception
    */
   @Test
   public void testUndo() throws Exception{
      // Setup
      ChassisStandard chassi = (ChassisStandard)ChassisDB.lookup("JR7-F");
      LoadoutStandard reference = new LoadoutStandard(chassi, xBar);
      LoadoutStandard loadout = new LoadoutStandard(chassi, xBar);
      OperationStack opstack = new OperationStack(1);
      opstack.pushAndApply(new OpLoadStock(loadout.getChassis(), loadout, xBar));

      // Execute
      opstack.undo();

      // Verify
      assertEquals(reference, loadout);
   }
}

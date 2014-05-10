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
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.ChassisClass;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message.Type;
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
      Chassis chassi = ChassisDB.lookup("JR7-F");
      Loadout loadout = new Loadout(chassi, xBar);
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new OpLoadStock(chassi, loadout, xBar));

      assertTrue(loadout.getMass() > 34.9);

      // Execute
      opstack.pushAndApply(new OpLoadStock(chassi, loadout, xBar));
   }

   public Object[] allChassis(){
      List<Chassis> chassii = new ArrayList<>(ChassisDB.lookup(ChassisClass.LIGHT));
      chassii.addAll(ChassisDB.lookup(ChassisClass.MEDIUM));
      chassii.addAll(ChassisDB.lookup(ChassisClass.HEAVY));
      chassii.addAll(ChassisDB.lookup(ChassisClass.ASSAULT));
      return chassii.toArray();
   }

   /**
    * Loading stock configuration shall produce a complete loadout for all chassis
    * 
    * @param aChassi
    *           Chassi to test on.
    * @throws Exception
    */
   @Test
   @Parameters(method = "allChassis")
   public void testApply(Chassis aChassi) throws Exception{
      // Setup
      Loadout loadout = new Loadout(aChassi, xBar);

      // Execute
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new OpLoadStock(aChassi, loadout, xBar));

      // Verify (What the hell is up with the misery's stock loadout with almost one ton free mass and not full armor?!)
      assertTrue(loadout.getFreeMass() < 0.5 || (loadout.getName().contains("STK-M") && loadout.getFreeMass() < 1));
      for(ConfiguredComponent part : loadout.getPartLoadOuts()){
         Mockito.verify(xBar, Mockito.atLeast(1)).post(new ConfiguredComponent.Message(part, Type.ArmorChanged));
      }
      Mockito.verify(xBar, Mockito.atLeast(1)).post(new ConfiguredComponent.Message(Matchers.any(ConfiguredComponent.class), Type.ItemAdded));
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
      Loadout loadout = new Loadout(ChassisDB.lookup("CN9-D"), xBar);

      // Execute
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new OpLoadStock(loadout.getChassi(), loadout, xBar));

      assertTrue(loadout.getPart(Location.LeftTorso).getItems().contains(ItemDB.lookup("LRM 10 + ARTEMIS")));
   }

   /**
    * Undoing load stock shall produce previous loadout.
    * 
    * @throws Exception
    */
   @Test
   public void testUndo() throws Exception{
      // Setup
      Chassis chassi = ChassisDB.lookup("JR7-F");
      Loadout reference = new Loadout(chassi, xBar);
      Loadout loadout = new Loadout(chassi, xBar);
      OperationStack opstack = new OperationStack(1);
      opstack.pushAndApply(new OpLoadStock(loadout.getChassi(), loadout, xBar));

      // Execute
      opstack.undo();

      // Verify
      assertEquals(reference, loadout);
   }
}

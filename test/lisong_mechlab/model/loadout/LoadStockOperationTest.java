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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.chassi.ChassiClass;
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
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
 * Test suite for {@link LoadStockOperation}.
 * 
 * @author Li Song
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
      Chassi chassi = ChassiDB.lookup("JR7-F");
      Loadout loadout = new Loadout(chassi, xBar);
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new LoadStockOperation(loadout, xBar));

      // Execute
      opstack.pushAndApply(new LoadStockOperation(loadout, xBar));
   }

   public Object[] allChassis(){
      List<Chassi> chassii = new ArrayList<>(ChassiDB.lookup(ChassiClass.LIGHT));
      chassii.addAll(ChassiDB.lookup(ChassiClass.MEDIUM));
      chassii.addAll(ChassiDB.lookup(ChassiClass.HEAVY));
      chassii.addAll(ChassiDB.lookup(ChassiClass.ASSAULT));
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
   public void testApply(Chassi aChassi) throws Exception{
      // Setup
      Loadout loadout = new Loadout(aChassi, xBar);

      // Execute
      OperationStack opstack = new OperationStack(0);
      opstack.pushAndApply(new LoadStockOperation(loadout, xBar));

      // Verify
      assertTrue(loadout.getFreeMass() < 0.05);
      for(LoadoutPart part : loadout.getPartLoadOuts()){
         Mockito.verify(xBar).post(new LoadoutPart.Message(part, Type.ArmorChanged));
      }
      Mockito.verify(xBar).post(new LoadoutPart.Message(Matchers.any(LoadoutPart.class), Type.ItemAdded));
      Mockito.verify(xBar).post(new Upgrades.Message(Matchers.any(ChangeMsg.class), loadout.getUpgrades()));
   }

   /**
    * Undoing load stock shall produce previous loadout.
    * 
    * @throws Exception
    */
   @Test
   public void testUndo() throws Exception{
      // Setup
      Chassi chassi = ChassiDB.lookup("JR7-F");
      Loadout reference = new Loadout(chassi, xBar);
      Loadout loadout = new Loadout(chassi, xBar);
      OperationStack opstack = new OperationStack(1);
      opstack.pushAndApply(new LoadStockOperation(loadout, xBar));

      // Execute
      opstack.undo();

      // Verify
      assertEquals(reference, loadout);
   }
}

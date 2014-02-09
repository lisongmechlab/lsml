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
import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.UpgradeDB;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link StripOperation}.
 * 
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.class)
public class StripOperationTest{

   @Mock
   private MessageXBar xBar;

   /**
    * Stripping a loadout shall remove all upgrades, items and armor.
    * 
    * @throws Exception
    */
   @Test
   public void testStrip() throws Exception{
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("KTO-19").getName(), xBar); // Has Ferro-Fib standard

      // Execute
      OperationStack opStack = new OperationStack(0);
      opStack.pushAndApply(new StripOperation(cut, xBar));

      // Verify
      for(LoadoutPart loadoutPart : cut.getPartLoadOuts()){
         assertEquals(0.0, loadoutPart.getItemMass(), 0.0);
         assertEquals(0, loadoutPart.getArmorTotal());
      }
      assertEquals(UpgradeDB.STANDARD_GUIDANCE, cut.getUpgrades().getGuidance());
      assertEquals(UpgradeDB.STANDARD_STRUCTURE, cut.getUpgrades().getStructure());
      assertEquals(UpgradeDB.STANDARD_ARMOR, cut.getUpgrades().getArmor());
      assertEquals(UpgradeDB.STANDARD_HEATSINKS, cut.getUpgrades().getHeatSink());
   }
}

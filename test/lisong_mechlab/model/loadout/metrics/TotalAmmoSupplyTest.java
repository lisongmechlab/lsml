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
package lisong_mechlab.model.loadout.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.TreeMap;

import lisong_mechlab.model.chassi.ChassiDB;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.UndoStack;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TotalAmmoSupplyTest{

   @Spy
   MessageXBar             xBar;

   @Mock
   UndoStack               undoStack;

   @Mock
   private Loadout         loadout;
   @InjectMocks
   private TotalAmmoSupply totalAmmoSupply;

   @Test
   public void testGenerate(){
      // Setup
      Loadout cut = new Loadout(ChassiDB.lookup("COM-2D"), xBar, undoStack);
      try{
         cut.loadStock();
      }
      catch( Exception e ){
         fail("Unexpected exception when loading stock!");
         e.printStackTrace();
      }
      totalAmmoSupply = new TotalAmmoSupply(cut);
      // Verify
      Item testItem = null;
      for(Item item : cut.getAllItems()){
         if( item instanceof Ammunition ){
            testItem = item;
         }

      }
      TreeMap<Ammunition, Integer> ammoValuesTest = totalAmmoSupply.calculate();
      Integer actual = ammoValuesTest.get(testItem);
      assertEquals(2, actual.intValue());

   }

}

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
package lisong_mechlab.model.metrics;

import static org.junit.Assert.assertEquals;

import java.util.TreeMap;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TotalWeaponsTest{
   @Spy
   MessageXBar          xBar;
   @Mock
   private Loadout      loadout;
   @InjectMocks
   private TotalWeapons totalWeapons;

   @Test
   public void testCalculate() throws Exception{
      // Setup
      Loadout cut = new Loadout("COM-2D", xBar);

      // Exception
      totalWeapons = new TotalWeapons(cut);

      // Verify
      Item testItem = null;
      for(Item item : cut.getAllItems()){
         if( item instanceof Weapon ){
            testItem = item;
         }
      }

      TreeMap<Weapon, Integer> ammoValuesTest = totalWeapons.calculate();
      Integer actual = ammoValuesTest.get(testItem);
      assertEquals(1, actual.intValue());
   }

}

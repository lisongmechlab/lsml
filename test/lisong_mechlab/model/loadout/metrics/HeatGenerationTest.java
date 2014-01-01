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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;

import org.junit.Test;

/**
 * Test suite for {@link HeatGeneration}.
 * 
 * @author Emily Björk
 */
public class HeatGenerationTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   private final HeatGeneration       cut = new HeatGeneration(mlc.loadout);

   /**
    * Heat generation shall include heat per second from all weapons as well as the base heat from the engine. But no
    * heat from the jump jets.
    */
   @Test
   public void testCalculate(){
      List<Item> items = new ArrayList<>();
      Weapon ppc = (Weapon)ItemDB.lookup("PPC");
      Weapon ll = (Weapon)ItemDB.lookup("LARGE LASER");
      Weapon lrm20 = (Weapon)ItemDB.lookup("LRM 20");
      Weapon lb10x = (Weapon)ItemDB.lookup("LB 10-X AC");
      Engine engine = (Engine)ItemDB.lookup("STD ENGINE 300");
      Item jj = ItemDB.lookup("JUMP JETS - CLASS V");
      items.add(ItemDB.BAP); // Shall not barf on non-weapons
      items.add(ppc);
      items.add(ll);
      items.add(lrm20);
      items.add(lb10x);
      items.add(engine);
      items.add(jj);
      when(mlc.loadout.getAllItems()).thenReturn(items);
      when(mlc.efficiencies.getWeaponCycleTimeModifier()).thenReturn(1.0);

      final double expected = ppc.getStat("h/s", null, null) + ll.getStat("h/s", null, null) + lrm20.getStat("h/s", null, null)
                              + lb10x.getStat("h/s", null, null) + engine.getHeat();
      assertEquals(expected, cut.calculate(), 0.0);
   }

}

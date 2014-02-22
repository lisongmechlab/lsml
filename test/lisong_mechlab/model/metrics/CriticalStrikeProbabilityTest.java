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
package lisong_mechlab.model.metrics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.Upgrades;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A test suite for {@link CriticalStrikeProbability}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class CriticalStrikeProbabilityTest{
   List<Item>                items = new ArrayList<>();
   @Mock
   LoadoutPart               loadoutPart;
   @Mock
   Loadout                   loadout;
   @Mock
   Upgrades                  upgrades;
   @InjectMocks
   CriticalStrikeProbability cut;

   @Before
   public void setup(){
      Mockito.when(loadoutPart.getItems()).thenReturn(items);
      Mockito.when(loadoutPart.getLoadout()).thenReturn(loadout);
      Mockito.when(loadout.getUpgrades()).thenReturn(upgrades);
   }

   /**
    * Easy case. When there is only one item, the chance to crit is the sum of the crit probabilities for 1, 2 and 3
    * hits.
    */
   @Test
   public void testOneItem(){
      Item i = Mockito.mock(Item.class);
      Mockito.when(i.getNumCriticalSlots(upgrades)).thenReturn(5);
      items.add(i);

      assertEquals(0.25 + 0.14 + 0.03, cut.calculate(i), 0.0);
   }

   /**
    * Internal items do not affect the crit rolls.
    */
   @Test
   public void testNoInternals(){
      Item i = Mockito.mock(Item.class);
      Item internal = Mockito.mock(Internal.class);
      Mockito.when(i.getNumCriticalSlots(upgrades)).thenReturn(5);
      Mockito.when(internal.getNumCriticalSlots(upgrades)).thenReturn(5);
      items.add(i);
      items.add(internal);

      assertEquals(0.25 + 0.14 + 0.03, cut.calculate(i), 0.0);
   }

   /**
    * XL engine sides do affect the crit rolls.
    */
   @Test
   public void testEngineInternals(){
      Item i = LoadoutPart.ENGINE_INTERNAL;
      Item internal = Mockito.mock(Internal.class);
      Mockito.when(internal.getNumCriticalSlots(upgrades)).thenReturn(5);
      items.add(i);
      items.add(internal);

      assertEquals(0.25 + 0.14 + 0.03, cut.calculate(i), 0.0);
   }

   /**
    * When two or more items are involved the calculations are a bit more complex. The chance that the item will be hit
    * is the chance that any of the crit rolls hits at least once. This is binominal(n,p) where n is the number of rolls
    * and p is the chance that any one roll will hit our item.
    */
   @Test
   public void testTwoItems(){
      Item i0 = Mockito.mock(Item.class);
      Item i1 = Mockito.mock(Item.class);
      Mockito.when(i0.getNumCriticalSlots(upgrades)).thenReturn(5);
      Mockito.when(i1.getNumCriticalSlots(upgrades)).thenReturn(15);
      items.add(i0);
      items.add(i1);

      double p_hit0 = 5.0 / 20;
      double p_hit1 = 15.0 / 20;
      double ans0 = 0, ans1 = 0;
      // 1 crit hit: 25%
      ans0 = p_hit0 * 0.25;
      ans1 = p_hit1 * 0.25;

      // 2 crit hits: 14%
      ans0 += 0.14 * (2 * p_hit0 * (1 - p_hit0) + p_hit0 * p_hit0);
      ans1 += 0.14 * (2 * p_hit1 * (1 - p_hit1) + p_hit1 * p_hit1);

      // 3 crit hits: 3%
      ans0 += 0.03 * (3 * p_hit0 * (1 - p_hit0) * (1 - p_hit0) + 3 * p_hit0 * p_hit0 * (1 - p_hit0) + p_hit0 * p_hit0 * p_hit0);
      ans1 += 0.03 * (3 * p_hit1 * (1 - p_hit1) * (1 - p_hit1) + 3 * p_hit1 * p_hit1 * (1 - p_hit1) + p_hit1 * p_hit1 * p_hit1);

      assertEquals(ans0, cut.calculate(i0), 0.000001);
      assertEquals(ans1, cut.calculate(i1), 0.000001);
   }
}

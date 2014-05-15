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
package lisong_mechlab.model.metrics.helpers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.model.metrics.CriticalStrikeProbability;
import lisong_mechlab.util.MessageXBar;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link ComponentDestructionSimulator}.
 * 
 * @author Emily Björk
 */
public class ComponentDestructionSimulatorTest{

   @Test
   public void testSoloComponent(){
      double partHp = 42;
      List<Item> partItems = new ArrayList<>();
      partItems.add(ItemDB.BAP);

      InternalComponent internalPart = Mockito.mock(InternalComponent.class);
      Mockito.when(internalPart.getHitpoints()).thenReturn(partHp);

      ConfiguredComponent part = Mockito.mock(ConfiguredComponent.class);
      Mockito.when(part.getItems()).thenReturn(partItems);
      Mockito.when(part.getInternalComponent()).thenReturn(internalPart);

      MessageXBar xBar = Mockito.mock(MessageXBar.class);

      ComponentDestructionSimulator cut = new ComponentDestructionSimulator(part, xBar);
      cut.simulate();

      // There are 5 shots before the item explodes, thus the probability of all of them missing the item is:
      // 0.58^5, i.e. the probability of the item exploding is: 1 - 0.58^5
      double P_miss = 1.0;
      for(int i = 0; i < CriticalStrikeProbability.CRIT_CHANCE.length; ++i){
         P_miss -= CriticalStrikeProbability.CRIT_CHANCE[i];
      }

      double P_hit = 1 - Math.pow(P_miss, 5);

      assertEquals(P_hit, cut.getProbabilityOfDestruction(ItemDB.BAP), 0.0001);
   }

   /**
    * The AC20 has 18 health and needs two 10-point alphas to be destroyed
    */
   @Test
   public void testAC20Health(){
      double partHp = 2; // Only allow one alpha
      List<Item> partItems = new ArrayList<>();
      partItems.add(ItemDB.lookup("AC/20"));

      InternalComponent internalPart = Mockito.mock(InternalComponent.class);
      Mockito.when(internalPart.getHitpoints()).thenReturn(partHp);

      ConfiguredComponent part = Mockito.mock(ConfiguredComponent.class);
      Mockito.when(part.getItems()).thenReturn(partItems);
      Mockito.when(part.getInternalComponent()).thenReturn(internalPart);

      MessageXBar xBar = Mockito.mock(MessageXBar.class);

      ComponentDestructionSimulator cut = new ComponentDestructionSimulator(part, xBar);
      cut.simulate();

      // The AC/20 will only explode if there is a double or triple critical hit (14+3%)
      double P_hit = CriticalStrikeProbability.CRIT_CHANCE[1] + CriticalStrikeProbability.CRIT_CHANCE[2];

      assertEquals(P_hit, cut.getProbabilityOfDestruction(ItemDB.lookup("AC/20")), 0.0001);
   }

   /**
    * The AC20 has 18 health and needs two 10-point alphas to be destroyed.
    * <p>
    * Test with two shots.
    */
   @Test
   public void testAC20HealthTwoAlphas(){
      double partHp = 12; // Allow two alphas
      List<Item> partItems = new ArrayList<>();
      partItems.add(ItemDB.lookup("AC/20"));

      InternalComponent internalPart = Mockito.mock(InternalComponent.class);
      Mockito.when(internalPart.getHitpoints()).thenReturn(partHp);

      ConfiguredComponent part = Mockito.mock(ConfiguredComponent.class);
      Mockito.when(part.getItems()).thenReturn(partItems);
      Mockito.when(part.getInternalComponent()).thenReturn(internalPart);

      MessageXBar xBar = Mockito.mock(MessageXBar.class);

      ComponentDestructionSimulator cut = new ComponentDestructionSimulator(part, xBar);
      cut.simulate();

      // The AC/20 will only explode if it is hit twice or more
      // First shot: 58% chance to miss, 25% chance of one hit and 17% of 2 or more hits.
      // Second shot, first one missed: Need 2 hits, ie 0.58*0.17 chance
      // Second shit, first one was a single hit: Need 1 hits, ie 0.25*0.42
      // Total probability: 0.17*1.0 + 0.25*0.42 + 0.58*0.17
      double P_hit = 0.17 * 1.0 + 0.25 * 0.42 + 0.58 * 0.17;
      assertEquals(P_hit, cut.getProbabilityOfDestruction(ItemDB.lookup("AC/20")), 0.0001);
   }
}

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

import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.BinomialDistribution;

/**
 * This {@link ItemMetric} calculates the probability that the given item will be critically hit by a shot.
 * <p>
 * This applies to high alpha weapons such as PPC, Gauss, AC/20,10,5.
 * 
 * @author Emily Björk
 */
public class CriticalStrikeProbability implements ItemMetric{
   public final static double CRIT_CHANCE[] = {0.25, 0.14, 0.03};
   private final LoadoutPart  loadoutPart;

   public CriticalStrikeProbability(LoadoutPart aLoadoutPart){
      loadoutPart = aLoadoutPart;
   }

   @Override
   public double calculate(Item aItem){
      int slots = 0;
      Upgrades upgrades = loadoutPart.getLoadout().getUpgrades();
      for(Item it : loadoutPart.getItems()){
         if( it instanceof Internal && it != LoadoutPart.ENGINE_INTERNAL ){
            continue;
         }
         slots += it.getNumCriticalSlots(upgrades);
      }
      
      double p_hit = (double)aItem.getNumCriticalSlots(upgrades) / slots;

      double ans = 0;
      for(int i = 0; i < CriticalStrikeProbability.CRIT_CHANCE.length; ++i){
         final int numCritRolls = i + 1;
         BinomialDistribution bin = new BinomialDistribution(p_hit, numCritRolls);

         for(int numHits = 1; numHits <= numCritRolls; ++numHits){
            ans += bin.pdf(numHits) * CriticalStrikeProbability.CRIT_CHANCE[i];
         }
      }
      return ans;
   }
}

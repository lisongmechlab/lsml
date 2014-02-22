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

import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.BinomialDistribution;

/**
 * This {@link Metric} calculates the multiplier of internal HP of the item lost per shot at the component.
 * <p>
 * For example shooting an AC/20 at a component with 1 MLAS and 1 DHS has 25% chance to deal 1 critical hit, 14% chance
 * to deal 2 hits and 3% chance to deal 3 hits. The DHS has a 75% chance of being hit by every critical hit. The actual
 * number of critical hits on the DHS is 0.25*bin(0.75, 1) + 0.14*bin(0.75, 2) + 0.03*bin(0.
 * 
 * @author Li Song
 */
public class CriticalStrikeMultiplier implements ItemMetric{
   private final static double CRIT_CHANCE[] = {0.25, 0.14, 0.03};
   private final LoadoutPart   loadoutPart;

   public CriticalStrikeMultiplier(LoadoutPart aLoadoutPart){
      loadoutPart = aLoadoutPart;
   }

   @Override
   public double calculate(Item aItem){
      return calculate(aItem, loadoutPart);
   }

   public static double calculate(Item anItem, LoadoutPart aLoadoutPart){
      int slots = 0;
      for(Item it : aLoadoutPart.getItems()){
         if( it instanceof Internal && it != LoadoutPart.ENGINE_INTERNAL ){
            continue; // Internals (apart from engine side torsos) cannot be crit.
         }
         slots += it.getNumCriticalSlots(aLoadoutPart.getLoadout().getUpgrades());
      }
      return calculate(anItem.getNumCriticalSlots(aLoadoutPart.getLoadout().getUpgrades()), slots);
   }

   public static double calculate(int aItemCrits, int aTotalCrits){
      double p_hit = (double)aItemCrits / aTotalCrits;

      double ans = 0;
      for(int i = 0; i < CRIT_CHANCE.length; ++i){
         final int numCritRolls = i + 1;
         BinomialDistribution bin = new BinomialDistribution(p_hit, numCritRolls);

         for(int numHits = 1; numHits <= numCritRolls; ++numHits){
            ans += bin.pdf(numHits) * numHits * CRIT_CHANCE[i];
         }
      }
      return ans;
   }
   
}

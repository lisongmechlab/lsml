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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.part.LoadoutPart;

/**
 * This {@link Metric} calculates the probability that a {@link LoadoutPart} will suffer an ammo destruction before it
 * is destroyed.
 * 
 * @author Li Song
 */
public class AmmoDestructionProbability implements Metric{

   private final LoadoutPart loadoutPart;

   /**
    * @param aLoadoutPart
    */
   public AmmoDestructionProbability(LoadoutPart aLoadoutPart){
      loadoutPart = aLoadoutPart;
   }

   /*
    * (non-Javadoc)
    * @see lisong_mechlab.model.metrics.Metric#calculate()
    */
   @Override
   public double calculate(){
      // How many ten point alphas can the component take?
      int numTenPoints = (int)(loadoutPart.getInternalPart().getHitpoints() / 10.0);

      double slots = 0;
      double P_hitammo = 0;
      for(Item item : loadoutPart.getItems()){
         if( item instanceof Internal )
            continue;
      }
      return 0;
   }

   private double simulateR(Map<Item, Integer> aMultiplicity, double aP, int slots){
      double ans = 0;
      for(Entry<Item, Integer> entry : aMultiplicity.entrySet()){
         Item item = entry.getKey();
         int itemSlots = item.getNumCriticalSlots(loadoutPart.getLoadout().getUpgrades());
         int multi = entry.getValue();
         double P_hit = ((double)itemSlots) / slots;
         P_hit *= multi;

         if( item instanceof Ammunition ){
            ans += aP * P_hit;
         }
         else{
            Map<Item, Integer> newMultiplicity = new HashMap<>(aMultiplicity);
            newMultiplicity.remove(item);

            ans += aP * simulateR(newMultiplicity, aP * P_hit, slots - itemSlots);
         }
      }
      return ans;
   }

   public double simulate(){
      Map<Item, Integer> multiplicity = new HashMap<>();
      int slots = 0;
      for(Item item : loadoutPart.getItems()){
         if( item instanceof Internal && item != LoadoutPart.ENGINE_INTERNAL ){
            continue;
         }
         slots += item.getNumCriticalSlots(loadoutPart.getLoadout().getUpgrades());
         Integer i = multiplicity.get(item);
         if( i == null )
            multiplicity.put(item, 1);
         else
            multiplicity.put(item, i + 1);
      }

      return simulateR(multiplicity, 1.0, slots);
   }

}

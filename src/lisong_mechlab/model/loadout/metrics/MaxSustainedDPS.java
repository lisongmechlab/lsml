/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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
package lisong_mechlab.model.loadout.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the maximal DPS that a {@link Loadout} can sustain indefinitely.
 * 
 * @author Li Song
 */
public class MaxSustainedDPS implements Metric{
   private final Loadout         loadout;
   private final HeatDissipation dissipation;

   public MaxSustainedDPS(final Loadout aLoadout, final HeatDissipation aHeatDissipation){
      loadout = aLoadout;
      dissipation = aHeatDissipation;
   }

   @Override
   public double calculate(){
      double ans = 0.0;
      Map<Weapon, Double> dd = getWeaponRatios(-1);
      for(Map.Entry<Weapon, Double> entry : dd.entrySet()){
         ans += entry.getKey().getStat("d/s", loadout.getUpgrades(), loadout.getEfficiencies()) * entry.getValue();
      }
      return ans;
   }

   /**
    * Calculates the ratio with each weapon should be fired to obtain the maximal sustained DPS. A ratio of 0.0 means
    * the weapon is never fired and a ratio of 0.5 means the weapon is fired every 2 cooldowns and a ratio of 1.0 means
    * the weapon is fired every time it is available.
    * 
    * @return A {@link Map} with {@link Weapon} as key and a {@link Double} as value representing a % of how often the
    *         weapon is used.
    */
   public Map<Weapon, Double> getWeaponRatios(final double range){
      double heatleft = dissipation.calculate();
      List<Weapon> weapons = new ArrayList<>(15);
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.AMS ){
            weapons.add((Weapon)item);
         }
      }
      if( range >= 0 ){
         Collections.sort(weapons, new Comparator<Weapon>(){
            @Override
            public int compare(Weapon aO1, Weapon aO2){
               return Double.compare(aO2.getRangeEffectivity(range) * aO2.getStat("d/h", loadout.getUpgrades(), loadout.getEfficiencies()),
                                     aO1.getRangeEffectivity(range) * aO1.getStat("d/h", loadout.getUpgrades(), loadout.getEfficiencies()));
            }
         });
      }
      else{
         Collections.sort(weapons, new Comparator<Weapon>(){
            @Override
            public int compare(Weapon aO1, Weapon aO2){
               return Double.compare(aO2.getStat("d/h", loadout.getUpgrades(), loadout.getEfficiencies()), aO1.getStat("d/h", loadout.getUpgrades(),
                                                                                                                       loadout.getEfficiencies()));
            }
         });
      }

      Map<Weapon, Double> ans = new HashMap<>();
      while( !weapons.isEmpty() ){
         Weapon weapon = weapons.remove(0);
         final double heat = weapon.getStat("h/s", loadout.getUpgrades(), loadout.getEfficiencies());
         final double ratio;
         final double rangefactor = (range >= 0) ? weapon.getRangeEffectivity(range) : 1.0;

         if( heatleft == 0 ){
            ratio = 0;
         }
         else if( heat < heatleft ){
            ratio = rangefactor;
            heatleft -= heat;
         }
         else{
            ratio = heatleft / heat * rangefactor;
            heatleft = 0;
         }

         if( ans.containsKey(weapon) )
            ans.put(weapon, Double.valueOf(ans.get(weapon).doubleValue() + ratio));
         else
            ans.put(weapon, Double.valueOf(ratio));
      }
      return ans;
   }
}

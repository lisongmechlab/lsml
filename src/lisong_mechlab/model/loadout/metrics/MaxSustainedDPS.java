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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;

/**
 * This {@link Metric} calculates the maximal DPS that a {@link Loadout} can sustain indefinitely.
 * 
 * @author Emily Björk
 */
public class MaxSustainedDPS extends RangeMetric{
   private final HeatDissipation dissipation;

   public MaxSustainedDPS(final Loadout aLoadout, final HeatDissipation aHeatDissipation){
      super(aLoadout);
      dissipation = aHeatDissipation;
   }

   @Override
   public double calculate(double aRange){
      double ans = 0.0;
      Map<Weapon, Double> dd = getWeaponRatios(aRange);
      for(Map.Entry<Weapon, Double> entry : dd.entrySet()){
         Weapon weapon = entry.getKey();
         double ratio = entry.getValue();
         double rangeEffectivity = weapon.getRangeEffectivity(aRange);
         ans += rangeEffectivity * weapon.getStat("d/s", loadout.getUpgrades(), loadout.getEfficiencies()) * ratio;
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
   public Map<Weapon, Double> getWeaponRatios(final double aRange){
      final Upgrades upgrades = loadout.getUpgrades();
      final Efficiencies efficiencies = loadout.getEfficiencies();

      double heatleft = dissipation.calculate();
      List<Weapon> weapons = new ArrayList<>(15);
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.AMS ){
            weapons.add((Weapon)item);
         }
      }
      if( aRange >= 0 ){
         Collections.sort(weapons, new Comparator<Weapon>(){
            @Override
            public int compare(Weapon aO1, Weapon aO2){
               double dps2 = aO2.getRangeEffectivity(aRange) * aO2.getStat("d/h", upgrades, efficiencies);
               double dps1 = aO1.getRangeEffectivity(aRange) * aO1.getStat("d/h", upgrades, efficiencies);
               if( aO1.getRangeMax() < aRange )
                  dps1 = 0;
               if( aO2.getRangeMax() < aRange )
                  dps2 = 0;
               return Double.compare(dps2, dps1);
            }
         });
      }
      else{
         Collections.sort(weapons, new Comparator<Weapon>(){
            @Override
            public int compare(Weapon aO1, Weapon aO2){
               return Double.compare(aO2.getStat("d/h", upgrades, efficiencies), aO1.getStat("d/h", upgrades, efficiencies));
            }
         });
      }

      Map<Weapon, Double> ans = new HashMap<>();
      while( !weapons.isEmpty() ){
         Weapon weapon = weapons.remove(0);
         final double heat = weapon.getStat("h/s", upgrades, efficiencies);
         final double ratio;

         if( heatleft == 0 ){
            ratio = 0;
         }
         else if( heat < heatleft ){
            ratio = 1.0;
            heatleft -= heat;
         }
         else{
            ratio = heatleft / heat;
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

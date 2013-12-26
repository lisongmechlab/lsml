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

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the maximal DPS a {@link Loadout} can output.
 * 
 * @author Emily Björk
 */
public class MaxDPS extends RangeMetric{
   public MaxDPS(Loadout aLoadout){
      super(aLoadout);
   }

   @Override
   public double calculate(double aRange){
      double ans = 0;
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon && item != ItemDB.AMS ){
            Weapon weapon = (Weapon)item;
            ans += weapon.getRangeEffectivity(aRange) * weapon.getStat("d/s", loadout.getUpgrades(), loadout.getEfficiencies());
         }
      }
      return ans;
   }
}

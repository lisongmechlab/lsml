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

import java.util.TreeMap;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

public class TotalWeapons extends TableMetric{

   private final Loadout            loadout;
   private TreeMap<Weapon, Integer> weaponValues;

   public TotalWeapons(Loadout aLoadout){
      this.loadout = aLoadout;

      weaponValues = new TreeMap<>();
   }

   @Override
   public TreeMap<Weapon, Integer> calculate(){
      weaponValues.clear();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            if( weaponValues.containsKey(item) ){
               int tempValue = weaponValues.get(item);
               tempValue++;
               weaponValues.put((Weapon)item, tempValue);
            }
            else{
               weaponValues.put((Weapon)item, 1);
            }

         }

      }
      return weaponValues;
   }

}

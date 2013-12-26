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

import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;

public class TotalAmmoSupply extends TableMetric{

   private final Loadout                loadout;
   private TreeMap<Ammunition, Integer> ammoValues;

   public TotalAmmoSupply(Loadout aLoadout){
      this.loadout = aLoadout;

      ammoValues = new TreeMap<>();
   }

   @Override
   public TreeMap<Ammunition, Integer> calculate(){
      TreeMap<Ammunition, Integer> ammoMap = new TreeMap<>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Ammunition ){
            if( ammoMap.containsKey(item) ){
               int tempValue = ammoMap.get(item);
               ammoMap.put((Ammunition)item, ++tempValue);
            }
            else{
               ammoMap.put((Ammunition)item, 1);
            }

         }

      }
      return ammoMap;
   }

   public TreeMap<String, Integer> getShotsPerVolleyForEach(){
      TreeMap<String, Integer> volleyValues = new TreeMap<>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof AmmoWeapon ){
            if( ammoValues.containsKey(((AmmoWeapon)item).getAmmoType(loadout.getUpgrades())) ){
               if( volleyValues.containsKey(((AmmoWeapon)item).getAmmoType(loadout.getUpgrades()).getName()) ){
                  int tempVolleyAmount = volleyValues.get(((AmmoWeapon)item).getAmmoType(loadout.getUpgrades()).getName())
                                         + ((AmmoWeapon)item).getAmmoPerPerShot();
                  volleyValues.put(((AmmoWeapon)item).getAmmoType(loadout.getUpgrades()).getName(), tempVolleyAmount);
               }
               else{
                  volleyValues.put(((AmmoWeapon)item).getAmmoType(loadout.getUpgrades()).getName(), ((AmmoWeapon)item).getAmmoPerPerShot());
               }

            }
            else{
               volleyValues.put(item.getName(), 0);
            }
         }
         else if( item instanceof Ammunition ){
            if( !volleyValues.containsKey(item.getName()) ){
               volleyValues.put(item.getName(), 0);
            }
         }
      }
      return volleyValues;
   }

   public TreeMap<String, Double> getSecondsForEach(){
      TreeMap<String, Double> secondValues = new TreeMap<>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof AmmoWeapon ){
            AmmoWeapon weapon = (AmmoWeapon)item;
            Ammunition ammo = weapon.getAmmoType(loadout.getUpgrades());
            if( ammoValues.containsKey(ammo) ){
               if( secondValues.containsKey(ammo.getName()) ){
                  double tempVolleyAmount = secondValues.get(ammo.getName()) + weapon.getSecondsPerShot(loadout.getEfficiencies());
                  secondValues.put(ammo.getName(), tempVolleyAmount);
               }
               else{
                  secondValues.put(ammo.getName(), weapon.getSecondsPerShot(loadout.getEfficiencies()));
               }

            }
            else{
               secondValues.put(item.getName(), (double)0);
            }
         }
         else if( item instanceof Ammunition ){
            if( !secondValues.containsKey(item.getName()) ){
               secondValues.put(item.getName(), (double)0);
            }
         }
      }
      return secondValues;
   }

}

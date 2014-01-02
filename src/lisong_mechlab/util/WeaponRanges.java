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
package lisong_mechlab.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This class will calculate the set of ranges at which weapons change damage. In essence, it calculates the ordered
 * union of the zero, min, long and max ranges for all given weapons.
 * 
 * @author Emily Björk
 */
public class WeaponRanges{

   static public Double[] getRanges(Collection<Weapon> aWeaponCollection){
      SortedSet<Double> ans = new TreeSet<>();

      ans.add(Double.valueOf(0.0));
      for(Weapon weapon : aWeaponCollection){
         if( weapon.hasSpread() ){
            ans.add(weapon.getRangeZero());
            double min = weapon.getRangeMin();
            double max = weapon.getRangeMax();
            ans.add(min);
            final double step = 10;
            while( min + step < max ){
               min += step;
               ans.add(min);
            }
            ans.add(max);
         }
         else if( weapon != ItemDB.AMS ){
            ans.add(weapon.getRangeZero());
            ans.add(weapon.getRangeMin());
            ans.add(weapon.getRangeLong());
            ans.add(weapon.getRangeMax());
         }
      }
      return ans.toArray(new Double[ans.size()]);
   }

   static public Double[] getRanges(Loadout aLoadout){
      List<Weapon> weapons = new ArrayList<>();
      for(Item item : aLoadout.getAllItems()){
         if( item instanceof Weapon ){
            Weapon weapon = (Weapon)item;
            weapons.add(weapon);
         }
      }
      return getRanges(weapons);
   }
}

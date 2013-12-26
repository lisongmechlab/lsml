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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the total ghost heat penalty for an alpha strike from a loadout.
 * 
 * @author Emily Björk
 */
public class GhostHeat implements Metric{
   private static final double HEAT_SCALE[] = {0, 0, 0.08, 0.18, 0.30, 0.45, 0.60, 0.80, 1.10, 1.50, 2.00, 3.00, 5.00};
   private final Loadout       loadout;

   public GhostHeat(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      List<Weapon> ungroupedWeapons = new LinkedList<>();
      Map<Integer, List<Weapon>> groups = new HashMap<Integer, List<Weapon>>();
      for(Item item : loadout.getAllItems()){
         if( item instanceof Weapon ){
            Weapon weapon = (Weapon)item;
            int group = weapon.getGhostHeatGroup();
            if( group == 0 ){
               ungroupedWeapons.add(weapon);
            }
            else if( group > 0 ){
               if( !groups.containsKey(group) ){
                  groups.put(group, new LinkedList<Weapon>());
               }
               groups.get(group).add(weapon);
            }
         }
      }

      double penalty = 0;
      while( !ungroupedWeapons.isEmpty() ){
         Weapon weapon = ungroupedWeapons.remove(0);
         int count = 1;
         Iterator<Weapon> it = ungroupedWeapons.iterator();
         while( it.hasNext() ){
            Weapon w = it.next();
            if( w == weapon ){
               count++;
               it.remove();
            }
         }
         penalty += calculatePenalty(weapon, count);
      }

      // XXX: http://mwomercs.com/forums/topic/127904-heat-scale-the-maths/ is not completely
      // clear on this. We interpret the post to mean that for the purpose of ghost heat, every weapon
      // in the linked group is equal to the weapon with highest base heat.
      for(List<Weapon> group : groups.values()){
         double maxbaseheat = Double.NEGATIVE_INFINITY;
         Weapon maxweapon = null;
         for(Weapon w : group){
            if( w.getHeat() > maxbaseheat ){
               maxbaseheat = w.getHeat();
               maxweapon = w;
            }
         }
         penalty += calculatePenalty(maxweapon, group.size());
      }
      return penalty;
   }

   private double calculatePenalty(Weapon aWeapon, int aCount){
      double penalty = 0;
      int count = aCount;
      while( count > aWeapon.getGhostHeatMaxFreeAlpha() ){
         penalty += HEAT_SCALE[count] * aWeapon.getGhostHeatMultiplier() * aWeapon.getHeat();
         count--;
      }
      return penalty;
   }

}

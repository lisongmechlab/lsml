/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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

package lisong_mechlab.model.tables;

import java.util.ArrayList;

import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.Weapon;

public class MissileEntry{
   
    private ArrayList<Double> damageList;
    private ArrayList<Double> coolDownList;
    private int volleyAmount;
    private Ammunition ammoType;

   public MissileEntry(Ammunition theType){
      damageList = new ArrayList<>();
      coolDownList = new ArrayList<>();
      volleyAmount = 0;
      ammoType = theType;
   }
   
   public void addAnotherWeapon(Weapon weapon){
      volleyAmount += weapon.getAmmoPerPerShot();
      damageList.add(weapon.getDamagePerShot());
      coolDownList.add(weapon.getSecondsPerShot());      
   }

   public int getVolleyTotal(){
      return volleyAmount;
   }

   public Ammunition getAmmoType(){
      return ammoType;
   }
   
   public double getDamageAverage(){
      Double total = 0.0;
      if(damageList.isEmpty()) return 0.0;
      for(Double inter : damageList){
         total += inter;
      }
      return total / volleyAmount;
   }
   
   public double getCooldownAverage(){
      Double total = 0.0;
      if(coolDownList.isEmpty()) return 0;
      for(Double inter : coolDownList){
         total += inter;
      }
      return total / coolDownList.size();
   }

}

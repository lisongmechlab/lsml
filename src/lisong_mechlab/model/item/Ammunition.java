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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;

/**
 * A generic ammunition item.
 * 
 * @author Li Song
 */
public class Ammunition extends Item{
   protected final int           shotsPerTon;
   protected final double        internalDamage;
   protected final int           hp;
   protected final HardPointType type;

   public Ammunition(ItemStatsModule aStatsModule){
      super(aStatsModule, HardPointType.NONE, 1, 1.0, aStatsModule.AmmoTypeStats.health);
      hp = aStatsModule.AmmoTypeStats.health;
      internalDamage = aStatsModule.AmmoTypeStats.internalDamage;
      shotsPerTon = aStatsModule.AmmoTypeStats.shotsPerTon;

      if( getName().contains("AC") || getName().contains("GAUSS") || getName().contains("LB") ){
         type = HardPointType.BALLISTIC;
      }
      else if( getName().contains("RM") || getName().contains("NARC") ){
         type = HardPointType.MISSILE;
      }
      else if( getName().contains("AMS") ){
         type = HardPointType.AMS;
      }
      else{
         type = HardPointType.ENERGY;
      }
   }

   public int getShotsPerTon(){
      return shotsPerTon;
   }

   /**
    * @return The {@link HardPointType} that the weapon that uses this ammo is using. Useful for color coding and
    *         searching.
    */
   public HardPointType getWeaponHardpointType(){
      return type;
   }

   @Override
   public String getShortName(Upgrades anUpgrades){
      String name = getName(anUpgrades);
      name = name.replace("ULTRA ", "U");
      name = name.replace("MACHINE GUN", "MG");
      return name;
   }
}

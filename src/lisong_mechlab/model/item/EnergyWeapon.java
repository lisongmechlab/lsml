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

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class EnergyWeapon extends Weapon{
   protected final double burnTime;
   protected final double zeroRange;

   public EnergyWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.ENERGY);
      burnTime = aStatsWeapon.WeaponStats.duration;
      if( getName().equals("PPC") ){
         zeroRange = getRangeMin() - Math.ulp(getRangeMin()) * RANGE_ULP_FUZZ;
      }
      else{
         zeroRange = 0;
      }
   }

   @Override
   public double getRangeZero(){
      return zeroRange;
   }

   @Override
   public double getSecondsPerShot(Efficiencies aEfficiencies){
      return getCycleTime(aEfficiencies) + burnTime;
   }

   @Override
   public String getShortName(Upgrades anUpgrades){
      String name = getName(anUpgrades);
      name = name.replace("LASER", "LAS");
      name = name.replace("LARGE ", "L");
      name = name.replace("LRG ", "L");
      name = name.replace("SML ", "S");
      name = name.replace("SMALL ", "S");
      name = name.replace("MED ", "M");
      name = name.replace("MEDIUM ", "M");
      name = name.replace("PULSE ", "P");
      return name;
   }
}

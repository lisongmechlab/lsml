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
package lisong_mechlab.model.item;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class EnergyWeapon extends Weapon{
   protected final double burnTime;
   protected final double zeroRange;

   public EnergyWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.ENERGY);
      if( aStatsWeapon.WeaponStats.duration < 0 )
         burnTime = Double.POSITIVE_INFINITY;
      else
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
      if( burnTime == Double.POSITIVE_INFINITY ){
         return getCycleTime(aEfficiencies);
      }
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

   public final static Comparator<EnergyWeapon> DEFAULT_ORDERING;
   static{
      DEFAULT_ORDERING = new Comparator<EnergyWeapon>(){
         Pattern p = Pattern.compile("(ER)?\\s*(LARGE|LRG|MEDIUM|MED|SMALL|SML)?\\s*(PULSE)?\\s*(LASER|PPC).*");

         @Override
         public int compare(EnergyWeapon aLhs, EnergyWeapon aRhs){
            Matcher mLhs = p.matcher(aLhs.getName());
            Matcher mRhs = p.matcher(aRhs.getName());

            if( mLhs.matches() && mRhs.matches() ){
               // Group PPCs and Lasers together
               int ppcVsLaser = mLhs.group(4).compareTo(mRhs.group(4));
               if( ppcVsLaser == 0 ){
                  // Group pulses together.
                  if( mLhs.group(3) != null && mRhs.group(3) == null )
                     return -1;
                  else if( mLhs.group(3) == null && mRhs.group(3) != null )
                     return 1;

                  // Group ER together
                  if( mLhs.group(1) != null && mRhs.group(1) == null )
                     return -1;
                  else if( mLhs.group(1) == null && mRhs.group(1) != null )
                     return 1;

                  // Order by size
                  if( mLhs.group(2) != null && mRhs.group(2) != null ){
                     return -Integer.compare(sizeOf(mLhs.group(2)), sizeOf(mRhs.group(2)));
                  }
               }
               return -ppcVsLaser;
            }
            else if( mLhs.matches() && !mRhs.matches() ){
               return -1;
            }
            else if( !mLhs.matches() && mRhs.matches() ){
               return 1;
            }

            return aLhs.getName().compareTo(aRhs.getName()); // Fall back to lexicographical comparison.
         }

         int sizeOf(String aSize){
            if( aSize.equals("LARGE") || aSize.equals("LRG") )
               return 3;
            else if( aSize.equals("MEDIUM") || aSize.equals("MED") )
               return 2;
            else if( aSize.equals("SMALL") || aSize.equals("SML") )
               return 1;
            else
               throw new RuntimeException("Unknown laser size!");
         }
      };
   }

   public double getDuration(){
      return burnTime;
   }
}

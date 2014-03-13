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

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;
import lisong_mechlab.model.upgrades.Upgrades;
import lisong_mechlab.util.GaussianDistribution;

public class BallisticWeapon extends AmmoWeapon{
   protected final double projectileSpeed;
   protected final double spread;
   protected final double jammingChance;
   protected final int    shotsduringcooldown;
   protected final double jammingTime;

   public BallisticWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.BALLISTIC);
      projectileSpeed = aStatsWeapon.WeaponStats.speed;
      if( aStatsWeapon.WeaponStats.spread > 0 )
         spread = aStatsWeapon.WeaponStats.spread;
      else
         spread = 0;

      if( aStatsWeapon.WeaponStats.JammingChance >= 0 ){
         jammingChance = aStatsWeapon.WeaponStats.JammingChance;
         shotsduringcooldown = aStatsWeapon.WeaponStats.ShotsDuringCooldown;
         jammingTime = aStatsWeapon.WeaponStats.JammedTime;
      }
      else{
         jammingChance = 0.0;
         shotsduringcooldown = 0;
         jammingTime = 0.0;
      }
   }

   @Override
   public String getShortName(Upgrades anUpgrades){
      String name = getName(anUpgrades);
      name = name.replace("ULTRA ", "U");
      name = name.replace("MACHINE GUN", "MG");
      return name;
   }

   @Override
   public boolean hasSpread(){
      return spread > 0;
   }

   public boolean canDoubleFire(){
      return jammingChance > 0.0;
   }

   @Override
   public double getSecondsPerShot(Efficiencies aEfficiencies){
      if( canDoubleFire() ){
         return (1.0 - jammingChance) * getCycleTime(aEfficiencies) / (1 + shotsduringcooldown) + jammingChance * jammingTime;
      }
      if( getMwoId() == 1021 ){ // Gauss rifle
         return getCycleTime(aEfficiencies) + 0.75; // TODO: Fix this when they add the charge time to the itemstats.xml
      }
      return getCycleTime(aEfficiencies);
   }

   @Override
   public double getRangeEffectivity(double range){
      double spreadFactor = 1.0;
      if( hasSpread() ){
         // Assumption:
         // The 'spread' value is the standard deviation of a zero-mean gaussian distribution of angles.
         GaussianDistribution gaussianDistribution = new GaussianDistribution();

         final double targetRadius = 6; // [m]
         double maxAngle = Math.atan2(targetRadius, range) * 180 / Math.PI; // [deg]

         // X ~= N(0, spread)
         // P_hit = P(-maxAngle <= X; X <= +maxangle)
         // Xn = (X - 0) / spread ~ N(0,1)
         // P_hit = cdf(maxangle / spread) - cdf(-maxangle / spread) = 2*cdf(maxangle / spread) - 1.0;
         double P_hit = 2 * gaussianDistribution.cdf(maxAngle / spread) - 1;
         spreadFactor = P_hit;
      }
      return spreadFactor * super.getRangeEffectivity(range);
   }

   public final static Comparator<Item> DEFAULT_ORDERING = DEFAULT_WEAPON_ORDERING;
}

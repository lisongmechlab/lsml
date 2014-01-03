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

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Efficiencies;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

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
      return getCycleTime(aEfficiencies);
   }

   @Override
   public double getRangeEffectivity(double range){
      double spreadFactor = 1.0;
      if( hasSpread() ){
         // Assumptions:
         // 1) The spread value is the half size of the scatter area after 100m.
         // I.e. a weapon with spread 2, will have a 4x4m spread area at 100m.
         // 2) An assault mech is about 16m tall. We'll consider any spread that
         // lands all pellets on the target, and relatively close to the aimed component, as full damage.
         // As such we assume that full damage is achieved at spreads of less than 4x4m.

         final double maxDamageSpread_m = 3; // Any spread less than this in radius will land full damage.

         spreadFactor = Math.min(1.0, (maxDamageSpread_m * maxDamageSpread_m) / (range * range / 10000 * spread * spread));
      }
      return spreadFactor * super.getRangeEffectivity(range);
   }

   public final static Comparator<Item> DEFAULT_ORDERING = DEFAULT_WEAPON_ORDERING;
}

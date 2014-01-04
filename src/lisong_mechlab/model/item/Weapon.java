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
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class Weapon extends HeatSource{
   public static final int RANGE_ULP_FUZZ = 5;

   private final double    damagePerProjectile;
   private final double    cycleTime;
   private final double    rangeMin;
   private final double    rangeLong;
   private final double    rangeMax;
   private final int       ammoPerShot;
   private final int       projectilesPerShot;
   private final int       shotsPerFiring;

   private final int       ghostHeatGroupId;
   private final double    ghostHeatMultiplier;
   private final int       ghostHeatFreeAlpha;

   public Weapon(ItemStatsWeapon aStatsWeapon, HardpointType aHardpointType){
      super(aStatsWeapon, aHardpointType, aStatsWeapon.WeaponStats.slots, aStatsWeapon.WeaponStats.tons, aStatsWeapon.WeaponStats.heat,
            aStatsWeapon.WeaponStats.Health);
      damagePerProjectile = aStatsWeapon.WeaponStats.damage;
      if( aStatsWeapon.WeaponStats.cooldown <= 0.0 ){
         // Some weapons are troublesome in that they have zero cooldown in the data files.
         // These include: Machine Gun, Flamer, TAG
         if( aStatsWeapon.WeaponStats.rof > 0.0 ){
            cycleTime = 1.0 / aStatsWeapon.WeaponStats.rof;
         }
         else if( aStatsWeapon.WeaponStats.type.toLowerCase().equals("energy") ){
            cycleTime = 1;
         }
         else{
            cycleTime = 0.10375; // Determined on testing grounds: 4000 mg rounds 6min 55s or 415s -> 415/4000 = 0.10375
         }
      }
      else{
         cycleTime = aStatsWeapon.WeaponStats.cooldown;
      }
      rangeMin = aStatsWeapon.WeaponStats.minRange;
      rangeMax = aStatsWeapon.WeaponStats.maxRange;
      rangeLong = aStatsWeapon.WeaponStats.longRange;

      shotsPerFiring = aStatsWeapon.WeaponStats.numFiring;
      projectilesPerShot = aStatsWeapon.WeaponStats.numPerShot > 0 ? aStatsWeapon.WeaponStats.numPerShot : 1;
      ammoPerShot = aStatsWeapon.WeaponStats.ammoPerShot;

      if( aStatsWeapon.WeaponStats.minheatpenaltylevel != 0 ){
         ghostHeatGroupId = aStatsWeapon.WeaponStats.heatPenaltyID;
         ghostHeatMultiplier = aStatsWeapon.WeaponStats.heatpenalty;
         ghostHeatFreeAlpha = aStatsWeapon.WeaponStats.minheatpenaltylevel - 1;
      }
      else{
         ghostHeatGroupId = -1;
         ghostHeatMultiplier = 0;
         ghostHeatFreeAlpha = -1;
      }
   }

   /**
    * 0 = ungrouped 1 = PPC, ER PPC 2 = LRM20/15/10 3 = LL, ER LL, LPL 4 = SRM6 SRM4
    * 
    * @return The ID of the group this weapon belongs to.
    */
   public int getGhostHeatGroup(){
      return ghostHeatGroupId;
   }

   public double getGhostHeatMultiplier(){
      return ghostHeatMultiplier;
   }

   public int getGhostHeatMaxFreeAlpha(){
      return ghostHeatFreeAlpha;
   }

   public double getDamagePerShot(){
      return damagePerProjectile * projectilesPerShot * shotsPerFiring;
   }

   public int getAmmoPerPerShot(){
      return ammoPerShot;
   }

   public double getSecondsPerShot(Efficiencies aEfficiencies){
      return getCycleTime(aEfficiencies);
   }

   public double getCycleTime(Efficiencies aEfficiencies){
      double factor = (null == aEfficiencies) ? 1.0 : aEfficiencies.getWeaponCycleTimeModifier();
      return cycleTime * factor;
   }

   public double getRangeZero(){
      return 0;
   }

   public double getRangeMin(){
      return rangeMin;
   }

   public double getRangeMax(){
      return rangeMax;
   }

   public double getRangeLong(){
      return rangeLong;
   }

   public double getRangeEffectivity(double range){
      // Assume linear fall off
      if( range < getRangeZero() )
         return 0;
      if( range < getRangeMin() )
         return (range - getRangeZero()) / (getRangeMin() - getRangeZero());
      else if( range <= getRangeLong() )
         return 1.0;
      else if( range < getRangeMax() )
         return 1.0 - (range - getRangeLong()) / (getRangeMax() - getRangeLong());
      else
         return 0;
   }

   /**
    * Calculates an arbitrary statistic for the weapon based on the string. The string format is (regexp):
    * "[dsthc]+(/[dsthc]+)?" where d=damage, s=seconds, t=tons, h=heat, c=criticalslots. For example "d/hhs" is damage
    * per heat^2 second.
    * 
    * @param aWeaponStat
    *           A string specifying the statistic to be calculated. Must match the regexp pattern
    *           "[dsthc]+(/[dsthc]+)?".
    * @return The calculated statistic.
    */
   public double getStat(String aWeaponStat, Upgrades anUpgrades, Efficiencies aEfficiencies){
      double nominator = 1;
      int index = 0;
      while( index < aWeaponStat.length() && aWeaponStat.charAt(index) != '/' ){
         switch( aWeaponStat.charAt(index) ){
            case 'd':
               nominator *= getDamagePerShot();
               break;
            case 's':
               nominator *= getSecondsPerShot(aEfficiencies);
               break;
            case 't':
               nominator *= getMass(anUpgrades);
               break;
            case 'h':
               nominator *= getHeat();
               break;
            case 'c':
               nominator *= getNumCriticalSlots(anUpgrades);
               break;
            default:
               throw new IllegalArgumentException("Unknown identifier: " + aWeaponStat.charAt(index));
         }
         index++;
      }

      index++; // Skip past the '/' if we encountered it, otherwise we'll be at the end of the string anyway.
      double denominator = 1;
      while( index < aWeaponStat.length() ){
         switch( aWeaponStat.charAt(index) ){
            case 'd':
               denominator *= getDamagePerShot();
               break;
            case 's':
               denominator *= getSecondsPerShot(aEfficiencies);
               break;
            case 't':
               denominator *= getMass(anUpgrades);
               break;
            case 'h':
               denominator *= getHeat();
               break;
            case 'c':
               denominator *= getNumCriticalSlots(anUpgrades);
               break;
            default:
               throw new IllegalArgumentException("Unknown identifier: " + aWeaponStat.charAt(index));
         }
         index++;
      }
      if( nominator == 0.0 && denominator == 0.0 ){
         // We take the Brahmaguptan interpretation of 0/0 to be 0 (year 628).
         return 0;
      }
      return nominator / denominator;
   }

   public boolean hasSpread(){
      return false;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getHardpointsCount(getHardpointType()) > 0;
   }

   public final static Comparator<Item> DEFAULT_WEAPON_ORDERING;
   static{
      DEFAULT_WEAPON_ORDERING = new Comparator<Item>(){
         private final Pattern p = Pattern.compile("(\\D*)(\\d*)?.*");

         @Override
         public int compare(Item aLhs, Item aRhs){
            Matcher mLhs = p.matcher(aLhs.getName());
            Matcher mRhs = p.matcher(aRhs.getName());

            if( !mLhs.matches() )
               throw new RuntimeException("LHS didn't match pattern! [" + aLhs.getName() + "]");

            if( !mRhs.matches() )
               throw new RuntimeException("RHS didn't match pattern! [" + aRhs.getName() + "]");

            if( mLhs.group(1).equals(mRhs.group(1)) ){
               // Same prefix
               String lhsSuffix = mLhs.group(2);
               String rhsSuffix = mRhs.group(2);
               if( lhsSuffix != null && lhsSuffix.length() > 0 && rhsSuffix != null && rhsSuffix.length() > 0 )
                  return -Integer.compare(Integer.parseInt(lhsSuffix), Integer.parseInt(rhsSuffix));
            }
            return mLhs.group(1).compareTo(mRhs.group(1));
         }
      };
   }
}

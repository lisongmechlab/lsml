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

import java.util.Collection;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lisong_mechlab.model.Efficiencies;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.mwo_data.helpers.ItemStatsWeapon;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Weapon extends HeatSource{
   public static final int RANGE_ULP_FUZZ = 5;

   @XStreamAsAttribute
   private final double    damagePerProjectile;
   @XStreamAsAttribute
   private final double    cycleTime;
   @XStreamAsAttribute
   private final double    rangeZero;
   @XStreamAsAttribute
   private final double    rangeMin;
   @XStreamAsAttribute
   private final double    rangeLong;
   @XStreamAsAttribute
   private final double    rangeMax;
   @XStreamAsAttribute
   private final double    fallOffExponent;
   @XStreamAsAttribute
   private final int       projectilesPerRound;
   @XStreamAsAttribute
   private final int       roundsPerShot;
   @XStreamAsAttribute
   private final int       ghostHeatGroupId;
   @XStreamAsAttribute
   private final double    ghostHeatMultiplier;
   @XStreamAsAttribute
   private final int       ghostHeatFreeAlpha;
   @XStreamAsAttribute
   private final double    projectileSpeed;

   public Weapon(ItemStatsWeapon aStatsWeapon, HardPointType aHardpointType){
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
      rangeZero = aStatsWeapon.WeaponStats.nullRange;
      rangeMin = aStatsWeapon.WeaponStats.minRange;
      rangeMax = aStatsWeapon.WeaponStats.maxRange;
      rangeLong = aStatsWeapon.WeaponStats.longRange;
      fallOffExponent = aStatsWeapon.WeaponStats.falloffexponent != 0 ? aStatsWeapon.WeaponStats.falloffexponent : 1.0;

      roundsPerShot = aStatsWeapon.WeaponStats.numFiring;
      projectilesPerRound = aStatsWeapon.WeaponStats.numPerShot > 0 ? aStatsWeapon.WeaponStats.numPerShot : 1;
      projectileSpeed = aStatsWeapon.WeaponStats.speed;

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

   public boolean isOffensive(){
      return this != ItemDB.AMS && this != ItemDB.C_AMS;
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
      return damagePerProjectile * projectilesPerRound * roundsPerShot;
   }

   public int getAmmoPerPerShot(){
      return roundsPerShot;
   }

   public double getSecondsPerShot(Efficiencies aEfficiencies, Collection<WeaponModifier> aModifiers){
      return getCoolDown(aEfficiencies, aModifiers);
   }

   public double getCoolDown(Efficiencies aEfficiencies, Collection<WeaponModifier> aModifiers){
      double a = 0;
      if( aModifiers != null ){
         for(WeaponModifier mod : aModifiers){
            if( mod.affectsWeapon(this) ){
               a += mod.extraCooldown(this, cycleTime, null);
            }
         }
      }

      double factor = (null == aEfficiencies) ? 1.0 : aEfficiencies.getWeaponCycleTimeModifier();
      return (cycleTime + a) * factor;
   }

   public double getProjectileSpeed(){
      return projectileSpeed;
   }

   public double getRangeZero(){
      if( rangeZero == rangeMin )
         return rangeZero - Math.ulp(rangeMin);
      return rangeZero;
   }

   public double getRangeMin(){
      return rangeMin;
   }

   public double getRangeMax(Collection<WeaponModifier> aModifiers){
      double a = 0;
      if( aModifiers != null ){
         for(WeaponModifier mod : aModifiers){
            if( mod.affectsWeapon(this) ){
               a += mod.extraMaxRange(this, rangeMax, null);
            }
         }
      }

      if( rangeMax == rangeLong )
         a += Math.ulp(rangeMax + a);

      return rangeMax + a;
   }

   public double getRangeLong(Collection<WeaponModifier> aModifiers){
      double a = 0;
      if( aModifiers != null ){
         for(WeaponModifier mod : aModifiers){
            if( mod.affectsWeapon(this) ){
               a += mod.extraLongRange(this, rangeLong, null);
            }
         }
      }
      return rangeLong + a;
   }

   @Override
   public double getHeat(Collection<WeaponModifier> aModifiers){
      double a = 0;
      if( aModifiers != null ){
         for(WeaponModifier mod : aModifiers){
            if( mod.affectsWeapon(this) ){
               a += mod.extraWeaponHeat(this, super.getHeat(aModifiers), null);
            }

         }
      }
      return super.getHeat(aModifiers) + a;
   }

   public double getRangeEffectivity(double range, Collection<WeaponModifier> aModifiers){
      // Assume linear fall off
      if( range < getRangeZero() )
         return 0;
      else if( range < getRangeMin() )
         return Math.pow((range - getRangeZero()) / (getRangeMin() - getRangeZero()), fallOffExponent);
      else if( range <= getRangeLong(aModifiers) )
         return 1.0;
      else if( range < getRangeMax(aModifiers) ){
         // Presumably long range fall off can also be exponential, we'll wait until there is evidence of the fact.
         return 1.0 - (range - getRangeLong(aModifiers)) / (getRangeMax(aModifiers) - getRangeLong(aModifiers));
      }
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
    * @param aEfficiencies
    *           Any {@link Efficiencies} that can affect the stats, can be <code>null</code> to assume no
    *           {@link Efficiencies}.
    * @param aModifiers
    *           A list of pilot modules to take into account.
    * @return The calculated statistic.
    */
   public double getStat(String aWeaponStat, Efficiencies aEfficiencies, Collection<WeaponModifier> aModifiers){
      double nominator = 1;
      int index = 0;
      while( index < aWeaponStat.length() && aWeaponStat.charAt(index) != '/' ){
         switch( aWeaponStat.charAt(index) ){
            case 'd':
               nominator *= getDamagePerShot();
               break;
            case 's':
               nominator *= getSecondsPerShot(aEfficiencies, aModifiers);
               break;
            case 't':
               nominator *= getMass();
               break;
            case 'h':
               nominator *= getHeat(aModifiers);
               break;
            case 'c':
               nominator *= getNumCriticalSlots();
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
               denominator *= getSecondsPerShot(aEfficiencies, aModifiers);
               break;
            case 't':
               denominator *= getMass();
               break;
            case 'h':
               denominator *= getHeat(aModifiers);
               break;
            case 'c':
               denominator *= getNumCriticalSlots();
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

   /**
    * @return <code>true</code> if this weapon has a non-linear fall off.
    */
   public boolean hasNonLinearFalloff(){
      return 1.0 != fallOffExponent;
   }

   /**
    * @return <code>true</code> if the Lower Arm Actuator (LAA) and/or Hand Actuator (HA) should be removed if this
    *         weapon is equipped.
    */
   public boolean isLargeBore(){
      boolean isLargeBore = false;
      isLargeBore |= getName().toLowerCase().contains("ppc");
      isLargeBore |= getName().toLowerCase().contains("gauss");
      isLargeBore |= getName().toLowerCase().contains("ac/");
      isLargeBore |= getName().toLowerCase().contains("x ac");
      isLargeBore |= getName().toLowerCase().contains("10-x");
      return isLargeBore;
   }
}

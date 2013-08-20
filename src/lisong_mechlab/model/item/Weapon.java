package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class Weapon extends HeatSource{
   protected final double damage;
   protected final double cycleTime;
   protected final double rangeMin;
   protected final double rangeLong;
   protected final double rangeMax;

   public Weapon(ItemStatsWeapon aStatsWeapon, HardpointType aHardpointType){
      super(aStatsWeapon, aHardpointType, aStatsWeapon.WeaponStats.slots, aStatsWeapon.WeaponStats.tons, aStatsWeapon.WeaponStats.heat);
      damage = aStatsWeapon.WeaponStats.damage;
      cycleTime = aStatsWeapon.WeaponStats.cooldown;
      rangeMin = aStatsWeapon.WeaponStats.minRange;
      rangeMax = aStatsWeapon.WeaponStats.maxRange;
      rangeLong = aStatsWeapon.WeaponStats.longRange;
   }

   public double getDamagePerShot(){
      return damage;
   }

   public double getSecondsPerShot(){
      return cycleTime;
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
   public double getStat(String aWeaponStat){
      double nominator = 1;
      int index = 0;
      while( index < aWeaponStat.length() && aWeaponStat.charAt(index) != '/' ){
         switch( aWeaponStat.charAt(index) ){
            case 'd':
               nominator *= getDamagePerShot();
               break;
            case 's':
               nominator *= getSecondsPerShot();
               break;
            case 't':
               nominator *= getMass();
               break;
            case 'h':
               nominator *= getHeat();
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
               denominator *= getSecondsPerShot();
               break;
            case 't':
               denominator *= getMass();
               break;
            case 'h':
               denominator *= getHeat();
               break;
            case 'c':
               denominator *= getNumCriticalSlots();
               break;
            default:
               throw new IllegalArgumentException("Unknown identifier: " + aWeaponStat.charAt(index));
         }
         index++;
      }
      return nominator / denominator;
   }
   
   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getHardpointsCount(getHardpointType()) > 0;
   }
}

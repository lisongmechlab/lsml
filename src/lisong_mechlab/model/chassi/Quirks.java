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
package lisong_mechlab.model.chassi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.item.WeaponModifier;
import lisong_mechlab.model.pilot.PilotSkillTree;

/**
 * This class represents quirks in the form of movement, health and weapon stats.
 * 
 * @author Li Song
 */
public class Quirks implements MovementModifier, WeaponModifier, HealthModifier{
   private final Map<String, Double>          quirks;

   /**
    * [ , armor_resist_ra_multiplier, internal_resist_rl_multiplier, , , internal_resist_ll_multiplier, ,
    * armor_resist_hd_multiplier, , , overheat_damage_multiplier, , , armor_resist_la_multiplier]
    */
   private static transient final Set<String> q = new HashSet<>();

   /**
    * @param aQuirks
    */
   public Quirks(Map<String, Double> aQuirks){
      quirks = aQuirks; // ui_quirk_ + <quirkname> is description

      for(String s : quirks.keySet()){
         q.add(s);
      }
   }

   @Override
   public double extraTorsoYawMax(double aBase){
      Double v = quirks.get("torso_angle_yaw_additive");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double extraTorsoYawSpeed(double aBase){
      Double v = quirks.get("torso_speed_yaw_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTorsoPitchMax(double aBase){
      Double v = quirks.get("torso_angle_pitch_additive");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double extraTorsoPitchSpeed(double aBase){
      Double v = quirks.get("torso_speed_pitch_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraArmYawMax(double aBase){
      Double v = quirks.get("arm_angle_yaw_additive");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double extraArmYawSpeed(double aBase){
      Double v = quirks.get("arm_speed_yaw_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraArmPitchMax(double aBase){
      Double v = quirks.get("arm_angle_pitch_additive");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double extraArmPitchSpeed(double aBase){
      Double v = quirks.get("arm_speed_pitch_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpLowSpeed(double aBase){
      Double v = quirks.get("turn_lerp_low_speed_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpMidSpeed(double aBase){
      Double v = quirks.get("turn_lerp_mid_speed_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpHighSpeed(double aBase){
      Double v = quirks.get("turn_lerp_high_speed_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpLowRate(double aBase){
      Double v = quirks.get("turn_lerp_low_rate_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpMidRate(double aBase){
      Double v = quirks.get("turn_lerp_mid_rate_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpHighRate(double aBase){
      Double v = quirks.get("turn_lerp_high_rate_multiplier");
      if( v != null )
         return v * aBase;
      return 0;
   }

   @Override
   public boolean affectsWeapon(Weapon aWeapon){
      if( aWeapon instanceof MissileWeapon && quirks.containsKey("missile_cooldown_multiplier") )
         return true;
      if( aWeapon instanceof EnergyWeapon && quirks.containsKey("energy_cooldown_multiplier") )
         return true;
      if( aWeapon instanceof BallisticWeapon && quirks.containsKey("ballistic_cooldown_multiplier") )
         return true;
      return false;
   }

   @Override
   public double extraMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return 0;
   }

   @Override
   public double extraLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return 0;
   }

   @Override
   public double extraHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree){
      return 0;
   }

   @Override
   public double extraCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree){
      if( aWeapon instanceof EnergyWeapon ){
         double value = quirks.get("energy_cooldown_multiplier");
         return aCooldown * value;
      }
      else if( aWeapon instanceof MissileWeapon ){
         double value = quirks.get("missile_cooldown_multiplier");
         return aCooldown * value;
      }
      else if( aWeapon instanceof BallisticWeapon ){
         double value = quirks.get("ballistic_cooldown_multiplier");
         return aCooldown * value;
      }
      return 0;
   }

   @Override
   public double extraInternalHP(Location aLocation, double aHP){
      Double v = quirks.get("internal_resist_" + aLocation.shortName().toLowerCase() + "_multiplier");
      if( null != v )
         return aHP * v;
      return 0;
   }

   @Override
   public double extraArmor(Location aLocation, double aHP){
      Double v = quirks.get("armor_resist_" + aLocation.shortName().toLowerCase() + "_multiplier");
      if( null != v )
         return aHP * v;
      return 0;
   }
}

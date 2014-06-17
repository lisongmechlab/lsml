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

import java.util.Map;

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
   public static class Quirk{
      public final String name;
      public final String key;
      public final double value;
      public final boolean positiveGood;

      public Quirk(String aKey, String aName, double aValue, boolean aPositiveGood){
         key = aKey;
         name = aName;
         value = aValue;
         positiveGood = aPositiveGood;
      }
   }

   private final Map<String, Quirk>           quirks;

   /**
    * @param aQuirks
    */
   public Quirks(Map<String, Quirk> aQuirks){
      quirks = aQuirks; // ui_quirk_ + <quirkname> is description
   }

   public String describeAsHtml(){
      if(quirks.isEmpty())
         return "";
      StringBuilder sb = new StringBuilder();
      sb.append("<html>");
      sb.append("<body>");
      sb.append("<p>Quirks:</p>");
      for(Quirk quirk : quirks.values()){
         final String color;
         if(quirk.positiveGood == quirk.value > 0){
            color = "green";
         }
         else{
            color = "red";
         }
         
         sb.append("<p style=\"color:").append(color).append(";\">");
         sb.append(quirk.name).append(": ").append(quirk.value);
         sb.append("</p>");
      }
      sb.append("</body>");
      sb.append("</html>");
      return sb.toString();
   }

   @Override
   public double extraTorsoYawMax(double aBase){
      Quirk quirk = quirks.get("torso_angle_yaw_additive");
      if( quirk != null )
         return quirk.value;
      return 0;
   }

   @Override
   public double extraTorsoYawSpeed(double aBase){
      Quirk quirk = quirks.get("torso_speed_yaw_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTorsoPitchMax(double aBase){
      Quirk quirk = quirks.get("torso_angle_pitch_additive");
      if( quirk != null )
         return quirk.value;
      return 0;
   }

   @Override
   public double extraTorsoPitchSpeed(double aBase){
      Quirk quirk = quirks.get("torso_speed_pitch_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraArmYawMax(double aBase){
      Quirk quirk = quirks.get("arm_angle_yaw_additive");
      if( quirk != null )
         return quirk.value;
      return 0;
   }

   @Override
   public double extraArmYawSpeed(double aBase){
      Quirk quirk = quirks.get("arm_speed_yaw_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraArmPitchMax(double aBase){
      Quirk quirk = quirks.get("arm_angle_pitch_additive");
      if( quirk != null )
         return quirk.value;
      return 0;
   }

   @Override
   public double extraArmPitchSpeed(double aBase){
      Quirk quirk = quirks.get("arm_speed_pitch_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpLowSpeed(double aBase){
      Quirk quirk = quirks.get("turn_lerp_low_speed_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpMidSpeed(double aBase){
      Quirk quirk = quirks.get("turn_lerp_mid_speed_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpHighSpeed(double aBase){
      Quirk quirk = quirks.get("turn_lerp_high_speed_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpLowRate(double aBase){
      Quirk quirk = quirks.get("turn_lerp_low_rate_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpMidRate(double aBase){
      Quirk quirk = quirks.get("turn_lerp_mid_rate_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
      return 0;
   }

   @Override
   public double extraTurnLerpHighRate(double aBase){
      Quirk quirk = quirks.get("turn_lerp_high_rate_multiplier");
      if( quirk != null )
         return quirk.value * aBase;
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
         double value = quirks.get("energy_cooldown_multiplier").value;
         return aCooldown * value;
      }
      else if( aWeapon instanceof MissileWeapon ){
         double value = quirks.get("missile_cooldown_multiplier").value;
         return aCooldown * value;
      }
      else if( aWeapon instanceof BallisticWeapon ){
         double value = quirks.get("ballistic_cooldown_multiplier").value;
         return aCooldown * value;
      }
      return 0;
   }

   @Override
   public double extraInternalHP(Location aLocation, double aHP){
      Quirk quirk = quirks.get("internal_resist_" + aLocation.shortName().toLowerCase() + "_multiplier");
      if( quirk != null )
         return quirk.value * aHP;
      return 0;
   }

   @Override
   public double extraArmor(Location aLocation, double aHP){
      Quirk quirk = quirks.get("armor_resist_" + aLocation.shortName().toLowerCase() + "_multiplier");
      if( quirk != null )
         return quirk.value * aHP;
      return 0;
   }
}

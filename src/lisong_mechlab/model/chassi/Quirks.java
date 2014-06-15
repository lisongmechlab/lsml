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

import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.item.WeaponModifier;
import lisong_mechlab.model.pilot.PilotSkillTree;

/**
 * This class represents quirks in the form of movement and weapon stats.
 * 
 * @author Li Song
 */
public class Quirks implements MovementProfile, WeaponModifier{
   private final Map<String, Double> quirks;

   /**
    * @param aQuirks
    */
   public Quirks(Map<String, Double> aQuirks){
      quirks = aQuirks;
   }

   @Override
   public MovementArchetype getMovementArchetype(){
      return null;
   }

   @Override
   public double getMaxMovementSpeed(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getReverseSpeedMultiplier(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTorsoYawMax(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTorsoYawSpeed(){
      Double v = quirks.get("torso_speed_yaw_multiplier");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double getTorsoPitchMax(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTorsoPitchSpeed(){
      Double v = quirks.get("torso_speed_pitch_multiplier");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double getArmYawMax(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getArmYawSpeed(){
      Double v = quirks.get("arm_speed_yaw_multiplier");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double getArmPitchMax(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getArmPitchSpeed(){
      Double v = quirks.get("arm_speed_pitch_multiplier");
      if( v != null )
         return v;
      return 0;
   }

   @Override
   public double getTurnLerpLowSpeed(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTurnLerpMidSpeed(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTurnLerpHighSpeed(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTurnLerpLowRate(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTurnLerpMidRate(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double getTurnLerpHighRate(){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public boolean affectsWeapon(Weapon aWeapon){
      return false; // TODO Auto-generated method stub
   }

   @Override
   public double applyMaxRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double applyLongRange(Weapon aWeapon, double aRange, PilotSkillTree aPilotSkillTree){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double applyHeat(Weapon aWeapon, double aHeat, PilotSkillTree aPilotSkillTree){
      return 0; // TODO Auto-generated method stub
   }

   @Override
   public double applyCooldown(Weapon aWeapon, double aCooldown, PilotSkillTree aPilotSkillTree){
      return 0; // TODO Auto-generated method stub
   }
}

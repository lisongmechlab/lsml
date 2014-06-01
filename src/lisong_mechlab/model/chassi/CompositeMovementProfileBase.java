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

/**
 * This {@link MovementProfile} provides an abstract base for a composite {@link MovementProfile} where the value of
 * each attribute is the result a function applied to a set of {@link MovementProfile}s.
 * 
 * @author Li Song
 */
public abstract class CompositeMovementProfileBase implements MovementProfile{

   /**
    * Uses reflection to calculate the sought for value.
    * 
    * @param aMethodName
    *           The name of the function to call to get the value.
    * @return The calculated value.
    */
   protected abstract double calc(String aMethodName);

   @Override
   public double getMaxMovementSpeed(){
      return calc("getMaxMovementSpeed");
   }

   @Override
   public double getReverseSpeedMultiplier(){
      return calc("getReverseSpeedMultiplier");
   }

   @Override
   public double getTorsoYawMax(){
      return calc("getTorsoYawMax");
   }

   @Override
   public double getTorsoYawSpeed(){
      return calc("getTorsoYawSpeed");
   }

   @Override
   public double getTorsoPitchMax(){
      return calc("getTorsoPitchMax");
   }

   @Override
   public double getTorsoPitchSpeed(){
      return calc("getTorsoPitchSpeed");
   }

   @Override
   public double getArmYawMax(){
      return calc("getArmYawMax");
   }

   @Override
   public double getArmYawSpeed(){
      return calc("getArmYawSpeed");
   }

   @Override
   public double getArmPitchMax(){
      return calc("getArmPitchMax");
   }

   @Override
   public double getArmPitchSpeed(){
      return calc("getArmPitchSpeed");
   }

   @Override
   public double getTurnLerpLowSpeed(){
      return calc("getTurnLerpLowSpeed");
   }

   @Override
   public double getTurnLerpMidSpeed(){
      return calc("getTurnLerpMidSpeed");
   }

   @Override
   public double getTurnLerpHighSpeed(){
      return calc("getTurnLerpHighSpeed");
   }

   @Override
   public double getTurnLerpLowRate(){
      return calc("getTurnLerpLowRate");
   }

   @Override
   public double getTurnLerpMidRate(){
      return calc("getTurnLerpMidRate");
   }

   @Override
   public double getTurnLerpHighRate(){
      return calc("getTurnLerpHighRate");
   }

}

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
package lisong_mechlab.model.chassi;

import java.util.ArrayList;
import java.util.List;

/**
 * This {@link MovementProfile} gives the sum of all added {@link MovementProfile}s. One profile has to be chosen as
 * main profile that gives base attributes.
 * 
 * @author Emily Björk
 */
public class MovementProfileSum implements MovementProfile{

   private List<MovementProfile> terms = new ArrayList<>();
   MovementProfile               mainProfile;

   public void addMovementProfile(MovementProfile aMovementProfile){
      terms.add(aMovementProfile);
   }

   public void removeMovementProfile(MovementProfile aMovementProfile){
      terms.remove(aMovementProfile);
   }

   public MovementProfileSum(MovementProfile aMainProfile){
      addMovementProfile(aMainProfile);
      mainProfile = aMainProfile;
   }

   @Override
   public MovementArchetype getMovementArchetype(){
      return mainProfile.getMovementArchetype();
   }

   @Override
   public double getMaxMovementSpeed(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getMaxMovementSpeed();
      }
      return ans;
   }

   @Override
   public double getReverseSpeedMultiplier(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getReverseSpeedMultiplier();
      }
      return ans;
   }

   @Override
   public double getTorsoYawMax(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getTorsoYawMax();
      }
      return ans;
   }

   @Override
   public double getTorsoYawSpeed(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getTorsoYawSpeed();
      }
      return ans;
   }

   @Override
   public double getTorsoPitchMax(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getTorsoPitchMax();
      }
      return ans;
   }

   @Override
   public double getTorsoPitchSpeed(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getTorsoPitchSpeed();
      }
      return ans;
   }

   @Override
   public double getArmYawMax(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getArmYawMax();
      }
      return ans;
   }

   @Override
   public double getArmYawSpeed(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getArmYawSpeed();
      }
      return ans;
   }

   @Override
   public double getArmPitchMax(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getArmPitchMax();
      }
      return ans;
   }

   @Override
   public double getArmPitchSpeed(){
      double ans = 0;
      for(MovementProfile profile : terms){
         ans += profile.getArmPitchSpeed();
      }
      return ans;
   }

}

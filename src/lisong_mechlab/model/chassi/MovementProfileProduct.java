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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link MovementProfile} gives the sum of all added {@link MovementProfile}s. One profile has to be chosen as
 * main profile that gives base attributes.
 * 
 * @author Li Song
 */
public class MovementProfileProduct extends CompositeMovementProfileBase{

   private List<MovementProfile> terms = new ArrayList<>();
   MovementProfile               mainProfile;

   @Override
   protected double calc(String aMethodName){
      try{
         double base = (double)mainProfile.getClass().getMethod(aMethodName).invoke(mainProfile);
         double ans = base;

         for(MovementProfile profile : terms){
            ans += base * (double)profile.getClass().getMethod(aMethodName).invoke(profile);
         }
         return ans;
      }
      catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e ){
         throw new IllegalArgumentException(e);
      }
   }

   public MovementProfileProduct(MovementProfile aMainProfile){
      mainProfile = aMainProfile;
   }

   public void addMovementProfile(MovementProfile aMovementProfile){
      terms.add(aMovementProfile);
   }

   public void removeMovementProfile(MovementProfile aMovementProfile){
      terms.remove(aMovementProfile);
   }

   @Override
   public MovementArchetype getMovementArchetype(){
      return mainProfile.getMovementArchetype();
   }
}

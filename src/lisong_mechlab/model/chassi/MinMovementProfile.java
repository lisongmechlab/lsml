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
import java.util.List;

/**
 * This movement profile gives the minimum possible value of a combination of different movement profiles.
 * <p>
 * More specifically, a base profile is defined (typically chassis standard values) and a number of alternatives are
 * given in groups. For each group the lowest value among the alternatives is taken and added to the base profile.
 * <p>
 * Movement archetype is always that of the base profile.
 * 
 * @author Li Song
 */
public class MinMovementProfile extends CompositeMovementProfileBase{

   private MovementProfile             base;
   private List<List<MovementProfile>> groups;

   public MinMovementProfile(MovementProfile aBase, List<List<MovementProfile>> aGroups){
      base = aBase;
      groups = aGroups;
   }

   @Override
   protected double calc(String aMethodName){
      try{
         double ans = (double)base.getClass().getMethod(aMethodName).invoke(base);
         for(List<MovementProfile> group : groups){
            double min = Double.POSITIVE_INFINITY;
            for(MovementProfile profile : group){
               min = Math.min(min, (double)profile.getClass().getMethod(aMethodName).invoke(profile));
            }
            if( min != Double.POSITIVE_INFINITY )
               ans += min;
         }
         return ans;
      }
      catch( IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e ){
         throw new IllegalArgumentException();
      }
   }

   @Override
   public MovementArchetype getMovementArchetype(){
      return base.getMovementArchetype();
   }
}

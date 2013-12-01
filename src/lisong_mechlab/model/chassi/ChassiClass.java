/*
 * @formatter:off
 * Li Song Mech Lab - A 'mech building tool for PGI's MechWarrior: Online.
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

public enum ChassiClass{
   LIGHT(1.4), MEDIUM(1.3), HEAVY(1.2), ASSAULT(1.2);

   private final static double TONNAGE_EPSILON = Math.ulp(100) * 5.0;

   ChassiClass(double aMultiplier){
      multiplier = aMultiplier;
   }

   public static ChassiClass fromMaxTons(double tons){

      if( tons < 40 - TONNAGE_EPSILON ){
         return ChassiClass.LIGHT;
      }
      else if( tons < 60 - TONNAGE_EPSILON ){
         return ChassiClass.MEDIUM;
      }
      else if( tons < 80 - TONNAGE_EPSILON ){
         return ChassiClass.HEAVY;
      }
      else{
         return ChassiClass.ASSAULT;
      }
   }

   public double EngineMultiplier(){
      return multiplier;
   }

   private final double multiplier;
}

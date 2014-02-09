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

/**
 * This enum represents the weight class of a chassi: Light, Medium, Heavy or Assault. It provides a way to determine
 * weight class from tonnage.
 * 
 * @author Emily Björk
 */
public enum ChassiClass{
   LIGHT, MEDIUM, HEAVY, ASSAULT;

   private final static double TONNAGE_EPSILON = Math.ulp(100) * 5.0;

   /**
    * Determines the {@link ChassiClass} from a tonnage amount.
    * 
    * @param tons
    *           The tonnage to calculate from.
    * @return The {@link ChassiClass} matching the argument.
    */
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
}

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
 * This interface represents an object that can alter health values of components and items.
 * 
 * @author Li Song
 */
public interface HealthModifier{

   /**
    * Calculates how much extra HP is added to the given location by this modifier.
    * 
    * @param aLocation
    *           The location to calculate bonus HP for.
    * @param aHP
    *           The original HP of the location.
    * @return The extra HP that should be added on top.
    */
   public double extraInternalHP(Location aLocation, double aHP);

   /**
    * Calculates how much armor HP is added to the given location by this modifier.
    * 
    * @param aLocation
    *           The location to calculate bonus HP for.
    * @param aHP
    *           The original HP of the location.
    * @return The extra HP that should be added on top.
    */
   double extraArmor(Location aLocation, double aHP);
}

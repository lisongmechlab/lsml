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
package lisong_mechlab.model.item;

/**
 * @author Li Song
 */
public enum ModuleCathegory{
   Support, Consumable, Vision, Sensor, Targeting;

   public static ModuleCathegory fromMwo(String aMwoValue){
      switch( aMwoValue ){
         case "ePTModule_Support":
            return Support;
         case "ePTModule_Vision":
            return Vision;
         case "ePTModule_Sensor":
            return Sensor;
         case "ePTModule_Target":
            return Targeting;
         case "ePTModule_Consumable":
            return Consumable;
         default:
            throw new IllegalArgumentException("Unknown module type.");
      }
   }
}

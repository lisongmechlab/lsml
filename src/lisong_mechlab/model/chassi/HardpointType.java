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

public enum HardpointType{
   ENERGY("E"), BALLISTIC("B"), MISSILE("M"), AMS("AMS"), ECM("ECM"), NONE("");

   private HardpointType(String aShortName){
      shortName = aShortName;
   }

   public String shortName(){
      return shortName;
   }

   public static HardpointType fromMwoType(String type){
      switch( type ){
         case "Energy":
            return HardpointType.ENERGY;
         case "AMS":
            return HardpointType.AMS;
         case "Ballistic":
            return HardpointType.BALLISTIC;
         case "Missile":
            return HardpointType.MISSILE;
         default:
            throw new RuntimeException("Unknown hardpoint type!");
      }
   }

   public static HardpointType fromMwoType(int type){
      switch( type ){
         case 1:
            return HardpointType.ENERGY;
         case 4:
            return HardpointType.AMS;
         case 0:
            return HardpointType.BALLISTIC;
         case 2:
            return HardpointType.MISSILE;
         default:
            throw new RuntimeException("Unknown hardpoint type!");
      }
   }

   private final String shortName;
}

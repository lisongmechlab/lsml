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

public enum Part{
   Head("Head", "HD"),
   LeftArm("Left Arm", "LA"),
   LeftLeg("Left Leg", "LL"),
   LeftTorso("Left Torso", "LT", true),
   CenterTorso("Center Torso", "CT", true),
   RightTorso("Right Torso", "RT", true),
   RightLeg("Right Leg", "RL"),
   RightArm("Right Arm", "RA");

   Part(String aLongName, String aShortName){
      this(aLongName, aShortName, false);
   }

   Part(String aLongName, String aShortName, boolean aTwosided){
      longName = aLongName;
      shortName = aShortName;
      twosided = aTwosided;
   }

   private final String  shortName;
   private final String  longName;
   private final boolean twosided;

   public String longName(){
      return longName;
   }

   public String shortName(){
      return shortName;
   }

   public boolean isTwoSided(){
      return twosided;
   }

   private final static Part[] left2right = new Part[] {Part.RightArm, Part.RightTorso, Part.RightLeg, Part.Head, Part.CenterTorso, Part.LeftTorso,
         Part.LeftLeg, Part.LeftArm       };

   public static Part[] leftToRight(){
      return left2right;
   }

   public static Part fromMwoName(String componentName){
      if( componentName.equals("head") ){
         return Head;
      }
      else if( componentName.equals("centre_torso") ){
         return CenterTorso;
      }
      else if( componentName.equals("left_torso") ){
         return LeftTorso;
      }
      else if( componentName.equals("right_torso") ){
         return RightTorso;
      }
      else if( componentName.equals("left_arm") ){
         return LeftArm;
      }
      else if( componentName.equals("right_arm") ){
         return RightArm;
      }
      else if( componentName.equals("left_leg") ){
         return LeftLeg;
      }
      else if( componentName.equals("right_leg") ){
         return RightLeg;
      }
      else if( componentName.equals("centre_torso_rear") ){
         return CenterTorso;
      }
      else if( componentName.equals("left_torso_rear") ){
         return LeftTorso;
      }
      else if( componentName.equals("right_torso_rear") ){
         return RightTorso;
      }
      else{
         throw new RuntimeException("Unknown component in mech chassi! " + componentName);
      }
   }

   public static boolean isRear(String aName){
      return aName.endsWith("_rear");
   }
}

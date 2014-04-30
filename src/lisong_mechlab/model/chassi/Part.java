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


public enum Part{
   Head("Head", "head", "HD"),
   LeftArm("Left Arm", "left_arm", "LA"),
   LeftLeg("Left Leg", "left_leg", "LL"),
   LeftTorso("Left Torso", "left_torso", "LT", true),
   CenterTorso("Center Torso", "centre_torso", "CT", true),
   RightTorso("Right Torso", "right_torso", "RT", true),
   RightLeg("Right Leg", "right_leg", "RL"),
   RightArm("Right Arm", "right_arm", "RA");

   Part(String aLongName, String aMwoName, String aShortName){
      this(aLongName, aMwoName, aShortName, false);
   }

   Part(String aLongName, String aMwoName, String aShortName, boolean aTwosided){
      longName = aLongName;
      shortName = aShortName;
      twosided = aTwosided;
      mwoName = aMwoName;
      mwoNameRear = mwoName + "_rear";
   }

   private final String  mwoName;
   private final String  mwoNameRear;
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

   public Part oppositeSide(){
      switch( this ){
         case LeftArm:
            return RightArm;
         case LeftLeg:
            return RightLeg;
         case LeftTorso:
            return RightTorso;
         case RightArm:
            return LeftArm;
         case RightLeg:
            return LeftLeg;
         case RightTorso:
            return LeftTorso;
         default:
            return null;
      }
   }

   public static Part fromMwoName(String componentName){
      for(Part part : Part.values()){
         if( part.mwoName.equals(componentName) || part.mwoNameRear.equals(componentName) ){
            return part;
         }
      }
      throw new RuntimeException("Unknown component in mech chassi! " + componentName);
   }

   public static boolean isRear(String aName){
      return aName.endsWith("_rear");
   }

   public String toMwoName(){
      return mwoName;
   }
   
   public String toMwoRearName(){
      return mwoNameRear;
   }
}

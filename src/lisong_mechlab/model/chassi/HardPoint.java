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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Represents a hard point on a chassis.
 * 
 * @author Emily Björk
 */
public class HardPoint{
   @XStreamAsAttribute
   private final int           tubes;
   @XStreamAsAttribute
   private final HardPointType type;
   @XStreamAsAttribute
   private final boolean       hasBayDoor;

   public HardPoint(HardPointType aType){
      this(aType, 0, false);
   }

   public HardPoint(HardPointType aType, int aNumTubes, boolean aHasBayDoor){
      if( aType == HardPointType.MISSILE && aNumTubes < 1 ){
         throw new IllegalArgumentException("Missile hard points must have a positive, non-zero number of tubes");
      }
      type = aType;
      tubes = aNumTubes;
      hasBayDoor = aHasBayDoor;
   }

   /**
    * @return The type of this hard point.
    */
   public HardPointType getType(){
      return type;
   }

   /**
    * @return The number of missile tubes this hard point has.
    */
   public int getNumMissileTubes(){
      return tubes;
   }

   /**
    * @return <code>true</code> if this hard point has missile bay doors.
    */
   public boolean hasBayDoor(){
      return hasBayDoor;
   }
}

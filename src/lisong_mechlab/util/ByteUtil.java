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
package lisong_mechlab.util;

/**
 * Utility helper to make certain operations on bytes behave like you'd expect to.
 * 
 * @author Emily Björk
 */
public class ByteUtil{

   /**
    * Performs a "Logical Shift Left" (lsl) operation on the byte.
    * 
    * @param b
    *           The byte to shift
    * @param shift
    *           The number of positions to shift.
    * @return The shifted byte
    */
   static public byte lsl(final int b, final int shift){
      return (byte)((b & 0xFF) << shift);
   }

   /**
    * Performs a "Logical Shift Right" (lsr) operation on the byte.
    * 
    * @param b
    *           The byte to shift
    * @param shift
    *           The number of positions to shift.
    * @return The shifted byte
    */
   static public byte lsr(final int b, final int shift){
      return (byte)((b & 0xFF) >>> shift);
   }
}

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
 * An exception that is thrown from various decoding algorithms in the case that they fail to handle the given data.
 * 
 * @author Emily Björk
 */
public class DecodingException extends Exception{
   private static final long serialVersionUID = 8948178136779804692L;

   public DecodingException(String aString){
      super(aString);
   }

   public DecodingException(Throwable aThrowable){
      super(aThrowable);
   }
}

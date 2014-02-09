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
 * This class contains helper functions for dealing with {@link String}s.
 * 
 * @author Emily Björk
 */
public class StringUtil{

   public static String longestCommonSubstring(final String a, final String b){

      int start = 0;
      int len = 0;

      for(int i = 0; i < a.length(); ++i){
         for(int j = 0; j < b.length(); ++j){

            int k = 0;
            while( i + k < a.length() && j + k < b.length() && a.charAt(i + k) == b.charAt(j + k) ){
               k++;
            }

            if( k > len ){
               len = k;
               start = i;
            }
         }
      }
      return a.substring(start, start + len);
   }

}

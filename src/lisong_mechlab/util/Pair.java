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
 * A pair, much similar in concept to std::pair from c++. Holds two values, the <code>first</code> and
 * <code>second</code>.
 * 
 * @author Emily Björk
 * @param <F>
 *           The type of the <code>first</code> value.
 * @param <S>
 *           The type of the <code>second</code> value.
 */
public class Pair<F, S> {
   public final F first;
   public final S second;

   public Pair(F aFirst, S aSecond){
      first = aFirst;
      second = aSecond;
   }

   @Override
   public int hashCode(){
      return first.hashCode() ^ second.hashCode();
   }

   @Override
   public boolean equals(Object o){
      if( this == o )
         return true;
      if( !(o instanceof Pair) )
         return false;
      @SuppressWarnings("rawtypes")
      Pair that = (Pair)o;
      return this.first.equals(that.first) && this.second.equals(that.second);
   }

   @Override
   public String toString(){
      return "{" + first.toString() + ", " + second.toString() + "}";
   }
}

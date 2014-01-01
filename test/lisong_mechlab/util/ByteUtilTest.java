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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * A simple test suite to make sure the {@link ByteUtil}s are working as intended.
 * 
 * @author Emily Björk
 */
public class ByteUtilTest{

   @Test
   public void testLsr(){
      byte input = (byte)0xFF;
      int shift = 2;
      byte expected = 0x3F;

      assertEquals(expected, ByteUtil.lsr(input, shift));
   }

   @Test
   public void testLsl(){
      byte input = (byte)0xFF;
      int shift = 2;
      byte expected = (byte)0xFC;

      assertEquals(expected, ByteUtil.lsl(input, shift));
   }

}

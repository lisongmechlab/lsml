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
 * Test suite for the {@link StringUtil} class.
 * 
 * @author Emily Björk
 */
public class StringUtilTest{

   @Test
   public final void testLongestCommonSubstring() throws Exception{
      String a = "abcdefgh";
      String b = "xkcdefasdef";

      assertEquals("cdef", StringUtil.longestCommonSubstring(a, b));
      assertEquals("cdef", StringUtil.longestCommonSubstring(b, a));
   }

   @Test
   public final void testLongestCommonSubstring_lowEdge() throws Exception{
      String a = "abcdefgh";
      String b = "abcdexkcdefasdef";

      assertEquals("abcde", StringUtil.longestCommonSubstring(a, b));
      assertEquals("abcde", StringUtil.longestCommonSubstring(b, a));
   }

   @Test
   public final void testLongestCommonSubstring_highEdge() throws Exception{
      String a = "abcdefgh1234567";
      String b = "abcdexkcdefasdf1234567";

      assertEquals("1234567", StringUtil.longestCommonSubstring(a, b));
      assertEquals("1234567", StringUtil.longestCommonSubstring(b, a));
   }

   @Test
   public final void testLongestCommonSubstring_automorphism() throws Exception{
      String str = "xkcdefasdef";
      assertEquals(str, StringUtil.longestCommonSubstring(str, str));
   }

}

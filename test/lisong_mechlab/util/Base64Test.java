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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Random;

import org.junit.Test;

/**
 * Test suite for {@link Base64}
 * 
 * @author Emily Björk
 */
public class Base64Test{
   private final Base64 cut = new Base64();

   /**
    * Test that {@link Base64#encode(byte[])} produces a standard compatible Base64 string.
    * <p>
    * Example string from Wikipedia article on Base64.
    */
   @Test
   public void testEncode(){
      String input = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
      String expected = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";

      assertEquals(expected, new String(cut.encode(input.getBytes())));
   }

   /**
    * Test that {@link Base64#decode(char[])} can decode a standard compatible Base64 string.
    * <p>
    * Example string from Wikipedia article on Base64.
    */
   @Test
   public void testDecode() throws DecodingException{
      String input = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";
      String expected = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";

      assertEquals(expected, new String(cut.decode(input.toCharArray())));
   }

   /**
    * Test that {@link Base64#encode(byte[])} and {@link Base64#decode(char[])} can handle raw data and not just ascii.
    */
   @Test
   public void testEncodeDecodeRawData() throws DecodingException{
      final int numbytes = 1000;
      byte[] input = new byte[numbytes];
      Random random = new Random(0);
      random.nextBytes(input);

      assertArrayEquals(input, cut.decode(cut.encode(input)));
   }

   /**
    * Test that {@link Base64#encode(byte[])} correctly handles all cases of padding according to the standard.
    */
   @Test
   public void testEncodePadding(){
      String i1 = "any carnal pleasure.";
      String i2 = "any carnal pleasure";
      String i3 = "any carnal pleasur";
      String i4 = "any carnal pleasu";
      String i5 = "any carnal pleas";

      String o1 = "YW55IGNhcm5hbCBwbGVhc3VyZS4=";
      String o2 = "YW55IGNhcm5hbCBwbGVhc3VyZQ==";
      String o3 = "YW55IGNhcm5hbCBwbGVhc3Vy";
      String o4 = "YW55IGNhcm5hbCBwbGVhc3U=";
      String o5 = "YW55IGNhcm5hbCBwbGVhcw==";

      assertEquals(o1, new String(cut.encode(i1.getBytes())));
      assertEquals(o2, new String(cut.encode(i2.getBytes())));
      assertEquals(o3, new String(cut.encode(i3.getBytes())));
      assertEquals(o4, new String(cut.encode(i4.getBytes())));
      assertEquals(o5, new String(cut.encode(i5.getBytes())));
   }

   /**
    * Test that {@link Base64#encode(byte[])} correctly handles all cases of padding according to the standard.
    */
   @Test
   public void testDecodePadding() throws DecodingException{
      String i1 = "YW55IGNhcm5hbCBwbGVhc3VyZS4=";
      String i2 = "YW55IGNhcm5hbCBwbGVhc3VyZQ==";
      String i3 = "YW55IGNhcm5hbCBwbGVhc3Vy";
      String i4 = "YW55IGNhcm5hbCBwbGVhc3U=";
      String i5 = "YW55IGNhcm5hbCBwbGVhcw==";

      String o1 = "any carnal pleasure.";
      String o2 = "any carnal pleasure";
      String o3 = "any carnal pleasur";
      String o4 = "any carnal pleasu";
      String o5 = "any carnal pleas";

      assertEquals(o1, new String(cut.decode(i1.toCharArray())));
      assertEquals(o2, new String(cut.decode(i2.toCharArray())));
      assertEquals(o3, new String(cut.decode(i3.toCharArray())));
      assertEquals(o4, new String(cut.decode(i4.toCharArray())));
      assertEquals(o5, new String(cut.decode(i5.toCharArray())));
   }

   /**
    * Test that {@link Base64#decode(char[])} throws a {@link DecodingException} on an input of the wrong length.
    */
   @Test(expected = DecodingException.class)
   public void testDecode_wronglength() throws DecodingException{
      String input = "TWFu=";
      cut.decode(input.toCharArray());
   }

   /**
    * Test that {@link Base64#decode(char[])} throws a {@link DecodingException} if there is a non Base64 symbol in the
    * input.
    */
   @Test
   public void testDecode_badChar(){
      char[] input = "YW55IGNhcm5hbCBwbGVhc3Vy".toCharArray();

      String validSymbols = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+/";

      for(int i = Character.MIN_VALUE; i < Character.MAX_VALUE; ++i){
         if( validSymbols.contains(String.valueOf((char)i)) )
            continue;
         input[5] = (char)i;
         try{
            cut.decode(input);
            fail("Expected exception");
         }
         catch( DecodingException e ){
            // success!
         }
      }
   }
}

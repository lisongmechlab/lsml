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

import static lisong_mechlab.util.ByteUtil.lsl;
import static lisong_mechlab.util.ByteUtil.lsr;

import java.util.Arrays;

/**
 * This class handles conversions between raw <code>byte</code> arrays and base64 strings.
 * <p>
 * The methods *could* be static but are not for testability reasons.
 * 
 * @author Emily Björk
 */
public class Base64{

   /**
    * Encodes a <code>byte</code> array into a base64 encoded <code>char</code> array.
    * 
    * @param aInput
    *           The data to encode.
    * @return A <code>char</code> array containing the base64 encoded string of the input.
    */
   public char[] encode(byte[] aInput){
      char[] output = new char[(aInput.length + 2) / 3 * 4];
      int output_idx = 0;
      int input_idx = 0;
      for(int i = 0; i < aInput.length / 3; ++i){
         byte b0 = aInput[input_idx++];
         byte b1 = aInput[input_idx++];
         byte b2 = aInput[input_idx++];
         output[output_idx++] = encodeChar0(b0);
         output[output_idx++] = encodeChar1(b0, b1);
         output[output_idx++] = encodeChar2(b1, b2);
         output[output_idx++] = encodeChar3(b2);
      }

      final int spareBytes = aInput.length % 3;
      if( spareBytes == 1 ){
         byte b0 = aInput[input_idx++];
         byte b1 = 0;
         output[output_idx++] = encodeChar0(b0);
         output[output_idx++] = encodeChar1(b0, b1);
         output[output_idx++] = '=';
         output[output_idx++] = '=';
      }
      else if( spareBytes == 2 ){
         byte b0 = aInput[input_idx++];
         byte b1 = aInput[input_idx++];
         byte b2 = 0;
         output[output_idx++] = encodeChar0(b0);
         output[output_idx++] = encodeChar1(b0, b1);
         output[output_idx++] = encodeChar2(b1, b2);
         output[output_idx++] = '=';
      }
      return output;
   }

   /**
    * Decodes a <code>char</code> array of Base64 symbols into a <code>byte</code> array.
    * 
    * @param aInput
    *           The data to decode.
    * @return A <code>byte</code> array with the output data.
    * @throws DecodingException
    *            Thrown if the input is not a valid Base64 string.
    */
   public byte[] decode(char[] aInput) throws DecodingException{
      if( aInput.length % 4 != 0 )
         throw new DecodingException("The string [" + String.valueOf(aInput)
                                     + "] is not of a valid Base64 string length (must be multiple of 4 characters).");

      final int numBlocks = aInput.length / 4;
      byte[] output = new byte[numBlocks * 3];
      int output_idx = 0;
      int input_idx = 0;
      while( input_idx < aInput.length ){
         byte b0 = revert(aInput[input_idx++]);
         byte b1 = revert(aInput[input_idx++]);
         output[output_idx++] = (byte)(lsl(b0, 2) | lsr(b1, 4));

         if( aInput[input_idx] != '=' ){
            byte b2 = revert(aInput[input_idx++]);
            output[output_idx++] = (byte)(lsl(b1, 4) | lsr(b2, 2));

            if( aInput[input_idx] != '=' ){
               byte b3 = revert(aInput[input_idx++]);
               output[output_idx++] = (byte)(lsl(b2, 6) | b3);
            }
            else{
               break;
            }
         }
         else{
            break;
         }
      }
      return Arrays.copyOf(output, output_idx);
   }

   /**
    * Converts a 6bit integer value to a Base64 symbol.
    * 
    * @param value
    *           The value containing the 6 bits to convert in its LSB.
    * @return A Base64 symbol.
    */
   private char convert(byte value){
      if( value < 26 )
         return (char)((byte)'A' + value);
      else if( value < 52 )
         return (char)((byte)'a' + value - 26);
      else if( value < 62 )
         return (char)((byte)'0' + value - 52);
      else if( value == 62 )
         return '+';
      else
         return '/'; // value == 63
   }

   /**
    * Converts a Base64 symbol to a 6bit integer.
    * 
    * @param value
    *           The Base64 symbol to convert.
    * @return A <code>byte</code> containing the 6 bits in its LSB.
    * @throws DecodingException
    *            Thrown if the argument is not a valid Base64 symbol.
    */
   private byte revert(char value) throws DecodingException{
      if( value >= 'A' && value <= 'Z' )
         return (byte)(value - 'A' + 0);
      else if( value >= 'a' && value <= 'z' )
         return (byte)(value - 'a' + 26);
      else if( value >= '0' && value <= '9' )
         return (byte)(value - '0' + 52);
      else if( value == '+' )
         return 62;
      else if( value == '/' )
         return 63;
      else
         throw new DecodingException("Invalid character [" + value + "] in string");
   }

   private char encodeChar0(byte b0){
      return convert(lsr(b0, 2));
   }

   private char encodeChar1(byte b0, byte b1){
      return convert((byte)(lsl(b0 & 0x03, 4) | lsr(b1 & 0xF0, 4)));
   }

   private char encodeChar2(byte b1, byte b2){
      return convert((byte)(lsl(b1 & 0x0F, 2) | lsr(b2, 6)));
   }

   private char encodeChar3(byte b2){
      return convert((byte)(b2 & 0x3F));
   }
}

package lisong_mechlab.util;

import java.util.Arrays;

public class Base64{

   private char convert(byte value) throws DecodingException{
      if( value < 0 )
         throw new DecodingException();
      else if( value < 26 )
         return (char)((byte)'A' + value);
      else if( value < 52 )
         return (char)((byte)'a' + value - 26);
      else if( value < 62 )
         return (char)((byte)'0' + value - 52);
      else if( value == 62 )
         return '+';
      else if( value == 63 )
         return '/';
      else
         throw new DecodingException();
   }

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
         throw new DecodingException();
   }

   private char[] encode8bits(byte b0) throws DecodingException{
      return Arrays.copyOf(encode24bits(b0, (byte)0, (byte)0), 2);
   }

   private char[] encode16bits(byte b0, byte b1) throws DecodingException{
      return Arrays.copyOf(encode24bits(b0, b1, (byte)0), 3);
   }

   final byte lsl(int b, int shift){
      return (byte)((b & 0xFF) << shift);
   }

   final byte lsr(int b, int shift){
      // Because java is brain dead and sign extends my byte when I right shift
      return (byte)((b & 0xFF) >>> shift);
   }

   private char[] encode24bits(byte b0, byte b1, byte b2) throws DecodingException{
      char c0 = convert(lsr(b0, 2));
      char c1 = convert((byte)(lsl(b0 & 0x03, 4) | lsr(b1 & 0xF0, 4)));
      char c2 = convert((byte)(lsl(b1 & 0x0F, 2) | lsr(b2, 6)));
      char c3 = convert((byte)(b2 & 0x3F));
      revert(c0); // Does not throw
      revert(c1); // Does not throw
      revert(c2); // Does not throw
      revert(c3); // Does not throw

      return new char[] {c0, c1, c2, c3};
   }

   public char[] encode(byte[] aInput) throws DecodingException{
      char[] output = new char[(aInput.length + 2) / 3 * 4];
      int output_idx = 0;
      int input_idx = 0;
      for(int i = 0; i < aInput.length / 3; ++i){
         char[] r = encode24bits(aInput[input_idx++], aInput[input_idx++], aInput[input_idx++]);
         output[output_idx++] = r[0];
         output[output_idx++] = r[1];
         output[output_idx++] = r[2];
         output[output_idx++] = r[3];
      }

      final int spareBytes = aInput.length % 3;
      if( spareBytes == 1 ){
         char[] r = encode8bits(aInput[input_idx++]);
         output[output_idx++] = r[0];
         output[output_idx++] = r[1];
         output[output_idx++] = '=';
         output[output_idx++] = '=';
      }
      else if( spareBytes == 2 ){
         char[] r = encode16bits(aInput[input_idx++], aInput[input_idx++]);
         output[output_idx++] = r[0];
         output[output_idx++] = r[1];
         output[output_idx++] = r[2];
         output[output_idx++] = '=';
      }
      return output;
   }

   public byte[] decode(char[] aInput) throws DecodingException{
      if( aInput.length % 4 != 0 )
         throw new DecodingException();

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
}

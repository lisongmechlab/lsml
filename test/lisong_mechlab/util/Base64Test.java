package lisong_mechlab.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

public class Base64Test{
   @Test
   public void testEncode() throws DecodingException{
      String input = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
      String expected = "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";

      Base64 base64 = new Base64();
      String ans = new String(base64.encode(input.getBytes()));
      assertEquals(expected, ans);
   }

   @Test
   public void testEncodeDecode() throws DecodingException{
      String input = "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
      Base64 base64 = new Base64();
      assertEquals(input, new String(base64.decode(base64.encode(input.getBytes()))));
   }

   @Test
   public void testEncodeDecodeRawData() throws DecodingException{
      final int numbytes = 100;
      byte[] input = new byte[numbytes];
      Random random = new Random(0);
      random.nextBytes(input);

      Base64 base64 = new Base64();
      assertArrayEquals(input, base64.decode(base64.encode(input)));
   }

   @Test
   public void testEncodePadding() throws DecodingException{
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

      Base64 base64 = new Base64();
      assertEquals(o1, new String(base64.encode(i1.getBytes())));
      assertEquals(o2, new String(base64.encode(i2.getBytes())));
      assertEquals(o3, new String(base64.encode(i3.getBytes())));
      assertEquals(o4, new String(base64.encode(i4.getBytes())));
      assertEquals(o5, new String(base64.encode(i5.getBytes())));
   }

   @Test
   public void testEncodeDecodePadding() throws DecodingException{
      String i1 = "any carnal pleasure.";
      String i2 = "any carnal pleasure";
      String i3 = "any carnal pleasur";
      String i4 = "any carnal pleasu";
      String i5 = "any carnal pleas";

      Base64 base64 = new Base64();
      assertEquals(i1, new String(base64.decode(base64.encode(i1.getBytes()))));
      assertEquals(i2, new String(base64.decode(base64.encode(i2.getBytes()))));
      assertEquals(i3, new String(base64.decode(base64.encode(i3.getBytes()))));
      assertEquals(i4, new String(base64.decode(base64.encode(i4.getBytes()))));
      assertEquals(i5, new String(base64.decode(base64.encode(i5.getBytes()))));
   }
}

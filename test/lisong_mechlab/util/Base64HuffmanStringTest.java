package lisong_mechlab.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class Base64HuffmanStringTest{

   public static void main(String[] a){
      byte aa = (byte)0xAA;
      byte x = (byte)(aa >>> 2);
      byte y = (byte)(aa >> 2);

      System.out.println(aa);
      System.out.println(x);
      System.out.println(y);
   }

   /**
    * This will test if a String object will survive being compressed with Huffman, the output encoded with base64 and
    * then decompressed.
    * 
    * @throws DecodingException
    */
   @Test
   public void testEncodeDecode() throws DecodingException{
      String input = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
      List<Character> i = new ArrayList<>();
      Map<Character, Integer> freq = new TreeMap<>();
      for(char c : input.toCharArray()){
         i.add(c);
         if( freq.containsKey(c) ){
            freq.put(c, freq.get(c) + 1);
         }
         else
            freq.put(c, 1);
      }

      Huffman1<Character> huff = new Huffman1<>(freq, '\0');
      Base64 base64 = new Base64();

      char[] encoded = base64.encode(huff.encode(i));
      assertTrue("Encoded length: " + encoded.length + " bytes, source length: " + input.length() + " bytes.", encoded.length < input.length() * 0.7);

      List<Character> o = huff.decode(base64.decode(encoded));
      assertArrayEquals(i.toArray(), o.toArray());
   }
}

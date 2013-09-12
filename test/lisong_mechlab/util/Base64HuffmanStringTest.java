package lisong_mechlab.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

/**
 * This is an integration test between {@link Base64} and {@link Huffman1} simply to see that they will play nice with
 * each other.
 * 
 * @author Emily Björk
 */
public class Base64HuffmanStringTest{
   /**
    * This will test if a String object will survive being compressed with Huffman, the output encoded with base64 and
    * then decompressed.
    * 
    * @throws DecodingException
    * @throws EncodingException
    */
   @Test
   public void testEncodeDecode() throws DecodingException, EncodingException{
      // Setup
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
 
      // Execute
      Huffman1<Character> huff = new Huffman1<>(freq, '\0');
      Base64 base64 = new Base64();
      char[] encoded = base64.encode(huff.encode(i));
      List<Character> o = huff.decode(base64.decode(encoded));
      
      // Verify
      assertArrayEquals(i.toArray(), o.toArray());
      assertTrue("Encoded length: " + encoded.length + " bytes, source length: " + input.length() + " bytes.", encoded.length < input.length() * 0.7);
   }
}

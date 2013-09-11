package lisong_mechlab.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import lisong_mechlab.util.Huffman1;

import org.junit.Test;
import static org.junit.Assert.*;

public class Huffman1Test{

   @Test
   public void testEncodeDecodeSimple() throws DecodingException{
      // Generate a deterministically random sequence of Gaussian numbers and the frequency of each number.
      List<Integer> values = new ArrayList<>();
      values.add(1);
      values.add(2);
      values.add(3);
      Map<Integer, Integer> freqs = new TreeMap<>();
      freqs.put(1, 1);
      freqs.put(2, 3);
      freqs.put(3, 2);

      Huffman1<Integer> huffman1 = new Huffman1<Integer>(freqs, null);

      byte[] encoded = huffman1.encode(values);
      List<Integer> ans = huffman1.decode(encoded);
      assertArrayEquals(values.toArray(), ans.toArray());
   }

   @Test
   public void testEncodeDecodeSimpleMultiByte() throws DecodingException{
      // Generate a deterministically random sequence of Gaussian numbers and the frequency of each number.
      List<Integer> values = new ArrayList<>();
      values.add(1);
      values.add(1);
      values.add(2);
      values.add(1);
      values.add(3);
      values.add(1);
      Map<Integer, Integer> freqs = new TreeMap<>();
      freqs.put(1, 1);
      freqs.put(2, 3);
      freqs.put(3, 2);

      Huffman1<Integer> huffman1 = new Huffman1<Integer>(freqs, null);

      byte[] encoded = huffman1.encode(values);
      List<Integer> ans = huffman1.decode(encoded);
      assertArrayEquals(values.toArray(), ans.toArray());
   }

   @Test
   public void testEncodeDecode() throws DecodingException{
      // Generate a deterministically random sequence of Gaussian numbers and the frequency of each number.
      Random random = new Random(0);
      int numSamples = 1400;
      List<Integer> values = new ArrayList<>();
      Map<Integer, Integer> freqs = new TreeMap<>();
      for(int i = 0; i < numSamples; ++i){
         int v = (int)(random.nextGaussian() * numSamples / 100.0);
         values.add(v);
         if( freqs.containsKey(v) ){
            freqs.put(v, freqs.get(v) + 1);
         }
         else{
            freqs.put(v, 1);
         }
      }

      // Calculate Shannon limit using Shannon's source coding theorem
      final double shannonLimit;
      {
         double sourceEntropy = 0;
         int totalFreq = 0;
         for(int i : freqs.keySet()){
            totalFreq += freqs.get(i);
         }
         for(int i : freqs.keySet()){
            double p = (double)freqs.get(i) / (double)totalFreq;
            sourceEntropy += -(Math.log(p) / Math.log(2)) * p;
         }
         shannonLimit = sourceEntropy * numSamples;
      }

      // Execute
      Huffman1<Integer> huffman1 = new Huffman1<Integer>(freqs, null);
      byte[] encoded = huffman1.encode(values);
      List<Integer> ans = huffman1.decode(encoded);

      // Verify data integrity
      assertArrayEquals(values.toArray(), ans.toArray());
      // Verify that we're within 1% of Shannon limit
      assertTrue("Actual entropy: " + encoded.length * 8 + " bits, actual calculated entropy: " + shannonLimit + " bits",
                 encoded.length * 8 < shannonLimit * 1.01);
   }
}

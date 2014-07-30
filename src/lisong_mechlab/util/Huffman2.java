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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * A Huffman source symbol coder/decoder.
 * <p>
 * Compared to {@link Huffman1} this class defines a strict, stable ordering of nodes in the tree to promote
 * portability.
 * 
 * @author Emily Björk
 * @param <T>
 *           The type of the symbols that shall be encoded. T must be {@link Comparable}.
 */
public class Huffman2<T extends Comparable<T>> {
   private static class Leaf<TT> extends Node{
      static final long LONG_HIGH_BIT = ((long)1 << (Long.SIZE - 1));
      final TT          symbol;
      long              prefix;
      int               prefixSize;

      void createPrefix(){
         prefix = 0;
         prefixSize = 0;
         Node n = this;
         while( n.parent != null ){
            prefix = (prefix >>> 1);
            if( n.parent.childOne == n ){
               prefix |= LONG_HIGH_BIT;
            }
            prefixSize++;
            if( prefixSize > Long.SIZE ){
               throw new IllegalArgumentException("The source data would generate a prefix code with more than 64 bits, we do not support this yet!");
            }
            n = n.parent;
         }
      }

      Leaf(TT aSymbol, int aFrequency, int aTieBreaker){
         super(aFrequency, aTieBreaker);
         symbol = aSymbol;
      }
   }

   private static class Branch extends Node{
      final Node childZero;
      final Node childOne;

      Branch(Node aLeftChild, Node aRightChild, int aTieBreaker){
         super(aLeftChild.frequency + aRightChild.frequency, aTieBreaker);
         childZero = aLeftChild;
         childOne = aRightChild;
         childZero.parent = this;
         childOne.parent = this;
      }
   }

   private static class Node implements Comparable<Node>{
      final int frequency;
      final int tieBreaker;
      Branch    parent;

      Node(int aFrequency, int aTieBreaker){
         frequency = aFrequency;
         tieBreaker = aTieBreaker;
      }

      @Override
      public int compareTo(Node that){
         int v = Integer.compare(this.frequency, that.frequency);
         if(v == 0)
            return Integer.compare(this.tieBreaker, that.tieBreaker);
         return v;
      }
   }

   private final Map<T, Leaf<T>> leafs       = new TreeMap<>();
   private final Node            root;
   private final Leaf<T>         stopLeaf;
   private final double          sourceEntropy;

   private int                   nodeCounter = 0;

   /**
    * Instantiates a new Huffman coder using the given frequency table to generate codewords.
    * <p>
    * The top symbol is treated specially. It must be a symbol that does not occur in the frequency table and it may be
    * <code>null</code> iff <code>null</code> is not in the frequency table.
    * <p>
    * If the encoded bit stream is not an even multiple of 8 bits, the encoder fills the remaining bits in the last byte
    * with a stop symbol. If the stop symbol's codeword didn't fit into the remaining bits, a partial stop symbol is
    * encoded and no extra byte is emitted. When decoding the stream and the decoder encounters a fully encoded stop
    * symbol, it stops the decoding and returns the decoded data up to the stop symbol. If the stop symbol was only
    * partially encoded the decoder will stop before fully decoding the symbol. In both cases the output has the correct
    * number of symbols.
    * 
    * @param aSymbolFrequencyTable
    *           A {@link Map} where each symbol <code>T</code> has a frequency associated with it. These frequencies are
    *           used to generate symbol probabilities.
    * @param aStopSymbol
    *           The special symbol to use as a stop symbol. Must not be represented in aSymbolFrequencyTable. Can be
    *           <code>null</code> iff <code>null</code> is not represented in aSymbolFrequencyTable.
    * @throws IllegalArgumentException
    *            Thrown if <code>aStopSymbol</code> exists in the <code>aSymbolFrequencyTable</code> {@link Map}.
    */
   public Huffman2(Map<T, Integer> aSymbolFrequencyTable, T aStopSymbol) throws IllegalArgumentException{
      if( aStopSymbol != null && aSymbolFrequencyTable.containsKey(aStopSymbol) ){
         throw new IllegalArgumentException("Frequency table contains stop symbol!");
      }

      sourceEntropy = calculateEntropy(aSymbolFrequencyTable);

      List<Map.Entry<T, Integer>> frequencies = new LinkedList<>(aSymbolFrequencyTable.entrySet());
      Collections.sort(frequencies, new Comparator<Map.Entry<T, Integer>>(){
         @Override
         public int compare(Map.Entry<T, Integer> aO1, Map.Entry<T, Integer> aO2){
            return aO1.getKey().compareTo(aO2.getKey());
         }
      }); // Order frequencies by natural ordering of the symbols.

      // Populate initial forest of leaves and the leaves map
      PriorityQueue<Node> forest = new PriorityQueue<>(aSymbolFrequencyTable.size());
      for(Map.Entry<T, Integer> pair : frequencies){
         if( pair.getValue() < 1 )
            continue;
         final Leaf<T> leaf = new Leaf<T>(pair.getKey(), pair.getValue(), nodeCounter);
         nodeCounter++;
         forest.offer(leaf);
         leafs.put(pair.getKey(), leaf);
      }
      // Some implementations of map do not allow null as a key, and stopLeaf can be null so don't add it to leaves.
      stopLeaf = new Leaf<T>(aStopSymbol, 0, nodeCounter);
      nodeCounter++;
      forest.offer(stopLeaf);

      // Create Huffman tree
      while( forest.size() > 1 ){
         forest.offer(new Branch(forest.poll(), forest.poll(), nodeCounter));
         nodeCounter++;
      }
      root = forest.poll();

      // Pre-calculate all the prefix codes for the leaves
      for(Leaf<T> leaf : leafs.values()){
         leaf.createPrefix();
         
//         int prefixlen = leaf.prefixSize;
//         long prefix = leaf.prefix;
//         String p = "";
//         while(prefixlen > 0){
//            p += ((prefix & Leaf.LONG_HIGH_BIT) != 0) ? "1" : "0";
//            prefix <<= 1;
//            prefixlen--;
//         }
//         System.out.println("["+leaf.symbol+"]="+p);
      }
      stopLeaf.createPrefix();
      
//      int prefixlen = stopLeaf.prefixSize;
//      long prefix = stopLeaf.prefix;
//      String p = "";
//      while(prefixlen > 0){
//         p += ((prefix & Leaf.LONG_HIGH_BIT) != 0) ? "1" : "0";
//         prefix <<= 1;
//         prefixlen--;
//      }
//      System.out.println("[STOP]="+p);
   }

   /**
    * Encodes a {@link List} of symbols, in order, into a bit stream using Huffman codes.
    * 
    * @param aSymbolList
    *           A {@link List} of symbols to be encoded.
    * @return The encoded bit stream as an array of <code>byte</code>s.
    * @throws EncodingException
    *            Thrown if <code>aSymbolList</code> contains a symbol which doesn't exist in the frequency table given
    *            to the constructor.
    */
   public byte[] encode(List<T> aSymbolList) throws EncodingException{
      // ----------------------------------------------------------------------
      // WARNING: THE OUTPUT BITSTREAM OF THIS METHOD MUST NOT CHANGE TO
      // PERSERVE COMPATIBILITY WITH PREVIOUSLY ENCODED STREAMS.
      //
      // IF YOU NEED A DIFFERENT BITSTREAM, IMPLEMENT A NEW CODER!
      // ----------------------------------------------------------------------
      final byte[] output = new byte[estimateRequiredBufferSize(aSymbolList)];
      int bytes = 0;
      int bits = 0;

      // Handle a stop symbol at end of the list without making a copy
      for(int i = 0; i < aSymbolList.size() + 1; ++i){
         final T symbol;
         final Leaf<T> node;
         // Detect if we're coding the stop symbol, it is not in the leafs map as it may have a null symbol.
         if( i < aSymbolList.size() ){
            symbol = aSymbolList.get(i);
            node = leafs.get(symbol);
         }
         else{
            symbol = stopLeaf.symbol;
            node = stopLeaf;
         }

         if( node == null ){
            throw new EncodingException("Error encoding symbol [" + symbol.toString() + "]! It was not present in the frequency map!");
         }

         // Don't emit a stop byte if the stream is compact.
         if( node == stopLeaf && bits == 0 )
            break;

         final long prefix = node.prefix;
         int prefixBitsLeft = node.prefixSize;

         while( prefixBitsLeft > 0 ){
            int prefixOffset = Long.SIZE - Byte.SIZE + bits - (node.prefixSize - prefixBitsLeft);
            output[bytes] = (byte)(output[bytes] | (prefix >>> prefixOffset));
            int bitsWritten = Math.min(prefixBitsLeft, Byte.SIZE - bits);
            prefixBitsLeft -= bitsWritten;
            bits += bitsWritten;
            if( bits == Byte.SIZE ){
               bits = 0;
               bytes++;
               // If we're encoding the stop symbol, don't emit any more bytes as we are sure to not get junk when
               // decoding anyway.
               if( node == stopLeaf )
                  break;
            }
         }
      }
      if(bits != 0){
         bytes++;
      }
      return Arrays.copyOf(output, bytes);
   }

   /**
    * Decodes a given bit stream into a {@link List} of symbols.
    * 
    * @param aBitstream
    *           The bit stream to decode.
    * @return A {@link List} of symbols decoded (excluding the stop symbol)
    * @throws DecodingException
    *            Thrown if the bit stream is broken.
    */
   public List<T> decode(final byte[] aBitstream) throws DecodingException{
      List<T> output = new ArrayList<>();
      Node n = root;
      boolean stop = false;
      for(byte b : aBitstream){
         for(int i = 0; i < Byte.SIZE; ++i){
            boolean one = (b >>> (Byte.SIZE - 1)) != 0;
            b = (byte)(b << 1);

            if( one ){
               if( n instanceof Branch ){
                  n = ((Branch)n).childOne;
               }
               else
                  throw new DecodingException("The bitstream is corrupt!");
            }
            else{
               if( n instanceof Branch ){
                  n = ((Branch)n).childZero;
               }
               else
                  throw new DecodingException("The bitstream is corrupt!");
            }

            if( n instanceof Leaf ){
               @SuppressWarnings("unchecked")
               T symbol = ((Leaf<T>)n).symbol;
               if( symbol == stopLeaf.symbol ){
                  stop = true;
                  break;
               }
               output.add(symbol);
               n = root;
            }
         }
         if( stop )
            break;
      }
      return output;
   }

   /**
    * Determines a buffer size that will be large enough to hold the result from encoding the given list of symbols with
    * this encoder. This method basically takes twice the result of Shannon's source coding theorem.
    * 
    * @param aSymbolList
    *           The list of symbols that should be encoded into the buffer.
    * @return An <code>int</code> with number of <code>byte</code>s the intermediary buffer needs to be.
    */
   private int estimateRequiredBufferSize(List<T> aSymbolList){
      final double shannonLimit = sourceEntropy * aSymbolList.size();
      return (int)((shannonLimit + Byte.SIZE) / Byte.SIZE * 2);
   }

   /**
    * Calculates the entropy of the given symbol frequency table.
    * 
    * @param aSymbolFrequencyTable
    *           The table to calculate the entropy for.
    * @return An entropy value in bits-per-symbol.
    */
   private double calculateEntropy(Map<T, Integer> aSymbolFrequencyTable){
      // Calculate source entropy
      int numSamples = 0;
      for(int freq : aSymbolFrequencyTable.values()){
         numSamples += freq;
      }
      final double log2 = Math.log(2);
      double entropy = 0;
      for(int freq : aSymbolFrequencyTable.values()){
         double p = (double)freq / (double)numSamples;
         entropy += -(Math.log(p) / log2) * p;
      }
      return entropy;
   }
}

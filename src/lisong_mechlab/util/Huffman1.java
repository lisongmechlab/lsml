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
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * A Huffman source symbol coder/decoder.
 * 
 * @author Emily Björk
 */
public class Huffman1<T> {
   private static class Leaf<TT> extends Node{
      static final long LONG_HIGH_BIT = ((long)1 << 63);
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
            n = n.parent;
         }
      }

      Leaf(TT aSymbol, int aFrequency){
         super(aFrequency);
         symbol = aSymbol;
      }
   }

   private static class Branch extends Node{
      final Node childZero;
      final Node childOne;

      Branch(Node aRightChild, Node aLeftChild){
         super(aLeftChild.frequency + aRightChild.frequency);
         childZero = aLeftChild;
         childOne = aRightChild;
         childZero.parent = this;
         childOne.parent = this;
      }
   }

   private static class Node implements Comparable<Node>{
      final int frequency;
      Branch    parent;

      Node(int aFrequency){
         frequency = aFrequency;
      }

      @Override
      public int compareTo(Node that){
         return Integer.compare(this.frequency, that.frequency);
      }
   }

   private final Map<T, Leaf<T>> leafs = new TreeMap<>();
   private final Node            root;
   private final Leaf<T>         stopLeaf;

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
   public Huffman1(Map<T, Integer> aSymbolFrequencyTable, T aStopSymbol) throws IllegalArgumentException{
      if( aStopSymbol != null && aSymbolFrequencyTable.containsKey(aStopSymbol) ){
         throw new IllegalArgumentException("Frequency table contains stop symbol!");
      }

      // Populate initial forest of leaves and the leaves map
      PriorityQueue<Node> forest = new PriorityQueue<>(aSymbolFrequencyTable.size());
      for(Map.Entry<T, Integer> pair : aSymbolFrequencyTable.entrySet()){
         if( pair.getValue() < 1 )
            continue;
         final Leaf<T> leaf = new Leaf<T>(pair.getKey(), pair.getValue());
         forest.offer(leaf);
         leafs.put(pair.getKey(), leaf);
      }
      stopLeaf = new Leaf<T>(aStopSymbol, 0);
      forest.offer(stopLeaf);

      // Create Huffman tree
      while( forest.size() > 1 ){
         forest.offer(new Branch(forest.poll(), forest.poll()));
      }
      root = forest.poll();

      // Pre-calculate all the prefix codes for the leaves
      for(Leaf<T> leaf : leafs.values()){
         leaf.createPrefix();
      }
      stopLeaf.createPrefix();
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

      final byte[] output = new byte[aSymbolList.size() * 2];
      int bytes = 0;
      int bits = 0;
      for(T symbol : aSymbolList){
         Leaf<T> node = leafs.get(symbol);
         if( node == null ){
            throw new EncodingException("Error encoding symbol [" + symbol.toString() + "]! It was not present in the frequency map!");
         }

         long prefix = node.prefix;
         int prefixBitsLeft = node.prefixSize;

         while( prefixBitsLeft > 0 ){
            int prefixOffset = 56 + bits - (node.prefixSize - prefixBitsLeft);
            output[bytes] = (byte)(output[bytes] | (prefix >>> prefixOffset));
            int bitsWritten = Math.min(prefixBitsLeft, 8 - bits);
            prefixBitsLeft -= bitsWritten;
            bits += bitsWritten;
            if( bits == 8 ){
               bits = 0;
               bytes++;
            }
         }
      }

      if( bits != 0 ){
         // Fill the rest with a (possibly partially) encoded stop symbol.
         long prefix = stopLeaf.prefix;
         int prefixBitsLeft = stopLeaf.prefixSize;

         while( prefixBitsLeft > 0 ){
            int prefixOffset = 56 + bits - (stopLeaf.prefixSize - prefixBitsLeft);
            output[bytes] = (byte)(output[bytes] | (prefix >>> prefixOffset));
            int bitsWritten = Math.min(prefixBitsLeft, 8 - bits);
            prefixBitsLeft -= bitsWritten;
            bits += bitsWritten;
            if( bits == 8 ){
               bits = 0;
               break; // Don't emit another byte to contain the stop symbol.
            }
         }
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
         for(int i = 0; i < 8; ++i){
            int one = (b & 0x80) >> 7;
            b = (byte)(b << 1);

            if( one == 1 ){
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
}

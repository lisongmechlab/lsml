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
 * @author Li Song
 */
public class Huffman1<T> {
   private static class Leaf<TT> extends Node{
      private static final long LONG_HIGH_BIT = ((long)1 << 63);
      
      final TT                  symbol;
      long                      prefix;
      int                       prefixSize;

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

      public Leaf(TT aSymbol, int aFrequency){
         super(aFrequency);
         symbol = aSymbol;
      }
   }

   private static class Branch extends Node{
      final Node childZero;
      final Node childOne;

      /**
       * Right child first guarantees that the lowest priority node is 11...1
       * 
       * @param aRightChild
       * @param aLeftChild
       */
      public Branch(Node aRightChild, Node aLeftChild){
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

   public Huffman1(Map<T, Integer> aSymbolFrequency, T aStopSymbol){
      // Populate initial forest of leaves and the leaves map
      PriorityQueue<Node> forest = new PriorityQueue<>(aSymbolFrequency.size());
      for(Map.Entry<T, Integer> pair : aSymbolFrequency.entrySet()){
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
    *           The list of symbols to be encoded.
    * @return The encoded bit stream as an array of <code>byte</code>s.
    */
   public byte[] encode(List<T> aSymbolList){
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
         // Fill the rest with a (possibly partially) decoded stop symbol.
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
                  throw new DecodingException();
            }
            else{
               if( n instanceof Branch ){
                  n = ((Branch)n).childZero;
               }
               else
                  throw new DecodingException();
            }

            if( n instanceof Leaf ){
               final Leaf<T> leaf;
               try{
                  @SuppressWarnings("unchecked")
                  Leaf<T> leaf1 = (Leaf<T>)n;
                  leaf = leaf1;
               }
               catch( ClassCastException e ){
                  throw new DecodingException();
               }
               T symbol = leaf.symbol;
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

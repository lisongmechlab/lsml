package lisong_mechlab.util;

/**
 * A pair, much similar in concept to std::pair from c++. Holds two values, the <code>first</code> and
 * <code>second</code>.
 * 
 * @author Li Song
 * @param <F>
 *           The type of the <code>first</code> value.
 * @param <S>
 *           The type of the <code>second</code> value.
 */
public class Pair<F, S> {
   public final F first;
   public final S second;

   public Pair(F aFirst, S aSecond){
      first = aFirst;
      second = aSecond;
   }

   @Override
   public int hashCode(){
      return first.hashCode() ^ second.hashCode();
   }

   @Override
   public boolean equals(Object o){
      if( this == o )
         return true;
      if( !(o instanceof Pair) )
         return false;
      @SuppressWarnings("rawtypes")
      Pair that = (Pair)o;
      return this.first.equals(that.first) && this.second.equals(that.second);
   }

   @Override
   public String toString(){
      return "{" + first.toString() + ", " + second.toString() + "}";
   }
}

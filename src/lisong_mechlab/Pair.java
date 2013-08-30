package lisong_mechlab;

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
      if( !(o instanceof Pair) )
         return false;
      @SuppressWarnings("rawtypes")
      Pair that = (Pair)o;
      return this.first.equals(that.first) && this.second.equals(that.second);
   }
   
   @Override
   public String toString(){
      return "{"+first.toString()+", "+second.toString()+"}";
   }
}

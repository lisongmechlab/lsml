package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

public class TopSpeedMetric extends Metric{
   private final Loadout loadout;

   public TopSpeedMetric(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      Engine engine = loadout.getEngine();
      if( null == engine )
         return 0;

      double maxspeed = calculate(engine.getRating(), loadout.getChassi());
      if( loadout.getEfficiencies().hasSpeedTweak() ){
         maxspeed *= loadout.getEfficiencies().getSpeedModifier();
      }
      return maxspeed;
   }
   
   static public double calculate(int aRating, Chassi aChassi){
      return aChassi.getSpeedFactor() * aRating / aChassi.getMassMax();
   }
}

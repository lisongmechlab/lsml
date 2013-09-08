package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

public class TopSpeed extends Metric{
   private final Loadout loadout;

   public TopSpeed(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      Engine engine = loadout.getEngine();
      if( null == engine )
         return 0;
      return calculate(engine.getRating(), loadout.getChassi(), loadout.getEfficiencies().getSpeedModifier());
   }

   static public double calculate(int aRating, Chassi aChassi, double aModifier){
      return aChassi.getSpeedFactor() * aRating / aChassi.getMassMax() * aModifier;
   }
}

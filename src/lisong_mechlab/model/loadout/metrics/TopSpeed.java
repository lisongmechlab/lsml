package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the maximal speed the loadout can have, taking speed tweak into account.
 * 
 * @author Emily Björk
 */
public class TopSpeed implements Metric{
   private final Loadout loadout;

   public TopSpeed(final Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      Engine engine = loadout.getEngine();
      if( null == engine )
         return 0;
      return calculate(engine.getRating(), loadout.getChassi(), loadout.getEfficiencies().getSpeedModifier());
   }

   /**
    * Performs the actual calculation. This has been extracted because there are situations where the maximal speed is
    * needed without having a {@link Loadout} at hand.
    * 
    * @param aRating
    *           The engine rating.
    * @param aChassi
    *           The chassi the speed is for (determines speed factor).
    * @param aModifier
    *           A modifier to use, 1.0 for normal and 1.1 for speed tweak.
    * @return The speed in [km/h].
    */
   static public double calculate(final int aRating, final Chassi aChassi, final double aModifier){
      return aChassi.getSpeedFactor() * aRating / aChassi.getMassMax() * aModifier;
   }
}

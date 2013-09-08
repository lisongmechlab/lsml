package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the effectiveness of the {@link Loadout}'s cooling. A ratio of 0.0 means no heat is
 * dissipated. A ratio of 1.0 means all generated heat is dissipated. A ratio of > 1.0 means the mech has too much
 * cooling.
 * 
 * @author Emily Björk
 */
public class CoolingRatio implements Metric{
   private final HeatDissipation dissipation;
   private final HeatGeneration  generation;

   public CoolingRatio(final HeatDissipation aDissipation, final HeatGeneration aHeatGeneration){
      dissipation = aDissipation;
      generation = aHeatGeneration;
   }

   @Override
   public double calculate(){
      final double generatedHeat = generation.calculate();
      if( generatedHeat <= 0 ){
         return 1.0;
      }
      return dissipation.calculate() / generatedHeat;
   }
}

package lisong_mechlab.model.loadout.metrics;

/**
 * This {@link Metric} calculates the number of seconds a mech can shoot all weapons before it over heats.
 * 
 * @author Li Song
 */
public class TimeToOverHeat implements Metric{
   private final HeatCapacity    capacity;
   private final HeatDissipation dissipation;
   private final HeatGeneration  generation;

   public TimeToOverHeat(final HeatCapacity aCapacity, final HeatDissipation aDissipation, final HeatGeneration aHeatGeneration){
      capacity = aCapacity;
      dissipation = aDissipation;
      generation = aHeatGeneration;
   }

   @Override
   public double calculate(){
      final double heatDifferential = generation.calculate() - dissipation.calculate();
      final double heatCapacity = capacity.calculate();
      if( heatDifferential <= 0 || heatCapacity / heatDifferential >= 15 * 60 ){ // 15min = infinity in MWO
         return Double.POSITIVE_INFINITY;
      }
      return heatCapacity / heatDifferential;
   }

}

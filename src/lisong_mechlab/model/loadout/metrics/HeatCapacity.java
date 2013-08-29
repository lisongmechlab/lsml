package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.loadout.Loadout;

public class HeatCapacity extends Metric{
   private final Loadout       loadout;
   private static final double MECH_BASE_HEAT_CAPACITY = 30;

   public HeatCapacity(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      double ans = 0;
      int enginehs = 0;
      if( loadout.getEngine() != null ){
         enginehs = loadout.getEngine().getNumInternalHeatsinks();
      }

      // Engine internal HS count as true doubles
      ans += enginehs * (loadout.getUpgrades().hasDoubleHeatSinks() ? 2 : 1);
      // Other doubles count as 1.4
      ans += (loadout.getHeatsinksCount() - enginehs) * (loadout.getUpgrades().hasDoubleHeatSinks() ? 1.4 : 1);
      return (MECH_BASE_HEAT_CAPACITY + ans) * loadout.getEfficiencies().getHeatCapacityModifier();
   }
}

package lisong_mechlab.model.item;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;


public class HeatSink extends Module{
   private final double dissapation;
   private final double capacity;
   
   public HeatSink(ItemStatsModule aStatsModule){
      super(aStatsModule);
      dissapation = aStatsModule.HeatSinkStats.cooling;
      capacity = -aStatsModule.HeatSinkStats.heatbase;
   }

   public double getDissapation(){
      return dissapation;
   }

   public double getCapacity(){
      return capacity;
   }
   
   public boolean isDouble(){
      return capacity > 1.00001; // Account for double precision
   }
   
   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getUpgrades().hasDoubleHeatSinks() == isDouble();
   }
}

package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStats;

public class HeatSource extends Item{
   protected final double heat;
   
   protected HeatSource(ItemStats anItemStats, HardpointType aHardpointType, int aSlots, double aTons, double aHeat){
      super(anItemStats, aHardpointType, aSlots, aTons);
      heat = aHeat;
   }

   public double getHeat(){
      return heat;
   }
}

package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class Module extends Item{
   public Module(ItemStatsModule aModule){
      super(aModule, HardpointType.NONE, aModule.ModuleStats.slots, aModule.ModuleStats.tons);
   }
   public Module(ItemStatsModule aModule, HardpointType hardpoint){
      super(aModule, hardpoint, aModule.ModuleStats.slots, aModule.ModuleStats.tons);
   }
}

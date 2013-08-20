package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class ECM extends Module{

   public ECM(ItemStatsModule aModule){
      super(aModule, HardpointType.ECM);
   }
   
   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getHardpointsCount(getHardpointType()) > 0;
   }
}

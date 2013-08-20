package lisong_mechlab.model.item;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class JumpJet extends Module{
   private double minTons;
   private double maxTons;
   
   public JumpJet(ItemStatsModule aModule){
      super(aModule);
      minTons = aModule.JumpJetStats.minTons;
      maxTons = aModule.JumpJetStats.maxTons;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getMaxJumpJets() > 0 && aLoadout.getChassi().getMassMax() < maxTons && aLoadout.getChassi().getMassMax() >= minTons;
   }
}

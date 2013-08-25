package lisong_mechlab.model.item;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class JumpJet extends Module{
   private final double minTons;
   private final double maxTons;
   private final double boost_z;
   private final double duration;
   private final double heat;
   
   public JumpJet(ItemStatsModule aModule){
      super(aModule);
      minTons = aModule.JumpJetStats.minTons;
      maxTons = aModule.JumpJetStats.maxTons;
      boost_z = aModule.JumpJetStats.boost;
      duration = aModule.JumpJetStats.duration;
      heat = aModule.JumpJetStats.heat;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getMaxJumpJets() > 0 && aLoadout.getChassi().getMassMax() < maxTons && aLoadout.getChassi().getMassMax() >= minTons;
   }

   public double getForce(){
      return boost_z;
   }

   public double getDuration(){
      return duration;
   }

   public double getJumpHeat(){
      return heat;
   }
   
}

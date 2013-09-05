package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class Engine extends HeatSource{
   public final static double ENGINE_HEAT_FULL_THROTTLE = 0.2;
   public final static double ENGINE_HEAT_66_THROTTLE   = 0.1;

   protected final int        rating;
   protected final EngineType type;
   final private int          internalHs;
   final private int          heatsinkslots;

   public Engine(ItemStatsModule aStatsModule){
      super(aStatsModule, HardpointType.NONE, 6, aStatsModule.EngineStats.weight, ENGINE_HEAT_FULL_THROTTLE);
      int hs = aStatsModule.EngineStats.heatsinks;
      internalHs = Math.min(10, hs);
      heatsinkslots = hs - internalHs;
      type = (aStatsModule.EngineStats.slots == 12) ? (EngineType.XL) : (EngineType.STD);
      rating = aStatsModule.EngineStats.rating;
   }

   public EngineType getType(){
      return type;
   }

   public int getRating(){
      return rating;
   }

   public int getNumInternalHeatsinks(){
      return internalHs;
   }

   public int getNumHeatsinkSlots(){
      return heatsinkslots;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getEngineMax() >= rating && aLoadout.getChassi().getEngineMin() <= rating;
   }
}

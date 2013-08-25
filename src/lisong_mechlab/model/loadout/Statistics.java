package lisong_mechlab.model.loadout;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.Weapon;

@Deprecated
public class Statistics{
   private static final double MECH_BASE_HEAT_CAPACITY = 30;
   private transient final Loadout loadout;

   public Statistics(Loadout aLoadout){
      loadout = aLoadout;
   }

   public double getTopSpeed(){
      Engine engine = loadout.getEngine();
      if( null == engine )
         return 0;

      double maxspeed = loadout.getChassi().getSpeedFactor() *  engine.getRating() / loadout.getChassi().getMassMax();
      if( loadout.getEfficiencies().hasSpeedTweak() ){
         maxspeed *= loadout.getEfficiencies().getSpeedModifier();
      }
      return maxspeed;
   }

   public double getJumpDistance(){
      JumpJet jj = loadout.getJumpJetType();
      if(jj == null)
         return 0;
      return loadout.getJumpJetCount() * jj.getForce() * jj.getDuration() * jj.getDuration() / (2*loadout.getChassi().getMassMax());
   }

   public double getHeatCapacity(){
      return (MECH_BASE_HEAT_CAPACITY + baseHeat()) * loadout.getEfficiencies().getHeatCapacityModifier();
   }

   public double getHeatDissipation(){
      return baseHeat() / 10.0 * loadout.getEfficiencies().getHeatDissipationModifier();
   }
   
   private double baseHeat(){
      double ans = 0;
      int enginehs = 0;
      if( loadout.getEngine() != null ){
         enginehs = loadout.getEngine().getNumInternalHeatsinks();
      }

      ans += enginehs * (loadout.getUpgrades().hasDoubleHeatSinks() ? 2 : 1); // Engine internal HS count as true doubles
      ans += (loadout.getHeatsinksCount() - enginehs) * (loadout.getUpgrades().hasDoubleHeatSinks() ? 1.4 : 1); // Other doubles count as 1.4
      return ans;
   }
   
   public double getHeatGeneration(){
      double heat = 0;
      for(LoadoutPart part : loadout.getPartLoadOuts()){
         for(Item item : part.getItems()){
            if( item instanceof Weapon){
               heat += ((Weapon)item).getStat("h/s");
            }
         }
      }
      return heat;
   }
  
   public double getTimeToOverHeat(){
      double heatDifferential = getHeatGeneration() - getHeatDissipation();
      if( heatDifferential <= 0  || getHeatCapacity() / heatDifferential >= 15*60 ){ // 15min = infinity in MWO
         return Double.POSITIVE_INFINITY;
      }
      return getHeatCapacity() / heatDifferential;
   }
   
   public double getCoolingRatio(){
      if(getHeatGeneration() <= 0){
         return 1.0;
      }
      return getHeatDissipation() / getHeatGeneration();
   }
}

package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class Ammunition extends Item{
   protected final int           shotsPerTon;
   protected final double        internalDamage;
   protected final int           hp;
   protected final HardpointType type;

   public Ammunition(ItemStatsModule aStatsModule){
      super(aStatsModule, HardpointType.NONE, 1, 1.0);
      hp = aStatsModule.AmmoTypeStats.health;
      internalDamage = aStatsModule.AmmoTypeStats.internalDamage;
      shotsPerTon = aStatsModule.AmmoTypeStats.shotsPerTon;

      if( getName().contains("AC") || getName().contains("GAUSS") || getName().contains("LB") ){
         type = HardpointType.BALLISTIC;
      }
      else if( getName().contains("RM") || getName().contains("NARC") ){
         type = HardpointType.MISSILE;
      }
      else if( getName().contains("AMS") ){
         type = HardpointType.AMS;
      }
      else{
         type = HardpointType.ENERGY;
      }
   }

   public int getShotsPerTon(){
      return shotsPerTon;
   }

   @Override
   public String getName(Upgrades aUpgrades){
      if( getName().contains("LRM") || getName().contains("SRM") ){
         if( aUpgrades.hasArtemis() && !getName().toLowerCase().contains("streak") )
            return super.getName() + " + ARTEMIS";
      }
      return super.getName();
   }

   /**
    * Returns the {@link HardpointType} that the weapon that uses this ammo is using. Useful for colour coding and
    * searching.
    * 
    * @return
    */
   public HardpointType getWeaponHardpointType(){
      return type;
   }

}

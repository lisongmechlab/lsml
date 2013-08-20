package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsAmmoType;

public class Ammunition extends Item{
   protected final int shotsPerTon;
   protected final double internalDamage;
   protected final int hp;

   public Ammunition(ItemStatsAmmoType aAmmoType){
      super(aAmmoType, HardpointType.NONE, 1, 1.0);
      hp = aAmmoType.AmmoTypeStats.health;
      internalDamage = aAmmoType.AmmoTypeStats.internalDamage;
      shotsPerTon = aAmmoType.AmmoTypeStats.shotsPerTon;
   }

   public int getShotsPerTon(){
      return shotsPerTon;
   }

   public String getName(boolean hasArtemis){
      if( getName().contains("LRM") || getName().contains("SRM") ){
         if( hasArtemis && !getName().toLowerCase().contains("streak") )
            return super.getName() + " + ARTEMIS";
      }
      return super.getName();
   }
}

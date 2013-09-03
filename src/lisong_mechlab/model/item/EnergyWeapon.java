package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class EnergyWeapon extends Weapon{
   protected final double burnTime;

   public EnergyWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.ENERGY);
      burnTime = aStatsWeapon.WeaponStats.duration;
   }

   @Override
   public double getSecondsPerShot(){
      if( cycleTime < 0.1 )
         return 0.10375; // Determined on testing grounds: 4000 mg rounds 6min 55s or 415s -> 415/4000 = 0.10375
      return cycleTime + burnTime;
   }
}

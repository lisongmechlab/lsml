package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class EnergyWeapon extends Weapon{
   protected final double burnTime;

   public EnergyWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.ENERGY);
      burnTime = aStatsWeapon.WeaponStats.duration;
   }
}

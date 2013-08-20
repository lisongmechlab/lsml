package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class BallisticWeapon extends AmmoWeapon{
   protected final double projectileSpeed;

   public BallisticWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.BALLISTIC);
      projectileSpeed = aStatsWeapon.WeaponStats.speed;
   }
}

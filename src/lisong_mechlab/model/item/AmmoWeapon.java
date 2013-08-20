package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class AmmoWeapon extends Weapon{
   private final Ammunition ammoType;

   public AmmoWeapon(ItemStatsWeapon aStatsWeapon, HardpointType aHardpointType){
      super(aStatsWeapon, aHardpointType);
      ammoType = (Ammunition)ItemDB.lookup(aStatsWeapon.WeaponStats.ammoType); // MWO Name
   }

   public Ammunition getAmmoType(){
      return ammoType;
   }

}

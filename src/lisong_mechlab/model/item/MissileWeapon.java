package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class MissileWeapon extends AmmoWeapon{
   private static final String ARTEMIS = " + ARTEMIS";
   protected final double      flightSpeed;

   public MissileWeapon(ItemStatsWeapon aStatsWeapon){
      super(aStatsWeapon, HardpointType.MISSILE);
      flightSpeed = aStatsWeapon.WeaponStats.speed;
   }
   
   @Override
   public double getRangeZero(){
      return super.getRangeMin() - Math.ulp(super.getRangeMin())*RANGE_ULP_FUZZ;
   }

   @Override
   public double getRangeMax(){
      // Missile fall off is a bit different from other weapons because long = max.
      // Emulate a steep fall off by nudging max ever so slightly
      return super.getRangeMax() + Math.ulp(super.getRangeMax())*RANGE_ULP_FUZZ; 
   }

   @Override
   public int getNumCriticalSlots(Upgrades aUpgrades){
      if( aUpgrades.hasArtemis() && isArtemisCapable() )
         return super.getNumCriticalSlots() + 1;
      return super.getNumCriticalSlots();
   }

   @Override
   public double getMass(Upgrades aUpgrades){
      if( aUpgrades.hasArtemis() && isArtemisCapable() )
         return super.getMass() + 1.0;
      return super.getMass();
   }

   @Override
   public String getName(Upgrades aUpgrades){
      if( aUpgrades.hasArtemis() && isArtemisCapable() )
         return super.getName() + ARTEMIS;
      return super.getName();
   }
   
   /**
    * Canonizes an item name with respect to MissileWeapon specifics.
    * 
    * @param name
    *           The item name to canonize
    * @return A canonized version of the argument.
    */
   static public String canonize(String name){
      if( name.endsWith(ARTEMIS) )
         return name.replace(ARTEMIS, "");
      return name;
   }

   public boolean isArtemisCapable(){
      return (getName().contains("LRM") || getName().contains("SRM") && !getName().contains("STREAK"));
   }

}

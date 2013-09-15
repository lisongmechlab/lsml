package lisong_mechlab.model.upgrade;

import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * Represents an upgrade to a 'mechs armor.
 * 
 * @author Li Song
 */
public class ArmorUpgrade extends Upgrade{
   private final int    slots;
   private final double armorPerTon;

   public ArmorUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);
      slots = aUpgradeType.UpgradeTypeStats.slots;
      armorPerTon = aUpgradeType.UpgradeTypeStats.pointMultiplier * 16;
   }

   /**
    * @return The number of extra slots required by this upgrade.
    */
   public int getExtraSlots(){
      return slots;
   }

   /**
    * @return The number of points of armor per ton from this armor type.
    */
   public double getArmorPerTon(){
      return armorPerTon;
   }

   /**
    * Calculates the mass of the given amount of armor points.
    * 
    * @param aArmor
    *           The amount of armor.
    * @return
    */
   public double getArmorMass(int aArmor){
      return aArmor / armorPerTon;
   }
}

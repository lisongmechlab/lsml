package lisong_mechlab.model.upgrade;

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * Represents an upgrade to a 'mechs internal structure.
 * 
 * @author Emily Björk
 */
public class StructureUpgrade extends Upgrade{
   private final double internalStructurePct;
   private final int    extraSlots;

   public StructureUpgrade(ItemStatsUpgradeType aUpgradeType){
      super(aUpgradeType);

      internalStructurePct = aUpgradeType.UpgradeTypeStats.pointMultiplier;
      extraSlots = aUpgradeType.UpgradeTypeStats.slots;
   }

   /**
    * @return The number of extra slots that this upgrade requires to be applied.
    */
   public int getExtraSlots(){
      return extraSlots;
   }

   /**
    * Calculates the mass of the internal structure of a mech of the given chassis.
    * 
    * @param aChassis
    *           The chassis to calculate the internal structure mass for.
    * @return The mass of the internal structure.
    */
   public double getStructureMass(Chassi aChassis){
      return (aChassis.getMassMax() + 9) / 10 * 10 * internalStructurePct;
   }
}

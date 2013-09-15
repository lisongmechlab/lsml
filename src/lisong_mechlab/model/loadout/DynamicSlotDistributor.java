package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.Part;

/**
 * This class handles distribution of dynamic slots from Ferro Fibrous armor and Endo Steel internal structure.
 * <p>
 * It only tells you how many slots of each type should be visualized for a given part. It doesn't actually add any
 * thing to those parts.
 * 
 * @author Emily Björk
 */
public class DynamicSlotDistributor{
   private final Loadout loadout;

   /**
    * Creates a new {@link DynamicSlotDistributor} for the given {@link Loadout}.
    * 
    * @param aLoadout
    *           The {@link Loadout} to distribute dynamic slots for.
    */
   public DynamicSlotDistributor(Loadout aLoadout){
      loadout = aLoadout;
   }

   /**
    * Returns the number of dynamic structure slots that should be visualized for the given {@link LoadoutPart}.
    * 
    * @param aPart
    *           The {@link LoadoutPart} to get results for.
    * @return A number of slots to display, can be 0.
    */
   public int getDynamicStructureSlots(LoadoutPart aPart){
      final int structSlots = loadout.getUpgrades().getStructure().getExtraSlots();
      final int armorSlots = loadout.getUpgrades().getArmor().getExtraSlots();
      if( structSlots < 1 )
         return 0;

      final int filled = getCumulativeFreeSlots(aPart.getInternalPart().getType());
      final int freeSlotsInPart = Math.min(aPart.getNumCriticalSlotsFree(), Math.max(0, aPart.getNumCriticalSlotsFree() + filled - armorSlots));
      final int numSlotsToFill = structSlots + armorSlots;
      return Math.min(freeSlotsInPart, Math.max(numSlotsToFill - filled, 0));
   }

   /**
    * Returns the number of dynamic armor slots that should be visualized for the given {@link LoadoutPart}.
    * 
    * @param aPart
    *           The {@link LoadoutPart} to get results for.
    * @return A number of slots to display, can be 0.
    */
   public int getDynamicArmorSlots(LoadoutPart aPart){
      final int armorSlots = loadout.getUpgrades().getArmor().getExtraSlots();
      if( armorSlots < 1 )
         return 0;

      int filled = getCumulativeFreeSlots(aPart.getInternalPart().getType());
      return Math.min(aPart.getNumCriticalSlotsFree(), Math.max(armorSlots - filled, 0));
   }

   /**
    * Gets the number of cumulative free slots up until the argument. Taking priority order into account.
    * 
    * @param aPart
    *           The part to sum up until.
    * @return A cumulative sum of the number of free slots.
    */
   private int getCumulativeFreeSlots(Part aPart){
      int ans = 0;
      for(Part part : Part.leftToRight()){
         if( part == aPart )
            break;
         ans += loadout.getPart(part).getNumCriticalSlotsFree();
      }
      return ans;
   }
}

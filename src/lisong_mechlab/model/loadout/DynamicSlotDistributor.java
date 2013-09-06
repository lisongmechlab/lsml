package lisong_mechlab.model.loadout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.chassi.Part;

public class DynamicSlotDistributor{
   private final Loadout           loadout;
   private static final List<Part> PART_PRIORITY = Collections.unmodifiableList(Arrays.asList(Part.RightArm, Part.RightTorso, Part.RightLeg,
                                                                                              Part.Head, Part.CenterTorso, Part.LeftTorso,
                                                                                              Part.LeftLeg, Part.LeftArm));

   public DynamicSlotDistributor(Loadout aLoadout){
      loadout = aLoadout;
   }

   public int getDynamicStructureSlots(LoadoutPart aPart){
      if( !loadout.getUpgrades().hasEndoSteel() )
         return 0;
      // FIXME: hard coded constants
      final int numFF = loadout.getUpgrades().hasFerroFibrous() ? 14 : 0;

      final int filled = getCumulativeFreeSlots(aPart.getInternalPart().getType());
      final int freeSlotsInPart = Math.min(aPart.getNumCriticalSlotsFree(), Math.max(0, aPart.getNumCriticalSlotsFree() + filled - numFF));
      final int numSlotsToFill = 14 + numFF;
      return Math.min(freeSlotsInPart, Math.max(numSlotsToFill - filled, 0));
   }

   public int getDynamicArmorSlots(LoadoutPart aPart){
      if( !loadout.getUpgrades().hasFerroFibrous() )
         return 0;

      int filled = getCumulativeFreeSlots(aPart.getInternalPart().getType());
      // FIXME: hard coded constant
      final int numSlotsToFill = 14;
      return Math.min(aPart.getNumCriticalSlotsFree(), Math.max(numSlotsToFill - filled, 0));
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
      for(Part part : PART_PRIORITY){
         if( part == aPart )
            break;
         ans += loadout.getPart(part).getNumCriticalSlotsFree();
      }
      return ans;
   }

}

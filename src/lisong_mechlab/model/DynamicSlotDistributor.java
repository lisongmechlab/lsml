/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */  
//@formatter:on
package lisong_mechlab.model;

import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.ConfiguredComponent;

/**
 * This class handles distribution of dynamic slots from Ferro Fibrous armor and Endo Steel internal structure.
 * <p>
 * It only tells you how many slots of each type should be visualized for a given part. It doesn't actually add any
 * thing to those parts.
 * 
 * @author Li Song
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
    * Returns the number of dynamic structure slots that should be visualized for the given {@link ConfiguredComponent}.
    * 
    * @param aPart
    *           The {@link ConfiguredComponent} to get results for.
    * @return A number of slots to display, can be 0.
    */
   public int getDynamicStructureSlots(ConfiguredComponent aPart){
      final int structSlots = loadout.getUpgrades().getStructure().getExtraSlots();
      final int armorSlots = loadout.getUpgrades().getArmor().getExtraSlots();
      if( structSlots < 1 )
         return 0;

      final int filled = getCumulativeFreeSlots(aPart.getInternalPart().getLocation());
      final int freeSlotsInPart = Math.min(aPart.getNumCriticalSlotsFree(), Math.max(0, aPart.getNumCriticalSlotsFree() + filled - armorSlots));
      final int numSlotsToFill = structSlots + armorSlots;
      return Math.min(freeSlotsInPart, Math.max(numSlotsToFill - filled, 0));
   }

   /**
    * Returns the number of dynamic armor slots that should be visualized for the given {@link ConfiguredComponent}.
    * 
    * @param aPart
    *           The {@link ConfiguredComponent} to get results for.
    * @return A number of slots to display, can be 0.
    */
   public int getDynamicArmorSlots(ConfiguredComponent aPart){
      final int armorSlots = loadout.getUpgrades().getArmor().getExtraSlots();
      if( armorSlots < 1 )
         return 0;

      int filled = getCumulativeFreeSlots(aPart.getInternalPart().getLocation());
      return Math.min(aPart.getNumCriticalSlotsFree(), Math.max(armorSlots - filled, 0));
   }

   /**
    * Gets the number of cumulative free slots up until the argument. Taking priority order into account.
    * 
    * @param aPart
    *           The part to sum up until.
    * @return A cumulative sum of the number of free slots.
    */
   private int getCumulativeFreeSlots(Location aPart){
      int ans = 0;
      for(Location part : Location.leftToRight()){
         if( part == aPart )
            break;
         ans += loadout.getPart(part).getNumCriticalSlotsFree();
      }
      return ans;
   }
}

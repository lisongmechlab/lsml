/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model;

import javax.inject.Inject;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.mechs.Location;

/**
 * This class handles distribution of dynamic slots from Ferro-Fibrous armour and Endo-Steel
 * internal structure.
 *
 * <p>It only tells you how many slots of each type should be visualized for a given part. It
 * doesn't actually add anything to those parts.
 *
 * <p>This class will transparently handle the fact that some slots are fixed per location on
 * Omnimechs.
 *
 * @author Li Song
 */
public class DynamicSlotDistributor {
  private final Loadout loadout;

  /**
   * Creates a new {@link DynamicSlotDistributor} for the given {@link Loadout}.
   *
   * @param aLoadout The {@link Loadout} to distribute dynamic slots for.
   */
  @Inject
  public DynamicSlotDistributor(Loadout aLoadout) {
    loadout = aLoadout;
  }

  /**
   * Returns the number of dynamic armour slots that should be visualized for the given {@link
   * ConfiguredComponent}.
   *
   * @param aLocation The {@link Location} to get results for.
   * @return A number of slots to display, can be 0.
   */
  public int getDynamicArmourSlots(Location aLocation) {
    final ConfiguredComponent component = loadout.getComponent(aLocation);
    if (component instanceof ConfiguredComponentOmniMech) {
      return ((ConfiguredComponentOmniMech) component)
          .getInternalComponent()
          .getDynamicArmourSlots();
    }

    final int armourSlots = loadout.getUpgrades().getArmour().getDynamicSlots();
    if (armourSlots < 1) {
      return 0;
    }

    final int freeSlotsUntilThis = getCumulativeFreeSlots(aLocation);

    if (freeSlotsUntilThis >= armourSlots) {
      return 0; // All slots are consumed by prior components
    }

    final int armorSlotsRemaining = armourSlots - freeSlotsUntilThis;
    return Math.min(armorSlotsRemaining, component.getSlotsFree());
  }

  /**
   * Returns the number of dynamic structure slots that should be visualized for the given {@link
   * ConfiguredComponent}.
   *
   * @param aLocation The {@link Location} to get results for.
   * @return A number of slots to display, can be 0.
   */
  public int getDynamicStructureSlots(Location aLocation) {
    final ConfiguredComponent component = loadout.getComponent(aLocation);
    if (component instanceof ConfiguredComponentOmniMech) {
      return ((ConfiguredComponentOmniMech) component)
          .getInternalComponent()
          .getDynamicStructureSlots();
    }

    final int structSlots = loadout.getUpgrades().getStructure().getDynamicSlots();
    if (structSlots < 1) {
      return 0;
    }

    final int thisFreeSlots = component.getSlotsFree();
    final int armourSlots = loadout.getUpgrades().getArmour().getDynamicSlots();
    final int totalDynamicSlots = armourSlots + structSlots;
    final int freeSlotsUntilThis = getCumulativeFreeSlots(aLocation);
    if (freeSlotsUntilThis + thisFreeSlots <= armourSlots) {
      return 0; // All slots are occupied by armour.
    }

    if (freeSlotsUntilThis > totalDynamicSlots) {
      return 0; // No slots are occupied
    }

    if (armourSlots > freeSlotsUntilThis) {
      // Some, but not all, slots are occupied by armour
      final int freeSlotsLeft = thisFreeSlots - (armourSlots - freeSlotsUntilThis);
      return Math.min(structSlots, freeSlotsLeft);
    }

    // No slots are occupied by armour when we come here...
    final int occupiedSlots = totalDynamicSlots - freeSlotsUntilThis;
    return Math.min(occupiedSlots, thisFreeSlots);
  }

  /**
   * Gets the number of cumulative free slots up until the argument. Taking priority order into
   * account.
   *
   * @param aLocation The {@link Location} to sum up until.
   * @return A cumulative sum of the number of free slots.
   */
  private int getCumulativeFreeSlots(Location aLocation) {
    int ans = 0;
    for (final Location part : Location.RIGHT_TO_LEFT) {
      if (part == aLocation) {
        break;
      }
      ans += loadout.getComponent(part).getSlotsFree();
    }
    return ans;
  }
}

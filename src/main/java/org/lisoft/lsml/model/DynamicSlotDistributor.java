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
package org.lisoft.lsml.model;

import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.ConfiguredComponentOmniMech;
import org.lisoft.lsml.model.loadout.Loadout;

import javax.inject.Inject;

/**
 * This class handles distribution of dynamic slots from Ferro Fibrous armour and Endo Steel internal structure.
 * <p>
 * It only tells you how many slots of each type should be visualised for a given part. It doesn't actually add any
 * thing to those parts.
 * <p>
 * This class will transparently handle the fact that some slots are fixed per location on omnimechs.
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
     * Returns the number of dynamic armour slots that should be visualised for the given {@link ConfiguredComponent} .
     *
     * @param aLocation The {@link Location} to get results for.
     * @return A number of slots to display, can be 0.
     */
    public int getDynamicArmourSlots(Location aLocation) {
        final ConfiguredComponent component = loadout.getComponent(aLocation);
        if (component instanceof ConfiguredComponentOmniMech) {
            return ((ConfiguredComponentOmniMech) component).getInternalComponent().getDynamicArmourSlots();
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
        if (armorSlotsRemaining < component.getSlotsFree()) {
            return armorSlotsRemaining; // Only some of the free slots are
            // filled
        }

        return component.getSlotsFree(); // All slots are filled.
    }

    /**
     * Returns the number of dynamic structure slots that should be visualised for the given {@link ConfiguredComponent}
     * .
     *
     * @param aLocation The {@link Location} to get results for.
     * @return A number of slots to display, can be 0.
     */
    public int getDynamicStructureSlots(Location aLocation) {
        final ConfiguredComponent component = loadout.getComponent(aLocation);
        if (component instanceof ConfiguredComponentOmniMech) {
            return ((ConfiguredComponentOmniMech) component).getInternalComponent().getDynamicStructureSlots();
        }

        final int structSlots = loadout.getUpgrades().getStructure().getExtraSlots();
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
            if (structSlots < freeSlotsLeft) {
                return structSlots; // The remainder of the slots are occupied
                // by structure
            }
            return freeSlotsLeft; // The remainder of the slots are only
            // partially occupied by structure.
        }

        // No slots are occupied by armour when we come here...
        final int occupiedSlots = totalDynamicSlots - freeSlotsUntilThis;
        if (occupiedSlots > thisFreeSlots) {
            return thisFreeSlots;
        }
        return occupiedSlots;
    }

    /**
     * Gets the number of cumulative free slots up until the argument. Taking priority order into account.
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

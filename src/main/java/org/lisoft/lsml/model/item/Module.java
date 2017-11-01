/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package org.lisoft.lsml.model.item;

import java.util.List;
import java.util.Optional;

import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;

/**
 * A refinement of {@link Item} for modules.
 *
 * @author Emily Björk
 */
public class Module extends Item {

    private final Integer allowedAmount; // May be null

    public Module(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
            HardPointType aHardpointType, double aHP, Faction aFaction, List<Location> aAllowedLocations,
            List<ChassisClass> aAllowedChassisClasses, Integer aAllowedAmount) {
        super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardpointType, aHP, aFaction, aAllowedLocations,
                aAllowedChassisClasses);
        if (aAllowedAmount == null || aAllowedAmount < 1) {
            allowedAmount = null;
        }
        else {
            allowedAmount = aAllowedAmount;
        }
    }

    /**
     * The data files specify a "amountAllowed" attribute that specifies how many of a specific item "type" you may
     * have. How the type is defined is not clear but I do believe that it is the "CType" attribute.
     *
     * In LSML we map the CType attribute to a specific {@link Item} subclass. To see if you would hit the limit you
     * need to see if the item to be equipped has an {@link #getAllowedAmountOfType()} attribute and then count the
     * number of items for which {@link #isSameTypeAs(Item)} returns true.
     *
     * @return An {@link Optional} {@link Integer} that describes how many of this type of item may be equipped
     *         simultaneously.
     */
    public Optional<Integer> getAllowedAmountOfType() {
        return Optional.ofNullable(allowedAmount);
    }

    public boolean isSameTypeAs(Item aItem) {
        return this.getClass().equals(aItem.getClass());
    }
}

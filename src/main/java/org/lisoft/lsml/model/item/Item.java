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
package org.lisoft.lsml.model.item;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.lisoft.lsml.model.chassi.ChassisClass;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.upgrades.Upgrades;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Item extends MwoObject {
    private final List<ChassisClass> allowedChassisClasses;
    @XStreamImplicit
    private final List<Location> allowedLocations;
    @XStreamAsAttribute
    private final HardPointType hardpointType;
    @XStreamAsAttribute
    private final double health;
    @XStreamAsAttribute
    private final int slots;
    @XStreamAsAttribute
    private final double tons;

    public Item(String aUiName, String aUiDesc, String aMwoName, int aMwoId, int aSlots, double aTons,
                HardPointType aHardpointType, double aHP, Faction aFaction, List<Location> aAllowedLocations,
                List<ChassisClass> aAllowedClasses) {
        super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
        slots = aSlots;
        tons = aTons;
        hardpointType = aHardpointType;
        health = aHP;

        if (aAllowedClasses != null && !aAllowedClasses.isEmpty()) {
            allowedChassisClasses = aAllowedClasses;
        } else {
            allowedChassisClasses = null;
        }

        if (aAllowedLocations != null && !aAllowedLocations.isEmpty()) {
            allowedLocations = aAllowedLocations;
        } else {
            allowedLocations = null;
        }
    }

    /**
     * @return An {@link Optional} {@link Collection} of allowed chassis classes.
     */
    public Optional<Collection<ChassisClass>> getAllowedChassisClasses() {
        if (allowedChassisClasses == null) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableCollection(allowedChassisClasses));
    }

    /**
     * @return An {@link Optional} {@link Collection} of locations on which this item is allowed.
     */
    public Optional<Collection<Location>> getAllowedComponents() {
        if (allowedLocations == null) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableCollection(allowedLocations));
    }

    public HardPointType getHardpointType() {
        return hardpointType;
    }

    public double getHealth() {
        return health;
    }

    public double getMass() {
        return tons;
    }

    public int getSlots() {
        return slots;
    }

    /**
     * This method checks if this {@link Item} can be equipped in combination with the given {@link Upgrades}.
     *
     * @param aUpgrades The {@link Upgrades} to check against.
     * @return <code>true</code> if this {@link Item} is compatible with the given upgrades.
     */
    public boolean isCompatible(Upgrades aUpgrades) {
        return true;
    }

    public boolean isCrittable() {
        return health > 0;
    }

    @Override
    public String toString() {
        return getName();
    }
}

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
package org.lisoft.lsml.model.chassi;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.modifiers.Modifier;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents an omnipod of an omnimech configuration.
 *
 * @author Li Song
 */
public class OmniPod {
    @XStreamAsAttribute
    private final String chassis;
    private final List<Item> fixedItems;
    private final List<HardPoint> hardPoints;
    @XStreamAsAttribute
    private final Location location;
    @XStreamAsAttribute
    private final int maxJumpJets;
    @XStreamAsAttribute
    private final int maxPilotModules;
    @XStreamAsAttribute
    private final int mwoID;
    @XStreamAsAttribute
    private final Collection<Modifier> quirks;
    @XStreamAsAttribute
    private final String series;
    private final List<Item> toggleableItems;
    private final OmniPodSet omniPodSet;

    /**
     * Creates a new {@link OmniPod}.
     *
     * @param aMwoID
     *            The MWO ID of this {@link OmniPod}.
     * @param aLocation
     *            The {@link Location} that this omni pod can be mounted at.
     * @param aSeriesName
     *            The name of the series this {@link OmniPod} belongs to, for example "TIMBER WOLF".
     * @param aOriginalChassisID
     *            The MWO ID of the specific variant that this {@link OmniPod} is part of, for example "TIMBER WOLF
     *            PRIME".
     * @param aOmniPodSet
     *            The {@link OmniPodSet} that this omni pod belongs to.
     * @param aQuirks
     *            A {@link Collection} of {@link Modifier}s this {@link OmniPod} will bring to the loadout if equipped.
     * @param aHardPoints
     *            A {@link List} of {@link HardPoint}s for this {@link OmniPod}.
     * @param aFixedItems
     *            A {@link List} of fixed items on this {@link OmniPod}.
     * @param aToggleableItems
     *            A {@link List} of items in this {@link OmniPod} that may be toggled.
     * @param aMaxJumpJets
     *            The maximum number of jump jets this {@link OmniPod} can support.
     * @param aMaxPilotModules
     *            The number of pilot modules that this {@link OmniPod} adds to the loadout.
     */
    public OmniPod(int aMwoID, Location aLocation, String aSeriesName, String aOriginalChassisID,
            OmniPodSet aOmniPodSet, Collection<Modifier> aQuirks, List<HardPoint> aHardPoints, List<Item> aFixedItems,
            List<Item> aToggleableItems, int aMaxJumpJets, int aMaxPilotModules) {
        mwoID = aMwoID;
        location = aLocation;
        series = aSeriesName.toUpperCase();
        chassis = aOriginalChassisID.toUpperCase();
        omniPodSet = aOmniPodSet;
        quirks = aQuirks;
        hardPoints = aHardPoints;
        maxJumpJets = aMaxJumpJets;
        maxPilotModules = aMaxPilotModules;
        fixedItems = aFixedItems;
        toggleableItems = aToggleableItems;
    }

    @Override
    public boolean equals(Object aObj) {
        if (aObj instanceof OmniPod) {
            return ((OmniPod) aObj).getMwoId() == getMwoId();
        }
        return false;
    }

    /**
     * @return The name of the chassis that this {@link OmniPod} belongs to.
     */
    public String getChassisName() {
        return chassis;
    }

    /**
     * @return The chassis series this {@link OmniPod} is part of. For example "DIRE WOLF".
     */
    public String getChassisSeries() {
        return series;
    }

    /**
     * @return A unmodifiable {@link List} of {@link Item}s that are fixed on this {@link OmniPod}. Typically empty.
     */
    public List<Item> getFixedItems() {
        return Collections.unmodifiableList(fixedItems);
    }

    /**
     * @param aHardpointType
     *            The type of {@link HardPoint}s to count.
     * @return The number of {@link HardPoint}s of the given type.
     */
    public int getHardPointCount(HardPointType aHardpointType) {
        int ans = 0;
        for (final HardPoint it : hardPoints) {
            if (it.getType() == aHardpointType) {
                ans++;
            }
        }
        return ans;
    }

    /**
     * @return An unmodifiable collection of all {@link HardPoint}s this {@link OmniPod} has.
     */
    public Collection<HardPoint> getHardPoints() {
        return Collections.unmodifiableCollection(hardPoints);
    }

    /**
     * @return The maximum number of jump jets one can equip on this omnipod.
     */
    public int getJumpJetsMax() {
        return maxJumpJets;
    }

    /**
     * @return {@link Location} that this omnipod can be equipped on.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @return The MWO ID of this {@link OmniPod}.
     */
    public int getMwoId() {
        return mwoID;
    }

    /**
     * @return The {@link OmniPodSet} that this {@link OmniPod} belongs to.
     */
    public OmniPodSet getOmniPodSet() {
        return omniPodSet;
    }

    /**
     * @return The maximum number of pilot modules this {@link OmniPod} can support.
     */
    public int getPilotModulesMax() {
        return maxPilotModules;
    }

    /**
     * @return The omnipod specific movement quirks.
     */
    public Collection<Modifier> getQuirks() {
        return quirks;
    }

    /**
     * @return A unmodifiable {@link List} of {@link Item}s that are toggleable on this {@link OmniPod}. Typically only
     *         LAA and HA.
     */
    public List<Item> getToggleableItems() {
        return Collections.unmodifiableList(toggleableItems);
    }

    @Override
    public int hashCode() {
        return mwoID;
    }

    /**
     * @return <code>true</code> if this {@link OmniPod} has missile bay doors.
     */
    public boolean hasMissileBayDoors() {
        for (final HardPoint hardPoint : hardPoints) {
            if (hardPoint.hasMissileBayDoor()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param aChassis
     *            The chassis to check for compatibility to.
     * @return <code>true</code> if the argument is a compatible chassis.
     */
    public boolean isCompatible(ChassisOmniMech aChassis) {
        return aChassis.getSeriesName().toUpperCase().equals(series);
    }

    @Override
    public String toString() {
        return getChassisName();
    }
}

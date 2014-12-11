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
package lisong_mechlab.model.chassi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class serves as a generic base for all chassis types (IS/Clan)
 * 
 * @author Li Song
 */
public abstract class ChassisBase {
    @XStreamAsAttribute
    private final int             baseVariant;
    @XStreamAsAttribute
    private final ChassisClass    chassiclass;
    private final ComponentBase[] components;
    @XStreamAsAttribute
    private final Faction         faction;
    @XStreamAsAttribute
    private final int             maxTons;
    private final MovementProfile movementProfile;
    @XStreamAsAttribute
    private final int             mwoId;
    @XStreamAsAttribute
    private final String          mwoName;
    @XStreamAsAttribute
    private final String          name;
    @XStreamAsAttribute
    private final int             mechModules;
    @XStreamAsAttribute
    private final String          series;
    @XStreamAsAttribute
    private final String          shortName;
    @XStreamAsAttribute
    private final ChassisVariant  variant;
    @XStreamAsAttribute
    private final int             consumableModules;
    @XStreamAsAttribute
    private final int             weaponModules;

    /**
     * @param aMwoID
     *            The MWO ID of the chassis as found in the XML.
     * @param aMwoName
     *            The MWO name of the chassis as found in the XML.
     * @param aSeries
     *            The name of the series for example "ORION" or "JENNER".
     * @param aName
     *            The long name of the mech, for example "JENNER JR7-F".
     * @param aShortName
     *            The short name of the mech, for example "JR7-F".
     * @param aMaxTons
     *            The maximum tonnage of the mech.
     * @param aVariant
     *            The variant type of the mech, like hero, champion etc.
     * @param aBaseVariant
     *            The base chassisID that this chassis is based on if any, -1 if not based on any chassis.
     * @param aMovementProfile
     *            The {@link MovementProfile} of this chassis.
     * @param aFaction
     *            The {@link Faction} of this clan.
     * @param aComponents
     *            An array of components for this chassis.
     * @param aMaxMechModules
     *            The maximum number of pilot modules that can be equipped.
     * @param aMaxConsumables
     *            The maximal number of consumable modules this chassis can support.
     * @param aMaxWeaponModules
     *            The maximal number of weapon modules this chassis can support.
     */
    public ChassisBase(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons,
            ChassisVariant aVariant, int aBaseVariant, MovementProfile aMovementProfile, Faction aFaction,
            ComponentBase[] aComponents, int aMaxMechModules, int aMaxConsumables, int aMaxWeaponModules) {
        if (aComponents.length != Location.values().length)
            throw new IllegalArgumentException("Components array must contain all components!");

        mwoId = aMwoID;
        mwoName = aMwoName;
        series = aSeries;
        name = aName;
        shortName = aShortName;
        maxTons = aMaxTons;
        chassiclass = ChassisClass.fromMaxTons(maxTons);
        variant = aVariant;
        baseVariant = aBaseVariant;
        movementProfile = aMovementProfile;
        faction = aFaction;
        components = aComponents;
        mechModules = aMaxMechModules;
        consumableModules = aMaxConsumables;
        weaponModules = aMaxWeaponModules;
    }

    @Override
    public boolean equals(Object aObject) {
        if (!(aObject instanceof ChassisBase))
            return false;
        return (mwoId == ((ChassisBase) aObject).mwoId);
    }

    /**
     * @return The maximal, total amount of armor the chassis can support.
     */
    public int getArmorMax() {
        int ans = 0;
        for (ComponentBase internalPart : components) {
            ans += internalPart.getArmorMax();
        }
        return ans;
    }

    /**
     * @return The ID of the base variant of this chassis, or <code>-1</code> if this is not a derived chassis type.
     */
    public int getBaseVariantId() {
        return baseVariant;
    }

    /**
     * @return The weight class of the chassis.
     */
    public ChassisClass getChassiClass() {
        return chassiclass;
    }

    /**
     * @param aLocation
     *            The location of the internal component we're interested in.
     * @return The internal component in the given location.
     */
    public ComponentBase getComponent(Location aLocation) {
        return components[aLocation.ordinal()];
    }

    /**
     * @return A {@link Collection} of all the internal components.
     */
    public Collection<? extends ComponentBase> getComponents() {
        return Collections.unmodifiableList(Arrays.asList(components));
    }

    /**
     * @return The total number of critical slots on this chassis.
     */
    public int getCriticalSlotsTotal() {
        return 12 * 5 + 6 * 3;
    }

    /**
     * @return The faction that this chassis is from.
     */
    public Faction getFaction() {
        return faction;
    }

    /**
     * @return The maximal tonnage the chassis can support.
     */
    public int getMassMax() {
        return maxTons;
    }

    /**
     * @return The base {@link MovementProfile} for this chassis.
     */
    public MovementProfile getMovementProfileBase() {
        return movementProfile;
    }

    /**
     * @return The MWO internal ID of the chassis.
     */
    public int getMwoId() {
        return mwoId;
    }

    /**
     * @return The MWO internal name of the chassis.
     */
    public String getMwoName() {
        return mwoName;
    }

    /**
     * @return The full, long name of the chassis.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The short, abbreviated name of the chassis.
     */
    public String getNameShort() {
        return shortName;
    }

    /**
     * @return The maximal number of mech modules this chassis can support.
     */
    public int getMechModulesMax() {
        return mechModules;
    }

    /**
     * @return The maximal number of consumable modules this chassis can support.
     */
    public int getConsumableModulesMax() {
        return consumableModules;
    }

    /**
     * @return The maximal number of weapon modules this chassis can support.
     */
    public int getWeaponModulesMax() {
        return weaponModules;
    }

    /**
     * @return The name of the series this {@link ChassisStandard} belongs to, e.g. "CATAPHRACT", "ATLAS" etc.
     */
    public String getSeriesName() {
        return series;
    }

    /**
     * @return The chassis variant of this mech.
     */
    public ChassisVariant getVariantType() {
        return variant;
    }

    @Override
    public int hashCode() {
        return mwoId;
    }

    /**
     * This method checks static, global constraints on an {@link Item}.
     * <p>
     * If this method returns <code>false</code> for an {@link Item}, that item will never be possible to equip on any
     * loadout based on this chassis.
     * 
     * @param aItem
     *            The {@link Item} to check for.
     * @return <code>true</code> if this chassis can, in some configuration, support the {@link Item}.
     */
    public boolean isAllowed(Item aItem) {
        if (!aItem.getFaction().isCompatible(getFaction()))
            return false;

        List<ChassisClass> allowedChassis = aItem.getAllowedChassisClasses();
        if (!(allowedChassis == null || allowedChassis.isEmpty() || allowedChassis.contains(chassiclass))) {
            return false;
        }

        if (aItem instanceof Internal)
            return false;

        if (aItem instanceof JumpJet) {
            JumpJet jj = (JumpJet) aItem;
            return jj.getMinTons() <= getMassMax() && getMassMax() < jj.getMaxTons();
        }

        for (ComponentBase part : getComponents()) {
            if (part.isAllowed(aItem, null))
                return true;
        }
        return false;
    }

    /**
     * @param aChassis
     *            The {@link ChassisBase} to compare to.
     * @return <code>true</code> if this and that chassis are of the same series (i.e. both are Hunchbacks etc).
     */
    public boolean isSameSeries(ChassisBase aChassis) {
        return series.equals(aChassis.series);
    }

    @Override
    public String toString() {
        return getNameShort();
    }
}

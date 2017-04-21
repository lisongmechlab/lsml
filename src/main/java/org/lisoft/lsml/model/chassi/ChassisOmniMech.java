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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.lisoft.lsml.model.database.OmniPodDB;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.util.ListArrayUtils;

/**
 * This class models an omnimech chassis, i.e. the basic attributes associated with the chassis and the center omnipod.
 *
 * @author Li Song
 */
public class ChassisOmniMech extends Chassis {

    private final ArmourUpgrade armourType;
    private final HeatSinkUpgrade heatSinkType;
    private final StructureUpgrade structureType;

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
     *            The faction this chassis belongs to.
     * @param aComponents
     *            An array of components for this chassis.
     * @param aMaxPilotModules
     *            The maximum number of pilot modules that can be equipped.
     * @param aMaxConsumableModules
     *            The maximal number of consumable modules this chassis can support.
     * @param aMaxWeaponModules
     *            The maximal number of weapon modules this chassis can support.
     * @param aStructureType
     *            The structure type that is fixed on this chassis.
     * @param aArmourType
     *            The armour type that is fixed on this chassis.
     * @param aHeatSinkType
     *            The heat sink type that is fixed on this chassis.
     * @param aMascCapable
     *            Whether or not this chassis is MASC capable.
     */
    public ChassisOmniMech(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons,
            ChassisVariant aVariant, int aBaseVariant, MovementProfile aMovementProfile, Faction aFaction,
            ComponentOmniMech[] aComponents, int aMaxPilotModules, int aMaxConsumableModules, int aMaxWeaponModules,
            StructureUpgrade aStructureType, ArmourUpgrade aArmourType, HeatSinkUpgrade aHeatSinkType,
            boolean aMascCapable) {
        super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile,
                aFaction, aComponents, aMaxPilotModules, aMaxConsumableModules, aMaxWeaponModules, aMascCapable);
        structureType = aStructureType;
        armourType = aArmourType;
        heatSinkType = aHeatSinkType;

        int s = 0;
        int a = 0;
        for (final ComponentOmniMech component : getComponents()) {
            s += component.getDynamicStructureSlots();
            a += component.getDynamicArmourSlots();
        }
        if (s != structureType.getExtraSlots()) {
            throw new IllegalArgumentException(
                    "The fixed structure slots in components must sum up the number of slots required by the structure type.");
        }
        if (a != armourType.getExtraSlots()) {
            throw new IllegalArgumentException(
                    "The fixed armour slots in components must sum up the number of slots required by the armour type.");
        }
    }

    @Override
    public ComponentOmniMech getComponent(Location aLocation) {
        return (ComponentOmniMech) super.getComponent(aLocation);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ComponentOmniMech> getComponents() {
        return (Collection<ComponentOmniMech>) super.getComponents();
    }

    /**
     * @return The type of the fixed armour of this omnimech.
     */
    public ArmourUpgrade getFixedArmourType() {
        return armourType;
    }

    /**
     * @return The engine that is fixed to this omnimech chassis.
     */
    public Engine getFixedEngine() {
        for (final Item item : getComponent(Location.CenterTorso).getFixedItems()) {
            if (item instanceof Engine) {
                return (Engine) item;
            }
        }
        throw new IllegalStateException("No engine found in omnimech!");
    }

    /**
     * @return The number of heat sinks that are fixed on this chassis, including the ones in the fixed engine.
     */
    public int getFixedHeatSinks() {
        int ans = getFixedEngine().getNumInternalHeatsinks();
        for (final ComponentOmniMech component : getComponents()) {
            ans += ListArrayUtils.countByType(component.getFixedItems(), HeatSink.class);
        }
        return ans;
    }

    /**
     * @return The type of the fixed heat sinks of this omnimech.
     */
    public HeatSinkUpgrade getFixedHeatSinkType() {
        return heatSinkType;
    }

    /**
     * @return The number of jump jets that are fixed on this chassis.
     */
    public int getFixedJumpJets() {
        int ans = 0;
        for (final ComponentOmniMech component : getComponents()) {
            ans += ListArrayUtils.countByType(component.getFixedItems(), JumpJet.class);
        }
        return ans;
    }

    /**
     * @return The mass of this chassis when all non-fixed components and all armour is removed.
     */
    public double getFixedMass() {
        double ans = structureType.getStructureMass(this);
        for (final ComponentOmniMech component : getComponents()) {
            for (final Item item : component.getFixedItems()) {
                ans += item.getMass();
            }
        }
        return ans;
    }

    /**
     * @return The type of the fixed internal structure of this omnimech.
     */
    public StructureUpgrade getFixedStructureType() {
        return structureType;
    }

    /**
     * @return The {@link MovementProfile} where the {@link OmniPod} for each {@link ComponentOmniMech} is selected to
     *         maximize each attribute. All the values of the {@link MovementProfile} may not be attainable
     *         simultaneously but each value of each attribute is independently attainable for some combination of
     *         {@link OmniPod}.
     */
    public MovementProfile getMovementProfileMax() {
        return new MaxMovementProfile(getMovementProfileBase(), getQuirkGroups());
    }

    /**
     * @return The {@link MovementProfile} where the {@link OmniPod} for each {@link ComponentOmniMech} is selected to
     *         minimize each attribute. All the values of the {@link MovementProfile} may not be attainable
     *         simultaneously but each value of each attribute is independently attainable for some combination of
     *         {@link OmniPod}.
     */
    public MovementProfile getMovementProfileMin() {
        return new MinMovementProfile(getMovementProfileBase(), getQuirkGroups());
    }

    /**
     * @return The set of {@link Modifier} for the stock selection of {@link OmniPod}s.
     */
    public Collection<Modifier> getStockModifiers() {
        return Arrays.stream(Location.values()).map(location -> OmniPodDB.lookupStock(this, location))
                .filter(pod -> pod.isPresent()).flatMap(pod -> pod.get().getQuirks().stream())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAllowed(Item aItem) {
        if (aItem instanceof Engine) {
            return false; // Engine is fixed.
        }
        return super.isAllowed(aItem); // Anything else depends on the actual combination of omnipods equipped
    }

    // {Location{OmniPod{Modifier}}}
    private List<List<Collection<Modifier>>> getQuirkGroups() {
        final List<List<Collection<Modifier>>> groups = new ArrayList<>();

        for (final Location location : Location.values()) {
            final List<Collection<Modifier>> group = new ArrayList<>();
            final ComponentOmniMech component = getComponent(location);

            if (component.hasFixedOmniPod()) {
                group.add(component.getFixedOmniPod().getQuirks());
            }
            else {
                for (final OmniPod omniPod : OmniPodDB.lookup(this, location)) {
                    group.add(omniPod.getQuirks());
                }
            }
            groups.add(group);
        }
        return groups;
    }
}

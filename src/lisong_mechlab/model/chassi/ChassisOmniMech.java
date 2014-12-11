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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Faction;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.modifiers.Modifier;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.util.ListArrayUtils;

/**
 * This class models an omnimech chassis, i.e. the basic attributes associated with the chassis and the center omnipod.
 * 
 * @author Li Song
 */
public class ChassisOmniMech extends ChassisBase {

    private final ArmorUpgrade     armorType;
    private final HeatSinkUpgrade  heatSinkType;
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
     * @param aArmorType
     *            The armor type that is fixed on this chassis.
     * @param aHeatSinkType
     *            The heat sink type that is fixed on this chassis.
     */
    public ChassisOmniMech(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons,
            ChassisVariant aVariant, int aBaseVariant, MovementProfile aMovementProfile, Faction aFaction,
            ComponentOmniMech[] aComponents, int aMaxPilotModules, int aMaxConsumableModules, int aMaxWeaponModules,
            StructureUpgrade aStructureType, ArmorUpgrade aArmorType, HeatSinkUpgrade aHeatSinkType) {
        super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile,
                aFaction, aComponents, aMaxPilotModules, aMaxConsumableModules, aMaxWeaponModules);
        structureType = aStructureType;
        armorType = aArmorType;
        heatSinkType = aHeatSinkType;

        int s = 0;
        int a = 0;
        for (ComponentOmniMech component : getComponents()) {
            s += component.getDynamicStructureSlots();
            a += component.getDynamicArmorSlots();
        }
        if (s != structureType.getExtraSlots()) {
            throw new IllegalArgumentException(
                    "The fixed structure slots in components must sum up the number of slots required by the structure type.");
        }
        if (a != armorType.getExtraSlots()) {
            throw new IllegalArgumentException(
                    "The fixed armor slots in components must sum up the number of slots required by the armor type.");
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
     * @return The type of the fixed armor of this omnimech.
     */
    public ArmorUpgrade getFixedArmorType() {
        return armorType;
    }

    /**
     * @return The engine that is fixed to this omnimech chassis.
     */
    public Engine getFixedEngine() {
        for (Item item : getComponent(Location.CenterTorso).getFixedItems()) {
            if (item instanceof Engine)
                return (Engine) item;
        }
        throw new IllegalStateException("No engine found in omnimech!");
    }

    /**
     * @return The number of heat sinks that are fixed on this chassis, including the ones in the fixed engine.
     */
    public int getFixedHeatSinks() {
        int ans = getFixedEngine().getNumInternalHeatsinks();
        for (ComponentOmniMech component : getComponents()) {
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
        for (ComponentOmniMech component : getComponents()) {
            ans += ListArrayUtils.countByType(component.getFixedItems(), JumpJet.class);
        }
        return ans;
    }

    /**
     * @return The mass of this chassis when all non-fixed components and all armor is removed.
     */
    public double getFixedMass() {
        double ans = structureType.getStructureMass(this);
        for (ComponentOmniMech component : getComponents()) {
            for (Item item : component.getFixedItems()) {
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
        List<Modifier> ans = new ArrayList<>();
        for (Location location : Location.values()) {
            OmniPod omniPod = OmniPodDB.lookupOriginal(this, location);
            ans.addAll(omniPod.getQuirks());
        }
        return ans;
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
        List<List<Collection<Modifier>>> groups = new ArrayList<>();

        for (Location location : Location.values()) {
            List<Collection<Modifier>> group = new ArrayList<>();

            if (getComponent(location).hasFixedOmniPod()) {
                group.add(OmniPodDB.lookupOriginal(this, location).getQuirks());
            }
            else {
                for (OmniPod omniPod : OmniPodDB.lookup(this, location)) {
                    group.add(omniPod.getQuirks());
                }
            }
            groups.add(group);
        }
        return groups;
    }
}

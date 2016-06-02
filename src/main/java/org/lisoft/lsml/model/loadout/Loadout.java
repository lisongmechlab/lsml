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
package org.lisoft.lsml.model.loadout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.Component;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.MovementProfile;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.EngineType;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.item.ModifierEquipment;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponent;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.util.ListArrayUtils;

/**
 * This class acts as a common base for loadouts for both Omni- and Standard- Battle 'Mechs.
 *
 * @author Emily Björk
 */
public abstract class Loadout extends NamedObject {
    private final Chassis chassisBase;
    private final ConfiguredComponent[] components;
    private final Efficiencies efficiencies;
    private final List<PilotModule> modules; // TODO: Modules should be handled as separate categories.
    private final WeaponGroups weaponGroups;

    protected Loadout(ConfiguredComponent[] aComponents, Chassis aChassisBase, WeaponGroups aWeaponGroups) {
        super(aChassisBase.getNameShort());
        chassisBase = aChassisBase;
        efficiencies = new Efficiencies();
        modules = new ArrayList<>();
        components = aComponents;
        weaponGroups = aWeaponGroups;
    }

    /**
     * @param aModule
     *            The {@link PilotModule} to add to this {@link Loadout}.
     */
    public void addModule(PilotModule aModule) {
        modules.add(aModule);
    }

    /**
     * @param aModule
     *            The module to test if it can be added to this loadout.
     * @return A {@link EquipResult}.
     */
    public EquipResult canAddModule(PilotModule aModule) {
        if (getModules().contains(aModule)) {
            return EquipResult.make(EquipResultType.ModuleAlreadyEquipped);
        }
        if (!aModule.getFaction().isCompatible(getChassis().getFaction())) {
            return EquipResult.make(EquipResultType.NotSupported);
        }

        final boolean canUseHybridSlot = aModule.getSlot() == ModuleSlot.WEAPON || aModule.getSlot() == ModuleSlot.MECH;

        final boolean isHybridSlotFree = !(getModulesOfType(ModuleSlot.MECH) > getModulesMax(ModuleSlot.MECH)
                || getModulesOfType(ModuleSlot.WEAPON) > getModulesMax(ModuleSlot.WEAPON));

        if (getModulesOfType(aModule.getSlot()) >= getModulesMax(aModule.getSlot())
                && (!canUseHybridSlot || !isHybridSlotFree)) {
            return EquipResult.make(EquipResultType.NotEnoughSlots);
        }

        // TODO: Apply any additional limitations on modules
        return EquipResult.SUCCESS;
    }

    /**
     * Checks global constraints that could prevent the item from being added to this {@link LoadoutStandard}.
     * <p>
     * This includes:
     * <ul>
     * <li>Only one engine.</li>
     * <li>Max jump jet count not exceeded.</li>
     * <li>Correct jump jet type.</li>
     * <li>Enough free mass.</li>
     * <li>Enough globally free critical slots.</li>
     * <li>Enough globally free hard points of applicable type.</li>
     * </ul>
     *
     * @param aItem
     *            The {@link Item} to check for.
     * @return <code>true</code> if the given {@link Item} is globally feasible on this loadout.
     */
    public EquipResult canEquipDirectly(Item aItem) {
        final EquipResult globalResult = canEquipGlobal(aItem);

        if (globalResult != EquipResult.SUCCESS) {
            // The case where adding a weapon that would cause LAA/HA to be removed will not cause an issue as omnimechs
            // where this can occur, have fixed armour and structure slots.
            return globalResult;
        }

        if (aItem instanceof Engine) {
            final Engine engine = (Engine) aItem;
            if (engine.getType() == EngineType.XL) {
                final int sideSlots = engine.getSide().getSlots();
                if (getComponent(Location.LeftTorso).getSlotsFree() < sideSlots) {
                    return EquipResult.make(Location.LeftTorso, EquipResultType.NotEnoughSlotsForXLSide);
                }
                if (getComponent(Location.RightTorso).getSlotsFree() < sideSlots) {
                    return EquipResult.make(Location.RightTorso, EquipResultType.NotEnoughSlotsForXLSide);
                }
            }
            return getComponent(Location.CenterTorso).canEquip(engine);
        }

        EquipResult reason = EquipResult.SUCCESS;
        for (final ConfiguredComponent part : components) {
            final EquipResult componentResult = part.canEquip(aItem);
            if (componentResult == EquipResult.SUCCESS) {
                return EquipResult.SUCCESS;
            }
            if (componentResult.isMoreSpecificThan(reason)) {
                reason = componentResult;
            }
        }
        // Loose component information from specific reason.
        return EquipResult.make(reason.getType());
    }

    /**
     * Checks only global constraints against the {@link Item}. These are necessary but not sufficient conditions. Local
     * conditions are needed to be sufficient.
     *
     * @param aItem
     *            The {@link Item} to check.
     * @return <code>true</code> if the necessary checks are passed.
     */
    public EquipResult canEquipGlobal(Item aItem) {
        if (!getChassis().isAllowed(aItem)) {
            return EquipResult.make(EquipResultType.NotSupported);
        }
        if (aItem.getMass() > getFreeMass()) {
            return EquipResult.make(EquipResultType.TooHeavy);
        }
        if (!aItem.isCompatible(getUpgrades())) {
            return EquipResult.make(EquipResultType.IncompatibleUpgrades);
        }

        if (aItem instanceof JumpJet && getJumpJetsMax() - getJumpJetCount() < 1) {
            return EquipResult.make(EquipResultType.JumpJetCapacityReached);
        }

        // Allow engine slot heat sinks as long as there is enough free mass.
        final ConfiguredComponent ct = getComponent(Location.CenterTorso);
        if (aItem instanceof HeatSink && ct.getEngineHeatSinks() < ct.getEngineHeatSinksMax()) {
            return EquipResult.SUCCESS;
        }

        // FIXME: The case where adding a weapon that would cause LAA/HA to be removed
        // while at max global slots fails even if it might succeed.

        int requiredSlots = aItem.getSlots();
        if (aItem instanceof Engine) {
            if (getEngine() != null) {
                return EquipResult.make(EquipResultType.EngineAlreadyEquipped);
            }

            final Engine engine = (Engine) aItem;
            if (engine.getType() == EngineType.XL) {
                requiredSlots += 2 * engine.getSide().getSlots();
            }
        }

        if (requiredSlots > getNumCriticalSlotsFree()) {
            return EquipResult.make(EquipResultType.NotEnoughSlots);
        }

        final HardPointType hp = aItem.getHardpointType();
        if (HardPointType.NONE != hp && getItemsOfHardPointType(hp) >= getHardpointsCount(hp)) {
            return EquipResult.make(EquipResultType.NoFreeHardPoints);
        }
        return EquipResult.SUCCESS;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Loadout that = getClass().cast(obj);
        if (!name.equals(that.name)) {
            return false;
        }
        if (chassisBase != that.chassisBase) {
            return false;
        }
        if (!ListArrayUtils.equalsUnordered(modules, that.modules)) {
            return false;
        }
        if (!Arrays.equals(components, that.components)) {
            return false;
        }
        return true;
    }

    /**
     * @return The total number of armour points on this loadout.
     */
    public int getArmour() {
        int ans = 0;
        for (final ConfiguredComponent component : components) {
            ans += component.getArmourTotal();
        }
        return ans;
    }

    /**
     * Gets a {@link List} of {@link ConfiguredComponent}s that could possibly house the given item.
     * <p>
     * This method checks necessary but not sufficient constraints. In other words, the {@link ConfiguredComponent}s in
     * the returned list may or may not be able to hold the {@link Item}. But the {@link ConfiguredComponent}s not in
     * the list are unable to hold the {@link Item}.
     * <p>
     * This method is mainly useful for limiting search spaces for various optimization algorithms.
     *
     * @param aItem
     *            The {@link Item} to find candidate {@link ConfiguredComponent}s for.
     * @return A {@link List} of {@link ConfiguredComponent}s that might be able to hold the {@link Item}.
     */
    public List<ConfiguredComponent> getCandidateLocationsForItem(Item aItem) {
        final List<ConfiguredComponent> candidates = new ArrayList<>();
        if (EquipResult.SUCCESS != canEquipGlobal(aItem)) {
            return candidates;
        }

        int globalFreeHardPoints = 0;
        final HardPointType hardpointType = aItem.getHardpointType();

        for (final ConfiguredComponent part : components) {
            final Component internal = part.getInternalComponent();
            if (internal.isAllowed(aItem, getEngine())) {
                if (aItem.getHardpointType() != HardPointType.NONE && part.getHardPointCount(hardpointType) < 1) {
                    continue;
                }
                candidates.add(part);
            }

            if (hardpointType != HardPointType.NONE) {
                final int localFreeHardPoints = part.getHardPointCount(hardpointType)
                        - part.getItemsOfHardpointType(hardpointType);
                globalFreeHardPoints += localFreeHardPoints;
            }
        }

        if (hardpointType != HardPointType.NONE && globalFreeHardPoints <= 0) {
            candidates.clear();
        }

        return candidates;
    }

    /**
     * @return The base chassis of this loadout.
     */
    public Chassis getChassis() {
        return chassisBase;
    }

    /**
     * @param aLocation
     *            The location to get the component for.
     * @return The component at the given location
     */
    public ConfiguredComponent getComponent(Location aLocation) {
        return components[aLocation.ordinal()];
    }

    /**
     * @return A unmodifiable list of all the components on this loadout.
     */
    public Collection<ConfiguredComponent> getComponents() {
        return Collections.unmodifiableList(Arrays.asList(components));
    }

    /**
     * TODO: This should be replaced by a pilot skill tree.
     *
     * @return The {@link Efficiencies} for this loadout.
     */
    public Efficiencies getEfficiencies() {
        return efficiencies;
    }

    /**
     * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is equipped.
     */
    public abstract Engine getEngine();

    /**
     * @return The amount of free tonnage the loadout can still support.
     */
    public double getFreeMass() {
        final double ans = chassisBase.getMassMax() - getMass();
        return ans;
    }

    /**
     * @param aHardpointType
     *            The type of hard points to count.
     * @return The number of hard points of the given type.
     */
    public int getHardpointsCount(HardPointType aHardpointType) {
        // Note: This has been moved from chassis base because for omnimechs, the hard point count depends on which
        // omnipods are equipped.
        int sum = 0;
        for (final ConfiguredComponent component : components) {
            sum += component.getHardPointCount(aHardpointType);
        }
        return sum;
    }

    /**
     * @return The total number of heat sinks equipped.
     */
    public int getHeatsinksCount() {
        int ans = countItemsOfType(HeatSink.class);

        final Engine engine = getEngine();
        if (engine != null) {
            ans += engine.getNumInternalHeatsinks();
        }

        return ans;
    }

    public int getItemsOfHardPointType(HardPointType aHardPointType) {
        int ans = 0;
        for (final ConfiguredComponent component : components) {
            ans += component.getItemsOfHardpointType(aHardPointType);
        }
        return ans;
    }

    /**
     * @return The total number of jump jets equipped.
     */
    public int getJumpJetCount() {
        return countItemsOfType(JumpJet.class);
    }

    /**
     * @return The maximal number of jump jets the loadout can support.
     */
    abstract public int getJumpJetsMax();

    /**
     * @return The current mass of the loadout.
     */
    public double getMass() {
        double ans = getMassStructItems();
        ans += getUpgrades().getArmour().getArmourMass(getArmour());
        return ans;
    }

    /**
     * @return The mass of the loadout excluding armour. This is useful to avoid floating point precision issues from
     *         irrational armour values.
     */
    public double getMassStructItems() {
        double ans = getUpgrades().getStructure().getStructureMass(chassisBase);
        for (final ConfiguredComponent component : components) {
            ans += component.getItemMass();
        }
        return ans;
    }

    /**
     * Returns a {@link Collection} of all equipment or modules or omnipods or quirks that are modifiers.
     *
     * @return The {@link Collection} of modifiers.
     */
    public Collection<Modifier> getModifiers() {
        final List<Modifier> modifiers = new ArrayList<>();
        for (final ModifierEquipment t : items(ModifierEquipment.class)) {
            modifiers.addAll(t.getModifiers());
        }
        for (final PilotModule module : getModules()) {
            if (module instanceof ModifierEquipment) {
                modifiers.addAll(((ModifierEquipment) module).getModifiers());
            }
        }
        modifiers.addAll(getEfficiencies().getModifiers());
        return modifiers;
    }

    /**
     * @return An unmodifiable {@link Collection} of all the equipped pilot modules.
     */
    public List<PilotModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * @param aModuleSlot
     *            The type of module slots to get the max for.
     * @return The maximal number of modules that can be equipped on this {@link Loadout}.
     */
    public abstract int getModulesMax(ModuleSlot aModuleSlot);

    /**
     * Counts the number of modules equipped of the given slot type.
     *
     * @param aModuleSlot
     *            The {@link ModuleSlot} type to count modules of.
     * @return The number of modules.
     */
    public int getModulesOfType(ModuleSlot aModuleSlot) {
        int ans = 0;
        for (final PilotModule module : getModules()) {
            if (module.getSlot() == aModuleSlot) {
                ans++;
            }
        }
        return ans;
    }

    public MovementProfile getMovementProfile() {
        return getChassis().getMovementProfileBase();
    }

    /**
     * @return The number of globally available critical slots.
     */
    public int getNumCriticalSlotsFree() {
        return chassisBase.getCriticalSlotsTotal() - getNumCriticalSlotsUsed();
    }

    /**
     * @return The number of globally used critical slots.
     */
    public abstract int getNumCriticalSlotsUsed();

    /**
     * @return The {@link Upgrades} that are equipped on this loadout.
     */
    public abstract Upgrades getUpgrades();

    /**
     * @return The {@link WeaponGroups} for this {@link Loadout}.
     */
    public WeaponGroups getWeaponGroups() {
        return weaponGroups;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chassisBase == null) ? 0 : chassisBase.hashCode());
        result = prime * result + ((efficiencies == null) ? 0 : efficiencies.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((components == null) ? 0 : components.hashCode());
        return result;
    }

    /**
     * @return An {@link Iterable} over all {@link Item}s.
     */
    public Iterable<Item> items() {
        return items(null);
    }

    /**
     * @param aClass
     *            The type to iterate over.
     * @return An {@link Iterable} over all {@link Item}s that implements <code>aClass</code>.
     */
    public <X> Iterable<X> items(Class<X> aClass) {
        return new LoadoutIterable<X>(this, aClass);
    }

    /**
     * @param aModule
     *            The {@link PilotModule} to remove from this {@link Loadout}.
     */
    public void removeModule(PilotModule aModule) {
        modules.remove(aModule);
    }

    @Override
    public String toString() {
        if (getName().contains(getChassis().getNameShort())) {
            return getName();
        }
        return getName() + " (" + getChassis().getNameShort() + ")";
    }

    private int countItemsOfType(Class<?> aClass) {
        int ans = 0;
        final Iterator<?> it = items(aClass).iterator();
        while (it.hasNext()) {
            ans++;
            it.next();
        }
        return ans;
    }
}

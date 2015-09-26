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

import org.lisoft.lsml.model.chassi.ChassisBase;
import org.lisoft.lsml.model.chassi.ComponentBase;
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
import org.lisoft.lsml.model.loadout.EquipResult.Type;
import org.lisoft.lsml.model.loadout.component.ComponentBuilder;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentBase;
import org.lisoft.lsml.model.loadout.component.ConfiguredComponentStandard;
import org.lisoft.lsml.model.modifiers.Efficiencies;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.upgrades.Upgrades;
import org.lisoft.lsml.parsing.datacache.ChassiConverter;
import org.lisoft.lsml.parsing.datacache.ConfiguredComponentConverter;
import org.lisoft.lsml.parsing.datacache.ItemConverter;
import org.lisoft.lsml.parsing.datacache.LoadoutConverter;
import org.lisoft.lsml.parsing.datacache.ModuleConverter;
import org.lisoft.lsml.parsing.datacache.UpgradeConverter;
import org.lisoft.lsml.parsing.datacache.UpgradesConverter;
import org.lisoft.lsml.util.ListArrayUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class acts as a common base for loadouts for both Omni- and Standard- Battle 'Mechs.
 * 
 * @author Emily Björk
 * @param <T>
 *            The type of the {@link ConfiguredComponentBase} in this loadout.
 */
public abstract class LoadoutBase<T extends ConfiguredComponentBase> {
    private String                  name;
    private final ChassisBase       chassisBase;
    private final T[]               components;
    private final Efficiencies      efficiencies;
    private final List<PilotModule> modules;     // TODO: Modules should be handled as separate categories.
    private final WeaponGroups      weaponGroups;

    protected LoadoutBase(ComponentBuilder.Factory<T> aFactory, ChassisBase aChassisBase) {
        name = aChassisBase.getNameShort();
        chassisBase = aChassisBase;
        efficiencies = new Efficiencies();
        modules = new ArrayList<>();
        components = aFactory.defaultComponents(chassisBase);
        weaponGroups = new WeaponGroups(this);
    }

    protected LoadoutBase(ComponentBuilder.Factory<T> aFactory, LoadoutBase<T> aLoadoutBase) {
        name = aLoadoutBase.name;
        chassisBase = aLoadoutBase.chassisBase;
        efficiencies = new Efficiencies(aLoadoutBase.efficiencies);
        modules = new ArrayList<>(aLoadoutBase.modules);
        components = aFactory.cloneComponents(aLoadoutBase);
        weaponGroups = new WeaponGroups(aLoadoutBase.getWeaponGroups(), this);
    }

    public static XStream loadoutXstream() {
        XStream stream = new XStream(new StaxDriver());
        stream.autodetectAnnotations(true);
        stream.setMode(XStream.NO_REFERENCES);
        stream.registerConverter(new ChassiConverter());
        stream.registerConverter(new ItemConverter());
        stream.registerConverter(new ModuleConverter());
        stream.registerConverter(new ConfiguredComponentConverter(null, null));
        stream.registerConverter(new LoadoutConverter());
        stream.registerConverter(new UpgradeConverter());
        stream.registerConverter(new UpgradesConverter());
        stream.addImmutableType(Item.class);
        stream.alias("component", ConfiguredComponentStandard.class);
        stream.alias("loadout", LoadoutBase.class);
        return stream;
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().isAssignableFrom(obj.getClass()))
            return false;
        LoadoutBase<T> that = getClass().cast(obj);
        if (!name.equals(that.name))
            return false;
        if (chassisBase != that.chassisBase)
            return false;
        if (!ListArrayUtils.equalsUnordered(modules, that.modules))
            return false;
        if (!Arrays.equals(components, that.components))
            return false;
        return true;
    }

    @Override
    public String toString() {
        if (getName().contains(getChassis().getNameShort()))
            return getName();
        return getName() + " (" + getChassis().getNameShort() + ")";
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
     * @return The total number of armor points on this loadout.
     */
    public int getArmor() {
        int ans = 0;
        for (T component : components) {
            ans += component.getArmorTotal();
        }
        return ans;
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
     * @return The mass of the loadout excluding armor. This is useful to avoid floating point precision issues from
     *         irrational armor values.
     */
    public double getMassStructItems() {
        double ans = getUpgrades().getStructure().getStructureMass(chassisBase);
        for (T component : components) {
            ans += component.getItemMass();
        }
        return ans;
    }

    /**
     * @return The current mass of the loadout.
     */
    public double getMass() {
        double ans = getMassStructItems();
        ans += getUpgrades().getArmor().getArmorMass(getArmor());
        return ans;
    }

    /**
     * @return The amount of free tonnage the loadout can still support.
     */
    public double getFreeMass() {
        double ans = chassisBase.getMassMax() - getMass();
        return ans;
    }

    /**
     * @return The base chassis of this loadout.
     */
    public ChassisBase getChassis() {
        return chassisBase;
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
     * @return The maximal number of modules that can be equipped on this {@link LoadoutBase}.
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
        for (PilotModule module : getModules()) {
            if (module.getSlot() == aModuleSlot)
                ans++;
        }
        return ans;
    }

    /**
     * @param aModule
     *            The module to test if it can be added to this loadout.
     * @return <code>true</code> if the given module can be added to this loadout.
     */
    public boolean canAddModule(PilotModule aModule) {
        if (getModules().contains(aModule))
            return false;
        if (!aModule.getFaction().isCompatible(getChassis().getFaction()))
            return false;

        final boolean canUseHybridSlot = aModule.getSlot() == ModuleSlot.WEAPON || aModule.getSlot() == ModuleSlot.MECH;

        final boolean isHybridSlotFree = !(getModulesOfType(ModuleSlot.MECH) > getModulesMax(ModuleSlot.MECH) || getModulesOfType(ModuleSlot.WEAPON) > getModulesMax(ModuleSlot.WEAPON));

        if (getModulesOfType(aModule.getSlot()) >= getModulesMax(aModule.getSlot())
                && (!canUseHybridSlot || !isHybridSlotFree))
            return false;

        // TODO: Apply any additional limitations on modules
        return true;
    }

    /**
     * @param aModule
     *            The {@link PilotModule} to add to this {@link LoadoutBase}.
     */
    public void addModule(PilotModule aModule) {
        modules.add(aModule);
    }

    /**
     * @param aModule
     *            The {@link PilotModule} to remove from this {@link LoadoutBase}.
     */
    public void removeModule(PilotModule aModule) {
        modules.remove(aModule);
    }

    /**
     * @return The user given name of the loadout.
     */
    public String getName() {
        return name;
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
     * @param aLocation
     *            The location to get the component for.
     * @return The component at the given location
     */
    public T getComponent(Location aLocation) {
        return components[aLocation.ordinal()];
    }

    /**
     * @return A {@link Collection} of all the configured components.
     */
    public Collection<T> getComponents() {
        return Collections.unmodifiableList(Arrays.asList(components));
    }

    /**
     * @return The {@link Upgrades} that are equipped on this loadout.
     */
    public abstract Upgrades getUpgrades();

    /**
     * @param aHardpointType
     *            The type of hard points to count.
     * @return The number of hard points of the given type.
     */
    public int getHardpointsCount(HardPointType aHardpointType) {
        // Note: This has been moved from chassis base because for omnimechs, the hard point count depends on which
        // omnipods are equipped.
        int sum = 0;
        for (T component : components) {
            sum += component.getHardPointCount(aHardpointType);
        }
        return sum;
    }

    /**
     * @return The maximal number of jump jets the loadout can support.
     */
    abstract public int getJumpJetsMax();

    /**
     * @return The total number of heat sinks equipped.
     */
    public int getHeatsinksCount() {
        int ans = countItemsOfType(HeatSink.class);

        Engine engine = getEngine();
        if (engine != null) {
            ans += engine.getNumInternalHeatsinks();
        }

        return ans;
    }

    private int countItemsOfType(Class<?> aClass) {
        int ans = 0;
        Iterator<?> it = items(aClass).iterator();
        while (it.hasNext()) {
            ans++;
            it.next();
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
     * Gets a {@link List} of {@link ConfiguredComponentBase}s that could possibly house the given item.
     * <p>
     * This method checks necessary but not sufficient constraints. In other words, the {@link ConfiguredComponentBase}s
     * in the returned list may or may not be able to hold the {@link Item}. But the {@link ConfiguredComponentBase}s
     * not in the list are unable to hold the {@link Item}.
     * <p>
     * This method is mainly useful for limiting search spaces for various optimization algorithms.
     * 
     * @param aItem
     *            The {@link Item} to find candidate {@link ConfiguredComponentBase}s for.
     * @return A {@link List} of {@link ConfiguredComponentBase}s that might be able to hold the {@link Item}.
     */
    public List<ConfiguredComponentBase> getCandidateLocationsForItem(Item aItem) {
        List<ConfiguredComponentBase> candidates = new ArrayList<>();
        if (EquipResult.SUCCESS != canEquipGlobal(aItem))
            return candidates;

        int globalFreeHardPoints = 0;
        HardPointType hardpointType = aItem.getHardpointType();

        for (ConfiguredComponentBase part : components) {
            ComponentBase internal = part.getInternalComponent();
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
     * Changes the name of the loadout.
     * 
     * @param aNewName
     *            The new name of the loadout.
     */
    public void rename(String aNewName) {
        name = aNewName;
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
    public EquipResult canEquip(Item aItem) {
        EquipResult globalResult = canEquipGlobal(aItem);

        if (globalResult != EquipResult.SUCCESS) {
            // The case where adding a weapon that would cause LAA/HA to be removed will not cause an issue as omnimechs
            // where this can occur, have fixed armor and structure slots.
            return globalResult;
        }

        if (aItem instanceof Engine) {
            Engine engine = (Engine) aItem;
            if (engine.getType() == EngineType.XL) {
                final int sideSlots = engine.getSide().getNumCriticalSlots();
                if (getComponent(Location.LeftTorso).getSlotsFree() < sideSlots) {
                    return EquipResult.make(Location.LeftTorso, Type.NotEnoughSlotsForXLSide);
                }
                if (getComponent(Location.RightTorso).getSlotsFree() < sideSlots) {
                    return EquipResult.make(Location.RightTorso, Type.NotEnoughSlotsForXLSide);
                }
            }
            return getComponent(Location.CenterTorso).canEquip(engine);
        }

        EquipResult reason = EquipResult.SUCCESS;
        for (ConfiguredComponentBase part : getComponents()) {
            EquipResult componentResult = part.canEquip(aItem);
            if (componentResult == EquipResult.SUCCESS)
                return componentResult;
            if (componentResult.isMoreSpecificThan(reason)) {
                reason = componentResult;
            }
        }
        return reason;
    }

    /**
     * Checks only global constraints against the {@link Item}. These are necessary but not sufficient conditions. Local
     * conditions are needed to be sufficient.
     * 
     * @param aItem
     *            The {@link Item} to check.
     * @return <code>true</code> if the necessary checks are passed.
     */
    protected EquipResult canEquipGlobal(Item aItem) {
        if (!getChassis().isAllowed(aItem))
            return EquipResult.make(Type.NotSupported);
        if (aItem.getMass() > getFreeMass())
            return EquipResult.make(Type.TooHeavy);
        if (!aItem.isCompatible(getUpgrades()))
            return EquipResult.make(Type.IncompatibleUpgrades);

        if (aItem instanceof JumpJet && getJumpJetsMax() - getJumpJetCount() < 1)
            return EquipResult.make(Type.JumpJetCapacityReached);

        // Allow engine slot heat sinks as long as there is enough free mass.
        ConfiguredComponentBase ct = getComponent(Location.CenterTorso);
        if (aItem instanceof HeatSink && ct.getEngineHeatsinks() < ct.getEngineHeatsinksMax())
            return EquipResult.SUCCESS;

        // FIXME: The case where adding a weapon that would cause LAA/HA to be removed
        // while at max global slots fails even if it might succeed.

        int requiredSlots = aItem.getNumCriticalSlots();
        if (aItem instanceof Engine) {
            if (getEngine() != null) {
                return EquipResult.make(Type.EngineAlreadyEquipped);
            }

            Engine engine = (Engine) aItem;
            if (engine.getType() == EngineType.XL) {
                requiredSlots += 2 * engine.getSide().getNumCriticalSlots();
            }
        }

        if (requiredSlots > getNumCriticalSlotsFree())
            return EquipResult.make(Type.NotEnoughSlots);
        return EquipResult.SUCCESS;
    }

    public abstract MovementProfile getMovementProfile();

    /**
     * @return A deep copy of <code>this</code>.
     */
    public abstract LoadoutBase<?> copy();

    /**
     * @return A String containing a HTML formatted summary of the quirks for this loadout.
     */
    public abstract String getQuirkHtmlSummary();

    /**
     * Returns a {@link Collection} of all equipment or modules or omnipods or quirks that are modifiers.
     * 
     * @return The {@link Collection} of modifiers.
     */
    public Collection<Modifier> getModifiers() {
        List<Modifier> modifiers = new ArrayList<>();
        for (ModifierEquipment t : items(ModifierEquipment.class)) {
            modifiers.addAll(t.getModifiers());
        }
        for (PilotModule module : getModules()) {
            if (module instanceof ModifierEquipment) {
                modifiers.addAll(((ModifierEquipment) module).getModifiers());
            }
        }
        modifiers.addAll(getEfficiencies().getModifiers());
        return modifiers;
    }

    /**
     * @return The {@link WeaponGroups} for this {@link LoadoutBase}.
     */
    public WeaponGroups getWeaponGroups() {
        return weaponGroups;
    }
}

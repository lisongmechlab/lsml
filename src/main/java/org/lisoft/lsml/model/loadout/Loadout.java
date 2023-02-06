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
package org.lisoft.lsml.model.loadout;

import java.util.*;
import org.lisoft.lsml.model.ItemDB;
import org.lisoft.lsml.model.NamedObject;
import org.lisoft.lsml.model.UpgradeDB;
import org.lisoft.lsml.model.loadout.EquipResult.EquipResultType;
import org.lisoft.lsml.util.ListArrayUtils;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.equipment.*;
import org.lisoft.mwo_data.equipment.Module;
import org.lisoft.mwo_data.mechs.*;
import org.lisoft.mwo_data.modifiers.Modifier;
import org.lisoft.mwo_data.modifiers.PilotSkills;

/**
 * This class acts as a common base for loadouts for both Omni- and Standard-Mechs.
 *
 * @author Li Song
 */
public abstract class Loadout extends NamedObject {
  private final Chassis chassisBase;
  private final ConfiguredComponent[] components;
  private final List<Consumable> consumables = new ArrayList<>();
  private final PilotSkills efficiencies;
  private final WeaponGroups weaponGroups;

  protected Loadout(
      ConfiguredComponent[] aComponents, Chassis aChassisBase, WeaponGroups aWeaponGroups) {
    super(aChassisBase.getShortName());
    chassisBase = aChassisBase;
    efficiencies = new PilotSkills();
    components = aComponents;
    weaponGroups = aWeaponGroups;
  }

  /**
   * @param aModule The {@link Consumable} to add to this {@link Loadout}.
   */
  public void addModule(Consumable aModule) {
    consumables.add(aModule);
  }

  /**
   * @param aModule The module to test if it can be added to this loadout.
   * @return A {@link EquipResult}.
   */
  public EquipResult canAddModule(Consumable aModule) {
    if (!aModule.getFaction().isCompatible(getChassis().getFaction())) {
      return EquipResult.make(EquipResultType.NotSupported);
    }
    if (getConsumables().size() >= getConsumablesMax()) {
      return EquipResult.make(EquipResultType.NotEnoughSlots);
    }
    return EquipResult.SUCCESS;
  }

  /**
   * Checks global constraints that could prevent the item from being added to this {@link
   * LoadoutStandard}.
   *
   * <p>This includes:
   *
   * <ul>
   *   <li>Only one engine.
   *   <li>Max jump jet count not exceeded.
   *   <li>Correct jump jet type.
   *   <li>Enough free mass.
   *   <li>Enough globally free critical slots.
   *   <li>Enough globally free hard points of applicable type.
   * </ul>
   *
   * @param aItem The {@link Item} to check for.
   * @return <code>true</code> if the given {@link Item} is globally feasible on this loadout.
   */
  public EquipResult canEquipDirectly(Item aItem) {
    final EquipResult globalResult = canEquipGlobal(aItem);

    if (globalResult != EquipResult.SUCCESS) {
      // The case where adding a weapon that would cause LAA/HA to be removed will not cause an
      // issue as omnimechs
      // where this can occur, have fixed armour and structure slots.
      return globalResult;
    }

    if (aItem instanceof final Engine engine) {
      if (engine.getSide().isPresent()) {
        final int sideSlots = engine.getSide().get().getSlots();
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
   * Checks only global constraints against the {@link Item}. These are necessary but not sufficient
   * conditions. Local conditions are needed to be sufficient.
   *
   * @param aItem The {@link Item} to check.
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
    if (aItem instanceof final Engine engine) {
      if (getEngine() != null) {
        return EquipResult.make(EquipResultType.EngineAlreadyEquipped);
      }

      if (engine.getSide().isPresent()) {
        requiredSlots += 2 * engine.getSide().get().getSlots();
      }
    }

    if (aItem == ItemDB.CASE) {
      boolean hasAllowedLocation = false;
      for (Location location : ItemDB.CASE.getAllowedComponents()) {
        if (!getComponent(location).getItemsEquipped().contains(ItemDB.CASE)) {
          hasAllowedLocation = true;
          break;
        }
      }
      if (!hasAllowedLocation) {
        return EquipResult.make(EquipResultType.EverythingAlreadyHasCase);
      }
    }

    if (requiredSlots > getFreeSlots()) {
      return EquipResult.make(EquipResultType.NotEnoughSlots);
    }

    if (aItem instanceof final Module module) {
      final Optional<Integer> allowedCount = module.getAllowedAmountOfType();
      if (allowedCount.isPresent()) {
        int allowedModulesLeft = allowedCount.get();
        for (final Module otherItem : items(Module.class)) {
          if (module.isSameTypeAs(otherItem)) {
            allowedModulesLeft--;
            if (allowedModulesLeft < 1) {
              return EquipResult.make(EquipResultType.TooManyOfThatType);
            }
          }
        }
      }
    }

    final HardPointType hp = aItem.getHardpointType();
    if (HardPointType.NONE != hp && getItemsOfHardPointType(hp) >= getHardPointsCount(hp)) {
      return EquipResult.make(EquipResultType.NoFreeHardPoints);
    }
    return EquipResult.SUCCESS;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof final Loadout that)) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }
    if (chassisBase != that.chassisBase) {
      return false;
    }
    if (!ListArrayUtils.equalsUnordered(consumables, that.consumables)) {
      return false;
    }
    return Arrays.equals(components, that.components);
  }

  /**
   * Returns a {@link Collection} of all {@link Modifier}s that affect the loadout. Equipment,
   * quirks and modules.
   *
   * @return The {@link Collection} of modifiers.
   */
  public Collection<Modifier> getAllModifiers() {
    final Collection<Modifier> modifiers = getEquipmentModifiers();
    modifiers.addAll(getEfficiencies().getModifiers());
    modifiers.addAll(getQuirks());
    return modifiers;
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
   *
   * <p>This method checks necessary but not sufficient constraints. In other words, the {@link
   * ConfiguredComponent}s in the returned list may or may not be able to hold the {@link Item}. But
   * the {@link ConfiguredComponent}s not in the list are unable to hold the {@link Item}.
   *
   * <p>This method is mainly useful for limiting search spaces for various optimization algorithms.
   *
   * @param aItem The {@link Item} to find candidate {@link ConfiguredComponent}s for.
   * @return A {@link List} of {@link ConfiguredComponent}s that might be able to hold the {@link
   *     Item}.
   */
  public List<ConfiguredComponent> getCandidateLocationsForItem(Item aItem) {
    final List<ConfiguredComponent> candidates = new ArrayList<>();
    if (EquipResult.SUCCESS != canEquipGlobal(aItem)) {
      return candidates;
    }

    final HardPointType hardpointType = aItem.getHardpointType();
    int globalFreeHardPoints = 0;

    for (final ConfiguredComponent part : components) {
      final Component internal = part.getInternalComponent();
      if (internal.isAllowed(aItem, getEngine())) {
        if (aItem.getHardpointType() != HardPointType.NONE
            && part.getHardPointCount(hardpointType) < 1) {
          continue;
        }
        candidates.add(part);
      }

      if (hardpointType != HardPointType.NONE) {
        final int localFreeHardPoints =
            part.getHardPointCount(hardpointType) - part.getItemsOfHardpointType(hardpointType);
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
   * @param aLocation The location to get the component for.
   * @return The component at the given location
   */
  public ConfiguredComponent getComponent(Location aLocation) {
    return components[aLocation.ordinal()];
  }

  /**
   * @return An unmodifiable list of all the components on this loadout.
   */
  public Collection<ConfiguredComponent> getComponents() {
    return Collections.unmodifiableList(Arrays.asList(components));
  }

  /**
   * @return An unmodifiable {@link Collection} of all the equipped pilot modules.
   */
  public List<Consumable> getConsumables() {
    return Collections.unmodifiableList(consumables);
  }

  /**
   * @return The maximal number of consumables that can be equipped on this {@link Loadout}.
   */
  public int getConsumablesMax() {
    return chassisBase.getConsumablesMax();
  }

  /**
   * TODO: This should be replaced by a pilot skill tree.
   *
   * @return The {@link PilotSkills} for this loadout.
   */
  public PilotSkills getEfficiencies() {
    return efficiencies;
  }

  /**
   * @return The {@link Engine} equipped on this loadout, or <code>null</code> if no engine is
   *     equipped.
   */
  public abstract Engine getEngine();

  /**
   * @return Modifiers for the loadout from equipment.
   */
  public Collection<Modifier> getEquipmentModifiers() {
    final List<Modifier> modifiers = new ArrayList<>();
    for (final ModifierEquipment t : items(ModifierEquipment.class)) {
      modifiers.addAll(t.getModifiers());
    }
    for (final Consumable module : getConsumables()) {
      if (module instanceof ModifierEquipment) {
        modifiers.addAll(((ModifierEquipment) module).getModifiers());
      }
    }
    return modifiers;
  }

  /**
   * @return The number of heat sinks external to the engine equipped.
   */
  public int getExternalHeatSinksCount() {
    return countItemsOfType(HeatSink.class);
  }

  /**
   * @return The amount of free tonnage the loadout can still support.
   */
  public double getFreeMass() {
    return chassisBase.getMassMax() - getMass();
  }

  /**
   * @return The number of globally available critical slots.
   */
  public int getFreeSlots() {
    return chassisBase.getSlotsTotal() - getSlotsUsed();
  }

  /**
   * @param aHardpointType The type of hard points to count.
   * @return The number of hard points of the given type.
   */
  public int getHardPointsCount(HardPointType aHardpointType) {
    // Note: This has been moved from chassis base because for omnimechs, the hard point count
    // depends on which
    // omnipods are equipped.
    int sum = 0;
    for (final ConfiguredComponent component : components) {
      sum += component.getHardPointCount(aHardpointType);
    }
    return sum;
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
  public abstract int getJumpJetsMax();

  /**
   * @return The current mass of the loadout.
   */
  public double getMass() {
    double ans = getMassStructItems();
    ans += getUpgrades().getArmour().getArmourMass(getArmour());
    return ans;
  }

  /**
   * @return The mass of the loadout excluding armour. This is useful to avoid floating point
   *     precision issues from irrational armour values.
   */
  public double getMassStructItems() {
    double ans = getUpgrades().getStructure().getStructureMass(chassisBase);
    for (final ConfiguredComponent component : components) {
      ans += component.getItemMass();
    }
    return ans;
  }

  public MovementProfile getMovementProfile() {
    return getChassis().getMovementProfileBase();
  }

  /**
   * @return All quirks for the loadout (I.e. chassis and omnipods)
   */
  public abstract Collection<Modifier> getQuirks();

  /**
   * @return The number of globally used critical slots.
   */
  public abstract int getSlotsUsed();

  /**
   * @return The total number of heat sinks equipped.
   */
  public int getTotalHeatSinksCount() {
    final Engine engine = getEngine();
    if (engine != null) {
      return getExternalHeatSinksCount() + engine.getNumInternalHeatsinks();
    }
    return getExternalHeatSinksCount();
  }

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
    result = prime * result + chassisBase.hashCode();
    result = prime * result + efficiencies.hashCode();
    result = prime * result + name.hashCode();
    result = prime * result + Arrays.hashCode(components);
    return result;
  }

  /**
   * @return An {@link Iterable} over all {@link Item}s.
   */
  public Iterable<Item> items() {
    return items(null);
  }

  /**
   * @param aClass The type to iterate over.
   * @return An {@link Iterable} over all {@link Item}s that implements <code>aClass</code>.
   */
  public <X> Iterable<X> items(Class<X> aClass) {
    return new LoadoutIterable<>(this, aClass);
  }

  /**
   * @param aModule The {@link Consumable} to remove from this {@link Loadout}.
   */
  public void removeModule(Consumable aModule) {
    consumables.remove(aModule);
  }

  @Override
  public String toString() {
    if (getName().contains(getChassis().getShortName())) {
      return getName();
    }
    return getName() + " (" + getChassis().getShortName() + ")";
  }

  private int countItemsOfType(Class<?> aClass) {
    int ans = 0;
    for (Object ignored : items(aClass)) {
      ans++;
    }
    return ans;
  }

  /**
   * Computes the total number of slots needed for a given upgrade over the "standard" version
   * (STANDARD ARMOUR, STANDARD STRUCTURE, No guidance, STD HEAT SINKS).
   *
   * <p>Possible usage: canUpgrade = getUpgradeSlotsCost(newUpgrade) -
   * getUpgradeSlotsCost(oldUpgrade) > freeSlots
   *
   * @param aUpgrade An upgrade to compute the change for.
   * @return A positive number of slots needed.
   */
  public int getUpgradeSlotsCost(Upgrade aUpgrade) {
    if (aUpgrade instanceof final ArmourUpgrade armourUpgrade) {
      return armourUpgrade.getTotalSlots();
    } else if (aUpgrade instanceof final StructureUpgrade structureUpgrade) {
      return structureUpgrade.getDynamicSlots();
    } else if (aUpgrade instanceof final GuidanceUpgrade guidanceUpgrade) {
      int ans = 0;
      for (final ConfiguredComponent part : getComponents()) {
        ans += part.getUpgradeSlotsCost(guidanceUpgrade);
      }
      return ans;
    } else if (aUpgrade instanceof final HeatSinkUpgrade heatSinkUpgrade) {
      final Faction faction = getChassis().getFaction();
      final int engineSlotHeatSinks = getComponent(Location.CenterTorso).getEngineHeatSinks();
      final int hs = getExternalHeatSinksCount() - engineSlotHeatSinks;
      final int stdHSSlots = UpgradeDB.getDefaultHeatSinks(faction).getHeatSinkType().getSlots();
      final int thisHSSlots = heatSinkUpgrade.getHeatSinkType().getSlots();
      return (thisHSSlots - stdHSSlots) * hs;
    }
    throw new IllegalArgumentException(
        "Unknown upgrade type: " + aUpgrade.getClass().getSimpleName());
  }

  /**
   * Computes the amount of extra tonnage needed for a given upgrade over the "standard" version
   * (STANDARD ARMOUR, STANDARD STRUCTURE, No guidance, STD HEAT SINKS).
   *
   * @param aUpgrade An upgrade to compute the change for.
   * @return A positive amount of tonnage needed.
   */
  public double getUpgradeMassCost(Upgrade aUpgrade) {
    if (aUpgrade instanceof final ArmourUpgrade armourUpgrade) {
      final int armour = getArmour();
      final ArmourUpgrade defaultArmour = UpgradeDB.getDefaultArmour(getChassis().getFaction());
      return armourUpgrade.getArmourMass(armour) - defaultArmour.getArmourMass(armour);
    } else if (aUpgrade instanceof final StructureUpgrade structureUpgrade) {
      final Chassis c = getChassis();
      final StructureUpgrade defaultUpgrade = UpgradeDB.getDefaultStructure(c.getFaction());
      return structureUpgrade.getStructureMass(c) - defaultUpgrade.getStructureMass(c);
    } else if (aUpgrade instanceof final GuidanceUpgrade guidanceUpgrade) {
      double ans = 0;
      for (final ConfiguredComponent part : getComponents()) {
        for (final Item item : part.getItemsEquipped()) {
          if (item instanceof final MissileWeapon weapon) {
            if (weapon.isArtemisCapable()) {
              ans += guidanceUpgrade.getTons();
            }
          }
        }
        for (final Item item : part.getItemsFixed()) {
          if (item instanceof final MissileWeapon weapon) {
            if (weapon.isArtemisCapable()) {
              ans += guidanceUpgrade.getTons();
            }
          }
        }
      }
      return ans;
    } else if (aUpgrade instanceof HeatSinkUpgrade) {
      return 0;
    }
    throw new IllegalArgumentException(
        "Unknown upgrade type: " + aUpgrade.getClass().getSimpleName());
  }
}

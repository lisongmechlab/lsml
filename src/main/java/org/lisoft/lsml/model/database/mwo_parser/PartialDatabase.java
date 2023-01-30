/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2023  Li Song
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
package org.lisoft.lsml.model.database.mwo_parser;

import static java.util.stream.Stream.concat;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lisoft.lsml.model.chassi.*;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.mwo_parser.GameVFS.GameFile;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;
import org.lisoft.lsml.util.ReflectionUtil;

/**
 * This class contains the partial state of a database object as the game files are being parsed. It allows for
 * easy reference to already parsed items allowing object-to-object links to be efficiently constructed for the
 * final database object.
 *
 * @author Li Song
 */
class PartialDatabase {
  private final List<Item> items = new ArrayList<>();
  private final Map<String, ModifierDescription> modifierDescriptions = new HashMap<>();
  private final List<Consumable> consumables = new ArrayList<>();
  private final List<Upgrade> upgrades = new ArrayList<>();
  private final List<OmniPod> omniPods = new ArrayList<>();
  private final List<Chassis> chassis = new ArrayList<>();
  private final List<Environment> environments = new ArrayList<>();
  private final List<StockLoadout> stockLoadouts = new ArrayList<>();
  private final Map<String, Long> checksums = new HashMap<>();
  private final RawMergedXML mergedXML = new RawMergedXML();
  private final Localisation localisation;
  private final Map<Integer, Item> id2item = new HashMap<>();

  PartialDatabase(Localisation aLocalisation, Collection<GameFile> aGameFiles) {
    localisation = aLocalisation;
    for (final GameFile file : aGameFiles) {
      mergedXML.append(file);
      checksums.put(file.path, file.crc32);
    }
  }

  String localise(String key){
    return localisation.key2string(key);
  }

  ModifierDescription getOrCreateModifierDescription(
      String key, Function<String, ModifierDescription> generator) {
    return modifierDescriptions.computeIfAbsent(key, generator);
  }

  Upgrade lookupUpgrade(int id) {
    return upgrades.stream().filter(x -> x.getId() == id).findAny().orElseThrow();
  }

  OmniPod lookupOmniPod(int id) {
    return omniPods.stream().filter(x -> x.getId() == id).findAny().orElseThrow();
  }

  Item lookupItem(int id) {
    if (!id2item.containsKey(id)) {
      throw new IllegalArgumentException("No item with ID: " + id + " has been parsed yet.");
    }
    return id2item.get(id);
  }

  Stream<Item> allItems() {
    return items.stream();
  }

  private void parseItems() throws Exception {
    final Iterator<ModuleXML> it = mergedXML.ModuleList.iterator();
    while (it.hasNext()) {
      final ModuleXML statsModule = it.next();
      final Optional<Item> optionalItem = statsModule.asItem(this);
      if (optionalItem.isPresent()) {
        Item item = optionalItem.get();
        items.add(item);
        id2item.put(item.getId(), item);
        // Remove parsed items from the input
        it.remove();
      }
    }

    // Weapons next.
    for (final WeaponXML statsWeapon : mergedXML.WeaponList) {
      Optional<Weapon> optionalWeapon = statsWeapon.asWeapon(mergedXML.WeaponList, this);
      if (optionalWeapon.isPresent()) {
        Weapon weapon = optionalWeapon.get();
        items.add(weapon);
        id2item.put(weapon.getId(), weapon);
      }
    }
  }

  /** Parses consumable data from the data files. */
  private void parseConsumables() {
    final Iterator<ModuleXML> it = mergedXML.ModuleList.iterator();
    while (it.hasNext()) {
      final ModuleXML statsModule = it.next();
      Optional<Consumable> optionalConsumable = statsModule.asConsumable(this);
      if (optionalConsumable.isPresent()) {
        consumables.add(optionalConsumable.get());
        it.remove();
      }
    }
  }

  private static final File MECH_ID_MAP_XML = new File("Game/Libs/Items/MechIDMap.xml");

  /**
   * Parses all inner sphere {@link ChassisStandard} from the ItemStats.xml file and related files.
   *
   * @param aGameVFS A {@link GameVFS} used to open other game files.
   */
  private void parseChassis(GameVFS aGameVFS) throws Exception {
    try (GameFile mechIdMapFile = aGameVFS.openGameFile(MECH_ID_MAP_XML)) {
      XMLMechIdMap mechIdMap = XMLMechIdMap.fromXml(mechIdMapFile.stream);

      for (final MechReferenceXML mech : mergedXML.MechList) {
        try {
          try (GameFile mdfFile = aGameVFS.openGameFile(mech.mdfFilePath())) {
            final MdfMechDefinition mdf = MdfMechDefinition.fromXml(mdfFile.stream);

            if (mdf.isPlayableOmniMech()) {
              try (GameFile loadoutXmlFile = aGameVFS.openGameFile(mech.stockLoadoutPath())) {
                final XMLLoadout stockXML = XMLLoadout.fromXml(loadoutXmlFile.stream);
                chassis.add(mdf.asChassisOmniMech(mech, this, mechIdMap, stockXML));
              }
            } else if (mdf.isPlayableStandardMech()) {
              try (GameFile hardPointsXmlFile = aGameVFS.openGameFile(mech.hardPointsXmlPath())) {
                final XMLHardpoints hardPoints = XMLHardpoints.fromXml(hardPointsXmlFile.stream);
                chassis.add(mdf.asChassisStandard(mech, this, mechIdMap, hardPoints));
              }
            }
          }
        } catch (final Exception e) {
          throw new IOException("Unable to load chassis configuration for [" + mech.name + "]!", e);
        }
      }
    }
  }

  /**
   * Do all the things to the parsed items that weren't possible to do due to dependencies on
   * parsing order.
   */
  private void postProcessItems() throws Exception {
    final Map<String, Ammunition> ammoMap =
        items.stream()
            .filter(o -> o instanceof Ammunition)
            .map(o -> (Ammunition) o)
            .collect(Collectors.toMap(a -> a.getKey().toLowerCase(), Function.identity()));

    for (final Item item : items) {
      if (item instanceof final AmmoWeapon ammoWeapon) {
        if (ammoWeapon.hasBuiltInAmmo()) {
          continue;
        }

        final String ammoType = ammoWeapon.getAmmoId().toLowerCase();
        final String ammoTypeHalf = ammoType + "half";

        final Ammunition ammo = ammoMap.get(ammoType);
        final Ammunition ammoHalf = ammoMap.get(ammoTypeHalf);

        if (null == ammo) {
          throw new IOException("Couldn't find ammo type: " + ammoType);
        }

        if (null == ammoHalf) {
          throw new IOException("Couldn't find ammo type: " + ammoTypeHalf);
        }

        ReflectionUtil.setField(Ammunition.class, ammo, "type", ammoWeapon.getHardpointType());
        ReflectionUtil.setField(Ammunition.class, ammoHalf, "type", ammoWeapon.getHardpointType());
        ReflectionUtil.setField(AmmoWeapon.class, ammoWeapon, "ammoType", ammo);
        ReflectionUtil.setField(AmmoWeapon.class, ammoWeapon, "ammoHalfType", ammoHalf);
      }

      if (item instanceof final MissileWeapon weapon) {
        final int upgradeKey = weapon.getRequiredUpgradeID();
        if (upgradeKey <= 0) {
          continue;
        }

        final Upgrade upgrade = lookupUpgrade(upgradeKey);
        if (upgrade instanceof final GuidanceUpgrade guidanceUpgrade) {
          ReflectionUtil.setField(MissileWeapon.class, weapon, "requiredGuidance", guidanceUpgrade);

          final Attribute spread = weapon.getRangeProfile().getSpread();
          if (null != spread) {
            final double newValue = spread.getBaseValue() * guidanceUpgrade.getSpreadFactor();
            spread.setBaseValue(newValue);
          }
        }
      }
    }
  }

  /**
   * Parses all {@link Environment} from the game files.
   *
   * @param aGameVFS A {@link GameVFS} to parse data from.
   */
  private void parseEnvironments(GameVFS aGameVFS) throws Exception {
    final File[] levels = aGameVFS.listGameDir(new File("Game/Levels"));
    if (levels == null) {
      throw new IOException("Couldn't find environments!");
    }

    final XStream xstream = Database.makeMwoSuitableXStream();
    xstream.alias("Mission", Mission.class);
    xstream.alias("Entity", Mission.Entity.class);
    xstream.alias("Object", Mission.Entity.class);
    xstream.alias("Properties", Mission.Entity.EntityProperties.class);

    for (final File file : levels) {
      // Skip the tutorials and mechlab
      if (file.getName().toLowerCase().contains("tutorial")
          || file.getName().toLowerCase().contains("mechlab")) {
        continue;
      }

      final String uiTag = "ui_" + file.getName();
      final String uiName = localisation.key2string(uiTag);
      try (GameFile missionFile = aGameVFS.openGameFile(new File(file, "mission_mission0.xml"))) {
        final Mission mission = (Mission) xstream.fromXML(missionFile.stream);

        boolean found = false;
        for (final Mission.Entity entity : mission.Objects) {
          if (entity.EntityClass != null
              && entity.EntityClass.equalsIgnoreCase("worldparameters")) {
            environments.add(new Environment(uiName, entity.Properties.temperature));
            found = true;
            break;
          }
        }
        if (!found) {
          environments.add(new Environment(uiName, 0.0));
        }
      }
    }
  }

  private void parseOmniPods(GameVFS aGameVFS) throws IOException {
    final Set<String> series =
        mergedXML.OmniPodList.stream()
            .map((omniPod) -> omniPod.chassis)
            .collect(Collectors.toSet());

    for (final String chassis : series) {
      try (var omniPodsFile = aGameVFS.openGameFile(MechReferenceXML.omniPodsXmlPath(chassis))) {
        final XMLOmniPods xmlOmniPods = XMLOmniPods.fromXml(omniPodsFile.stream);
        try (GameFile hardPointsXmlFile =
            aGameVFS.openGameFile(MechReferenceXML.hardPointsXmlPath(chassis))) {
          final XMLHardpoints hardPoints = XMLHardpoints.fromXml(hardPointsXmlFile.stream);
          omniPods.addAll(xmlOmniPods.asOmniPods(mergedXML, hardPoints, this));
        }
      } catch (final Exception e) {
        throw new IOException("Unable to load chassis configuration! Chassis: " + chassis, e);
      }
    }
  }

  private void parseStockLoadouts(GameVFS aGameVFS) throws ParseErrorException {
    // For some reason, as of the patch 2016-06-21 some stock loadouts contain pilot
    // modules in the mechs which
    // are ignored by the game client. No mention of plans to add pilot modules to
    // stock loadouts have been
    // announced by PGI. We can only assume that this is a bug for now. We filter
    // out all pilot modules from the
    // stock loadouts before storing them.
    final Set<Integer> itemDenyList =
        consumables.stream().map(Consumable::getId).collect(Collectors.toSet());

    for (final Chassis chassis : chassis) {
      final File loadoutXmlFilePath =
          new File("Game/Libs/MechLoadout/" + chassis.getKey().toLowerCase() + ".xml");
      try (var loadoutXmlFile = aGameVFS.openGameFile(loadoutXmlFilePath)) {
        final XMLLoadout stockXML = XMLLoadout.fromXml(loadoutXmlFile.stream);

        StockLoadout.StockComponent.ActuatorState leftArmState = null;
        StockLoadout.StockComponent.ActuatorState rightArmState = null;
        if (stockXML.actuatorState != null) {
          leftArmState =
              StockLoadout.StockComponent.ActuatorState.fromMwoString(
                  stockXML.actuatorState.LeftActuatorState);
          rightArmState =
              StockLoadout.StockComponent.ActuatorState.fromMwoString(
                  stockXML.actuatorState.RightActuatorState);
        }

        final List<StockLoadout.StockComponent> components = new ArrayList<>();
        for (final XMLLoadout.Component component : stockXML.ComponentList) {
          Stream<Integer> itemIdStream = Stream.empty();
          if (component.Ammo != null) {
            itemIdStream = concat(itemIdStream, component.Ammo.stream().map(aAmmo -> aAmmo.ItemID));
          }
          if (component.Module != null) {
            itemIdStream =
                concat(itemIdStream, component.Module.stream().map(aModule -> aModule.ItemID));
          }
          if (component.Weapon != null) {
            itemIdStream =
                concat(itemIdStream, component.Weapon.stream().map(aWeapon -> aWeapon.ItemID));
          }
          final List<Integer> items =
              itemIdStream
                  .filter(aItem -> !itemDenyList.contains(aItem))
                  .collect(Collectors.toList());

          Integer omniPod = null;
          if (chassis instanceof ChassisOmniMech && null != component.OmniPod) {
            omniPod = Integer.parseInt(component.OmniPod);
          }

          final Location location = Location.fromMwoName(component.ComponentName);
          final boolean isRear = Location.isRear(component.ComponentName);
          int armourFront = isRear ? 0 : component.Armor;
          int armourBack = isRear ? component.Armor : 0;

          // Merge front and back sides
          final Iterator<StockLoadout.StockComponent> it = components.iterator();
          while (it.hasNext()) {
            final StockLoadout.StockComponent stockComponent = it.next();
            if (stockComponent.getLocation() == location) {
              items.addAll(stockComponent.getItems());
              armourFront = isRear ? stockComponent.getArmourFront() : armourFront;
              armourBack = isRear ? armourBack : stockComponent.getArmourBack();
              omniPod = stockComponent.getOmniPod().orElse(null);
              it.remove();
              break;
            }
          }

          final StockLoadout.StockComponent.ActuatorState actuatorState =
              location == Location.LeftArm
                  ? leftArmState
                  : location == Location.RightArm ? rightArmState : null;

          final StockLoadout.StockComponent stockComponent =
              new StockLoadout.StockComponent(
                  location, armourFront, armourBack, items, omniPod, actuatorState);
          components.add(stockComponent);
        }

        int armourId = 2810; // Standard armour
        int structureId = 3100; // Standard Structure
        int heatsinkId = 3003; // Standard heat sinks
        int guidanceId = 3051; // No Artemis

        if (stockXML.upgrades != null) {
          armourId = stockXML.upgrades.armor.ItemID;
          structureId = stockXML.upgrades.structure.ItemID;
          heatsinkId = stockXML.upgrades.heatsinks.ItemID;
          guidanceId = stockXML.upgrades.artemis.Equipped != 0 ? 3050 : 3051;
        }
        final StockLoadout loadout =
            new StockLoadout(
                chassis.getId(), components, armourId, structureId, heatsinkId, guidanceId);
        stockLoadouts.add(loadout);
      } catch (final Throwable e) {
        throw new ParseErrorException(
            "Error while parsing stock loadout for: "
                + chassis.getName()
                + " from: "
                + loadoutXmlFilePath,
            e);
      }
    }
  }

  private void parseUpgrades() {
    for (var upgradeType : mergedXML.UpgradeTypeList) {
      upgrades.add(upgradeType.asUpgrade(this));
    }
  }

  Database generateDatabase(String version, GameVFS gameVFS) throws Exception {

    parseItems();
    parseConsumables();
    parseUpgrades();
    postProcessItems();

    parseOmniPods(gameVFS);
    parseChassis(gameVFS);
    parseEnvironments(gameVFS);
    parseStockLoadouts(gameVFS);
    return new Database(
        version,
        checksums,
        items,
        upgrades,
        omniPods,
        consumables,
        chassis,
        environments,
        stockLoadouts,
        modifierDescriptions);
  }
}

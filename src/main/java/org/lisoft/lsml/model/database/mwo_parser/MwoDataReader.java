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
package org.lisoft.lsml.model.database.mwo_parser;

import static java.util.stream.Stream.concat;

import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.chassi.*;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.mwo_parser.GameVFS.GameFile;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent.ActuatorState;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.upgrades.*;
import org.lisoft.lsml.util.ReflectionUtil;

/**
 * This class handles all the dirty details about loading data from the MWO game files and produces
 * a usable {@link Database} file.
 *
 * @author Li Song
 */
public class MwoDataReader {

  private static final File MDF_ROOT = new File("Game/mechs/Objects/mechs/");
  private static final File MECH_ID_MAP_XML = new File("Game/Libs/Items/MechIDMap.xml");
  private static final List<File> FILES_TO_PARSE =
      Arrays.asList(
          new File("Game/Libs/Items/Weapons/Weapons.xml"),
          new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml"),
          new File("Game/Libs/Items/Modules/Ammo.xml"),
          new File("Game/Libs/Items/Modules/Engines.xml"),
          new File("Game/Libs/Items/Modules/Equipment.xml"),
          new File("Game/Libs/Items/Modules/JumpJets.xml"),
          new File("Game/Libs/Items/Modules/Internals.xml"),
          new File("Game/Libs/Items/Modules/PilotModules.xml"),
          new File("Game/Libs/Items/Modules/WeaponMods.xml"),
          new File("Game/Libs/Items/Modules/Consumables.xml"),
          new File("Game/Libs/Items/Modules/MASC.xml"),
          new File("Game/Libs/Items/Mechs/Mechs.xml"),
          new File("Game/Libs/Items/OmniPods.xml"));
  private final ErrorReporter errorReporter;
  private final String runningVersion;

  @Inject
  public MwoDataReader(@Named("version") String aRunningVersion, ErrorReporter aErrorReporter) {
    runningVersion = aRunningVersion;
    errorReporter = aErrorReporter;
  }

  /**
   * Reads the latest data from the game files and creates a new database.
   *
   * @param aLog a {@link Writer} to write any log messages to.
   * @param aGameDirectory A directory that contains a game installation.
   * @return An {@link Optional} {@link Database} if the parsing succeeds.
   */
  public Optional<Database> parseGameFiles(Writer aLog, File aGameDirectory) {
    try {
      final GameVFS gameVFS = new GameVFS(aGameDirectory);

      Localisation.initialize(
          gameVFS); // FIXME: Make localisation into an object and inject it into the reader.

      final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
      final Map<String, Long> checksums = new HashMap<>();
      final XMLItemStats itemStatsXml = new XMLItemStats();
      for (final GameFile file : gameFiles) {
        itemStatsXml.append(file);
        checksums.put(file.path, file.crc32);
      }

      final Map<Integer, Object> id2obj = new HashMap<>();
      final Map<String, ModifierDescription> modifierDescriptions =
          new HashMap<>(); // Filled in as we go.

      final List<Item> items = parseItems(itemStatsXml);
      addAllTo(id2obj, items);

      final List<Consumable> modules = parseConsumables(itemStatsXml);
      addAllTo(id2obj, modules);

      final List<Upgrade> upgrades = parseUpgrades(itemStatsXml, id2obj);
      addAllTo(id2obj, upgrades);

      postProcessItems(id2obj);

      final List<OmniPod> omniPods =
          parseOmniPods(itemStatsXml, id2obj, modifierDescriptions, gameVFS);
      addAllTo(id2obj, omniPods);

      final List<Chassis> chassis =
          parseChassis(itemStatsXml, id2obj, modifierDescriptions, gameVFS);
      addAllTo(id2obj, chassis);

      // For some reason, as of the patch 2016-06-21 some stock loadouts contain pilot
      // modules in the mechs which
      // are ignored by the game client. No mention of plans to add pilot modules to
      // stock loadouts have been
      // announced by PGI. We can only assume that this is a bug for now. We filter
      // out all pilot modules from the
      // stock loadouts before storing them.
      final Set<Integer> itemBlackList =
          modules.stream().map(Consumable::getId).collect(Collectors.toSet());
      final List<Environment> environments = parseEnvironments(aLog, gameVFS);
      final List<StockLoadout> stockLoadouts = parseStockLoadouts(chassis, itemBlackList, gameVFS);

      return Optional.of(
          new Database(
              runningVersion,
              checksums,
              items,
              upgrades,
              omniPods,
              modules,
              chassis,
              environments,
              stockLoadouts,
              modifierDescriptions));
    } catch (final Throwable t) {
      errorReporter.error(
          "Parse error",
          "This usually happens when PGI has changed the structure of the data files "
              + "in a patch. Please look for an updated version of LSML at www.li-soft.org."
              + " In the meanwhile LSML will continue to function with the data from the last"
              + " successfully parsed patch.",
          t);
      return Optional.empty();
    }
  }

  /**
   * Compares the database to the game files and determines if there is any reason to attempt a
   * further parse.
   *
   * @param aDatabase The {@link Database} to compare to.
   * @param aGameDirectory The directory to read game files to compare to.
   * @return <code>true</code> if the game files have newer data than what's in the database.
   */
  public boolean shouldUpdate(Database aDatabase, File aGameDirectory) {
    try {
      final GameVFS gameVFS = new GameVFS(aGameDirectory);
      final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
      final Map<String, Long> checkSums = aDatabase.getChecksums();
      if (gameFiles.size() != checkSums.size()) {
        return true;
      }

      for (final GameFile gameFile : gameFiles) {
        if (gameFile.crc32 != checkSums.get(gameFile.path)) {
          return true;
        }
      }
    } catch (final IOException e) {
      errorReporter.error(
          "Error reading data files", "LSML couldn't open game data files for reading.", e);
    }
    return false;
  }

  private void addAllTo(Map<Integer, Object> aId2obj, Collection<? extends MwoObject> aEquipment) {
    for (final MwoObject eq : aEquipment) {
      aId2obj.put(eq.getId(), eq);
    }
  }

  /**
   * Parses all inner sphere {@link ChassisStandard} from the ItemStats.xml file and related files.
   *
   * @param aItemStatsXml A {@link GameFile} containing the ItemStats.xml file to parse.
   * @param aId2obj The {@link Database} that is being parsed.
   * @param aGameVFS A {@link GameVFS} used to open other game files.
   * @return A List of all {@link ChassisStandard} found in aItemStatsXml.
   */
  private List<Chassis> parseChassis(
      XMLItemStats aItemStatsXml,
      Map<Integer, Object> aId2obj,
      Map<String, ModifierDescription> aModifierDescriptors,
      GameVFS aGameVFS)
      throws Exception {

    final List<Chassis> ans = new ArrayList<>();
    try (GameFile mechIdMapFile = aGameVFS.openGameFile(MECH_ID_MAP_XML)) {
      XMLMechIdMap mechIdMap = XMLMechIdMap.fromXml(mechIdMapFile.stream);

      for (final XMLItemStatsMech mech : aItemStatsXml.MechList) {
        try {
          final String mdfFilePath = mech.chassis + "/" + mech.name + ".mdf";
          try (GameFile mdfFile = aGameVFS.openGameFile(new File(MDF_ROOT, mdfFilePath))) {
            final MdfMechDefinition mdf = MdfMechDefinition.fromXml(mdfFile.stream);

            if (!mdf.isUsable()) {
              continue;
            }

            if (mdf.isOmniMech()) {
              final File loadoutXml = new File("Game/Libs/MechLoadout/" + mech.name + ".xml");
              try (GameFile loadoutXmlFile = aGameVFS.openGameFile(loadoutXml)) {
                final XMLLoadout stockXML = XMLLoadout.fromXml(loadoutXmlFile.stream);
                ans.add(mdf.asChassisOmniMech(mech, aId2obj, mechIdMap, stockXML));
              }
            } else {
              final String hardPointsXml = mech.chassis + "/" + mech.chassis + "-hardpoints.xml";
              try (GameFile hardPointsXmlFile =
                  aGameVFS.openGameFile(new File(MDF_ROOT, hardPointsXml))) {
                final XMLHardpoints hardPoints = XMLHardpoints.fromXml(hardPointsXmlFile.stream);
                ans.add(
                    mdf.asChassisStandard(
                        mech, aId2obj, aModifierDescriptors, mechIdMap, hardPoints));
              }
            }
          }
        } catch (final Exception e) {
          throw new IOException("Unable to load chassis configuration for [" + mech.name + "]!", e);
        }
      }
    }
    return ans;
  }

  /**
   * Parses all {@link Environment} from the game files.
   *
   * @param aGameVFS A {@link GameVFS} to parse data from.
   * @return A List of all {@link Environment} found in the game files.
   */
  private List<Environment> parseEnvironments(Writer aLog, GameVFS aGameVFS) throws Exception {
    final List<Environment> ans = new ArrayList<>();

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
      final String uiName = Localisation.key2string(uiTag);
      try (GameFile missionFile = aGameVFS.openGameFile(new File(file, "mission_mission0.xml"))) {
        final Mission mission = (Mission) xstream.fromXML(missionFile.stream);

        boolean found = false;
        for (final Mission.Entity entity : mission.Objects) {
          if (entity.EntityClass != null
              && entity.EntityClass.equalsIgnoreCase("worldparameters")) {
            ans.add(new Environment(uiName, entity.Properties.temperature));
            found = true;
            break;
          }
        }
        if (!found) {
          ans.add(new Environment(uiName, 0.0));
          if (aLog != null) {
            aLog.append("Unable to load temperature for level: ")
                .append(uiName)
                .append("! Assuming 0.0.")
                .append(System.getProperty("line.separator"));
          }
        }
      }
    }
    return ans;
  }

  /**
   * Parses all {@link Item}s from the ItemStats.xml file.
   *
   * @param aItemStatsXml A {@link GameFile} containing the ItemStats.xml file to parse.
   * @return A List of all {@link Item}s found in aItemStatsXml.
   */
  private List<Item> parseItems(XMLItemStats aItemStatsXml) throws IOException {
    final List<Item> ans = new ArrayList<>();
    // Modules (they contain ammo now, and weapons need to find their ammo
    // types when parsed)
    final Iterator<ItemStatsModule> it = aItemStatsXml.ModuleList.iterator();
    while (it.hasNext()) {
      final ItemStatsModule statsModule = it.next();
      final Optional<Item> optionalItem = statsModule.asItem();
      if (optionalItem.isPresent()) {
        ans.add(optionalItem.get());
        it.remove();
      }
    }

    // Weapons next.
    for (final ItemStatsWeapon statsWeapon : aItemStatsXml.WeaponList) {
      statsWeapon.asWeapon(aItemStatsXml.WeaponList).ifPresent(ans::add);
    }
    return ans;
  }

  /**
   * Parses consumable data from the data files.
   *
   * @param aItemStatsXml A {@link GameFile} containing the ItemStats.xml file to parse.
   * @return A {@link List} of consumable modules
   */
  private List<Consumable> parseConsumables(XMLItemStats aItemStatsXml) {
    final List<Consumable> ans = new ArrayList<>();
    final Iterator<ItemStatsModule> it = aItemStatsXml.ModuleList.iterator();
    while (it.hasNext()) {
      final ItemStatsModule statsModule = it.next();
      Optional<Consumable> optionalConsumable = statsModule.asConsumable();
      if (optionalConsumable.isPresent()) {
        ans.add(optionalConsumable.get());
        it.remove();
      }
    }
    return ans;
  }

  private List<OmniPod> parseOmniPods(
      XMLItemStats aItemStatsXml,
      Map<Integer, Object> aId2obj,
      Map<String, ModifierDescription> aModifierDescriptors,
      GameVFS aGameVFS)
      throws IOException {
    final List<OmniPod> ans = new ArrayList<>();
    final Set<String> series =
        aItemStatsXml.OmniPodList.stream()
            .map((omniPod) -> omniPod.chassis)
            .collect(Collectors.toSet());

    for (final String chassis : series) {
      final String omniPodsFilePath = chassis + "/" + chassis + "-omnipods.xml";
      try (var omniPodsFile = aGameVFS.openGameFile(new File(MDF_ROOT, omniPodsFilePath))) {
        final XMLOmniPods omniPods = XMLOmniPods.fromXml(omniPodsFile.stream);
        final String hardPointsXmlFilePath = chassis + "/" + chassis + "-hardpoints.xml";
        try (GameFile hardPointsXmlFile =
            aGameVFS.openGameFile(new File(MDF_ROOT, hardPointsXmlFilePath))) {
          final XMLHardpoints hardPoints = XMLHardpoints.fromXml(hardPointsXmlFile.stream);
          ans.addAll(omniPods.asOmniPods(aItemStatsXml, hardPoints, aId2obj, aModifierDescriptors));
        }
      } catch (final Exception e) {
        throw new IOException("Unable to load chassis configuration! Chassis: " + chassis, e);
      }
    }
    return ans;
  }

  private List<StockLoadout> parseStockLoadouts(
      List<Chassis> aChassis, Set<Integer> aItemBlackList, GameVFS aGameVFS)
      throws ParseErrorException {
    final List<StockLoadout> ans = new ArrayList<>();
    for (final Chassis chassis : aChassis) {
      final File loadoutXmlFilePath =
          new File("Game/Libs/MechLoadout/" + chassis.getKey().toLowerCase() + ".xml");
      try (var loadoutXmlFile = aGameVFS.openGameFile(loadoutXmlFilePath)) {
        final XMLLoadout stockXML = XMLLoadout.fromXml(loadoutXmlFile.stream);

        ActuatorState leftArmState = null;
        ActuatorState rightArmState = null;
        if (stockXML.actuatorState != null) {
          leftArmState = ActuatorState.fromMwoString(stockXML.actuatorState.LeftActuatorState);
          rightArmState = ActuatorState.fromMwoString(stockXML.actuatorState.RightActuatorState);
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
                  .filter(aItem -> !aItemBlackList.contains(aItem))
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
          final Iterator<StockComponent> it = components.iterator();
          while (it.hasNext()) {
            final StockComponent stockComponent = it.next();
            if (stockComponent.getLocation() == location) {
              items.addAll(stockComponent.getItems());
              armourFront = isRear ? stockComponent.getArmourFront() : armourFront;
              armourBack = isRear ? armourBack : stockComponent.getArmourBack();
              omniPod = stockComponent.getOmniPod().orElse(null);
              it.remove();
              break;
            }
          }

          final ActuatorState actuatorState =
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
        ans.add(loadout);
      } catch (final Throwable e) {
        throw new ParseErrorException(
            "Error while parsing stock loadout for: "
                + chassis.getName()
                + " from: "
                + loadoutXmlFilePath,
            e);
      }
    }
    return ans;
  }

  private List<Upgrade> parseUpgrades(XMLItemStats aItemStatsXml, Map<Integer, Object> id2obj) {
    return aItemStatsXml.UpgradeTypeList.stream()
        .map(x -> x.asUpgrade(id2obj))
        .collect(Collectors.toList());
  }

  private void postProcessItems(final Map<Integer, Object> id2obj) throws Exception {
    final Map<String, Ammunition> ammoMap =
        id2obj.values().stream()
            .filter(o -> o instanceof Ammunition)
            .map(o -> (Ammunition) o)
            .collect(Collectors.toMap(a -> a.getKey().toLowerCase(), Function.identity()));

    for (final Object obj : id2obj.values()) {
      if (obj instanceof final AmmoWeapon weapon) {
        if (weapon.hasBuiltInAmmo()) {
          continue;
        }

        final String ammoType = weapon.getAmmoId().toLowerCase();
        final String ammoTypeHalf = ammoType + "half";

        final Ammunition ammo = ammoMap.get(ammoType);
        final Ammunition ammoHalf = ammoMap.get(ammoTypeHalf);

        ReflectionUtil.setField(Ammunition.class, ammo, "type", weapon.getHardpointType());
        ReflectionUtil.setField(Ammunition.class, ammoHalf, "type", weapon.getHardpointType());

        if (null == ammo) {
          throw new IOException("Couldn't find ammo type: " + ammoType);
        }

        if (null == ammoHalf) {
          throw new IOException("Couldn't find ammo type: " + ammoType);
        }

        ReflectionUtil.setField(AmmoWeapon.class, weapon, "ammoType", ammo);
        ReflectionUtil.setField(AmmoWeapon.class, weapon, "ammoHalfType", ammoHalf);
      }

      if (obj instanceof final MissileWeapon weapon) {
        final int upgradeKey = weapon.getRequiredUpgradeID();
        if (upgradeKey <= 0) {
          continue;
        }

        final Object upgrade = id2obj.get(upgradeKey);
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
}

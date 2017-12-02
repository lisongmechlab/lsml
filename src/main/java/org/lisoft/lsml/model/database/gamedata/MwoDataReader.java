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
package org.lisoft.lsml.model.database.gamedata;

import static java.util.stream.Stream.concat;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.application.ErrorReporter;
import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.database.Database;
import org.lisoft.lsml.model.database.ItemDB;
import org.lisoft.lsml.model.database.gamedata.GameVFS.GameFile;
import org.lisoft.lsml.model.database.gamedata.helpers.ItemStatsModule;
import org.lisoft.lsml.model.database.gamedata.helpers.ItemStatsOmniPodType;
import org.lisoft.lsml.model.database.gamedata.helpers.ItemStatsUpgradeType;
import org.lisoft.lsml.model.database.gamedata.helpers.ItemStatsWeapon;
import org.lisoft.lsml.model.database.gamedata.helpers.Mission;
import org.lisoft.lsml.model.database.gamedata.helpers.XMLItemStatsMech;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.Consumable;
import org.lisoft.lsml.model.item.ConsumableType;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.MwoObject;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent.ActuatorState;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;
import org.lisoft.lsml.model.upgrades.UpgradeType;
import org.lisoft.lsml.util.ReflectionUtil;

import com.thoughtworks.xstream.XStream;

/**
 * This class handles all the dirty details about loading data from the MWO game files and produces a usable
 * {@link Database} file.
 *
 * @author Emily Björk
 */
public class MwoDataReader {
    private final static List<File> FILES_TO_PARSE = Arrays.asList(new File("Game/Libs/Items/Weapons/Weapons.xml"),
            new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml"), new File("Game/Libs/Items/Modules/Ammo.xml"),
            new File("Game/Libs/Items/Modules/Engines.xml"), new File("Game/Libs/Items/Modules/Equipment.xml"),
            new File("Game/Libs/Items/Modules/JumpJets.xml"), new File("Game/Libs/Items/Modules/Internals.xml"),
            new File("Game/Libs/Items/Modules/PilotModules.xml"), new File("Game/Libs/Items/Modules/WeaponMods.xml"),
            new File("Game/Libs/Items/Modules/Consumables.xml"), new File("Game/Libs/Items/Modules/MASC.xml"),
            new File("Game/Libs/Items/Mechs/Mechs.xml"), new File("Game/Libs/Items/OmniPods.xml"));

    public static Item findItem(int aItemId, List<Item> aItems) {
        for (final Item item : aItems) {
            if (item.getId() == aItemId) {
                return item;
            }
        }

        throw new IllegalArgumentException("Unknown item: " + aItemId);
    }

    public static Item findItem(String aKey, List<? extends Item> aItems) {
        for (final Item item : aItems) {
            if (aKey.equalsIgnoreCase(item.getKey()) || aKey.equalsIgnoreCase(item.getName())) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown item: " + aKey);
    }

    static public OmniPod findOmniPod(List<OmniPod> aOmniPods, int aOmniPod) {
        for (final OmniPod item : aOmniPods) {
            if (item.getId() == aOmniPod) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown OmniPod: " + aOmniPod);
    }

    private final String runningVersion;
    private final ErrorReporter errorReporter;

    @Inject
    public MwoDataReader(@Named("version") String aRunningVersion, ErrorReporter aErrorReporter) {
        runningVersion = aRunningVersion;
        errorReporter = aErrorReporter;
    }

    /**
     * Reads the latest data from the game files and creates a new database.
     *
     * @param aLog
     *            a {@link Writer} to write any log messages to.
     * @param aGameDirectory
     *            A directory that contains a game install.
     */
    public Optional<Database> parseGameFiles(Writer aLog, File aGameDirectory) throws Exception {
        try {
            final GameVFS gameVFS = new GameVFS(aGameDirectory);

            Localisation.initialize(gameVFS); // FIXME: Make Localization into an object and inject it into the reader.

            final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
            final Map<String, Long> checksums = new HashMap<>();
            final XMLItemStats itemStatsXml = new XMLItemStats();
            for (final GameFile file : gameFiles) {
                itemStatsXml.append(file);
                checksums.put(file.path, file.crc32);
            }

            final Map<Integer, Object> id2obj = new HashMap<>();
            final Map<String, ModifierDescription> modifierDescriptions = new HashMap<>(); // Filled in as we go.

            final List<Item> items = parseItems(itemStatsXml);
            addAllTo(id2obj, items);

            final List<Consumable> modules = parseModules(itemStatsXml, gameVFS);
            addAllTo(id2obj, modules);

            final List<Upgrade> upgrades = parseUpgrades(itemStatsXml, id2obj);
            addAllTo(id2obj, upgrades);

            postProcessItems(id2obj);

            final List<OmniPod> omniPods = parseOmniPods(itemStatsXml, id2obj, modifierDescriptions, gameVFS);
            addAllTo(id2obj, omniPods);

            final List<Chassis> chassis = parseChassis(itemStatsXml, id2obj, modifierDescriptions, gameVFS);
            addAllTo(id2obj, chassis);

            // For some reason, as of the patch 2016-06-21 some stock loadouts contain pilot modules in the mechs which
            // are ignored by the game client. No mention of plans to add pilot modules to stock loadouts have been
            // announced by PGI. We can only assume that this is a bug for now. We filter out all pilot modules from the
            // stock loadouts before storing them.
            final Set<Integer> itemBlackList = modules.stream().map(Consumable::getId).collect(Collectors.toSet());
            final List<Environment> environments = parseEnvironments(aLog, gameVFS);
            final List<StockLoadout> stockLoadouts = parseStockLoadouts(chassis, itemBlackList, gameVFS);

            return Optional.of(new Database(runningVersion, checksums, items, upgrades, omniPods, modules, chassis,
                    environments, stockLoadouts, modifierDescriptions));
        }
        catch (final Throwable t) {
            errorReporter.error("Parse error",
                    "This usually happens when PGI has changed the structure of the data files "
                            + "in a patch. Please look for an updated version of LSML at www.li-soft.org."
                            + " In the meanwhile LSML will continue to function with the data from the last"
                            + " successfully parsed patch.",
                    t);
            return Optional.empty();
        }
    }

    /**
     * Compares the database to the game files and determines if there is any reason to attempt a further parse.
     *
     * @param aDatabase
     *            The {@link Database} to compare to.
     * @param aGameDirectory
     *            The directory to read game files to compare to.
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
        }
        catch (final IOException e) {
            errorReporter.error("Error reading data files", "LSML couldn't open game data files for reading.", e);
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
     * @param aGameVfs
     *            A {@link GameVFS} used to open other game files.
     * @param aItemStatsXml
     *            A {@link GameFile} containing the ItemStats.xml file to parse.
     * @param aId2obj
     *            The {@link Database} that is being parsed.
     * @param aGameVFS
     * @return A List of all {@link ChassisStandard} found in aItemStatsXml.
     */
    private List<Chassis> parseChassis(XMLItemStats aItemStatsXml, Map<Integer, Object> aId2obj,
            Map<String, ModifierDescription> aModifierDescriptors, GameVFS aGameVFS) throws IOException {
        final XMLMechIdMap mechIdMap = XMLMechIdMap.fromXml(aGameVFS.openGameFile(GameVFS.MECH_ID_MAP_XML).stream);
        final List<Chassis> ans = new ArrayList<>();

        for (final XMLItemStatsMech mech : aItemStatsXml.MechList) {
            try {
                final String mdfFile = mech.chassis + "/" + mech.name + ".mdf";
                final MdfMechDefinition mdf = MdfMechDefinition
                        .fromXml(aGameVFS.openGameFile(new File(GameVFS.MDF_ROOT, mdfFile)).stream);

                if (!mdf.isUsable()) {
                    continue;
                }

                if (mdf.isOmniMech()) {
                    final File loadoutXml = new File("Game/Libs/MechLoadout/" + mech.name + ".xml");
                    final XMLLoadout stockXML = XMLLoadout.fromXml(aGameVFS.openGameFile(loadoutXml).stream);
                    ans.add(mdf.asChassisOmniMech(mech, aId2obj, mechIdMap, stockXML));
                }
                else {
                    final String hardPointsXml = mech.chassis + "/" + mech.chassis + "-hardpoints.xml";
                    final XMLHardpoints hardPoints = XMLHardpoints
                            .fromXml(aGameVFS.openGameFile(new File(GameVFS.MDF_ROOT, hardPointsXml)).stream);
                    ans.add(mdf.asChassisStandard(mech, aId2obj, aModifierDescriptors, mechIdMap, hardPoints));
                }
            }
            catch (final Exception e) {
                throw new IOException("Unable to load chassi configuration for [" + mech.name + "]!", e);
            }
        }
        return ans;
    }

    /**
     * Parses all {@link Environment} from the game files.
     *
     * @param aGameVFS
     *
     * @param aGameVfs
     *            A {@link GameVFS} to parse data from.
     * @return A List of all {@link Environment} found in the game files.
     */
    private List<Environment> parseEnvironments(Writer aLog, GameVFS aGameVFS) throws IOException {
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
            if (file.getName().toLowerCase().contains("tutorial") || file.getName().toLowerCase().contains("mechlab")) {
                continue;
            }

            final String uiTag = "ui_" + file.getName();
            final String uiName = Localisation.key2string(uiTag);
            final Mission mission = (Mission) xstream
                    .fromXML(aGameVFS.openGameFile(new File(file, "mission_mission0.xml")).stream);

            boolean found = false;
            for (final Mission.Entity entity : mission.Objects) {
                if (entity.EntityClass != null && entity.EntityClass.toLowerCase().equals("worldparameters")) {
                    ans.add(new Environment(uiName, entity.Properties.temperature));
                    found = true;
                    break;
                }
            }
            if (!found) {
                ans.add(new Environment(uiName, 0.0));
                if (aLog != null) {
                    aLog.append("Unable to load temprature for level: ").append(uiName).append("! Assuming 0.0.")
                            .append(System.getProperty("line.separator"));
                }
            }
        }
        return ans;
    }

    /**
     * Parses all {@link Item}s from the ItemStats.xml file.
     *
     * @param aItemStatsXml
     *            A {@link GameFile} containing the ItemStats.xml file to parse.
     * @return A List of all {@link Item}s found in aItemStatsXml.
     */
    private List<Item> parseItems(XMLItemStats aItemStatsXml) throws IOException {
        final List<Item> ans = new ArrayList<>();

        // Special items
        ans.add(new Internal("ENGINE", "", "mdf_Engine", ItemDB.ENGINE_INTERNAL_ID, 3, 0, HardPointType.NONE, 15,
                Faction.INNERSPHERE));
        ans.add(new Internal("C-ENGINE", "", "mdf_CEngine", ItemDB.ENGINE_INTERNAL_CLAN_ID, 2, 0, HardPointType.NONE,
                15, Faction.CLAN));

        // Modules (they contain ammo now, and weapons need to find their ammo
        // types when parsed)
        final Iterator<ItemStatsModule> it = aItemStatsXml.ModuleList.iterator();
        while (it.hasNext()) {
            final ItemStatsModule statsModule = it.next();
            final Item item = statsModule.asItem();
            if (null != item) {
                ans.add(item);
                it.remove();
            }
        }

        // Weapons next.
        for (final ItemStatsWeapon statsWeapon : aItemStatsXml.WeaponList) {
            if (statsWeapon.isUsable()) {
                ans.add(statsWeapon.asWeapon(aItemStatsXml.WeaponList));
            }
        }
        return ans;
    }

    /**
     * Parses module data from the data files.
     *
     * @param aGameVfs
     * @param aItemStatsXml
     * @param aGameVFS
     * @return
     * @throws Exception
     */
    private List<Consumable> parseModules(XMLItemStats aItemStatsXml, GameVFS aGameVFS) throws Exception {

        final List<Consumable> ans = new ArrayList<>();
        final Iterator<ItemStatsModule> it = aItemStatsXml.ModuleList.iterator();
        while (it.hasNext()) {
            boolean processed = true;
            final ItemStatsModule statsModule = it.next();
            switch (statsModule.CType) {
                case "CCoolantFlushStats":
                case "CStrategicStrikeStats":
                case "CUAVStats": {
                    ans.add(new Consumable(statsModule.getUiName(), statsModule.getUiShortName(),
                            statsModule.getUiDescription(), statsModule.getMwoKey(), statsModule.getMwoId(),
                            Faction.fromMwo(statsModule.faction),
                            ConsumableType.fromMwo(statsModule.ConsumableStats.equipType)));
                    break;
                }
                case "CPilotModule":
                case "CPilotModuleStats":
                case "CWeaponModStats":
                case "CAdvancedZoomStats":
                case "CBackFacingTargetStats":
                case "CCaptureAcceleratorStats":
                case "CGyroStats":
                case "CHillClimbStats":
                case "CSeismicStats":
                case "CSensorRangeStats":
                case "CTargetDecayStats":
                case "CTargetInfoGatherStats":
                case "CStealthDecayStats":
                case "CCrippledPerformanceStats":
                case "CStrategicStrikeUpgrade":
                case "CImpulseElectricFieldStats":
                    // Ignore all of these.
                    break;
                default:
                    processed = false;
                    System.out.println("Unknown module type: " + statsModule.CType);
                    break; // Other modules not yet supported
            }
            if (processed) {
                it.remove();
            }
        }
        return ans;
    }

    private List<OmniPod> parseOmniPods(XMLItemStats aItemStatsXml, Map<Integer, Object> aId2obj,
            Map<String, ModifierDescription> aModifierDescriptors, GameVFS aGameVFS) throws IOException {
        final List<OmniPod> ans = new ArrayList<>();
        final Set<String> series = new HashSet<>();
        for (final ItemStatsOmniPodType omniPod : aItemStatsXml.OmniPodList) {
            series.add(omniPod.chassis);
        }
        for (final String chassis : series) {
            try {
                final String omniPodsFile = chassis + "/" + chassis + "-omnipods.xml";
                final XMLOmniPods omniPods = XMLOmniPods
                        .fromXml(aGameVFS.openGameFile(new File(GameVFS.MDF_ROOT, omniPodsFile)).stream);

                final String hardPointsXml = chassis + "/" + chassis + "-hardpoints.xml";
                final XMLHardpoints hardPoints = XMLHardpoints
                        .fromXml(aGameVFS.openGameFile(new File(GameVFS.MDF_ROOT, hardPointsXml)).stream);

                ans.addAll(omniPods.asOmniPods(aItemStatsXml, hardPoints, aId2obj, aModifierDescriptors));

            }
            catch (final Exception e) {
                throw new IOException("Unable to load chassi configuration! Chassis: " + chassis, e);
            }
        }

        return ans;
    }

    private List<StockLoadout> parseStockLoadouts(List<Chassis> aChassis, Set<Integer> aItemBlackList, GameVFS aGameVFS)
            throws ParseErrorException {
        final List<StockLoadout> ans = new ArrayList<>();

        for (final Chassis chassis : aChassis) {
            final File loadoutXml = new File("Game/Libs/MechLoadout/" + chassis.getKey().toLowerCase() + ".xml");
            try {
                final XMLLoadout stockXML = XMLLoadout.fromXml(aGameVFS.openGameFile(loadoutXml).stream);

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
                        itemIdStream = concat(itemIdStream, component.Module.stream().map(aModule -> aModule.ItemID));
                    }
                    if (component.Weapon != null) {
                        itemIdStream = concat(itemIdStream, component.Weapon.stream().map(aWeapon -> aWeapon.ItemID));
                    }
                    final List<Integer> items = itemIdStream.filter(aItem -> !aItemBlackList.contains(aItem))
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

                    final ActuatorState actuatorState = location == Location.LeftArm ? leftArmState
                            : location == Location.RightArm ? rightArmState : null;

                    final StockLoadout.StockComponent stockComponent = new StockLoadout.StockComponent(location,
                            armourFront, armourBack, items, omniPod, actuatorState);
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
                final StockLoadout loadout = new StockLoadout(chassis.getId(), components, armourId, structureId,
                        heatsinkId, guidanceId);
                ans.add(loadout);
            }
            catch (final Throwable e) {
                throw new ParseErrorException("Error while parsing stock loadout for: " + chassis.getName() + " from: "
                        + loadoutXml.toString(), e);
            }
        }
        return ans;
    }

    private List<Upgrade> parseUpgrades(XMLItemStats aItemStatsXml, Map<Integer, Object> id2obj) {
        final List<Upgrade> ans = new ArrayList<>();

        for (final ItemStatsUpgradeType upgradeType : aItemStatsXml.UpgradeTypeList) {
            final UpgradeType type = UpgradeType.fromMwo(upgradeType.CType);
            final String mwoName = upgradeType.getMwoKey();
            final String name = Localisation.key2string(upgradeType.Loc.nameTag);
            final String desc = Localisation.key2string(upgradeType.Loc.descTag);
            final Faction faction = Faction.fromMwo(upgradeType.faction);
            final int mwoid = Integer.parseInt(upgradeType.id);

            switch (type) {
                case ARMOUR: {
                    final int slots = upgradeType.SlotUsage == null ? 0 : upgradeType.SlotUsage.slots;
                    final double armourPerTon = upgradeType.ArmorTypeStats.armorPerTon;
                    ans.add(new ArmourUpgrade(name.replace("ARMOR", "ARMOUR"), desc, mwoName, mwoid, faction, slots,
                            armourPerTon));
                    break;
                }
                case ARTEMIS: {
                    final int slots = upgradeType.ArtemisTypeStats.extraSlots;
                    final double tons = upgradeType.ArtemisTypeStats.extraTons;
                    final double spread = upgradeType.ArtemisTypeStats.missileSpread;
                    ans.add(new GuidanceUpgrade(name, desc, mwoName, mwoid, faction, slots, tons, spread));
                    break;
                }
                case HEATSINK: {
                    final HeatSink heatSink = (HeatSink) id2obj.get(upgradeType.HeatSinkTypeStats.compatibleHeatSink);
                    ans.add(new HeatSinkUpgrade(name, desc, mwoName, mwoid, faction, heatSink));
                    break;
                }
                case STRUCTURE: {
                    final int slots = upgradeType.SlotUsage == null ? 0 : upgradeType.SlotUsage.slots;
                    final double structurePct = upgradeType.StructureTypeStats.weightPerTon;
                    ans.add(new StructureUpgrade(name, desc, mwoName, mwoid, faction, slots, structurePct));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown upgrade type: " + type);
            }
        }
        return ans;
    }

    private void postProcessItems(final Map<Integer, Object> id2obj) throws Exception {
        final Map<String, Ammunition> ammoMap = id2obj.values().stream().filter(o -> o instanceof Ammunition)
                .map(o -> (Ammunition) o).collect(Collectors.toMap(a -> a.getKey().toLowerCase(), Function.identity()));

        for (final Object obj : id2obj.values()) {
            if (obj instanceof AmmoWeapon) {
                final AmmoWeapon weapon = (AmmoWeapon) obj;

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

            if (obj instanceof MissileWeapon) {
                final MissileWeapon weapon = (MissileWeapon) obj;
                final int upgradeKey = weapon.getRequiredUpgradeID();
                if (upgradeKey <= 0) {
                    continue;
                }

                final Object upgrade = id2obj.get(upgradeKey);
                if (upgrade instanceof GuidanceUpgrade) {
                    final GuidanceUpgrade guidanceUpgrade = (GuidanceUpgrade) upgrade;
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

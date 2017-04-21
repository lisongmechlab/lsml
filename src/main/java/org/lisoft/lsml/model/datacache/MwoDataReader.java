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
package org.lisoft.lsml.model.datacache;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.HardPointType;
import org.lisoft.lsml.model.chassi.Location;
import org.lisoft.lsml.model.chassi.OmniPod;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS;
import org.lisoft.lsml.model.datacache.gamedata.GameVFS.GameFile;
import org.lisoft.lsml.model.datacache.gamedata.Localization;
import org.lisoft.lsml.model.datacache.gamedata.MdfMechDefinition;
import org.lisoft.lsml.model.datacache.gamedata.QuirkModifiers;
import org.lisoft.lsml.model.datacache.gamedata.XMLHardpoints;
import org.lisoft.lsml.model.datacache.gamedata.XMLItemStats;
import org.lisoft.lsml.model.datacache.gamedata.XMLLoadout;
import org.lisoft.lsml.model.datacache.gamedata.XMLMechEfficiencyTalent;
import org.lisoft.lsml.model.datacache.gamedata.XMLMechEfficiencyTalent.XMLTalentRank;
import org.lisoft.lsml.model.datacache.gamedata.XMLMechIdMap;
import org.lisoft.lsml.model.datacache.gamedata.XMLOmniPods;
import org.lisoft.lsml.model.datacache.gamedata.XMLPilotTalents;
import org.lisoft.lsml.model.datacache.gamedata.XMLPilotTalents.XMLTalent;
import org.lisoft.lsml.model.datacache.gamedata.XMLPilotTalents.XMLTalent.XMLRank;
import org.lisoft.lsml.model.datacache.gamedata.XMLQuirkDef;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsModule;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsOmniPodType;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsUpgradeType;
import org.lisoft.lsml.model.datacache.gamedata.helpers.ItemStatsWeapon;
import org.lisoft.lsml.model.datacache.gamedata.helpers.Mission;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLItemStatsMech;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLPilotModuleStats;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLPilotModuleWeaponStats;
import org.lisoft.lsml.model.datacache.gamedata.helpers.XMLWeaponStats;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.ModuleCathegory;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.MwoObject;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent.ActuatorState;
import org.lisoft.lsml.model.modifiers.MechEfficiency;
import org.lisoft.lsml.model.modifiers.MechEfficiencyType;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;
import org.lisoft.lsml.model.modifiers.ModifierType;
import org.lisoft.lsml.model.modifiers.Operation;
import org.lisoft.lsml.model.upgrades.ArmourUpgrade;
import org.lisoft.lsml.model.upgrades.GuidanceUpgrade;
import org.lisoft.lsml.model.upgrades.HeatSinkUpgrade;
import org.lisoft.lsml.model.upgrades.StructureUpgrade;
import org.lisoft.lsml.model.upgrades.Upgrade;
import org.lisoft.lsml.model.upgrades.UpgradeType;
import org.lisoft.lsml.view_fx.ErrorReporter;

import com.thoughtworks.xstream.XStream;

/**
 * This class handles all the dirty details about loading data from the MWO game files and produces a usable
 * {@link DataCache} file.
 *
 * @author Li Song
 */
public class MwoDataReader {
    private final static List<File> FILES_TO_PARSE = Arrays.asList(new File("Game/Libs/Items/Weapons/Weapons.xml"),
            new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml"), new File("Game/Libs/Items/Modules/Ammo.xml"),
            new File("Game/Libs/Items/Modules/Engines.xml"), new File("Game/Libs/Items/Modules/Equipment.xml"),
            new File("Game/Libs/Items/Modules/JumpJets.xml"), new File("Game/Libs/Items/Modules/Internals.xml"),
            new File("Game/Libs/Items/Modules/PilotModules.xml"), new File("Game/Libs/Items/Modules/WeaponMods.xml"),
            new File("Game/Libs/Items/Modules/Consumables.xml"), new File("Game/Libs/Items/Modules/MASC.xml"),
            new File("Game/Libs/Items/Mechs/Mechs.xml"), new File("Game/Libs/Items/OmniPods.xml"),
            new File("Game/Libs/MechPilotTalents/MechEfficiencies.xml"));

    public static Item findItem(int aItemId, List<Item> aItems) {
        for (final Item item : aItems) {
            if (item.getMwoId() == aItemId) {
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
            if (item.getMwoId() == aOmniPod) {
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
     * Reads the latest data from the game files and creates a new cache.
     *
     * @param aLog
     *            a {@link Writer} to write any log messages to.
     * @param aGameDirectory
     *            A directory that contains a game install.
     */
    public Optional<DataCache> parseGameFiles(Writer aLog, File aGameDirectory) throws Exception {
        try {
            final GameVFS gameVFS = new GameVFS(aGameDirectory);

            Localization.initialize(gameVFS); // FIXME: Make Localization into an object and inject it into the reader.

            final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
            final Map<String, Long> checksums = new HashMap<>();
            final XMLItemStats itemStatsXml = new XMLItemStats();
            for (final GameFile file : gameFiles) {
                itemStatsXml.append(file);
                checksums.put(file.path, file.crc32);
            }

            final Map<Integer, Object> id2obj = new HashMap<>();

            final Map<String, ModifierDescription> modifierDescriptions = XMLQuirkDef
                    .fromXml(ClassLoader.getSystemClassLoader().getResourceAsStream("Quirks.def.xml"));
            final Map<MechEfficiencyType, MechEfficiency> mechEfficiencies = parseEfficiencies(itemStatsXml);

            final List<Item> items = parseItems(itemStatsXml);
            addAllTo(id2obj, items);

            final List<PilotModule> modules = parseModules(itemStatsXml, gameVFS);
            addAllTo(id2obj, modules);

            final List<Upgrade> upgrades = parseUpgrades(itemStatsXml, id2obj);
            addAllTo(id2obj, upgrades);

            final List<OmniPod> omniPods = parseOmniPods(itemStatsXml, id2obj, modifierDescriptions, gameVFS);
            addAllTo(id2obj, omniPods);

            final List<Chassis> chassis = parseChassis(itemStatsXml, id2obj, modifierDescriptions, gameVFS);
            addAllTo(id2obj, chassis);

            // For some reason, as of the patch 2016-06-21 some stock loadouts contain pilot modules in the mechs which
            // are
            // ignored by the game client. No mention of plans to add pilot modules to stock loadouts have been
            // announced by
            // PGI. We can only assume that this is a bug for now. We filter out all pilot modules from the stock
            // loadouts
            // before storing them.
            final Set<Integer> itemBlackList = modules.stream().map(PilotModule::getMwoId).collect(Collectors.toSet());
            final List<Environment> environments = parseEnvironments(aLog, gameVFS);
            final List<StockLoadout> stockLoadouts = parseStockLoadouts(chassis, itemBlackList, gameVFS);

            return Optional.of(new DataCache(runningVersion, checksums, items, upgrades, omniPods, modules,
                    mechEfficiencies, chassis, environments, stockLoadouts, modifierDescriptions));
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
     * Compares the data cache to the game files and determines if there is any reason to attempt a further parse.
     *
     * @param aDataCache
     *            The {@link DataCache} to compare to.
     * @param aGameDirectory
     *            The directory to read game files to compare to.
     * @return <code>true</code> if the game files have newer data than what's in the data cache.
     */
    public boolean shouldUpdate(DataCache aDataCache, File aGameDirectory) {
        try {
            final GameVFS gameVFS = new GameVFS(aGameDirectory);
            final Collection<GameFile> gameFiles = gameVFS.openGameFiles(FILES_TO_PARSE);
            final Map<String, Long> checkSums = aDataCache.getChecksums();
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
            aId2obj.put(eq.getMwoId(), eq);
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
     *            The {@link DataCache} that is being parsed.
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

    private Map<MechEfficiencyType, MechEfficiency> parseEfficiencies(XMLItemStats aItemStatsXml) {
        final Map<MechEfficiencyType, MechEfficiency> ans = new HashMap<>();
        for (final XMLMechEfficiencyTalent talent : aItemStatsXml.MechEfficiencies) {
            XMLTalentRank bestRank = talent.ranks.get(0);
            for (final XMLTalentRank rank : talent.ranks) {
                if (bestRank.id < rank.id) {
                    bestRank = rank;
                }
            }

            double value = bestRank.Bonus.value;
            final double eliteModifier = talent.EliteBonus > 0 ? talent.EliteBonus : 1.0;
            double valueElited = value * eliteModifier;
            final MechEfficiencyType type = MechEfficiencyType.fromMwo(talent.name);
            final List<ModifierDescription> descriptions = new ArrayList<>();
            switch (type) {
                case ANCHORTURN:
                    descriptions.add(new ModifierDescription("ANCHOR TURN", null, Operation.MUL,
                            ModifierDescription.SEL_MOVEMENT_TURN_RATE, ModifierDescription.SPEC_ALL,
                            ModifierType.POSITIVE_GOOD));
                    break;
                case ARM_REFLEX:
                    descriptions.add(new ModifierDescription("ARM MOVEMENT RATE", null, Operation.MUL,
                            ModifierDescription.SEL_MOVEMENT_ARM_SPEED, ModifierDescription.SPEC_ALL,
                            ModifierType.POSITIVE_GOOD));
                    break;
                case COOL_RUN:
                    descriptions.add(new ModifierDescription("COOL RUN", null, Operation.MUL,
                            ModifierDescription.SEL_HEAT_DISSIPATION, null, ModifierType.POSITIVE_GOOD));
                    break;
                case FAST_FIRE:
                    value = -value; // Because PGI...
                    valueElited = -valueElited;
                    descriptions.add(new ModifierDescription("FAST FIRE", null, Operation.MUL,
                            ModifierDescription.SEL_ALL_WEAPONS, ModifierDescription.SPEC_WEAPON_COOL_DOWN,
                            ModifierType.NEGATIVE_GOOD));
                    break;
                case HARD_BRAKE: // NYI
                    break;
                case HEAT_CONTAINMENT:
                    descriptions.add(new ModifierDescription("HEAT CONTAINMENT", null, Operation.MUL,
                            ModifierDescription.SEL_HEAT_LIMIT, null, ModifierType.POSITIVE_GOOD));
                    break;
                case KINETIC_BURST: // NYI
                    break;
                case MODULESLOT: // NYI
                    break;
                case PINPOINT: // NYI
                    break;
                case QUICKIGNITION: // NYI
                    break;
                case SPEED_TWEAK:
                    descriptions.add(new ModifierDescription("SPEED TWEAK", null, Operation.MUL,
                            ModifierDescription.SEL_MOVEMENT_MAX_SPEED, null, ModifierType.POSITIVE_GOOD));
                    break;
                case TWIST_SPEED:
                    descriptions.add(new ModifierDescription("TORSO TURN RATE", null, Operation.MUL,
                            ModifierDescription.SEL_MOVEMENT_TORSO_SPEED, ModifierDescription.SPEC_ALL,
                            ModifierType.POSITIVE_GOOD));
                    break;
                case TWIST_X:
                    descriptions.add(new ModifierDescription("TORSO TURN ANGLE", null, Operation.MUL,
                            ModifierDescription.SEL_MOVEMENT_TORSO_ANGLE, ModifierDescription.SPEC_ALL,
                            ModifierType.POSITIVE_GOOD));

                    break;
                default:
                    throw new RuntimeException("Unknown efficiency: " + type);
            }

            final MechEfficiency efficiency = new MechEfficiency(value, valueElited, descriptions);
            ans.put(type, efficiency);
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

        final XStream xstream = DataCache.makeMwoSuitableXStream();
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
            final String uiName = Localization.key2string(uiTag);
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
    private List<PilotModule> parseModules(XMLItemStats aItemStatsXml, GameVFS aGameVFS) throws Exception {

        final XMLPilotTalents pt = XMLPilotTalents.read(aGameVFS);

        final List<PilotModule> ans = new ArrayList<>();
        final Iterator<ItemStatsModule> it = aItemStatsXml.ModuleList.iterator();
        while (it.hasNext()) {
            boolean processed = true;
            final ItemStatsModule statsModule = it.next();
            final int moduleID = Integer.parseInt(statsModule.id);
            switch (statsModule.CType) {
                case "CWeaponModStats": {
                    final XMLPilotModuleStats pms = statsModule.PilotModuleStats;
                    final XMLPilotModuleWeaponStats pmws = statsModule.PilotModuleWeaponStats;
                    final List<XMLWeaponStats> weaponStats = statsModule.WeaponStats;
                    final Faction faction = Faction.fromMwo(statsModule.faction);
                    final ModuleSlot moduleSlot = ModuleSlot.fromMwo(pms.slot);
                    final String name;
                    final String desc;
                    final ModuleCathegory cathegory;

                    // The full details of the quirk-2-name PGI name matching scheme
                    // still eludes me,
                    // this fix will make sure that AMS OVERLOAD and ENHANCED NARC
                    // will match the correct modules in
                    // their modifiers.
                    if (moduleID == 4043) { // ENHANCED NARC
                        pmws.compatibleWeapons = "ClanNarcBeacon, NarcBeacon";
                    }
                    else if (moduleID == 4048) { // ENHANCED NARC - LTD (Clan
                                                 // Only)
                        pmws.compatibleWeapons = "ClanNarcBeacon";
                    }

                    if (0 != pms.talentid) {
                        final XMLTalent talent = pt.getTalent(statsModule.PilotModuleStats.talentid);
                        final XMLRank rank = talent.rankEntries.get(talent.rankEntries.size() - 1);
                        name = Localization.key2string(rank.title);
                        desc = Localization.key2string(rank.description);
                        cathegory = ModuleCathegory.fromMwo(talent.category);
                    }
                    else {
                        name = Localization.key2string(statsModule.Loc.nameTag);
                        desc = Localization.key2string(statsModule.Loc.descTag);
                        cathegory = ModuleCathegory.fromMwo(statsModule.PilotModuleStats.category);
                    }

                    final int maxRank = weaponStats.size();
                    final double longRange[] = new double[maxRank];
                    final double maxRange[] = new double[maxRank];
                    final double cooldown[] = new double[maxRank];
                    final double speed[] = new double[maxRank];
                    final double TAGDuration[] = new double[maxRank];
                    final double damage[] = new double[maxRank];

                    for (int i = 0; i < maxRank; ++i) {
                        final int rank = weaponStats.get(i).rank;
                        longRange[rank - 1] = weaponStats.get(i).longRange;
                        if (null != weaponStats.get(i).maxRange) {
                            maxRange[rank - 1] = Double.parseDouble(weaponStats.get(i).maxRange.replace(',', '.')); // *sigh*
                        }
                        else {
                            maxRange[rank - 1] = 0;
                        }
                        cooldown[rank - 1] = weaponStats.get(i).cooldown;

                        speed[rank - 1] = weaponStats.get(i).speed;
                        TAGDuration[rank - 1] = weaponStats.get(i).TAGDuration;
                        damage[rank - 1] = weaponStats.get(i).damage;
                    }

                    final Collection<Modifier> modifiers = QuirkModifiers.createModifiers(name,
                            weaponStats.get(weaponStats.size() - 1).operation, pmws.compatibleWeapons,
                            cooldown[maxRank - 1], longRange[maxRank - 1], maxRange[maxRank - 1], speed[maxRank - 1],
                            TAGDuration[maxRank - 1], damage[maxRank - 1]);

                    ans.add(new WeaponModule(statsModule.name, moduleID, name, desc, faction, cathegory, moduleSlot,
                            modifiers));
                    break;
                }
                case "CAdvancedZoomStats":
                case "CBackFacingTargetStats":
                case "CCaptureAcceleratorStats":
                case "CCoolantFlushStats":
                case "CGyroStats":
                case "CHillClimbStats":
                case "CSeismicStats":
                case "CSensorRangeStats":
                case "CStrategicStrikeStats":
                case "CTargetDecayStats":
                case "CTargetInfoGatherStats":
                case "CStealthDecayStats":
                case "CCrippledPerformanceStats":
                case "CStrategicStrikeUpgrade":
                case "CImpulseElectricFieldStats":
                case "CUAVStats": {
                    final String name;
                    final String desc;
                    final ModuleCathegory cathegory;

                    // TODO: This should be cleaned up
                    if (statsModule.PilotModuleStats.talentid != 0) {
                        final XMLTalent talent = pt.getTalent(statsModule.PilotModuleStats.talentid);
                        name = Localization.key2string(talent.rankEntries.get(0).title);
                        desc = Localization.key2string(talent.rankEntries.get(0).description);
                        cathegory = ModuleCathegory.fromMwo(talent.category);
                    }
                    else {
                        name = Localization.key2string(statsModule.Loc.nameTag);
                        desc = Localization.key2string(statsModule.Loc.descTag);
                        if (statsModule.PilotModuleStats.category != null) {
                            cathegory = ModuleCathegory.fromMwo(statsModule.PilotModuleStats.category);
                        }
                        else {
                            switch (statsModule.CType) {
                                case "CUAVStats":
                                case "CStrategicStrikeStats":
                                case "CCoolantFlushStats":
                                    cathegory = ModuleCathegory.CONSUMABLE;
                                    break;
                                case "CTargetDecayStats":
                                    cathegory = ModuleCathegory.TARGETING;
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Unknown module cathegory: " + statsModule.CType);
                            }
                        }
                    }
                    final Faction faction = Faction.fromMwo(statsModule.faction);

                    final ModuleSlot moduleSlot = ModuleSlot.fromMwo(statsModule.PilotModuleStats.slot);
                    ans.add(new PilotModule(statsModule.name, moduleID, name, desc, faction, cathegory, moduleSlot));
                    break;
                }
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

    /**
     * @param aGameVfs
     * @param aChassis
     * @param aGameVFS
     * @return
     */
    private List<StockLoadout> parseStockLoadouts(List<Chassis> aChassis, Set<Integer> aItemBlackList, GameVFS aGameVFS)
            throws IOException {
        final List<StockLoadout> ans = new ArrayList<>();

        for (final Chassis chassis : aChassis) {
            final File loadoutXml = new File("Game/Libs/MechLoadout/" + chassis.getKey().toLowerCase() + ".xml");
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
                if (null != component.OmniPod) {
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
            final StockLoadout loadout = new StockLoadout(chassis.getMwoId(), components, armourId, structureId,
                    heatsinkId, guidanceId);
            ans.add(loadout);
        }
        return ans;
    }

    /**
     * @param aItemStatsXml
     * @param aDataCache
     * @param aId2obj
     * @return
     */
    private List<Upgrade> parseUpgrades(XMLItemStats aItemStatsXml, Map<Integer, Object> id2obj) {
        final List<Upgrade> ans = new ArrayList<>();

        for (final ItemStatsUpgradeType upgradeType : aItemStatsXml.UpgradeTypeList) {
            final UpgradeType type = UpgradeType.fromMwo(upgradeType.CType);
            final String mwoName = upgradeType.getMwoKey();
            final String name = Localization.key2string(upgradeType.Loc.nameTag);
            final String desc = Localization.key2string(upgradeType.Loc.descTag);
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
}

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lisoft.lsml.model.chassi.Chassis;
import org.lisoft.lsml.model.chassi.ChassisOmniMech;
import org.lisoft.lsml.model.chassi.ChassisStandard;
import org.lisoft.lsml.model.chassi.ComponentOmniMech;
import org.lisoft.lsml.model.chassi.ComponentStandard;
import org.lisoft.lsml.model.chassi.HardPoint;
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
import org.lisoft.lsml.model.export.garage.HardPointConverter;
import org.lisoft.lsml.model.item.AmmoWeapon;
import org.lisoft.lsml.model.item.Ammunition;
import org.lisoft.lsml.model.item.BallisticWeapon;
import org.lisoft.lsml.model.item.ECM;
import org.lisoft.lsml.model.item.EnergyWeapon;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.Faction;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.item.Internal;
import org.lisoft.lsml.model.item.Item;
import org.lisoft.lsml.model.item.JumpJet;
import org.lisoft.lsml.model.item.MissileWeapon;
import org.lisoft.lsml.model.item.Module;
import org.lisoft.lsml.model.item.ModuleCathegory;
import org.lisoft.lsml.model.item.ModuleSlot;
import org.lisoft.lsml.model.item.PilotModule;
import org.lisoft.lsml.model.item.TargetingComputer;
import org.lisoft.lsml.model.item.WeaponModule;
import org.lisoft.lsml.model.loadout.StockLoadout;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent;
import org.lisoft.lsml.model.loadout.StockLoadout.StockComponent.ActuatorState;
import org.lisoft.lsml.model.modifiers.Attribute;
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
import org.lisoft.lsml.view_fx.Settings;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * This class provides a centralised access point for all game data.
 *
 * @author Li Song
 */
public class DataCache {
	public static enum ParseStatus {
		/**
		 * No game install was detected and the built-in or cached data was
		 * loaded.
		 */
		BUILT_IN,
		/** A previously created cache was loaded. */
		LOADED,
		/** The cache has not yet been initialised. */
		NOT_INITIALISED,
		/** A game install was detected and successfully parsed. */
		PARSED,
		/** A game install was detected but parsing failed. */
		PARSE_FAILED
	}

	private static transient volatile DataCache instance = null;
	private static transient Boolean loading = false;
	private static transient ParseStatus status = ParseStatus.NOT_INITIALISED;

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

	/**
	 * @param aRunningVersion
	 * @see DataCache#getInstance(Writer)
	 */
	public static DataCache getInstance(Settings aSettings, String aRunningVersion) throws IOException {
		final OutputStreamWriter osw = new OutputStreamWriter(System.out, "ASCII");
		return getInstance(aSettings, new PrintWriter(osw), aRunningVersion);
	}

	/**
	 * Gets the global singleton instance for this class. The first call to this
	 * function is not thread safe.
	 *
	 * @param aLog
	 *            A {@link Writer} to write messages to. Can be
	 *            <code>null</code>.
	 * @param aRunningVersion
	 * @return The global {@link DataCache} instance.
	 * @throws IOException
	 *             Thrown if creating the global instance failed. Can only be
	 *             thrown on the first run.
	 */
	public static DataCache getInstance(Settings aSettings, Writer aLog, String aRunningVersion) throws IOException {
		if (instance == null) {
			if (loading) {
				throw new RuntimeException("Recursion while loading data cache!");
			}
			loading = true;

			final File dataCacheFile = new File(aSettings.getString(Settings.CORE_DATA_CACHE).getValue());
			DataCache dataCache = null;
			if (dataCacheFile.isFile()) {
				try {
					dataCache = (DataCache) makeDataCacheXStream().fromXML(dataCacheFile);
					status = ParseStatus.LOADED;
				} catch (final XStreamException exception) {
					dataCache = null; // This is expected to happen when format
										// changes and should be handled
										// silently.
				}

				if (dataCache == null || dataCache.mustUpdate()) {
					if (null != aLog) {
						aLog.append("Found a data cache, but it's from an older LSML version, discarding...")
								.append(System.lineSeparator());
						aLog.flush();
					}
					if (!dataCacheFile.delete() && null != aLog) {
						aLog.append("Failed to delete file: " + dataCacheFile);
					}
					dataCache = null;
				}
			}

			final File gameDir = new File(aSettings.getString(Settings.CORE_GAME_DIRECTORY).getValue());
			if (GameVFS.isValidGameDirectory(gameDir)) {
				try {
					final GameVFS gameVfs = new GameVFS(gameDir);
					final Collection<GameFile> filesToParse = filesToParse(gameVfs);

					if (null == dataCache || dataCache.shouldUpdate(filesToParse)) {
						// If this throws, the old cache is un-touched.
						dataCache = updateCache(aSettings, gameVfs, filesToParse, aLog, aRunningVersion);
						if (null != aLog) {
							aLog.append("Cache updated...").append(System.lineSeparator());
							aLog.flush();
						}
						status = ParseStatus.PARSED;
					}
				} catch (final Throwable exception) {
					if (null != aLog) {
						aLog.append("Parsing of game data failed...").append(System.lineSeparator());
						try (PrintWriter pw = new PrintWriter(aLog)) {
							exception.printStackTrace(pw);
						}
						aLog.flush();
					}
					status = ParseStatus.PARSE_FAILED;
				}
			}

			if (dataCache == null) {
				if (null != aLog) {
					aLog.append("Falling back on bundled data cache.").append(System.lineSeparator());
					aLog.flush();
				}
				try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("data_cache.xml")) {
					dataCache = (DataCache) makeDataCacheXStream().fromXML(is); // Let
																				// this
																				// throw
																				// as
																				// this
																				// is
																				// fatal.
				} catch (final Throwable t) {
					throw new RuntimeException("Oops! Li forgot to update the bundled data cache!");
				}

				if (status == ParseStatus.NOT_INITIALISED) {
					status = ParseStatus.BUILT_IN;
				}
				if (!dataCache.lsmlVersion.equals(aRunningVersion)) {
					// It's from a different LSML version, it's not safe to use
					// it.
					throw new RuntimeException("Bundled data cache not udpated!");
				}
			}
			instance = dataCache;
			loading = false;
		}
		return instance;
	}

	/**
	 * @return The {@link ParseStatus} describing how the game content was
	 *         loaded.
	 */
	public static ParseStatus getStatus() {
		return status;
	}

	public static XStream makeMwoSuitableXStream() {
		final XStream xstream = new XStream(new XppDriver(new NoNameCoder()));
		xstream.ignoreUnknownElements();
		xstream.autodetectAnnotations(true);
		return xstream;
	}

	/**
	 * Returns a list of all files to parse. This is also used for checksumming
	 * to see if the data files have changes.
	 *
	 * @param aGameVfs
	 *            The {@link GameVFS} object to use for reading files.
	 * @return A {@link Collection} of {@link GameFile}s.
	 * @throws IOException
	 */
	private static Collection<GameFile> filesToParse(GameVFS aGameVfs) throws IOException {
		final List<GameFile> ans = new ArrayList<>();
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Weapons/Weapons.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Ammo.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Engines.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Equipment.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/JumpJets.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Internals.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/PilotModules.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/WeaponMods.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Consumables.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/MASC.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Mechs/Mechs.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/OmniPods.xml")));
		ans.add(aGameVfs.openGameFile(new File("Game/Libs/MechPilotTalents/MechEfficiencies.xml")));
		return ans;
	}

	/**
	 * Figures out where to place a new (or overwritten) cache data files.
	 *
	 * @return A {@link File} with a location.
	 * @throws IOException
	 *             Thrown if no location could be determined or the location is
	 *             invalid.
	 */
	private static File getNewCacheLocation(Settings aSettings) throws IOException {
		final String dataCacheLocation = aSettings.getString(Settings.CORE_DATA_CACHE).getValue();
		if (dataCacheLocation.isEmpty()) {
			throw new IOException("An empty string was used as data cache location in the settings file!");
		}
		final File dataCacheFile = new File(dataCacheLocation);
		if (dataCacheFile.isDirectory()) {
			throw new IOException("The data cache location (" + dataCacheLocation
					+ ") is a directory! Expected non-existent or a plain file.");
		}
		return dataCacheFile;
	}

	private static XStream makeDataCacheXStream() {
		final XStream stream = new XStream();
		stream.autodetectAnnotations(true);
		stream.setMode(XStream.ID_REFERENCES);
		stream.alias("datacache", DataCache.class);
		stream.alias("jumpjet", JumpJet.class);
		stream.alias("ammunition", Ammunition.class);
		stream.alias("chassis", ChassisStandard.class);
		stream.alias("chassisOmni", ChassisOmniMech.class);
		stream.alias("hardpoint", HardPoint.class);
		stream.alias("component", ComponentStandard.class);
		stream.alias("componentOmni", ComponentOmniMech.class);
		stream.alias("env", Environment.class);
		stream.alias("ammoweapon", AmmoWeapon.class);
		stream.alias("ballisticweapon", BallisticWeapon.class);
		stream.alias("energyweapon", EnergyWeapon.class);
		stream.alias("engine", Engine.class);
		stream.alias("ecm", ECM.class);
		stream.alias("heatsink", HeatSink.class);
		stream.alias("internal", Internal.class);
		stream.alias("missileweapon", MissileWeapon.class);
		stream.alias("module", Module.class);
		stream.alias("part", Location.class);
		stream.alias("pilotmodule", PilotModule.class);
		stream.alias("weaponmodule", WeaponModule.class);
		stream.alias("omnipod", OmniPod.class);
		stream.alias("attribute", Attribute.class);
		stream.alias("modifierdescription", ModifierDescription.class);
		stream.alias("modifier", Modifier.class);
		stream.alias("structureupgrade", StructureUpgrade.class);
		stream.alias("heatsinkupgrade", HeatSinkUpgrade.class);
		stream.alias("armorupgrade", ArmourUpgrade.class);
		stream.alias("guidanceupgrade", GuidanceUpgrade.class);
		stream.alias("targetingcomp", TargetingComputer.class);
		stream.alias("mechefficiencytype", MechEfficiencyType.class);
		stream.alias("mechefficiency", MechEfficiency.class);
		stream.registerConverter(new HardPointConverter());
		stream.registerConverter(new AttributeConverter());
		stream.registerConverter(new ModifierDescriptionConverter());
		return stream;
	}

	/**
	 * Parses all inner sphere {@link ChassisStandard} from the ItemStats.xml
	 * file and related files.
	 *
	 * @param aGameVfs
	 *            A {@link GameVFS} used to open other game files.
	 * @param aItemStatsXml
	 *            A {@link GameFile} containing the ItemStats.xml file to parse.
	 * @param aDataCache
	 *            The {@link DataCache} that is being parsed.
	 * @return A List of all {@link ChassisStandard} found in aItemStatsXml.
	 */
	private static List<Chassis> parseChassis(GameVFS aGameVfs, XMLItemStats aItemStatsXml, DataCache aDataCache)
			throws IOException {
		final XMLMechIdMap mechIdMap = XMLMechIdMap.fromXml(aGameVfs.openGameFile(GameVFS.MECH_ID_MAP_XML).stream);
		final List<Chassis> ans = new ArrayList<>();

		for (final XMLItemStatsMech mech : aItemStatsXml.MechList) {
			try {
				final String mdfFile = mech.chassis + "/" + mech.name + ".mdf";
				final MdfMechDefinition mdf = MdfMechDefinition
						.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, mdfFile)).stream);

				if (!mdf.isUsable()) {
					continue;
				}

				if (mdf.isOmniMech()) {
					final File loadoutXml = new File("Game/Libs/MechLoadout/" + mech.name + ".xml");
					final XMLLoadout stockXML = XMLLoadout.fromXml(aGameVfs.openGameFile(loadoutXml).stream);
					ans.add(mdf.asChassisOmniMech(mech, aDataCache, mechIdMap, stockXML));
				} else {
					final String hardPointsXml = mech.chassis + "/" + mech.chassis + "-hardpoints.xml";
					final XMLHardpoints hardPoints = XMLHardpoints
							.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, hardPointsXml)).stream);
					ans.add(mdf.asChassisStandard(mech, aDataCache, mechIdMap, hardPoints));
				}
			} catch (final Exception e) {
				throw new IOException("Unable to load chassi configuration for [" + mech.name + "]!", e);
			}
		}
		return ans;
	}

	private static Map<MechEfficiencyType, MechEfficiency> parseEfficiencies(XMLItemStats aItemStatsXml) {
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
				descriptions.add(
						new ModifierDescription("FAST FIRE", null, Operation.MUL, ModifierDescription.SEL_ALL_WEAPONS,
								ModifierDescription.SPEC_WEAPON_COOL_DOWN, ModifierType.NEGATIVE_GOOD));
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
	 * @param aGameVfs
	 *            A {@link GameVFS} to parse data from.
	 * @return A List of all {@link Environment} found in the game files.
	 */
	private static List<Environment> parseEnvironments(GameVFS aGameVfs, Writer aLog) throws IOException {
		final List<Environment> ans = new ArrayList<>();

		final File[] levels = aGameVfs.listGameDir(new File("Game/Levels"));
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
					.fromXML(aGameVfs.openGameFile(new File(file, "mission_mission0.xml")).stream);

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
	private static List<Item> parseItems(XMLItemStats aItemStatsXml) throws IOException {
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
	 * @return
	 * @throws Exception
	 */
	private static List<PilotModule> parseModules(GameVFS aGameVfs, XMLItemStats aItemStatsXml) throws Exception {

		final XMLPilotTalents pt = XMLPilotTalents.read(aGameVfs);

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
				} else if (moduleID == 4048) { // ENHANCED NARC - LTD (Clan
												// Only)
					pmws.compatibleWeapons = "ClanNarcBeacon";
				}

				if (0 != pms.talentid) {
					final XMLTalent talent = pt.getTalent(statsModule.PilotModuleStats.talentid);
					final XMLRank rank = talent.rankEntries.get(talent.rankEntries.size() - 1);
					name = Localization.key2string(rank.title);
					desc = Localization.key2string(rank.description);
					cathegory = ModuleCathegory.fromMwo(talent.category);
				} else {
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
					} else {
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
				} else {
					name = Localization.key2string(statsModule.Loc.nameTag);
					desc = Localization.key2string(statsModule.Loc.descTag);
					if (statsModule.PilotModuleStats.category != null) {
						cathegory = ModuleCathegory.fromMwo(statsModule.PilotModuleStats.category);
					} else {
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
							throw new IllegalArgumentException("Unknown module cathegory: " + statsModule.CType);
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

	private static List<? extends OmniPod> parseOmniPods(GameVFS aGameVfs, XMLItemStats aItemStatsXml,
			DataCache aDataCache) throws IOException {
		final List<OmniPod> ans = new ArrayList<>();
		final Set<String> series = new HashSet<>();
		for (final ItemStatsOmniPodType omniPod : aItemStatsXml.OmniPodList) {
			series.add(omniPod.chassis);
		}
		for (final String chassis : series) {
			try {
				final String omniPodsFile = chassis + "/" + chassis + "-omnipods.xml";
				final XMLOmniPods omniPods = XMLOmniPods
						.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, omniPodsFile)).stream);

				final String hardPointsXml = chassis + "/" + chassis + "-hardpoints.xml";
				final XMLHardpoints hardPoints = XMLHardpoints
						.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, hardPointsXml)).stream);

				ans.addAll(omniPods.asOmniPods(aItemStatsXml, hardPoints, aDataCache));

			} catch (final Exception e) {
				throw new IOException("Unable to load chassi configuration! Chassis: " + chassis, e);
			}
		}

		return ans;
	}

	/**
	 * @param aGameVfs
	 * @param aChassis
	 * @return
	 */
	private static List<StockLoadout> parseStockLoadouts(GameVFS aGameVfs, List<Chassis> aChassis,
			Set<Integer> aItemBlackList) throws IOException {
		final List<StockLoadout> ans = new ArrayList<>();

		for (final Chassis chassis : aChassis) {
			final File loadoutXml = new File("Game/Libs/MechLoadout/" + chassis.getMwoName().toLowerCase() + ".xml");
			final XMLLoadout stockXML = XMLLoadout.fromXml(aGameVfs.openGameFile(loadoutXml).stream);

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
	 * @return
	 */
	private static List<Upgrade> parseUpgrades(XMLItemStats aItemStatsXml, DataCache aDataCache) {
		final List<Upgrade> ans = new ArrayList<>();

		for (final ItemStatsUpgradeType upgradeType : aItemStatsXml.UpgradeTypeList) {
			final UpgradeType type = UpgradeType.fromMwo(upgradeType.CType);
			final String name = Localization.key2string(upgradeType.Loc.nameTag);
			final String desc = Localization.key2string(upgradeType.Loc.descTag);
			final Faction faction = Faction.fromMwo(upgradeType.faction);
			final int mwoid = Integer.parseInt(upgradeType.id);

			switch (type) {
			case ARMOUR: {
				final int slots = upgradeType.SlotUsage == null ? 0 : upgradeType.SlotUsage.slots;
				final double armourPerTon = upgradeType.ArmorTypeStats.armorPerTon;
				ans.add(new ArmourUpgrade(name.replace("ARMOR", "ARMOUR"), desc, mwoid, faction, slots, armourPerTon));
				break;
			}
			case ARTEMIS: {
				final int slots = upgradeType.ArtemisTypeStats.extraSlots;
				final double tons = upgradeType.ArtemisTypeStats.extraTons;
				final double spread = upgradeType.ArtemisTypeStats.missileSpread;
				ans.add(new GuidanceUpgrade(name, desc, mwoid, faction, slots, tons, spread));
				break;
			}
			case HEATSINK: {
				final HeatSink heatSink = (HeatSink) DataCache
						.findItem(upgradeType.HeatSinkTypeStats.compatibleHeatSink, aDataCache.getItems());
				ans.add(new HeatSinkUpgrade(name, desc, mwoid, faction, heatSink));
				break;
			}
			case STRUCTURE: {
				final int slots = upgradeType.SlotUsage == null ? 0 : upgradeType.SlotUsage.slots;
				final double structurePct = upgradeType.StructureTypeStats.weightPerTon;
				ans.add(new StructureUpgrade(name, desc, mwoid, faction, slots, structurePct));
				break;
			}
			default:
				throw new IllegalArgumentException("Unknown upgrade type: " + type);
			}
		}
		return ans;
	}

	/**
	 * Reads the latest data from the game files and creates a new cache.
	 *
	 * @param aGameVfs
	 * @param aLog
	 * @param aRunningVersion
	 * @param aItemStatsXmlFile
	 * @throws Exception
	 */
	private static DataCache updateCache(Settings aSettings, GameVFS aGameVfs, Collection<GameFile> aGameFiles,
			Writer aLog, String aRunningVersion) throws Exception {
		final File cacheLocation = getNewCacheLocation(aSettings);

		Localization.initialize(aGameVfs);
		final DataCache dataCache = new DataCache(aRunningVersion);

		final XMLItemStats itemStatsXml = new XMLItemStats();
		for (final GameFile gameFile : aGameFiles) {
			itemStatsXml.append(gameFile);
			dataCache.checksums.put(gameFile.path, gameFile.crc32);
		}

		dataCache.lsmlVersion = aRunningVersion;
		dataCache.modifierDescriptions = Collections.unmodifiableMap(
				XMLQuirkDef.fromXml(ClassLoader.getSystemClassLoader().getResourceAsStream("Quirks.def.xml")));
		dataCache.mechEfficiencies = Collections.unmodifiableMap(parseEfficiencies(itemStatsXml));
		dataCache.items = Collections.unmodifiableList(parseItems(itemStatsXml));
		dataCache.modules = Collections.unmodifiableList(parseModules(aGameVfs, itemStatsXml));
		dataCache.upgrades = Collections.unmodifiableList(parseUpgrades(itemStatsXml, dataCache));
		dataCache.omniPods = Collections.unmodifiableList(parseOmniPods(aGameVfs, itemStatsXml, dataCache));
		dataCache.chassis = Collections.unmodifiableList(parseChassis(aGameVfs, itemStatsXml, dataCache));

		// For some reason, as of the patch 2016-06-21 some stock loadouts
		// contain pilot modules in the mechs
		// which are ignored by the game client. No mention of plans to add
		// pilot modules to stock loadouts
		// have been announced by PGI. We can only assume that this is a bug for
		// now. We filter out all pilot
		// modules from the stock loadouts before storing them.
		final Set<Integer> itemBlackList = dataCache.modules.stream().map(PilotModule::getMwoId)
				.collect(Collectors.toSet());
		dataCache.environments = Collections.unmodifiableList(parseEnvironments(aGameVfs, aLog));
		dataCache.stockLoadouts = Collections
				.unmodifiableList(parseStockLoadouts(aGameVfs, dataCache.chassis, itemBlackList));

		final XStream stream = makeDataCacheXStream();
		try (OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(cacheLocation), "UTF-8");
				StringWriter sw = new StringWriter()) {
			// Write to memory first, this prevents touching the old file if the
			// marshaling fails
			sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			stream.marshal(dataCache, new PrettyPrintWriter(sw));
			// Write to file
			ow.append(sw.toString());
		}
		aSettings.getString(Settings.CORE_DATA_CACHE).setValue(cacheLocation.getPath());

		return dataCache;
	}

	private final String runningVersion;

	@XStreamAsAttribute
	private String lsmlVersion;

	private final Map<String, Long> checksums = new HashMap<>(); // Filename -
																	// CRC
	private Map<String, ModifierDescription> modifierDescriptions;
	private List<Item> items;
	private List<Upgrade> upgrades;
	private List<OmniPod> omniPods;
	private List<PilotModule> modules;
	private Map<MechEfficiencyType, MechEfficiency> mechEfficiencies;
	private List<Chassis> chassis;

	private List<Environment> environments;

	private List<StockLoadout> stockLoadouts;

	public DataCache(String aRunningVersion) {
		runningVersion = aRunningVersion;
	}

	public OmniPod findOmniPod(int aOmniPod) {
		for (final OmniPod item : getOmniPods()) {
			if (item.getMwoId() == aOmniPod) {
				return item;
			}
		}
		throw new IllegalArgumentException("Unknown OmniPod: " + aOmniPod);
	}

	public Upgrade findUpgrade(int aId) {
		for (final Upgrade upgrade : getUpgrades()) {
			if (aId == upgrade.getMwoId()) {
				return upgrade;
			}
		}
		throw new IllegalArgumentException("Unknown upgrade: " + aId);
	}

	public Upgrade findUpgrade(String aKey) {
		for (final Upgrade upgrade : getUpgrades()) {
			if (aKey.equals(upgrade.getName())) {
				return upgrade;
			}
		}
		throw new IllegalArgumentException("Unknown upgrade: " + aKey);
	}

	/**
	 * @return An unmodifiable {@link List} of all inner sphere
	 *         {@link ChassisStandard}s.
	 */
	public List<Chassis> getChassis() {
		return Collections.unmodifiableList(chassis);
	}

	/**
	 * @return An unmodifiable {@link List} of all {@link Environment}s.
	 */
	public List<Environment> getEnvironments() {
		return Collections.unmodifiableList(environments);
	}

	/**
	 * @return An unmodifiable {@link List} of all {@link Item}s.
	 */
	public List<Item> getItems() {
		return Collections.unmodifiableList(items);
	}

	/**
	 * @return A {@link Collection} of all the mech efficiencies read from the
	 *         data files.
	 */
	public Map<MechEfficiencyType, MechEfficiency> getMechEfficiencies() {
		return mechEfficiencies;
	}

	/**
	 * @return A {@link Map} of all the modifier descriptions indexed by their
	 *         keys.
	 */
	public Map<String, ModifierDescription> getModifierDescriptions() {
		return modifierDescriptions;
	}

	/**
	 * @return An unmodifiable {@link List} of {@link OmniPod}s.
	 */
	public List<OmniPod> getOmniPods() {
		return omniPods;
	}

	/**
	 * @return An unmodifiable {@link List} of all {@link PilotModule}s.
	 */
	public List<PilotModule> getPilotModules() {
		return Collections.unmodifiableList(modules);
	}

	/**
	 * @return An unmodifiable {@link List} of all {@link StockLoadout}s.
	 */
	public List<StockLoadout> getStockLoadouts() {
		return Collections.unmodifiableList(stockLoadouts);
	}

	/**
	 * @return An unmodifiable {@link List} of all {@link Upgrade}s.
	 */
	public List<Upgrade> getUpgrades() {
		return Collections.unmodifiableList(upgrades);
	}

	private boolean mustUpdate() {
		if (!lsmlVersion.equals(runningVersion)) {
			return true;
		}
		return false;
	}

	private boolean shouldUpdate(Collection<GameFile> aGameFiles) {
		if (null == aGameFiles) {
			return false;
		}

		final Map<String, Long> crc = new HashMap<>(checksums);
		for (final GameFile gameFile : aGameFiles) {
			final Long fileCrc = crc.remove(gameFile.path);
			if (null == fileCrc || fileCrc != gameFile.crc32) {
				return true;
			}
		}
		return !crc.isEmpty();
	}
}

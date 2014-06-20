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
package lisong_mechlab.model;

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

import lisong_mechlab.model.StockLoadout.StockComponent;
import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.ECM;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.item.Module;
import lisong_mechlab.model.item.ModuleCathegory;
import lisong_mechlab.model.item.PilotModule;
import lisong_mechlab.model.item.TargetingComputer;
import lisong_mechlab.model.item.Weapon;
import lisong_mechlab.model.item.WeaponModule;
import lisong_mechlab.model.loadout.converters.HardPointConverter;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.Upgrade;
import lisong_mechlab.model.upgrades.UpgradeType;
import lisong_mechlab.mwo_data.GameVFS;
import lisong_mechlab.mwo_data.GameVFS.GameFile;
import lisong_mechlab.mwo_data.Localization;
import lisong_mechlab.mwo_data.MdfMechDefinition;
import lisong_mechlab.mwo_data.XMLHardpoints;
import lisong_mechlab.mwo_data.XMLItemStats;
import lisong_mechlab.mwo_data.XMLLoadout;
import lisong_mechlab.mwo_data.XMLMechIdMap;
import lisong_mechlab.mwo_data.XMLOmniPods;
import lisong_mechlab.mwo_data.XMLPilotTalents;
import lisong_mechlab.mwo_data.XMLPilotTalents.XMLTalent;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;
import lisong_mechlab.mwo_data.helpers.ItemStatsOmniPodType;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;
import lisong_mechlab.mwo_data.helpers.ItemStatsWeapon;
import lisong_mechlab.mwo_data.helpers.Mission;
import lisong_mechlab.mwo_data.helpers.XMLItemStatsMech;
import lisong_mechlab.mwo_data.helpers.XMLPilotModuleStats;
import lisong_mechlab.mwo_data.helpers.XMLWeaponStats;
import lisong_mechlab.util.OS;
import lisong_mechlab.util.OS.WindowsVersion;
import lisong_mechlab.view.LSML;
import lisong_mechlab.view.preferences.PreferenceStore;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class provides a centralized access point for all game data.
 * 
 * @author Emily Björk
 */
public class DataCache{
   public static enum ParseStatus{
      /** No game install was detected and the built-in or cached data was loaded. */
      Builtin,
      /** A previously created cache was loaded. */
      Loaded,
      /** The cache has not yet been initialized. */
      NotInitialized,
      /** A game install was detected and successfully parsed. */
      Parsed,
      /** A game install was detected but parsing failed. */
      ParseFailed
   }

   private static transient DataCache   instance;
   private static transient Boolean     loading   = false;
   private static transient ParseStatus status    = ParseStatus.NotInitialized;

   private List<ChassisBase>            chassis;

   private Map<String, Long>            checksums = new HashMap<>();           // Filename - CRC

   private List<Environment>            environments;

   private List<Item>                   items;

   @XStreamAsAttribute
   private String                       lsmlVersion;

   private List<PilotModule>            modules;

   private List<OmniPod>                omniPods;

   private List<StockLoadout>           stockLoadouts;

   private List<Upgrade>                upgrades;

   /**
    * @return An unmodifiable {@link List} of all inner sphere {@link ChassisStandard}s.
    */
   public List<ChassisBase> getChassis(){
      return Collections.unmodifiableList(chassis);
   }

   /**
    * @return An unmodifiable {@link List} of all {@link Environment}s.
    */
   public List<Environment> getEnvironments(){
      return Collections.unmodifiableList(environments);
   }

   /**
    * @return An unmodifiable {@link List} of all {@link Item}s.
    */
   public List<Item> getItems(){
      return Collections.unmodifiableList(items);
   }

   /**
    * @return An unmodifiable {@link List} of {@link OmniPod}s.
    */
   public List<OmniPod> getOmniPods(){
      return omniPods;
   }

   /**
    * @return An unmodifiable {@link List} of all {@link PilotModule}s.
    */
   public List<PilotModule> getPilotModules(){
      return Collections.unmodifiableList(modules);
   }

   /**
    * @return An unmodifiable {@link List} of all {@link StockLoadout}s.
    */
   public List<StockLoadout> getStockLoadouts(){
      return Collections.unmodifiableList(stockLoadouts);
   }

   /**
    * @return An unmodifiable {@link List} of all {@link Upgrade}s.
    */
   public List<Upgrade> getUpgrades(){
      return Collections.unmodifiableList(upgrades);
   }

   private boolean mustUpdate(){
      if( !lsmlVersion.equals(LSML.getVersion()) )
         return true;
      return false;
   }

   private boolean shouldUpdate(Collection<GameFile> aGameFiles){
      if( null == aGameFiles ){
         return false;
      }

      Map<String, Long> crc = new HashMap<>(checksums);
      for(GameFile gameFile : aGameFiles){
         Long fileCrc = crc.remove(gameFile.path);
         if( null == fileCrc || fileCrc != gameFile.crc32 ){
            return true;
         }
      }
      return !crc.isEmpty();
   }

   public static Item findItem(int aItemId, List<Item> aItems){
      for(Item item : aItems){
         if( item.getMwoId() == aItemId )
            return item;
      }

      throw new IllegalArgumentException("Unknown item: " + aItemId);
   }

   public static Item findItem(String aKey, List<? extends Item> aItems){
      for(Item item : aItems){
         if( aKey.equals(item.getKey()) || aKey.equals(item.getName()) )
            return item;
      }
      throw new IllegalArgumentException("Unknown item: " + aKey);
   }

   public Upgrade findUpgrade(String aKey){
      for(Upgrade upgrade : getUpgrades()){
         if( aKey.equals(upgrade.getName()) )
            return upgrade;
      }
      throw new IllegalArgumentException("Unknown upgrade: " + aKey);
   }

   public Upgrade findUpgrade(int aId){
      for(Upgrade upgrade : getUpgrades()){
         if( aId == upgrade.getMwoId() )
            return upgrade;
      }
      throw new IllegalArgumentException("Unknown upgrade: " + aId);
   }

   /**
    * @see DataCache#getInstance(Writer)
    */
   @SuppressWarnings("javadoc")
   public static DataCache getInstance() throws IOException{
      return getInstance(null);
   }

   /**
    * Gets the global singleton instance for this class. The first call to this function is not thread safe.
    * 
    * @param aLog
    *           A {@link Writer} to write messages to. Can be <code>null</code>.
    * @return The global {@link DataCache} instance.
    * @throws IOException
    *            Thrown if creating the global instance failed. Can only be thrown on the first run.
    */
   public static DataCache getInstance(Writer aLog) throws IOException{
      if( instance == null ){
         if( loading ){
            throw new RuntimeException("Recursion while loading data cache!");
         }
         loading = true;

         File dataCacheFile = new File(PreferenceStore.getString(PreferenceStore.GAME_DATA_CACHE));
         DataCache dataCache = null;
         if( dataCacheFile.isFile() ){
            try{
               dataCache = (DataCache)stream().fromXML(dataCacheFile);
               status = ParseStatus.Loaded;
            }
            catch( XStreamException exception ){
               dataCache = null; // This is expected to happen when format changes and should be handled silently.
            }

            if( dataCache == null || dataCache.mustUpdate() ){
               if( null != aLog ){
                  aLog.append("Found a data cache, but it's from an older LSML version, discarding...").append(System.lineSeparator());
                  aLog.flush();
               }
               dataCacheFile.delete();
               dataCache = null;
            }
         }

         File gameDir = new File(PreferenceStore.getString(PreferenceStore.GAMEDIRECTORY_KEY));
         if( gameDir.isDirectory() ){
            try{
               GameVFS gameVfs = new GameVFS(gameDir);
               Collection<GameFile> filesToParse = filesToParse(gameVfs);

               if( null == dataCache || dataCache.shouldUpdate(filesToParse) ){
                  dataCache = updateCache(gameVfs, filesToParse); // If this throws, the old cache is un-touched.
                  if( null != aLog ){
                     aLog.append("Cache updated...").append(System.lineSeparator());
                     aLog.flush();
                  }
                  status = ParseStatus.Parsed;
               }
            }
            catch( IOException exception ){
               if( null != aLog ){
                  aLog.append("Parsing of game data failed...").append(System.lineSeparator());
                  exception.printStackTrace(new PrintWriter(aLog));
                  aLog.flush();
               }
               status = ParseStatus.ParseFailed;
            }
         }

         if( dataCache == null ){
            if( null != aLog ){
               aLog.append("Falling back on bundled data cache.").append(System.lineSeparator());
               aLog.flush();
            }
            InputStream is = DataCache.class.getResourceAsStream("/resources/bundleDataCache.xml");
            dataCache = (DataCache)stream().fromXML(is); // Let this throw as this is fatal.
            if( status == ParseStatus.NotInitialized )
               status = ParseStatus.Builtin;
            if( !dataCache.lsmlVersion.equals(LSML.getVersion()) ){
               // It's from a different LSML version, it's not safe to use it.
               throw new RuntimeException("Bundled data cache not udpated!");
            }
         }
         instance = dataCache;
         loading = false;
      }
      return instance;
   }

   /**
    * @return The {@link ParseStatus} describing how the game content was loaded.
    */
   public static ParseStatus getStatus(){
      return status;
   }

   private static Collection<GameFile> filesToParse(GameVFS aGameVfs) throws IOException{
      List<GameFile> ans = new ArrayList<>();
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Weapons/Weapons.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Ammo.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Engines.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Equipment.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/JumpJets.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Internals.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/PilotModules.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/WeaponMods.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Mechs/Mechs.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/OmniPods.xml")));
      return ans;
   }

   /**
    * Figures out where to place a new (or overwritten) cache data files.
    * 
    * @return A {@link File} with a location.
    * @throws IOException
    *            Thrown if no location could be determined or the location is invalid.
    */
   private static File getNewCacheLocation() throws IOException{
      String dataCacheLocation = PreferenceStore.getString(PreferenceStore.GAME_DATA_CACHE);
      if( dataCacheLocation.isEmpty() ){
         if( OS.isWindowsOrNewer(WindowsVersion.WinOld) ){
            dataCacheLocation = System.getenv("AppData") + "/lsml_datacache.xml";
         }
         else{
            dataCacheLocation = System.getProperty("user.home") + "/.lsml_datacache.xml";
         }
      }
      File dataCacheFile = new File(dataCacheLocation);
      if( dataCacheFile.isDirectory() ){
         throw new IOException("The data cache location (" + dataCacheLocation + ") is a directory! Expected non-existent or a plain file.");
      }
      return dataCacheFile;
   }

   /**
    * Parses all inner sphere {@link ChassisStandard} from the ItemStats.xml file and related files.
    * 
    * @param aGameVfs
    *           A {@link GameVFS} used to open other game files.
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @param aDataCache
    *           The {@link DataCache} that is being parsed.
    * @return A List of all {@link ChassisStandard} found in aItemStatsXml.
    */
   private static List<ChassisBase> parseChassis(GameVFS aGameVfs, XMLItemStats aItemStatsXml, DataCache aDataCache) throws IOException{
      XMLMechIdMap mechIdMap = XMLMechIdMap.fromXml(aGameVfs.openGameFile(GameVFS.MECH_ID_MAP_XML).stream);
      List<ChassisBase> ans = new ArrayList<>();

      for(XMLItemStatsMech mech : aItemStatsXml.MechList){
         try{
            String mdfFile = mech.chassis + "/" + mech.name + ".mdf";
            MdfMechDefinition mdf = MdfMechDefinition.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, mdfFile)).stream);

            if( mdf.isOmniMech() ){
               File loadoutXml = new File("Game/Libs/MechLoadout/" + mech.name + ".xml");
               XMLLoadout stockXML = XMLLoadout.fromXml(aGameVfs.openGameFile(loadoutXml).stream);
               ans.add(mdf.asChassisOmniMech(mech, aDataCache, mechIdMap, stockXML));
            }
            else{
               String hardPointsXml = mech.chassis + "/" + mech.chassis + "-hardpoints.xml";
               XMLHardpoints hardPoints = XMLHardpoints.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, hardPointsXml)).stream);
               ans.add(mdf.asChassisStandard(mech, aDataCache, mechIdMap, hardPoints));
            }
         }
         catch( Exception e ){
            throw new IOException("Unable to load chassi configuration!", e);
         }
      }
      return ans;
   }

   /**
    * Parses all {@link Environment} from the game files.
    * 
    * @param aGameVfs
    *           A {@link GameVFS} to parse data from.
    * @return A List of all {@link Environment} found in the game files.
    */
   private static List<Environment> parseEnvironments(GameVFS aGameVfs) throws IOException{
      List<Environment> ans = new ArrayList<>();

      File[] levels = aGameVfs.listGameDir(new File("Game/Levels"));
      if( levels == null )
         throw new IOException("Couldn't find environments!");

      XStream xstream = new XStream(new StaxDriver(new NoNameCoder())){
         @Override
         protected MapperWrapper wrapMapper(MapperWrapper next){
            return new MapperWrapper(next){
               @Override
               public boolean shouldSerializeMember(Class definedIn, String fieldName){
                  if( definedIn == Object.class ){
                     return false;
                  }
                  return super.shouldSerializeMember(definedIn, fieldName);
               }
            };
         }
      };
      xstream.autodetectAnnotations(true);
      xstream.alias("Mission", Mission.class);
      xstream.alias("Entity", Mission.Entity.class);
      xstream.alias("Object", Mission.Entity.class);
      xstream.alias("Properties", Mission.Entity.EntityProperties.class);

      for(File file : levels){
         // Skip the tutorials and mechlab
         if( file.getName().toLowerCase().contains("tutorial") || file.getName().toLowerCase().contains("mechlab") ){
            continue;
         }

         String uiTag = "ui_" + file.getName();
         String uiName = Localization.key2string(uiTag);
         Mission mission = (Mission)xstream.fromXML(aGameVfs.openGameFile(new File(file, "mission_mission0.xml")).stream);

         boolean found = false;
         for(Mission.Entity entity : mission.Objects){
            if( entity.EntityClass != null && entity.EntityClass.toLowerCase().equals("worldparameters") ){
               ans.add(new Environment(uiName, entity.Properties.temperature));
               found = true;
               break;
            }
         }
         if( !found ){
            throw new IOException("Unable to find temperature for environment: [" + uiName + "]!");
         }
      }
      return ans;
   }

   /**
    * Parses all {@link Item}s from the ItemStats.xml file.
    * 
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @return A List of all {@link Item}s found in aItemStatsXml.
    */
   private static List<Item> parseItems(XMLItemStats aItemStatsXml) throws IOException{
      List<Item> ans = new ArrayList<>();

      // Special items
      ans.add(new Internal("ENGINE", "", "mdf_Engine", 60000, 3, 0, HardPointType.NONE, 15, Faction.InnerSphere));
      ans.add(new Internal("C-ENGINE", "", "mdf_CEngine", 60001, 2, 0, HardPointType.NONE, 15, Faction.Clan));
      // ans.add(new Internal("mdf_Engine", "mdf_EngineDesc", 3, 15, Faction.InnerSphere));

      // Modules (they contain ammo now, and weapons need to find their ammo types when parsed)
      for(ItemStatsModule statsModule : aItemStatsXml.ModuleList){
         switch( statsModule.CType ){
            case "CAmmoTypeStats":
               ans.add(new Ammunition(statsModule));
               break;
            case "CEngineStats":
               ans.add(new Engine(statsModule));
               break;
            case "CHeatSinkStats":
               ans.add(new HeatSink(statsModule));
               break;
            case "CJumpJetStats":
               ans.add(new JumpJet(statsModule));
               break;
            case "CGECMStats":
               ans.add(new ECM(statsModule));
               break;
            case "CBAPStats":
            case "CClanBAPStats":
            case "CCASEStats":
            case "CCommandConsoleStats":
               ans.add(new Module(statsModule));
               break;
            case "CLowerArmActuatorStats":
            case "CInternalStats":
               ans.add(new Internal(statsModule));
               break;
            case "CTargetingComputerStats":
               ans.add(new TargetingComputer(statsModule));
               break;
            default:
               break; // Other modules not yet supported
         }
      }

      // Weapons next.
      for(ItemStatsWeapon statsWeapon : aItemStatsXml.WeaponList){
         int baseType = -1;
         if( statsWeapon.InheritFrom > 0 ){
            baseType = statsWeapon.InheritFrom;
            for(ItemStatsWeapon w : aItemStatsXml.WeaponList){
               try{
                  if( Integer.parseInt(w.id) == statsWeapon.InheritFrom ){
                     statsWeapon.WeaponStats = w.WeaponStats;
                     if( statsWeapon.Loc.descTag == null ){
                        statsWeapon.Loc.descTag = w.Loc.descTag;
                     }
                     break;
                  }
               }
               catch( NumberFormatException e ){
                  continue;
               }
            }
            if( statsWeapon.WeaponStats == null ){
               throw new IOException("Unable to find referenced item in \"inherit statement from clause\" for: " + statsWeapon.name);
            }
         }

         switch( HardPointType.fromMwoType(statsWeapon.WeaponStats.type) ){
            case AMS:
               ans.add(new AmmoWeapon(statsWeapon, HardPointType.AMS));
               break;
            case BALLISTIC:
               ans.add(new BallisticWeapon(statsWeapon));
               break;
            case ENERGY:
               ans.add(new EnergyWeapon(statsWeapon));
               break;
            case MISSILE:
               ans.add(new MissileWeapon(statsWeapon, baseType));
               break;
            default:
               throw new IOException("Unknown value for type field in ItemStatsXML. Please update the program!");
         }
      }
      return ans;
   }

   /**
    * Parses module data from the data files.
    * 
    * @param aGameVfs
    * @param aItemStatsXml
    * @param aDataCache
    * @return
    * @throws IOException
    */
   private static List<PilotModule> parseModules(GameVFS aGameVfs, XMLItemStats aItemStatsXml, DataCache aDataCache) throws IOException{

      XMLPilotTalents pt = XMLPilotTalents.read(aGameVfs);

      List<PilotModule> ans = new ArrayList<>();
      for(ItemStatsModule statsModule : aItemStatsXml.ModuleList){
         switch( statsModule.CType ){
            case "CWeaponModStats":{
               XMLPilotModuleStats pms = statsModule.PilotModuleStats;
               List<XMLWeaponStats> weaponStats = statsModule.WeaponStats;

               String[] weaps = statsModule.PilotModuleWeaponStats.compatibleWeapons.split(",");
               List<Weapon> affected = new ArrayList<>();
               for(int i = 0; i < weaps.length; ++i){
                  affected.add((Weapon)findItem(weaps[i].trim(), aDataCache.items));
               }

               final String name;
               final String desc;

               final ModuleCathegory cathegory;
               if( null != pms.talentId && !"".equals(pms.talentId) ){
                  XMLTalent talent = pt.getTalent(statsModule.PilotModuleStats.talentId);
                  name = Localization.key2string(talent.rankEntries.get(talent.rankEntries.size() - 1).title);
                  desc = Localization.key2string(talent.rankEntries.get(talent.rankEntries.size() - 1).description);
                  cathegory = ModuleCathegory.fromMwo(talent.category);
               }
               else{
                  name = Localization.key2string(statsModule.Loc.nameTag);
                  desc = Localization.key2string(statsModule.Loc.descTag);

                  cathegory = ModuleCathegory.fromMwo(statsModule.PilotModuleStats.category);
               }

               Faction faction = Faction.fromMwo(statsModule.faction);

               int maxRank = weaponStats.size();
               double longRange[] = new double[maxRank];
               double maxRange[] = new double[maxRank];
               double heat[] = new double[maxRank];

               for(int i = 0; i < maxRank; ++i){
                  int rank = weaponStats.get(i).rank;
                  longRange[rank - 1] = weaponStats.get(i).longRange;
                  if( null != weaponStats.get(i).maxRange ){
                     maxRange[rank - 1] = Double.parseDouble(weaponStats.get(i).maxRange.replace(',', '.')); // *sigh*
                  }
                  else{
                     maxRange[rank - 1] = 0;
                  }
                  heat[rank - 1] = weaponStats.get(i).heat;
               }

               ans.add(new WeaponModule(statsModule.name, Integer.parseInt(statsModule.id), name, desc, faction, cathegory, affected, maxRank,
                                        longRange, maxRange, heat));
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
            case "CUAVStats":{
               final String name;
               final String desc;
               final ModuleCathegory cathegory;

               if( statsModule.PilotModuleStats.talentId != null && !statsModule.PilotModuleStats.talentId.isEmpty() ){
                  XMLTalent talent = pt.getTalent(statsModule.PilotModuleStats.talentId);
                  name = Localization.key2string(talent.rankEntries.get(0).title);
                  desc = Localization.key2string(talent.rankEntries.get(0).description);
                  cathegory = ModuleCathegory.fromMwo(talent.category);
               }
               else{
                  name = Localization.key2string(statsModule.Loc.nameTag);
                  desc = Localization.key2string(statsModule.Loc.descTag);
                  switch( statsModule.CType ){
                     case "CUAVStats":
                     case "CStrategicStrikeStats":
                     case "CCoolantFlushStats":
                        cathegory = ModuleCathegory.Consumable;
                        break;
                     default:
                        throw new IllegalArgumentException("Unknown module cathegory");
                  }
               }
               Faction faction = Faction.fromMwo(statsModule.faction);
               ans.add(new PilotModule(statsModule.name, Integer.parseInt(statsModule.id), name, desc, faction, cathegory));
               break;
            }
            default:
               break; // Other modules not yet supported
         }
      }
      return ans;
   }

   private static List<? extends OmniPod> parseOmniPods(GameVFS aGameVfs, XMLItemStats aItemStatsXml, DataCache aDataCache) throws IOException{
      List<OmniPod> ans = new ArrayList<>();
      Set<String> series = new HashSet<>();
      for(ItemStatsOmniPodType omniPod : aItemStatsXml.OmniPodList){
         series.add(omniPod.chassis);
      }
      for(String chassis : series){
         try{
            String omniPodsFile = chassis + "/" + chassis + "-omnipods.xml";
            XMLOmniPods omniPods = XMLOmniPods.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, omniPodsFile)).stream);

            String hardPointsXml = chassis + "/" + chassis + "-hardpoints.xml";
            XMLHardpoints hardPoints = XMLHardpoints.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, hardPointsXml)).stream);

            ans.addAll(omniPods.asOmniPods(aItemStatsXml, hardPoints, aDataCache));

         }
         catch( Exception e ){
            throw new IOException("Unable to load chassi configuration!", e);
         }
      }

      return ans;
   }

   /**
    * @param aGameVfs
    * @param aChassis
    * @return
    */
   private static List<StockLoadout> parseStockLoadouts(GameVFS aGameVfs, List<ChassisBase> aChassis) throws IOException{
      List<StockLoadout> ans = new ArrayList<>();

      for(ChassisBase chassis : aChassis){
         File loadoutXml = new File("Game/Libs/MechLoadout/" + chassis.getMwoName().toLowerCase() + ".xml");
         XMLLoadout stockXML = XMLLoadout.fromXml(aGameVfs.openGameFile(loadoutXml).stream);

         List<StockLoadout.StockComponent> components = new ArrayList<>();
         for(XMLLoadout.Component xmlComponent : stockXML.ComponentList){
            List<Integer> items = new ArrayList<>();

            if( xmlComponent.Ammo != null ){
               for(XMLLoadout.Component.Item item : xmlComponent.Ammo){
                  items.add(item.ItemID);
               }
            }

            if( xmlComponent.Module != null ){
               for(XMLLoadout.Component.Item item : xmlComponent.Module){
                  items.add(item.ItemID);
               }
            }

            if( xmlComponent.Weapon != null ){
               for(XMLLoadout.Component.Weapon item : xmlComponent.Weapon){
                  items.add(item.ItemID);
               }
            }

            Location partType = Location.fromMwoName(xmlComponent.ComponentName);
            boolean isRear = Location.isRear(xmlComponent.ComponentName);
            int armorFront = isRear ? 0 : xmlComponent.Armor;
            int armorBack = isRear ? xmlComponent.Armor : 0;

            // Merge front and back sides
            Iterator<StockComponent> it = components.iterator();
            while( it.hasNext() ){
               StockComponent stockComponent = it.next();
               if( stockComponent.getPart() == partType ){
                  items.addAll(stockComponent.getItems());
                  armorFront = isRear ? stockComponent.getArmorFront() : armorFront;
                  armorBack = isRear ? armorBack : stockComponent.getArmorBack();
                  it.remove();
                  break;
               }
            }

            StockLoadout.StockComponent stockComponent = new StockLoadout.StockComponent(partType, armorFront, armorBack, items);
            components.add(stockComponent);
         }

         int armorId = 2810; // Standard armor
         int structureId = 3100; // Standard Structure
         int heatsinkId = 3003; // Standard heat sinks
         int guidanceId = 3051; // No Artemis

         if( stockXML.upgrades != null ){
            armorId = stockXML.upgrades.armor.ItemID;
            structureId = stockXML.upgrades.structure.ItemID;
            heatsinkId = stockXML.upgrades.heatsinks.ItemID;
            guidanceId = stockXML.upgrades.artemis.Equipped != 0 ? 3050 : 3051;
         }
         StockLoadout loadout = new StockLoadout(chassis.getMwoId(), components, armorId, structureId, heatsinkId, guidanceId);
         ans.add(loadout);
      }
      return ans;
   }

   /**
    * @param aItemStatsXml
    * @param aDataCace
    * @return
    */
   private static List<Upgrade> parseUpgrades(XMLItemStats aItemStatsXml, DataCache aDataCace){
      List<Upgrade> ans = new ArrayList<>();

      for(ItemStatsUpgradeType upgradeType : aItemStatsXml.UpgradeTypeList){
         UpgradeType type = UpgradeType.fromMwo(upgradeType.CType);
         switch( type ){
            case ARMOR:
               ans.add(new ArmorUpgrade(upgradeType));
               break;
            case ARTEMIS:
               ans.add(new GuidanceUpgrade(upgradeType));
               break;
            case HEATSINK:
               ans.add(new HeatSinkUpgrade(upgradeType, aDataCace));
               break;
            case STRUCTURE:
               ans.add(new StructureUpgrade(upgradeType));
               break;
         }
      }
      return ans;
   }

   private static XStream stream(){
      XStream stream = new XStream();
      stream.autodetectAnnotations(true);
      stream.alias("datacache", DataCache.class);
      stream.alias("jumpjet", JumpJet.class);
      stream.alias("ammunition", Ammunition.class);
      stream.alias("chassis", ChassisStandard.class);
      stream.alias("hardpoint", HardPoint.class);
      stream.alias("internalpart", ComponentStandard.class);
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
      stream.alias("GuidanceUpgrade", GuidanceUpgrade.class);

      // stream.addImmutableType(Internal.class);
      stream.registerConverter(new HardPointConverter());
      // stream.registerLocalConverter(InternalComponent.class, "internal", new ItemConverter());
      return stream;
   }

   /**
    * Reads the latest data from the game files and creates a new cache.
    * 
    * @param aGameVfs
    * @param aItemStatsXmlFile
    * @throws IOException
    */
   private static DataCache updateCache(GameVFS aGameVfs, Collection<GameFile> aGameFiles) throws IOException{
      File cacheLocation = getNewCacheLocation();

      Localization.initialize(aGameVfs);
      DataCache dataCache = new DataCache();

      XMLItemStats itemStatsXml = new XMLItemStats();
      for(GameFile gameFile : aGameFiles){
         itemStatsXml.append(gameFile);
         dataCache.checksums.put(gameFile.path, gameFile.crc32);
      }

      dataCache.lsmlVersion = LSML.getVersion();
      dataCache.items = Collections.unmodifiableList(parseItems(itemStatsXml));
      dataCache.modules = Collections.unmodifiableList(parseModules(aGameVfs, itemStatsXml, dataCache));
      dataCache.upgrades = Collections.unmodifiableList(parseUpgrades(itemStatsXml, dataCache));
      dataCache.omniPods = Collections.unmodifiableList(parseOmniPods(aGameVfs, itemStatsXml, dataCache));
      dataCache.chassis = Collections.unmodifiableList(parseChassis(aGameVfs, itemStatsXml, dataCache));

      dataCache.environments = Collections.unmodifiableList(parseEnvironments(aGameVfs));
      dataCache.stockLoadouts = Collections.unmodifiableList(parseStockLoadouts(aGameVfs, dataCache.chassis));

      XStream stream = stream();
      try( OutputStreamWriter ow = new OutputStreamWriter(new FileOutputStream(cacheLocation), "UTF-8"); StringWriter sw = new StringWriter() ){
         // Write to memory first, this prevents touching the old file if the marshaling fails
         sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         stream.marshal(dataCache, new PrettyPrintWriter(sw));
         // Write to file
         ow.append(sw.toString());
      }
      PreferenceStore.setString(PreferenceStore.GAME_DATA_CACHE, cacheLocation.getPath());

      return dataCache;
   }

   public OmniPod findOmniPod(int aOmniPod){
      for(OmniPod item : getOmniPods()){
         if( item.getMwoId() == aOmniPod )
            return item;
      }
      throw new IllegalArgumentException("Unknown OmniPod: " + aOmniPod);
   }
}

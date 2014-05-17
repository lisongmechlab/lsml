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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lisong_mechlab.model.StockLoadout.StockComponent;
import lisong_mechlab.model.chassi.BaseMovementProfile;
import lisong_mechlab.model.chassi.ChassisOmniMech;
import lisong_mechlab.model.chassi.ChassisStandard;
import lisong_mechlab.model.chassi.ChassisVariant;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalComponent;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.item.AmmoWeapon;
import lisong_mechlab.model.item.Ammunition;
import lisong_mechlab.model.item.BallisticWeapon;
import lisong_mechlab.model.item.ECM;
import lisong_mechlab.model.item.EnergyWeapon;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.item.MissileWeapon;
import lisong_mechlab.model.item.Module;
import lisong_mechlab.model.loadout.converters.HardPointConverter;
import lisong_mechlab.model.loadout.converters.ItemConverter;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.Upgrade;
import lisong_mechlab.model.upgrades.UpgradeType;
import lisong_mechlab.mwo_data.GameVFS;
import lisong_mechlab.mwo_data.GameVFS.GameFile;
import lisong_mechlab.mwo_data.HardpointsXml;
import lisong_mechlab.mwo_data.ItemStatsXml;
import lisong_mechlab.mwo_data.Localization;
import lisong_mechlab.mwo_data.MechDefinition;
import lisong_mechlab.mwo_data.MechIdMap;
import lisong_mechlab.mwo_data.helpers.ItemStatsMech;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;
import lisong_mechlab.mwo_data.helpers.ItemStatsUpgradeType;
import lisong_mechlab.mwo_data.helpers.ItemStatsWeapon;
import lisong_mechlab.mwo_data.helpers.LoadoutXML;
import lisong_mechlab.mwo_data.helpers.MdfMovementTuning;
import lisong_mechlab.mwo_data.helpers.Mission;
import lisong_mechlab.util.OS;
import lisong_mechlab.util.OS.WindowsVersion;
import lisong_mechlab.view.LSML;
import lisong_mechlab.view.preferences.PreferenceStore;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This class provides a centralized access point for all game data.
 * <p>
 * FIXME: Devise a method to prevent loading of out dated cache formats.
 * 
 * @author Li Song
 */
public class DataCache{
   public static enum ParseStatus{
      /** The cache has not yet been initialized. */
      NotInitialized,
      /** A game install was detected and successfully parsed. */
      Parsed,
      /** A game install was detected but parsing failed. */
      ParseFailed,
      /** No game install was detected and the built-in or cached data was loaded. */
      Builtin
   }

   private static transient DataCache   instance;
   private static transient Boolean     loading = false;
   private static transient ParseStatus status  = ParseStatus.NotInitialized;

   @XStreamAsAttribute
   private String                       lsmlVersion;
   @XStreamAsAttribute
   private long                         itemStatsCrc;
   private List<Item>                   items;
   private List<ChassisStandard>        chassisIS;
   private List<ChassisOmniMech>        chassisClan;
   private List<StockLoadout>           stockLoadouts;
   private List<Upgrade>                upgrades;
   private List<Environment>            environments;
   private List<OmniPod>                omniPods;

   /**
    * @see DataCache#getInstance(Writer)
    */
   @SuppressWarnings("javadoc")
   public static DataCache getInstance() throws IOException{
      return getInstance(null);
   }

   /**
    * @return The {@link ParseStatus} describing how the game content was loaded.
    */
   public static ParseStatus getStatus(){
      return status;
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
         if( instance != null )
            return instance;

         XStream stream = stream();
         GameVFS gameVfs = null;
         File dataCacheFile = new File(PreferenceStore.getString(PreferenceStore.GAME_DATA_CACHE));

         try{
            gameVfs = new GameVFS(PreferenceStore.getString(PreferenceStore.GAMEDIRECTORY_KEY));
         }
         catch( IOException exception ){
            if( null != aLog ){
               aLog.append("No game files are available...").append(System.lineSeparator());
               exception.printStackTrace(new PrintWriter(aLog));
            }
         }

         GameVFS.GameFile itemStatsXml = gameVfs != null ? gameVfs.openGameFile(GameVFS.ITEM_STATS_XML) : null;

         DataCache cached = null;
         boolean shouldUpdateCache = false;
         if( dataCacheFile.isFile() ){
            // We have a local cache file, lets see if it's usable.
            try{
               cached = (DataCache)stream.fromXML(dataCacheFile);
               status = ParseStatus.Builtin;
               if( !cached.lsmlVersion.equals(LSML.getVersion()) ){
                  // It's from a different LSML version, it's not safe to use it.
                  dataCacheFile.delete(); // No use in keeping it around
                  cached = null;
                  status = ParseStatus.NotInitialized;
                  shouldUpdateCache = true;
                  if( null != aLog ){
                     aLog.append("Found a data cache for another version of LSML, it's not safe to load.").append(System.lineSeparator());
                  }
               }
               else if( itemStatsXml != null && cached.itemStatsCrc != itemStatsXml.crc32 ){
                  // Correct LSML version but they don't match the game files.
                  shouldUpdateCache = true;
                  if( null != aLog ){
                     aLog.append("Found a data cache, it doesn't match game files.").append(System.lineSeparator());
                  }
               }
               else{
                  // Correct, version and ma
               }

            }
            catch( Throwable t ){
               shouldUpdateCache = true;
               if( null != aLog ){
                  aLog.append("Loading cached data failed.").append(System.lineSeparator());
                  t.printStackTrace(new PrintWriter(aLog));
               }
            }
         }
         else{
            shouldUpdateCache = true;
            if( null != aLog ){
               aLog.append("No cache found.").append(System.lineSeparator());
            }
         }

         if( shouldUpdateCache && gameVfs != null ){
            try{
               cached = updateCache(gameVfs, itemStatsXml);
               status = ParseStatus.Parsed;
            }
            catch( Throwable t ){
               status = ParseStatus.ParseFailed;

               if( null != aLog ){
                  aLog.append("Updating the cache failed: " + t.getMessage()).append(System.lineSeparator());
                  t.printStackTrace(new PrintWriter(aLog));
                  if( cached != null ){
                     aLog.append("Proceeding by using old cache.").append(System.lineSeparator());
                  }
                  aLog.flush();
               }
            }
         }

         if( cached == null ){
            if( null != aLog ){
               aLog.append("Falling back on bundled data cache.").append(System.lineSeparator());
            }
            InputStream is = DataCache.class.getResourceAsStream("/resources/bundleDataCache.xml");
            cached = (DataCache)stream.fromXML(is); // Let this throw as this is fatal.
            if( status == ParseStatus.NotInitialized )
               status = ParseStatus.Builtin;
            if( !cached.lsmlVersion.equals(LSML.getVersion()) ){
               // It's from a different LSML version, it's not safe to use it.
               throw new RuntimeException("Bundled data cache not udpated!");
            }
         }
         instance = cached;
         loading = false;
      }
      return instance;
   }

   /**
    * @return An unmodifiable {@link List} of all {@link Item}s.
    */
   public List<Item> getItems(){
      return Collections.unmodifiableList(items);
   }

   /**
    * @return An unmodifiable {@link List} of all inner sphere {@link ChassisStandard}s.
    */
   public List<ChassisStandard> getChassisIS(){
      return Collections.unmodifiableList(chassisIS);
   }

   /**
    * @return An unmodifiable {@link List} of all clan {@link ChassisOmniMech}s.
    */
   public List<ChassisOmniMech> getChassisClan(){
      return Collections.unmodifiableList(chassisClan);
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

   /**
    * @return An unmodifiable {@link List} of all {@link Environment}s.
    */
   public List<Environment> getEnvironments(){
      return Collections.unmodifiableList(environments);
   }

   /**
    * @return An unmodifiable {@link List} of {@link OmniPod}s.
    */
   public List<OmniPod> getOmniPods(){
      return omniPods;
   }

   private static XStream stream(){
      XStream stream = new XStream();
      stream.autodetectAnnotations(true);
      stream.alias("datacache", DataCache.class);
      stream.alias("jumpjet", JumpJet.class);
      stream.alias("ammunition", Ammunition.class);
      stream.alias("chassis", ChassisStandard.class);
      stream.alias("hardpoint", HardPoint.class);
      stream.alias("internalpart", InternalComponent.class);
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

      //stream.addImmutableType(Internal.class);
      stream.registerConverter(new HardPointConverter());
      //stream.registerLocalConverter(InternalComponent.class, "internal", new ItemConverter());
      return stream;
   }

   /**
    * Reads the latest data from the game files and creates a new cache.
    * 
    * @param aGameVfs
    * @param aItemStatsXmlFile
    * @throws IOException
    */
   private static DataCache updateCache(GameVFS aGameVfs, GameFile aItemStatsXmlFile) throws IOException{
      File cacheLocation = getNewCacheLocation();

      Localization.initialize(aGameVfs);

      ItemStatsXml itemStatsXml = ItemStatsXml.fromXml(aItemStatsXmlFile);

      List<Internal> internalsList = new ArrayList<>();

      DataCache dataCache = new DataCache();
      dataCache.lsmlVersion = LSML.getVersion();
      dataCache.itemStatsCrc = aItemStatsXmlFile.crc32;
      dataCache.items = parseItems(itemStatsXml);
      dataCache.items.addAll(parseClanItems());
      dataCache.upgrades = Collections.unmodifiableList(parseUpgrades(itemStatsXml));
      dataCache.chassisIS = Collections.unmodifiableList(parseChassisIS(aGameVfs, itemStatsXml, internalsList));

      dataCache.omniPods = Collections.unmodifiableList(parseOmniPods(internalsList));
      dataCache.chassisClan = Collections.unmodifiableList(parseChassisClan(aGameVfs, itemStatsXml, dataCache.items, dataCache.omniPods,
                                                                            dataCache.upgrades));
      dataCache.environments = Collections.unmodifiableList(parseEnvironments(aGameVfs));
      dataCache.stockLoadouts = Collections.unmodifiableList(parseStockLoadouts(aGameVfs, dataCache.chassisIS));

      dataCache.items.addAll(internalsList);
      dataCache.items = Collections.unmodifiableList(dataCache.items);

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
    * Parses all {@link Item}s from the ItemStats.xml file.
    * 
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @return A List of all {@link Item}s found in aItemStatsXml.
    */
   private static List<Item> parseItems(ItemStatsXml aItemStatsXml) throws IOException{
      List<Item> ans = new ArrayList<>();

      // Special items
      ans.add(new Internal("mdf_Engine", "mdf_EngineDesc", 3, 15));

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
            case "CCASEStats":
            case "CCommandConsoleStats":
               ans.add(new Module(statsModule));
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
    * Parses all inner sphere {@link ChassisStandard} from the ItemStats.xml file and related files.
    * 
    * @param aGameVfs
    *           A {@link GameVFS} used to open other game files.
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @param aInternalsList
    * @return A List of all {@link ChassisStandard} found in aItemStatsXml.
    */
   private static List<ChassisStandard> parseChassisIS(GameVFS aGameVfs, ItemStatsXml aItemStatsXml, List<Internal> aInternalsList)
                                                                                                                                   throws IOException{
      MechIdMap mechIdMap = MechIdMap.fromXml(aGameVfs.openGameFile(GameVFS.MECH_ID_MAP_XML).stream);
      List<ChassisStandard> ans = new ArrayList<>();

      for(ItemStatsMech mech : aItemStatsXml.MechList){
         int basevariant = -1;
         for(MechIdMap.Mech mappedmech : mechIdMap.MechIdMap){
            if( mappedmech.variantID == mech.id ){
               basevariant = mappedmech.baseID;
               break;
            }
         }

         MechDefinition mdf = null;
         HardpointsXml hardpoints = null;
         try{
            String mdfFile = mech.chassis + "/" + mech.name + ".mdf";
            mdf = MechDefinition.fromXml(aGameVfs.openGameFile(new File(GameVFS.MDF_ROOT, mdfFile)).stream);
            hardpoints = HardpointsXml.fromXml(aGameVfs.openGameFile(new File("Game", mdf.HardpointPath)).stream);
         }
         catch( Exception e ){
            throw new IOException("Unable to load chassi configuration!", e);
         }

         if( mdf.Mech.VariantParent > 0 ){
            if( basevariant > 0 && mdf.Mech.VariantParent != basevariant ){
               // Inconsistency between MechIDMap and ParentAttribute.
               throw new IOException("MechIDMap.xml and VariantParent attribute are inconsistent for: " + mech.name);
            }
            basevariant = mdf.Mech.VariantParent;
         }
         final ChassisStandard chassi = new ChassisStandard(mech, mdf, hardpoints, basevariant, mech.chassis, aInternalsList);
         ans.add(chassi);
      }
      return ans;
   }

   private static Upgrade findUpgrade(String aKey, List<? extends Upgrade> aUpgrades){
      for(Upgrade upgrade : aUpgrades){
         if( aKey.equals(upgrade.getName()) )
            return upgrade;
      }
      throw new IllegalArgumentException("Unknown upgrade: " + aKey);
   }

   private static Item findItem(String aKey, List<? extends Item> aItems){
      for(Item item : aItems){
         if( aKey.equals(item.getKey()) || aKey.equals(item.getName()) )
            return item;
      }
      throw new IllegalArgumentException("Unknown item: " + aKey);
   }

   /**
    * @return
    */
   private static Collection<? extends Item> parseClanItems(){
      List<Item> ans = new ArrayList<>();

      ans.add(new Engine("CLAN XL ENGINE 375", "super fast engine", "@clanxl375", 50000, 6, 19.5, HardPointType.NONE, 15, true, 375, EngineType.XL,
                         10, 0));
      ans.add(new HeatSink("CLAN DOUBLE HEAT SINK", "super dhs", "@clandhs", 50001, 2, 1.0, HardPointType.NONE, 10, true, 0.14, 1.4));

      return ans;
   }

   /**
    * Parses all clan {@link ChassisOmniMech} from the ItemStats.xml file and related files.
    * 
    * @param aGameVfs
    *           A {@link GameVFS} used to open other game files.
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @return A List of all {@link ChassisStandard} found in aItemStatsXml.
    */
   private static List<ChassisOmniMech> parseChassisClan(GameVFS aGameVfs, ItemStatsXml aItemStatsXml, List<Item> aItems, List<OmniPod> aOmniPods,
                                                         List<Upgrade> aUpgrades) throws IOException{
      List<ChassisOmniMech> ans = new ArrayList<>();

      OmniPod ct = null;
      for(OmniPod omniPod : aOmniPods){
         if( omniPod.getLocation() == Location.CenterTorso ){
            ct = omniPod;
            break;
         }
      }
      if( ct == null )
         throw new RuntimeException("Fail");
      ArmorUpgrade clanff = (ArmorUpgrade)findUpgrade("CLAN FERRO-FIBROUS", aUpgrades);
      StructureUpgrade clanes = (StructureUpgrade)findUpgrade("CLAN ENDO-STEEL", aUpgrades);
      HeatSinkUpgrade clandhs = (HeatSinkUpgrade)findUpgrade("CLAN DOUBLE HEAT SINKS", aUpgrades);
      Engine xl375 = (Engine)findItem("CLAN XL ENGINE 375", aItems);

      MdfMovementTuning aMdfMovement = new MdfMovementTuning();
      aMdfMovement.MovementArchetype = "Large";

      aMdfMovement.MaxMovementSpeed = 16.2;
      aMdfMovement.TorsoTurnSpeedYaw = 20;
      aMdfMovement.TorsoTurnSpeedPitch = 11.25;
      aMdfMovement.ArmTurnSpeedYaw = 45;
      aMdfMovement.ArmTurnSpeedPitch = 45;
      // aMovementProfile.TurnLerpLowRate = 0.2;
      // aMovementProfile.TurnLerpMidRate = 0.15;
      // aMovementProfile.TurnLerpHighRate = 0.1333;
      // aMovementProfile.AccelLerpLowRate = 0.1875;
      // aMovementProfile.AccelLerpMidRate = 0.0425;
      // aMovementProfile.AccelLerpHighRate = 0.025;
      // aMovementProfile.DecelLerpLowRate = 0.15;
      // aMovementProfile.DecelLerpMidRate = 22.5;
      // aMovementProfile.DecelLerpHighRate = 22.5;
      aMdfMovement.ReverseSpeedMultiplier = 0.666;
      aMdfMovement.MaxTorsoAngleYaw = 90;
      aMdfMovement.MaxTorsoAnglePitch = 20;
      aMdfMovement.MaxArmRotationYaw = 20;
      aMdfMovement.MaxArmRotationPitch = 30;

      ans.add(new ChassisOmniMech(40000, "TBW-PRIME", "TIMBERWOLF", "TIMBERWOLF PRIME", "TBW-PRIME", 75, ChassisVariant.NORMAL, -1,
                                  new BaseMovementProfile(aMdfMovement), true, xl375, clanes, clanff, clandhs, ct));

      return ans;
   }

   /**
    * @param aInternalsList
    * @return
    */
   private static List<? extends OmniPod> parseOmniPods(List<Internal> aInternalsList){
      List<OmniPod> ans = new ArrayList<>();

      //@formatter:off
      ans.add(new OmniPod(Location.Head, 6, 15.0, 
                          Arrays.asList(findItem("@mdf_LifeSupport", aInternalsList), 
                                        findItem("@mdf_Sensors", aInternalsList),
                                        findItem("@mdf_Cockpit", aInternalsList)),
                          Arrays.asList(new HardPoint[]{}), 
                          40000, "TIMBERWOLF", 0, 0));

      ans.add(new OmniPod(Location.CenterTorso, 12, 46.0, 
                          Arrays.asList(findItem("@mdf_Gyro", aInternalsList)),
                          Arrays.asList(new HardPoint(HardPointType.BALLISTIC)), 
                          40000, "TIMBERWOLF", 0, 0));
      
      ans.add(new OmniPod(Location.LeftTorso, 12, 32.0, 
                          Arrays.asList(new Item[]{}),
                          Arrays.asList(new HardPoint(HardPointType.BALLISTIC),
                                        new HardPoint(HardPointType.MISSILE, 20, false)), 
                          40000, "TIMBERWOLF", 0, 0));
      
      ans.add(new OmniPod(Location.RightTorso, 12, 32.0, 
                          Arrays.asList(new Item[]{}),
                          Arrays.asList(new HardPoint(HardPointType.BALLISTIC),
                                        new HardPoint(HardPointType.MISSILE, 20, false)), 
                          40000, "TIMBERWOLF", 0, 0));
            
      ans.add(new OmniPod(Location.LeftArm, 12, 24.0, 
                          Arrays.asList(findItem("@mdf_Shoulder", aInternalsList),
                                        findItem("@mdf_UAA", aInternalsList),
                                        findItem("@mdf_LAA", aInternalsList),
                                        findItem("@mdf_HA", aInternalsList)),
                          Arrays.asList(new HardPoint(HardPointType.ENERGY),
                                        new HardPoint(HardPointType.ENERGY)),
                          40000, "TIMBERWOLF", 0, 0));
      
      ans.add(new OmniPod(Location.RightArm, 12, 24.0, 
                          Arrays.asList(findItem("@mdf_Shoulder", aInternalsList),
                                        findItem("@mdf_UAA", aInternalsList),
                                        findItem("@mdf_LAA", aInternalsList),
                                        findItem("@mdf_HA", aInternalsList)),
                          Arrays.asList(new HardPoint(HardPointType.ENERGY),
                                        new HardPoint(HardPointType.ENERGY)),
                          40000, "TIMBERWOLF", 0, 0));
      
      ans.add(new OmniPod(Location.LeftLeg, 6, 32.0, 
                          Arrays.asList(findItem("@mdf_Hip", aInternalsList),
                                        findItem("@mdf_ULA", aInternalsList),
                                        findItem("@mdf_LLA", aInternalsList),
                                        findItem("@mdf_FA", aInternalsList)),
                          Arrays.asList(new HardPoint[]{}), 
                          40000, "TIMBERWOLF", 0, 0));
      
      ans.add(new OmniPod(Location.RightLeg, 6, 32.0, 
                          Arrays.asList(findItem("@mdf_Hip", aInternalsList),
                                        findItem("@mdf_ULA", aInternalsList),
                                        findItem("@mdf_LLA", aInternalsList),
                                        findItem("@mdf_FA", aInternalsList)),
                          Arrays.asList(new HardPoint[]{}), 
                          40000, "TIMBERWOLF", 0, 0));
      //@formatter:on      
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
    * @param aItemStatsXml
    * @return
    */
   private static List<Upgrade> parseUpgrades(ItemStatsXml aItemStatsXml){
      List<Upgrade> ans = new ArrayList<>();

      for(ItemStatsUpgradeType upgradeType : aItemStatsXml.UpgradeTypeList){
         UpgradeType type = UpgradeType.fromMwo(upgradeType.UpgradeTypeStats.type);
         switch( type ){
            case ARMOR:
               ans.add(new ArmorUpgrade(upgradeType));
               break;
            case ARTEMIS:
               ans.add(new GuidanceUpgrade(upgradeType));
               break;
            case HEATSINK:
               ans.add(new HeatSinkUpgrade(upgradeType));
               break;
            case STRUCTURE:
               ans.add(new StructureUpgrade(upgradeType));
               break;
         }
      }

      // FIXME: Adding clan here

      ans.add(new ArmorUpgrade("CLAN FERRO-FIBROUS", "super ff", 30000, -1, 7, 38.4));
      ans.add(new StructureUpgrade("CLAN ENDO-STEEL", "super es", 30001, -1, 7, 0.05));
      ans.add(new HeatSinkUpgrade("CLAN DOUBLE HEAT SINKS", "super dhs", 30002, 50001));

      return ans;
   }

   /**
    * @param aGameVfs
    * @param aChassis
    * @return
    */
   private static List<StockLoadout> parseStockLoadouts(GameVFS aGameVfs, List<ChassisStandard> aChassis) throws IOException{
      List<StockLoadout> ans = new ArrayList<>();

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
      xstream.alias("Loadout", LoadoutXML.class);

      for(ChassisStandard chassis : aChassis){
         File loadoutXml = new File("Game/Libs/MechLoadout/" + chassis.getMwoName().toLowerCase() + ".xml");
         LoadoutXML stockXML = (LoadoutXML)xstream.fromXML(aGameVfs.openGameFile(loadoutXml).stream);

         List<StockLoadout.StockComponent> components = new ArrayList<>();
         for(LoadoutXML.Component xmlComponent : stockXML.ComponentList){
            List<Integer> items = new ArrayList<>();

            if( xmlComponent.Ammo != null ){
               for(LoadoutXML.Component.Item item : xmlComponent.Ammo){
                  items.add(item.ItemID);
               }
            }

            if( xmlComponent.Module != null ){
               for(LoadoutXML.Component.Item item : xmlComponent.Module){
                  items.add(item.ItemID);
               }
            }

            if( xmlComponent.Weapon != null ){
               for(LoadoutXML.Component.Weapon item : xmlComponent.Weapon){
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
}

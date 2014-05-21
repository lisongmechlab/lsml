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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lisong_mechlab.model.StockLoadout.StockComponent;
import lisong_mechlab.model.chassi.Chassis;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.chassi.InternalPart;
import lisong_mechlab.model.chassi.Part;
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
import lisong_mechlab.mwo_data.helpers.Mission;
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
      Builtin,
      /** A previously created cache was loaded. */
      Loaded
   }

   private static transient DataCache   instance;
   private static transient Boolean     loading   = false;
   private static transient ParseStatus status    = ParseStatus.NotInitialized;

   @XStreamAsAttribute
   private String                       lsmlVersion;
   private Map<String, Long>            checksums = new HashMap<>();           // Filename - CRC
   private List<Item>                   items;
   private List<Chassis>                chassis;
   private List<StockLoadout>           stockLoadouts;
   private List<Upgrade>                upgrades;
   private List<Environment>            environments;

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
                  }
                  status = ParseStatus.Parsed;
               }
            }
            catch( IOException exception ){
               if( null != aLog ){
                  aLog.append("Parsing of game data failed...").append(System.lineSeparator());
                  exception.printStackTrace(new PrintWriter(aLog));
               }
               status = ParseStatus.ParseFailed;
            }
         }

         if( dataCache == null ){
            if( null != aLog ){
               aLog.append("Falling back on bundled data cache.").append(System.lineSeparator());
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
    * @return An unmodifiable {@link List} of all {@link Item}s.
    */
   public List<Item> getItems(){
      return Collections.unmodifiableList(items);
   }

   /**
    * @return An unmodifiable {@link List} of all {@link Chassis}s.
    */
   public List<Chassis> getChassis(){
      return Collections.unmodifiableList(chassis);
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

   private static XStream stream(){
      XStream stream = new XStream();
      stream.autodetectAnnotations(true);
      stream.alias("datacache", DataCache.class);
      stream.alias("jumpjet", JumpJet.class);
      stream.alias("ammunition", Ammunition.class);
      stream.alias("chassis", Chassis.class);
      stream.alias("hardpoint", HardPoint.class);
      stream.alias("internalpart", InternalPart.class);
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
      stream.alias("part", Part.class);

      return stream;
   }

   private static Collection<GameFile> filesToParse(GameVFS aGameVfs) throws IOException{
      List<GameFile> ans = new ArrayList<>();
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Weapons/Weapons.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/UpgradeTypes/UpgradeTypes.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Ammo.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Engines.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/Equipment.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Modules/JumpJets.xml")));
      ans.add(aGameVfs.openGameFile(new File("Game/Libs/Items/Mechs/Mechs.xml")));
      return ans;
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

      ItemStatsXml itemStatsXml = new ItemStatsXml();
      for(GameFile gameFile : aGameFiles){
         itemStatsXml.append(gameFile);
         dataCache.checksums.put(gameFile.path, gameFile.crc32);
      }

      dataCache.lsmlVersion = LSML.getVersion();
      dataCache.items = Collections.unmodifiableList(parseItems(itemStatsXml));
      dataCache.chassis = Collections.unmodifiableList(parseChassis(aGameVfs, itemStatsXml));
      dataCache.environments = Collections.unmodifiableList(parseEnvironments(aGameVfs));
      dataCache.upgrades = Collections.unmodifiableList(parseUpgrades(itemStatsXml));
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
    * Parses all {@link Chassis} from the ItemStats.xml file and related files.
    * 
    * @param aGameVfs
    *           A {@link GameVFS} used to open other game files.
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @return A List of all {@link Chassis} found in aItemStatsXml.
    */
   private static List<Chassis> parseChassis(GameVFS aGameVfs, ItemStatsXml aItemStatsXml) throws IOException{
      MechIdMap mechIdMap = MechIdMap.fromXml(aGameVfs.openGameFile(GameVFS.MECH_ID_MAP_XML).stream);
      List<Chassis> ans = new ArrayList<>();

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

         // TODO: Find a better way of parsing this
         String series = mech.chassis;
         String seriesShort = mech.name.split("-")[0];

         final Chassis chassi = new Chassis(mech, mdf, hardpoints, basevariant, series, seriesShort);
         ans.add(chassi);
      }
      return ans;
   }

   /**
    * Parses all {@link Chassis} from the ItemStats.xml file and related files.
    * 
    * @param aItemStatsXml
    *           A {@link GameFile} containing the ItemStats.xml file to parse.
    * @return A List of all {@link Chassis} found in aItemStatsXml.
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
      return ans;
   }

   /**
    * @param aGameVfs
    * @param aChassis
    * @return
    */
   private static List<StockLoadout> parseStockLoadouts(GameVFS aGameVfs, List<Chassis> aChassis) throws IOException{
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

      for(Chassis chassis : aChassis){
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

            Part partType = Part.fromMwoName(xmlComponent.componentName);
            boolean isRear = Part.isRear(xmlComponent.componentName);
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

         // {
         //
         // File loadoutXml1 = new File("Game/Libs/MechLoadout/" + chassis.getMwoName().toLowerCase() + ".xml");
         // XmlReader reader;
         // try{
         // reader = new XmlReader(aGameVfs.openGameFile(loadoutXml1).stream);
         // }
         // catch( ParserConfigurationException e ){
         // throw new IOException(e);
         // }
         // catch( SAXException e ){
         // throw new IOException(e);
         // }
         // try{
         // List<StockLoadout.StockComponent> components1 = new ArrayList<>();
         // for(Element component : reader.getElementsByTagName("component")){
         // List<Integer> items = new ArrayList<>();
         // String name = component.getAttribute("Name");
         // Part partType = Part.fromMwoName(name);
         // boolean isRear = Part.isRear(name);
         // int armorFront = isRear ? 0 : Integer.parseInt(component.getAttribute("Armor"));
         // int armorBack = isRear ? Integer.parseInt(component.getAttribute("Armor")) : 0;
         // for(Node child = component.getFirstChild(); child != null; child = child.getNextSibling()){
         // if( child.getNodeType() == Node.ELEMENT_NODE ){
         // items.add(Integer.parseInt(((Element)child).getAttribute("ItemID")));
         // }
         // } // Merge front and back sides
         // Iterator<StockComponent> it = components1.iterator();
         // while( it.hasNext() ){
         // StockComponent stockComponent = it.next();
         // if( stockComponent.getPart() == partType ){
         // items.addAll(stockComponent.getItems());
         // armorFront = isRear ? stockComponent.getArmorFront() : armorFront;
         // armorBack = isRear ? armorBack : stockComponent.getArmorBack();
         // it.remove();
         // break;
         // }
         // }
         // StockLoadout.StockComponent stockComponent = new StockLoadout.StockComponent(partType, armorFront,
         // armorBack, items);
         // components1.add(stockComponent);
         // }
         // int armorId1 = 2810; // Standard armor
         // int structureId1 = 3100; // Standard Structure
         // int heatsinkId1 = 3003; // Standard heat sinks
         // int guidanceId1 = 3051; // No Artemis
         // List<Element> maybeUpgrades = reader.getElementsByTagName("Upgrades");
         // if( maybeUpgrades.size() == 1 ){
         // Element stockUpgrades = maybeUpgrades.get(0);
         // armorId1 = Integer.parseInt(reader.getElementByTagName("Armor", stockUpgrades).getAttribute("ItemID"));
         // structureId1 = Integer.parseInt(reader.getElementByTagName("Structure",
         // stockUpgrades).getAttribute("ItemID"));
         // heatsinkId1 = Integer.parseInt(reader.getElementByTagName("HeatSinks",
         // stockUpgrades).getAttribute("ItemID"));
         // guidanceId1 = reader.getElementByTagName("Artemis", stockUpgrades).getAttribute("Equipped").equals("1") ?
         // 3050 : 3051;
         // }
         // StockLoadout loadout1 = new StockLoadout(chassis.getMwoId(), components1, armorId1, structureId1,
         // heatsinkId1, guidanceId1);
         // ans.add(loadout1);
         // }
         // catch( Exception e ){
         // throw new IOException("Error reading file: " + loadoutXml1, e);
         // }
         // }
      }
      return ans;
   }
}

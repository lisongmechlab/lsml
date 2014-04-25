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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

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
import lisong_mechlab.mwo_data.helpers.Mission;
import lisong_mechlab.util.OS;
import lisong_mechlab.util.OS.WindowsVersion;
import lisong_mechlab.util.XmlReader;
import lisong_mechlab.view.LSML;
import lisong_mechlab.view.preferences.PreferenceStore;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
 * @author Emily Björk
 */
public class DataCache{
   private static transient DataCache instance;
   private static transient Boolean   loading = false;

   @XStreamAsAttribute
   private String                     lsmlVersion;
   @XStreamAsAttribute
   private long                       itemStatsCrc;
   private List<Item>                 items;
   private List<Chassis>              chassis;
   private List<StockLoadout>         stockLoadouts;
   private List<Upgrade>              upgrades;
   private List<Environment>          environments;

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
               aLog.append("No game files are available...");
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
               if( cached.lsmlVersion != LSML.VERSION_STRING ){
                  // It's from a different LSML version, it's not safe to use it.
                  dataCacheFile.delete(); // No use in keeping it around
                  cached = null;
                  shouldUpdateCache = true;
                  if( null != aLog ){
                     aLog.append("Found a data cache for another version of LSML, it's not safe to load.");
                  }
               }
               else if( itemStatsXml != null && cached.itemStatsCrc != itemStatsXml.crc32 ){
                  // Correct LSML version but they don't match the game files.
                  shouldUpdateCache = true;
                  if( null != aLog ){
                     aLog.append("Found a data cache, it doesn't match game files.");
                  }
               }
            }
            catch( Throwable t ){
               shouldUpdateCache = true;
               if( null != aLog ){
                  aLog.append("Loading cached data failed.");
                  t.printStackTrace(new PrintWriter(aLog));
               }
            }
         }
         else{
            shouldUpdateCache = true;
            if( null != aLog ){
               aLog.append("No cache found.");
            }
         }

         if( shouldUpdateCache && gameVfs != null ){
            try{
               cached = updateCache(gameVfs, itemStatsXml);
            }
            catch( Throwable t ){
               if( null != aLog ){
                  aLog.append("Updating the cache failed: " + t.getMessage());
                  t.printStackTrace(new PrintWriter(aLog));
                  if( cached != null ){
                     aLog.append("Proceeding by using old cache.");
                  }
               }
            }
         }

         if( cached == null ){
            if( null != aLog ){
               aLog.append("Falling back on bundled data cache.");
            }
            InputStream is = DataCache.class.getResourceAsStream("/resources/bundleDataCache.xml");
            cached = (DataCache)stream.fromXML(is); // Let this throw as this is fatal.
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

      DataCache dataCache = new DataCache();
      dataCache.lsmlVersion = LSML.VERSION_STRING;
      dataCache.itemStatsCrc = aItemStatsXmlFile.crc32;
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
            String mdfFile = mech.mdf.replace('\\', '/');
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
         String[] mdfsplit = mech.mdf.split("\\\\");
         String series = mdfsplit[1];
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
            case GUIDANCE:
               ans.add(new GuidanceUpgrade(upgradeType));
               break;
            case HEATSINKS:
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

      for(Chassis chassis : aChassis){
         File loadoutXml = new File("Game/Libs/MechLoadout/" + chassis.getMwoName().toLowerCase() + ".xml");
         XmlReader reader;
         try{
            reader = new XmlReader(aGameVfs.openGameFile(loadoutXml).stream);
         }
         catch( ParserConfigurationException e ){
            throw new IOException(e);
         }
         catch( SAXException e ){
            throw new IOException(e);
         }

         List<StockLoadout.StockComponent> components = new ArrayList<>();
         for(Element component : reader.getElementsByTagName("component")){
            List<Integer> items = new ArrayList<>();

            String name = component.getAttribute("Name");
            Part partType = Part.fromMwoName(name);
            boolean isRear = Part.isRear(name);
            int armorFront = isRear ? 0 : Integer.parseInt(component.getAttribute("Armor"));
            int armorBack = isRear ? Integer.parseInt(component.getAttribute("Armor")) : 0;

            for(Node child = component.getFirstChild(); child != null; child = child.getNextSibling()){
               if( child.getNodeType() == Node.ELEMENT_NODE ){
                  items.add(Integer.parseInt(((Element)child).getAttribute("ItemID")));
               }
            }

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
         List<Element> maybeUpgrades = reader.getElementsByTagName("Upgrades");
         if( maybeUpgrades.size() == 1 ){
            Element stockUpgrades = maybeUpgrades.get(0);
            armorId = Integer.parseInt(reader.getElementByTagName("Armor", stockUpgrades).getAttribute("ItemID"));
            structureId = Integer.parseInt(reader.getElementByTagName("Structure", stockUpgrades).getAttribute("ItemID"));
            heatsinkId = reader.getElementByTagName("HeatSinks", stockUpgrades).getAttribute("Type").equals("Double") ? 3002 : 3003;
            guidanceId = reader.getElementByTagName("Artemis", stockUpgrades).getAttribute("Equipped").equals("1") ? 3050 : 3051;
         }

         StockLoadout loadout = new StockLoadout(chassis.getMwoId(), components, armorId, structureId, heatsinkId, guidanceId);
         ans.add(loadout);
      }
      return ans;
   }
}

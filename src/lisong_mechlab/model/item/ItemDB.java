package lisong_mechlab.model.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsAmmoType;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class ItemDB{
   static private final Map<String, Item>  locname2item;
   static private final Map<String, Item>  mwoname2item;
   static private final Map<Integer, Item> mwoidx2item;

   static public Item lookup(final String anItemName){
      String key = canonize(anItemName);
      if( !locname2item.containsKey(key) ){
         if( !mwoname2item.containsKey(key) ){
            throw new IllegalArgumentException("There exists no item by name:" + anItemName);
         }
         return mwoname2item.get(key);
      }
      return locname2item.get(key);
   }

   @SuppressWarnings("unchecked")
   // It is checked...
   static public <T extends Item> List<T> lookup(Class<T> type){
      List<T> ans = new ArrayList<T>();
      for(Item it : locname2item.values()){
         if( type.isInstance(it) ){
            ans.add((T)it);
         }
      }
      return ans;
   }

   public static Item lookup(int anMwoIndex){
      if( !mwoidx2item.containsKey(anMwoIndex) ){
         throw new IllegalArgumentException("No item with that index! :" + anMwoIndex);
      }
      return mwoidx2item.get(anMwoIndex);
   }

   static private void put(Item anItem){
      assert anItem != null;
      mwoname2item.put(canonize(anItem.getKey()), anItem);
      locname2item.put(canonize(anItem.getName()), anItem);
      mwoidx2item.put(anItem.getMwoIdx(), anItem);
   }

   static private String canonize(String aString){
      return MissileWeapon.canonize(aString).toLowerCase();
   }

   static{
      mwoname2item = new HashMap<String, Item>();
      locname2item = new HashMap<String, Item>();
      mwoidx2item = new TreeMap<Integer, Item>();

      ItemStatsXml stats = ItemStatsXml.stats;

      // Modules (they contain ammo now, and weapons need to find their ammo types when parsed)
      for(ItemStatsModule statsModule : stats.ModuleList){
         switch( statsModule.CType ){
            case "CAmmoTypeStats":
               put(new Ammunition(statsModule));
               break;
            case "CEngineStats":
               put(new Engine(statsModule));
               break;
            case "CHeatSinkStats":
               put(new HeatSink(statsModule));
               break;
            case "CJumpJetStats":
               put(new JumpJet(statsModule));
               break;
            case "CGECMStats":
               put(new ECM(statsModule));
               break;
            case "CBAPStats":
            case "CCASEStats":
            case "CDummyHeadStats":
               put(new Module(statsModule));
               break;
            default:
               break; // Other modules not yet supported
         }
      }

      // Weapons next.
      for(ItemStatsWeapon statsWeapon : stats.WeaponList){
         switch( HardpointType.fromMwoType(statsWeapon.WeaponStats.type) ){
            case AMS:
               put(new AmmoWeapon(statsWeapon, HardpointType.AMS));
               break;
            case BALLISTIC:
               put(new BallisticWeapon(statsWeapon));
               break;
            case ENERGY:
               put(new EnergyWeapon(statsWeapon));
               break;
            case MISSILE:
               put(new MissileWeapon(statsWeapon));
               break;
            default:
               throw new RuntimeException("Unknown value for type field in ItemStatsXML. Please update the program!");
         }
      }
   }
}

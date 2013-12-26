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
package lisong_mechlab.model.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsWeapon;

public class ItemDB{
   // Special global "item constants" use full when checking special cases.
   // Feel free to populate if you find yourself consistently using
   // ItemDB.lookup() with constant strings.

   // AMS
   static public final AmmoWeapon          AMS;
   static public final HeatSink            SHS;
   static public final HeatSink            DHS;
   static public final Item                ECM;
   static public final Item                BAP;

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
      String key = aString.toLowerCase();
      if( !key.contains("ammo") && key.contains("rm") )
         return MissileWeapon.canonize(aString).toLowerCase();
      return key;
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

      // Initialize special items
      AMS = (AmmoWeapon)lookup("ANTI-MISSILE SYSTEM");
      SHS = (HeatSink)lookup("STD HEAT SINK");
      DHS = (HeatSink)lookup("DOUBLE HEAT SINK");
      ECM = lookup("GUARDIAN ECM");
      BAP = lookup("BEAGLE ACTIVE PROBE");
   }
}

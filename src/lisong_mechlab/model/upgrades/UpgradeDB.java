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
package lisong_mechlab.model.upgrades;

import java.util.Map;
import java.util.TreeMap;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.mwo_parsing.ItemStatsXml;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsUpgradeType;

/**
 * A database class that holds all the {@link Upgrade}s parsed from the game files.
 * 
 * @author Emily Björk
 */
public class UpgradeDB{
   public static final ArmorUpgrade     STANDARD_ARMOR;
   public static final StructureUpgrade STANDARD_STRUCTURE;
   public static final GuidanceUpgrade  STANDARD_GUIDANCE;
   public static final HeatsinkUpgrade  STANDARD_HEATSINKS;
   public static final GuidanceUpgrade  ARTEMIS_IV;
   public static final HeatsinkUpgrade  DOUBLE_HEATSINKS;
   public static final ArmorUpgrade     FERRO_FIBROUS_ARMOR;
   public static final StructureUpgrade ENDO_STEEL_STRUCTURE;
   private static Map<Integer, Upgrade> id2upgrade;

   public static void initialize(){
      ItemStatsXml xml = ItemStatsXml.stats;
      id2upgrade = new TreeMap<Integer, Upgrade>();
      @SuppressWarnings("unused")
      Item dummy = ItemDB.DHS; // force itemDB to become loaded

      for(ItemStatsUpgradeType upgradeType : xml.UpgradeTypeList){
         UpgradeType type = UpgradeType.fromMwo(upgradeType.UpgradeTypeStats.type);
         switch( type ){
            case ARMOR:
               addUpgrade(new ArmorUpgrade(upgradeType), upgradeType);
               break;
            case GUIDANCE:
               addUpgrade(new GuidanceUpgrade(upgradeType), upgradeType);
               break;
            case HEATSINKS:
               addUpgrade(new HeatsinkUpgrade(upgradeType), upgradeType);
               break;
            case STRUCTURE:
               addUpgrade(new StructureUpgrade(upgradeType), upgradeType);
               break;
         }
      }
   }

   static{
      initialize();
      STANDARD_ARMOR = (ArmorUpgrade)lookup(2810);
      FERRO_FIBROUS_ARMOR = (ArmorUpgrade)lookup(2811);
      
      STANDARD_STRUCTURE = (StructureUpgrade)lookup(3100);
      ENDO_STEEL_STRUCTURE = (StructureUpgrade)lookup(3101);
      
      STANDARD_HEATSINKS = (HeatsinkUpgrade)lookup(3003);
      DOUBLE_HEATSINKS = (HeatsinkUpgrade)lookup(3002);
      
      STANDARD_GUIDANCE = (GuidanceUpgrade)lookup(3051);
      ARTEMIS_IV = (GuidanceUpgrade)lookup(3050);
   }

   /**
    * Looks up an {@link Upgrade} by its MW:O ID.
    * 
    * @param aMwoId
    *           The ID to look up.
    * @return The {@link Upgrade} for the sought for ID.
    * @throws IllegalArgumentException
    *            Thrown if the ID is not a valid upgrade ID.
    */
   public static Upgrade lookup(int aMwoId) throws IllegalArgumentException{
      Upgrade ans = id2upgrade.get(aMwoId);
      if( null == ans ){
         throw new IllegalArgumentException("The ID: " + aMwoId + " is not a valid MWO upgrade ID!");
      }
      return ans;
   }

   private static void addUpgrade(Upgrade anUpgrade, ItemStatsUpgradeType anUpgradeType){
      id2upgrade.put(anUpgrade.getMwoId(), anUpgrade);
      if( anUpgradeType.UpgradeTypeStats.associatedItem > 0 )
         id2upgrade.put(anUpgradeType.UpgradeTypeStats.associatedItem, anUpgrade);
   }
}

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

import java.util.Collections;
import java.util.List;

import lisong_mechlab.model.chassi.ChassisIS;
import lisong_mechlab.model.chassi.ChassisDB;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.GuidanceUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;

/**
 * This immutable class defines a stock loadout pattern that can be used for loading stock on a {@link Loadout}.
 * 
 * @author Li Song
 */
public class StockLoadout{
   /**
    * This immutable class defines a component in a stock loadout.
    * 
    * @author Li Song
    */
   public static class StockComponent{
      private final Location      part;
      private final Integer       armorFront;
      private final Integer       armorBack;
      private final List<Integer> items;

      /**
       * Creates a new {@link StockComponent}.
       * 
       * @param aPart
       *           The {@link Location} that this {@link StockComponent} is for.
       * @param aFront
       *           The front armor (or total armor if one sided).
       * @param aBack
       *           The back armor (must be zero if one sided).
       * @param aItems
       *           A {@link List} of items in the component.
       */
      public StockComponent(Location aPart, int aFront, int aBack, List<Integer> aItems){
         part = aPart;
         armorFront = aFront;
         if( part.isTwoSided() ){
            armorBack = aBack;
         }
         else{
            armorBack = null;
         }
         items = Collections.unmodifiableList(aItems);
      }

      /**
       * @return The {@link Location} that defines this {@link StockComponent}.
       */
      public Location getPart(){
         return part;
      }

      /**
       * @return The front armor of this {@link StockComponent}. Or total armor if the component is one sided.
       */
      public int getArmorFront(){
         return armorFront;
      }

      /**
       * @return The back armor of this {@link StockComponent}. Will throw if the component is one sided.
       */
      public int getArmorBack(){
         return armorBack;
      }

      /**
       * @return The {@link Item} IDs that are housed in this {@link StockComponent}.
       */
      public List<Integer> getItems(){
         return items;
      }
   }

   private final List<StockComponent> components;

   private final Integer              armorId;
   private final Integer              structureId;
   private final Integer              heatsinkId;
   private final Integer              guidanceId;
   private final Integer              chassisId;

   /**
    * Creates a new {@link StockLoadout}
    * 
    * @param aChassisId
    *           The ID of the chassis that this loadout was originally for.
    * @param aComponents
    *           The list of {@link StockComponent} that make up this {@link StockLoadout}.
    * @param aArmor
    *           The armor upgrade type.
    * @param aStructure
    *           The structure upgrade type.
    * @param aHeatSink
    *           The heat sink upgrade type.
    * @param aGuidance
    *           The guidance upgrade type.
    */
   public StockLoadout(int aChassisId, List<StockComponent> aComponents, int aArmor, int aStructure, int aHeatSink, int aGuidance){
      chassisId = aChassisId;
      armorId = aArmor;
      structureId = aStructure;
      heatsinkId = aHeatSink;
      guidanceId = aGuidance;
      components = Collections.unmodifiableList(aComponents);
   }

   /**
    * @return The {@link ChassisIS} for this {@link StockLoadout}.
    */
   public ChassisIS getChassis(){
      return ChassisDB.lookup(chassisId);
   }

   /**
    * @return The {@link ArmorUpgrade} for this {@link StockLoadout}.
    */
   public ArmorUpgrade getArmorType(){
      return (ArmorUpgrade)UpgradeDB.lookup(armorId);
   }

   /**
    * @return The {@link StructureUpgrade} for this {@link StockLoadout}.
    */
   public StructureUpgrade getStructureType(){
      return (StructureUpgrade)UpgradeDB.lookup(structureId);
   }

   /**
    * @return The {@link HeatSinkUpgrade} for this {@link StockLoadout}.
    */
   public HeatSinkUpgrade getHeatSinkType(){
      return (HeatSinkUpgrade)UpgradeDB.lookup(heatsinkId);
   }

   /**
    * @return The {@link GuidanceUpgrade} for this {@link StockLoadout}.
    */
   public GuidanceUpgrade getGuidanceType(){
      return (GuidanceUpgrade)UpgradeDB.lookup(guidanceId);
   }

   /**
    * @return The {@link StockComponent}s in this {@link StockLoadout}.
    */
   public List<StockComponent> getComponents(){
      return components;
   }
}

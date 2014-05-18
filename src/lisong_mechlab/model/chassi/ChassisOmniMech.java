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
package lisong_mechlab.model.chassi;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;

/**
 * This class models an omnimech chassis, i.e. the basic attributes associated with the chassis and the center omnipod.
 * 
 * @author Emily Björk
 */
public class ChassisOmniMech extends ChassisBase{

   private final Engine           engine;
   private final StructureUpgrade structureType;
   private final ArmorUpgrade     armorType;
   private final HeatSinkUpgrade  heatSinkType;
   private final OmniPod          centerTorsoOmniPod;
   private final int[]            dynamicStructure;
   private final int[]            dynamicArmor;

   private transient int          maxArmor = -1;

   /**
    * @param aMwoID
    *           The MWO ID of the chassis as found in the XML.
    * @param aMwoName
    *           The MWO name of the chassis as found in the XML.
    * @param aSeries
    *           The name of the series for example "ORION" or "JENNER".
    * @param aName
    *           The long name of the mech, for example "JENNER JR7-F".
    * @param aShortName
    *           The short name of the mech, for example "JR7-F".
    * @param aMaxTons
    *           The maximum tonnage of the mech.
    * @param aVariant
    *           The variant type of the mech, like hero, champion etc.
    * @param aBaseVariant
    *           The base chassisID that this chassis is based on if any, -1 if not based on any chassis.
    * @param aMovementProfile
    *           The {@link MovementProfile} of this chassis.
    * @param aIsClan
    *           True if this is a clan chassis.
    * @param aEngine
    *           The engine that is fixed on this chassis.
    * @param aStructureType
    *           The structure type that is fixed on this chassis.
    * @param aArmorType
    *           The armor type that is fixed on this chassis.
    * @param aHeatSinkType
    *           The heat sink type that is fixed on this chassis.
    * @param aCenterTorso
    *           TODO
    * @param aDynamicStructureSlots
    *           An array where each element represents the ordinal of a {@link Location} and how many dynamic structure
    *           slots are fixed at that location.
    * @param aDynamicArmorSlots
    *           An array where each element represents the ordinal of a {@link Location} and how many dynamic armor
    *           slots are fixed at that location.
    */
   public ChassisOmniMech(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons, ChassisVariant aVariant,
                          int aBaseVariant, MovementProfile aMovementProfile, boolean aIsClan, Engine aEngine, StructureUpgrade aStructureType,
                          ArmorUpgrade aArmorType, HeatSinkUpgrade aHeatSinkType, OmniPod aCenterTorso, int[] aDynamicStructureSlots,
                          int[] aDynamicArmorSlots){
      super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile, aIsClan);
      engine = aEngine;
      structureType = aStructureType;
      armorType = aArmorType;
      heatSinkType = aHeatSinkType;
      centerTorsoOmniPod = aCenterTorso;
      dynamicStructure = aDynamicStructureSlots;
      dynamicArmor = aDynamicArmorSlots;

      if( dynamicArmor.length != Location.values().length ){
         throw new IllegalArgumentException("The aDynamicArmorSlots argument must have one entry for each Location.");
      }

      if( dynamicStructure.length != Location.values().length ){
         throw new IllegalArgumentException("The aDynamicStructureSlots argument must have one entry for each Location.");
      }

      int s = 0;
      int a = 0;
      for(int i = 0; i < dynamicArmor.length; ++i){
         s += dynamicStructure[i];
         a += dynamicArmor[i];
      }
      if( s != structureType.getExtraSlots() ){
         throw new IllegalArgumentException("The values in aDynamicStructureSlots must sum up to the number of slots required by the structure type.");
      }
      if( a != armorType.getExtraSlots() ){
         throw new IllegalArgumentException("The values in aDynamicArmorSlots must sum up to the number of slots required by the armor type.");
      }
   }

   @Override
   public int getArmorMax(){
      if( maxArmor <= 0 ){
         maxArmor = calculateMaxArmor();
      }
      return maxArmor;
   }

   /**
    * @return The maximal amount of armor this chassis can support.
    */
   private int calculateMaxArmor(){
      // Assuming that all omnipods for a location can carry the same amount of armor.
      int ans = 0;
      for(Location location : Location.values()){
         OmniPod omniPod = OmniPodDB.lookupOriginal(this, location);
         ans += omniPod.getArmorMax();
      }
      return ans;
   }

   /**
    * @return The engine that is fixed to this omnimech chassis.
    */
   public Engine getEngine(){
      return engine;
   }

   /**
    * @return The type of the fixed internal structure of this omnimech.
    */
   public StructureUpgrade getStructureType(){
      return structureType;
   }

   /**
    * @return The type of the fixed armor of this omnimech.
    */
   public ArmorUpgrade getArmorType(){
      return armorType;
   }

   /**
    * @return The type of the fixed heat sinks of this omnimech.
    */
   public HeatSinkUpgrade getHeatSinkType(){
      return heatSinkType;
   }

   /**
    * @param aLocation
    *           The location to query for.
    * @return The number of dynamic armor slots in the given location.
    */
   public int getDynamicArmorSlots(Location aLocation){
      return dynamicArmor[aLocation.ordinal()];
   }

   /**
    * @param aLocation
    *           The location to query for.
    * @return The number of dynamic structure slots in the given location.
    */
   public int getDynamicStructureSlots(Location aLocation){
      return dynamicStructure[aLocation.ordinal()];
   }
}

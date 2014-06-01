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
package lisong_mechlab.model.chassi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.upgrades.ArmorUpgrade;
import lisong_mechlab.model.upgrades.HeatSinkUpgrade;
import lisong_mechlab.model.upgrades.StructureUpgrade;

/**
 * This class models an omnimech chassis, i.e. the basic attributes associated with the chassis and the center omnipod.
 * 
 * @author Li Song
 */
public class ChassisOmniMech extends ChassisBase{

   private final Engine           engine;
   private final StructureUpgrade structureType;
   private final ArmorUpgrade     armorType;
   private final HeatSinkUpgrade  heatSinkType;

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
    * @param aComponents
    *           An array of components for this chassis.
    * @param aEngine
    *           The engine that is fixed on this chassis.
    * @param aStructureType
    *           The structure type that is fixed on this chassis.
    * @param aArmorType
    *           The armor type that is fixed on this chassis.
    * @param aHeatSinkType
    *           The heat sink type that is fixed on this chassis.
    */
   public ChassisOmniMech(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons, ChassisVariant aVariant,
                          int aBaseVariant, MovementProfile aMovementProfile, boolean aIsClan, ComponentOmniMech[] aComponents, Engine aEngine,
                          StructureUpgrade aStructureType, ArmorUpgrade aArmorType, HeatSinkUpgrade aHeatSinkType){
      super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile, aIsClan, aComponents);
      engine = aEngine;
      structureType = aStructureType;
      armorType = aArmorType;
      heatSinkType = aHeatSinkType;

      int s = 0;
      int a = 0;
      for(ComponentOmniMech component : getComponents()){
         s += component.getDynamicStructureSlots();
         a += component.getDynamicArmorSlots();
      }
      if( s != structureType.getExtraSlots() ){
         throw new IllegalArgumentException("The values in aDynamicStructureSlots must sum up to the number of slots required by the structure type.");
      }
      if( a != armorType.getExtraSlots() ){
         throw new IllegalArgumentException("The values in aDynamicArmorSlots must sum up to the number of slots required by the armor type.");
      }
   }

   @Override
   public ComponentOmniMech getComponent(Location aLocation){
      return (ComponentOmniMech)super.getComponent(aLocation);
   }

   @SuppressWarnings("unchecked")
   @Override
   public Collection<ComponentOmniMech> getComponents(){
      return (Collection<ComponentOmniMech>)super.getComponents();
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

   @Override
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Engine ){
         return false; // Engine is fixed.
      }
      return super.isAllowed(aItem); // Anything else depends on the actual combination of omnipods equipped
   }

   /**
    * TODO: Test when we have all omnipods in {@link OmniPodDB}.
    * 
    * @return The {@link MovementProfile} for the stock selection of {@link OmniPod}s.
    */
   public MovementProfile getMovementProfileStock(){
      MovementProfileSum ans = new MovementProfileSum(getMovementProfileBase());
      for(Location location : Location.values()){
         OmniPod omniPod = OmniPodDB.lookupOriginal(this, location);
         ans.addMovementProfile(omniPod.getQuirks());
      }
      return ans;
   }

   /**
    * TODO: Test when we have all omnipods in {@link OmniPodDB}.
    * 
    * @return The {@link MovementProfile} where the {@link OmniPod} for each {@link ComponentOmniMech} is selected to
    *         minimize each attribute. All the values of the {@link MovementProfile} may not be attainable
    *         simultaneously but each value of each attribute is independently attainable for some combination of
    *         {@link OmniPod}.
    */
   public MovementProfile getMovementProfileMin(){
      return new MinMovementProfile(getMovementProfileBase(), getOmniPodMovementProfileGroups());
   }

   /**
    * TODO: Test when we have all omnipods in {@link OmniPodDB}.
    * 
    * @return The {@link MovementProfile} where the {@link OmniPod} for each {@link ComponentOmniMech} is selected to
    *         maximize each attribute. All the values of the {@link MovementProfile} may not be attainable
    *         simultaneously but each value of each attribute is independently attainable for some combination of
    *         {@link OmniPod}.
    */
   public MovementProfile getMovementProfileMax(){
      return new MaxMovementProfile(getMovementProfileBase(), getOmniPodMovementProfileGroups());
   }

   private List<List<MovementProfile>> getOmniPodMovementProfileGroups(){
      List<List<MovementProfile>> groups = new ArrayList<>();

      for(Location location : Location.values()){
         List<MovementProfile> group = new ArrayList<>();

         if( getComponent(location).hasFixedOmniPod() ){
            group.add(OmniPodDB.lookupOriginal(this, location).getQuirks());
         }
         else{
            for(OmniPod omniPod : OmniPodDB.lookup(this, location)){
               group.add(omniPod.getQuirks());
            }
         }
         groups.add(group);
      }
      return groups;
   }
}

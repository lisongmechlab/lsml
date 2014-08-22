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

import java.util.Collection;
import java.util.List;

import lisong_mechlab.model.Faction;
import lisong_mechlab.model.chassi.Quirks.Quirk;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.JumpJet;
import lisong_mechlab.model.loadout.LoadoutStandard;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents a bare inner sphere 'mech chassis.
 * <p>
 * The class is immutable as the chassis are fixed. To configure a inner sphere 'mech use {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class ChassisStandard extends ChassisBase{
   @XStreamAsAttribute
   private final int    engineMin;
   @XStreamAsAttribute
   private final int    engineMax;
   @XStreamAsAttribute
   private final int    maxJumpJets;
   private final Quirks quirks;

   /**
    * Creates a new {@link ChassisStandard}.
    * 
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
    * @param aFaction
    *           The {@link Faction} this chassis belongs to.
    * @param aEngineMin
    *           The smallest engine rating that can be equipped.
    * @param aEngineMax
    *           The largest engine rating that can be equipped.
    * @param aMaxJumpJets
    *           The maximal number of jump jets that can be equipped.
    * @param aComponents
    *           An array of {@link ComponentStandard} that defines the internal components of the chassis.
    * @param aMaxPilotModules
    *           The maximum number of pilot modules that can be equipped.
    * @param aMaxConsumableModules
    *           The maximal number of consumable modules this chassis can support.
    * @param aMaxWeaponModules
    *           The maximal number of weapon modules this chassis can support.
    * @param aQuirks
    *           The chassis quirks for this chassis.
    */
   public ChassisStandard(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons, ChassisVariant aVariant,
                          int aBaseVariant, MovementProfile aMovementProfile, Faction aFaction, int aEngineMin, int aEngineMax, int aMaxJumpJets,
                          ComponentStandard[] aComponents, int aMaxPilotModules, int aMaxConsumableModules, int aMaxWeaponModules, Quirks aQuirks){
      super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile, aFaction, aComponents,
            aMaxPilotModules, aMaxConsumableModules, aMaxWeaponModules);
      engineMin = aEngineMin;
      engineMax = aEngineMax;
      maxJumpJets = aMaxJumpJets;
      quirks = aQuirks;
   }

   /**
    * @return The largest engine rating that this chassis can support.
    */
   public int getEngineMax(){
      return engineMax;
   }

   /**
    * @return The smallest engine rating required to move this chassis.
    */
   public int getEngineMin(){
      return engineMin;
   }

   @Override
   public ComponentStandard getComponent(Location aLocation){
      return (ComponentStandard)super.getComponent(aLocation);
   }

   @SuppressWarnings("unchecked")
   @Override
   public Collection<ComponentStandard> getComponents(){
      return (Collection<ComponentStandard>)super.getComponents();
   }

   /**
    * @param aHardPointType
    *           The type of hard points to count.
    * @return The number of hard points of the given type.
    */
   public int getHardPointsCount(HardPointType aHardPointType){
      int sum = 0;
      for(ComponentStandard part : getComponents()){
         sum += part.getHardPointCount(aHardPointType);
      }
      return sum;
   }

   /**
    * @return The maximal number of jump jets the chassis can support.
    */
   public int getJumpJetsMax(){
      return maxJumpJets;
   }

   @Override
   public boolean isAllowed(Item aItem){
      if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;

         if( engine.getRating() < getEngineMin() || engine.getRating() > getEngineMax() ){
            return false;
         }
      }
      else if( aItem instanceof JumpJet && getJumpJetsMax() <= 0 ){
         return false;
      }
      return super.isAllowed(aItem);
   }

   /**
    * @return A {@link List} of all the {@link Quirk} on the chassis.
    */
   public Quirks getQuirks(){
      return quirks;
   }
}

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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
   private final int                 engineMin;
   @XStreamAsAttribute
   private final int                 engineMax;
   @XStreamAsAttribute
   private final int                 maxJumpJets;
   private final InternalComponent[] components;

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
    * @param aIsClan
    *           True if this is a clan chassis.
    * @param aEngineMin
    *           The smallest engine rating that can be equipped.
    * @param aEngineMax
    *           The largest engine rating that can be equipped.
    * @param aMaxJumpJets
    *           The maximal number of jump jets that can be equipped.
    * @param aComponents
    *           An array of {@link InternalComponent} that defines the internal components of the chassis.
    */
   public ChassisStandard(int aMwoID, String aMwoName, String aSeries, String aName, String aShortName, int aMaxTons, ChassisVariant aVariant,
                          int aBaseVariant, MovementProfile aMovementProfile, boolean aIsClan, int aEngineMin, int aEngineMax, int aMaxJumpJets,
                          InternalComponent[] aComponents){
      super(aMwoID, aMwoName, aSeries, aName, aShortName, aMaxTons, aVariant, aBaseVariant, aMovementProfile, aIsClan);
      engineMin = aEngineMin;
      engineMax = aEngineMax;
      maxJumpJets = aMaxJumpJets;
      components = aComponents;

      if( components.length != Location.values().length )
         throw new IllegalArgumentException("Components array must contain all components!");
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

   /**
    * @return The maximal, total amount of armor the chassis can support.
    */
   @Override
   public int getArmorMax(){
      int ans = 0;
      for(InternalComponent internalPart : components){
         ans += internalPart.getArmorMax();
      }
      return ans;
   }

   /**
    * @param aHardpointType
    *           The type of hard points to count.
    * @return The number of hard points of the given type.
    */
   public int getHardpointsCount(HardPointType aHardpointType){
      int sum = 0;
      for(InternalComponent part : components){
         sum += part.getHardPointCount(aHardpointType);
      }
      return sum;
   }

   /**
    * @param aLocation
    *           The location of the internal component we're interested in.
    * @return The internal component in the given location.
    */
   public InternalComponent getComponent(Location aLocation){
      return components[aLocation.ordinal()];
   }

   /**
    * @return A {@link Collection} of all the internal components.
    */
   public Collection<InternalComponent> getComponents(){
      return Collections.unmodifiableList(Arrays.asList(components));
   }

   /**
    * @return The maximal number of jump jets the chassis can support.
    */
   public int getJumpJetsMax(){
      return maxJumpJets;
   }

   @Override
   public boolean isAllowed(Item aItem){
      if( !super.isAllowed(aItem) ){
         return false;
      }
      else if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;
         return engine.getRating() >= getEngineMin() && engine.getRating() <= getEngineMax();
      }
      else if( aItem instanceof JumpJet ){
         JumpJet jj = (JumpJet)aItem;
         return getJumpJetsMax() > 0 && jj.getMinTons() <= getMassMax() && getMassMax() < jj.getMaxTons();
      }
      for(InternalComponent part : components){
         if( part.isAllowed(aItem) )
            return true;
      }
      return false;
   }
}

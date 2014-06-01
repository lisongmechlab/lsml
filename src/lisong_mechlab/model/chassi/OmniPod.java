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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * This class represents an omnipod of an omnimech configuration.
 * 
 * @author Emily Björk
 */
public class OmniPod{
   private final int             mwoID;
   private final Location        location;
   private final String          chassis;
   private final int             chassisVariant;
   private final MovementProfile quirks;
   private final HardPoint[]     hardPoints;
   private final int             maxJumpJets;
   private final int             maxPilotModules;

   /**
    * Creates a new {@link OmniPod}.
    * 
    * @param aMwoID
    *           The MWO ID of this {@link OmniPod}.
    * @param aLocation
    *           The {@link Location} that this omnipod can be mounted at.
    * @param aSeriesName
    *           The name of the series this {@link OmniPod} belongs to, for example "TIMBER WOLF".
    * @param aOriginalChassisID
    *           The MWO ID of the specific variant that this {@link OmniPod} is part of, for example
    *           "TIMBER WOLF PRIME".
    * @param aQuirks
    *           A set of quirks this {@link OmniPod} will bring to the loadout if equipped.
    * @param aHardPoints
    *           An array of {@link HardPoint}s for this {@link OmniPod}.
    * @param aMaxJumpJets
    *           The maximum number of jump jets this {@link OmniPod} can support.
    * @param aMaxPilotModules
    *           The number of pilot modules that this {@link OmniPod} adds to the loadout.
    */
   public OmniPod(int aMwoID, Location aLocation, String aSeriesName, int aOriginalChassisID, MovementProfile aQuirks, HardPoint[] aHardPoints,
                  int aMaxJumpJets, int aMaxPilotModules){
      mwoID = aMwoID;
      location = aLocation;
      chassis = aSeriesName;
      chassisVariant = aOriginalChassisID;
      quirks = aQuirks;
      hardPoints = aHardPoints;
      maxJumpJets = aMaxJumpJets;
      maxPilotModules = aMaxPilotModules;

   }

   @Override
   public String toString(){
      return ((ChassisOmniMech)ChassisDB.lookup(chassisVariant)).getNameShort() + " " + location.shortName();
   }

   /**
    * @return The maximum number of jump jets one can equip on this omnipod.
    */
   public int getJumpJetsMax(){
      return maxJumpJets;
   }

   /**
    * @return {@link Location} that this omnipod can be equipped on.
    */
   public Location getLocation(){
      return location;
   }

   /**
    * @return The mech ID of the original chassis this omnipod is a part of.
    */
   public int getOriginalChassisId(){
      return chassisVariant;
   }

   /**
    * @return The omnipod specific movement quirks.
    */
   public MovementProfile getQuirks(){
      return quirks;
   }

   /**
    * @param aChassis
    *           The chassis to check for compatibility to.
    * @return <code>true</code> if the argument is a compatible chassis.
    */
   public boolean isCompatible(ChassisOmniMech aChassis){
      return aChassis.getSeriesName().toLowerCase().contains(chassis);
   }

   /**
    * @return The MWO ID of this {@link OmniPod}.
    */
   public int getMwoID(){
      return mwoID;
   }

   /**
    * @return The maximum number of pilot modules this {@link OmniPod} can support.
    */
   public int getMaxPilotModules(){
      return maxPilotModules;
   }

   /**
    * @return An unmodifiable collection of all {@link HardPoint}s this {@link OmniPod} has.
    */
   public Collection<HardPoint> getHardPoints(){
      return Collections.unmodifiableCollection(Arrays.asList(hardPoints));
   }

   /**
    * @param aHardpointType
    *           The type of {@link HardPoint}s to count.
    * @return The number of {@link HardPoint}s of the given type.
    */
   public int getHardPointCount(HardPointType aHardpointType){
      int ans = 0;
      for(HardPoint it : hardPoints){
         if( it.getType() == aHardpointType ){
            ans++;
         }
      }
      return ans;
   }

   /**
    * @return <code>true</code> if this {@link OmniPod} has missile bay doors.
    */
   public boolean hasMissileBayDoors(){
      // TODO Auto-generated method stub FIXME
      return true;
   }
}

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

import java.util.List;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.mwo_data.helpers.MdfMovementTuning;

/**
 * This class represents an omnipod of an omnimech configuration.
 * 
 * @author Emily Björk
 */
public class OmniPod extends InternalComponent{

   private final String          compatibleChassis;
   private final int             maxJumpJets;
   private final int             maxPilotModules;
   private final MovementProfile quirks;
   private final int             originalChassisId;

   /**
    * @param aLocation
    * @param aSlots
    * @param aHP
    * @param aFixedItems
    * @param aHardPoints
    * @param aOriginalChassisID
    * @param aSeriesName
    * @param aMaxJumpJets
    * @param aMaxPilotModules
    */
   public OmniPod(Location aLocation, int aSlots, double aHP, List<Item> aFixedItems, List<HardPoint> aHardPoints, int aOriginalChassisID,
                  String aSeriesName, int aMaxJumpJets, int aMaxPilotModules){
      super(aLocation, aSlots, aHP, aFixedItems, aHardPoints);

      compatibleChassis = aSeriesName;
      maxJumpJets = aMaxJumpJets;
      maxPilotModules = aMaxPilotModules;

      quirks = new BaseMovementProfile(new MdfMovementTuning());
      originalChassisId = aOriginalChassisID;

      // TODO: Add locked items to internalItems
   }

   @Override
   public String toString(){
      return ((ChassisOmniMech)ChassisDB.lookup(originalChassisId)).getNameShort() + " " + getLocation().shortName();
   }

   /**
    * @param aChassis
    *           The chassis to check for compatibility to.
    * @return <code>true</code> if the argument is a compatible chassis.
    */
   public boolean isCompatible(ChassisOmniMech aChassis){
      return aChassis.getSeriesName().toLowerCase().contains(compatibleChassis);
   }

   /**
    * @return The omnipod specific movement quirks.
    */
   public MovementProfile getQuirks(){
      return quirks;
   }

   /**
    * @return The maximum number of jump jets one can equip on this omnipod.
    */
   public int getJumpJetsMax(){
      return maxJumpJets;
   }

   /**
    * @return The mech ID of the original chassis this omnipod is a part of.
    */
   public int getOriginalChassisId(){
      return originalChassisId;
   }

}

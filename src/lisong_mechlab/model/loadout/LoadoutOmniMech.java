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
package lisong_mechlab.model.loadout;

import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.chassi.OmniPod;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * @author Li Song
 */
public class LoadoutOmniMech{
   private final OmniPod omnipods[] = new OmniPod[Location.values().length];

   /**
    * Gets the {@link OmniPod} equipped at a particular location. Or <code>null</code> if no omnipod is assigned to the
    * given location.
    * 
    * @param aPart
    *           A {@link Location} location for the pod.
    * @return An {@link OmniPod} or <code>null</code>.
    */
   public OmniPod getOmniPod(Location aPart){
      return omnipods[aPart.ordinal()];
   }

   /**
    * This setter method is only intended to be used from package local {@link Operation}s. It's a raw, unchecked
    * accessor.
    * 
    * @param aOmniPod
    *           The omnipod to set, it's put in it's dedicated slot.
    */
   void setOmniPod(OmniPod aOmniPod){
      omnipods[aOmniPod.getLocation().ordinal()] = aOmniPod;
   }

}

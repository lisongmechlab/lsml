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
package lisong_mechlab.model.upgrades;

import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * An abstract package local class that facilitates implementing {@link Operation}s that relate to {@link Upgrades}.
 * 
 * @author Li Song
 */
abstract class UpgradeOperation extends CompositeOperation{
   protected final Loadout               loadout;
   protected final Upgrades              upgrades;
   protected final transient MessageXBar xBar;

   public UpgradeOperation(MessageXBar anXBar, Loadout aLoadout, String aDescription){
      super(aDescription);
      loadout = aLoadout;
      upgrades = aLoadout.getUpgrades();
      xBar = anXBar;
   }

   /**
    * This creates an {@link UpgradeOperation} without an associated loadout. This is useful in cases when the
    * {@link Upgrades} is only used as a container for upgrades without an actual loadout.
    * 
    * @param aUpgrades
    *           The {@link Upgrades} object to affect.
    * @param aDescription
    *           The name of this operation.
    */
   public UpgradeOperation(Upgrades aUpgrades, String aDescription){
      super(aDescription);
      loadout = null;
      upgrades = aUpgrades;
      xBar = null;
   }

   protected void verifyLoadoutInvariant(){
      if( loadout == null )
         return;
      if( loadout.getFreeMass() < 0 ){
         throw new IllegalArgumentException("Not enough tonnage!");
      }
      if( loadout.getNumCriticalSlotsFree() < 0 ){
         throw new IllegalArgumentException("Not enough free slots!");
      }
      for(LoadoutPart loadoutPart : loadout.getPartLoadOuts()){
         if( loadoutPart.getNumCriticalSlotsFree() < 0 ){
            throw new IllegalArgumentException("Not enough free slots!");
         }
      }
   }
}

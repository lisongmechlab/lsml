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

import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.component.ConfiguredComponent;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * An abstract package local class that facilitates implementing {@link Operation}s that relate to
 * {@link UpgradesMutable}.
 * 
 * @author Emily Björk
 */
abstract class OpUpgradeBase extends CompositeOperation{
   protected final transient MessageXBar xBar;

   protected OpUpgradeBase(MessageXBar anXBar, String aDescription){
      super(aDescription);
      xBar = anXBar;
   }

   protected void verifyLoadoutInvariant(LoadoutBase<?, ?> aLoadout){
      if( aLoadout == null )
         return;
      if( aLoadout.getFreeMass() < 0 ){
         throw new IllegalArgumentException("Not enough tonnage!");
      }
      if( aLoadout.getNumCriticalSlotsFree() < 0 ){
         throw new IllegalArgumentException("Not enough free slots!");
      }
      for(ConfiguredComponent loadoutPart : aLoadout.getComponents()){
         if( loadoutPart.getSlotsFree() < 0 ){
            throw new IllegalArgumentException("Not enough free slots!");
         }
      }
   }
}

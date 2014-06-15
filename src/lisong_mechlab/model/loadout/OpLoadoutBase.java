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
package lisong_mechlab.model.loadout;

import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.CompositeOperation;

/**
 * Base class for operations operating on a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public abstract class OpLoadoutBase extends CompositeOperation{

   protected final MessageXBar       xBar;
   protected final LoadoutBase<?> loadout;

   /**
    * @param aLoadout
    *           The {@link LoadoutStandard} to operate on.
    * @param aXBar
    *           The {@link MessageXBar} to announce changes on the loadout to.
    * @param aDescription
    *           A human readable description of the operation.
    */
   public OpLoadoutBase(LoadoutBase<?> aLoadout, MessageXBar aXBar, String aDescription){
      super(aDescription);
      loadout = aLoadout;
      xBar = aXBar;
   }
}

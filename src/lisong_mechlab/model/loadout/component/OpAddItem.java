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
package lisong_mechlab.model.loadout.component;

import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * This {@link Operation} adds an {@link Item} to a {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
public class OpAddItem extends OpItemBase{
   /**
    * Creates a new operation.
    * 
    * @param aXBar
    *           The {@link MessageXBar} to send messages on when items are added.
    * @param aLoadout
    *           The {@link LoadoutBase} to remove the item from.
    * @param aComponent
    *           The {@link ConfiguredComponentBase} to add to.
    * @param aItem
    *           The {@link Item} to add.
    */
   public OpAddItem(MessageXBar aXBar, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent, Item aItem){
      super(aXBar, aLoadout, aComponent, aItem);
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((item == null) ? 0 : item.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( !(obj instanceof OpRemoveItem) )
         return false;
      OpAddItem other = (OpAddItem)obj;
      return item == other.item && super.equals(other);
   }

   @Override
   public String describe(){
      return "add " + item.getName() + " to " + component.getInternalComponent().getLocation();
   }

   @Override
   public void undo(){
      removeItem(item);
   }

   @Override
   public void apply(){
      if( !loadout.canEquip(item) )
         throw new IllegalArgumentException("Can't add " + item + " to " + loadout.getName() + "!");
      if( !component.canAddItem(item) )
         throw new IllegalArgumentException("Can't add " + item + " to " + component.getInternalComponent().getLocation() + "!");
      addItem(item);
   }
}

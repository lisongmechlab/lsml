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
package lisong_mechlab.model.loadout.part;

import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.OperationStack.Operation;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.util.MessageXBar;

/**
 * A helper class for implementing {@link Operation}s that affect items on a {@link LoadoutPart}.
 * 
 * @author Emily Björk
 */
abstract class ItemOperation extends Operation{
   protected final Item        item;
   private int                 numEngineHS = 0;
   protected final LoadoutPart loadoutPart;
   private MessageXBar         xBar;

   /**
    * Creates a new {@link ItemOperation}. The deriving classes shall throw if the the operation with the given item
    * would violate the {@link Loadout} or {@link LoadoutPart} invariant.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to send messages to when changes occur.
    * @param aLoadoutPart
    *           The {@link LoadoutPart} that this operation will affect.
    * @param anItem
    *           The item that shall be affected.
    */
   ItemOperation(MessageXBar anXBar, LoadoutPart aLoadoutPart, Item anItem){
      item = anItem;
      loadoutPart = aLoadoutPart;
      xBar = anXBar;
   }

   /**
    * Removes an item without checks. Will count up the numEngineHS variable to the number of heat sinks removed.
    */
   protected void removeItem(){
      if( item instanceof Engine ){
         Engine engine = (Engine)item;
         if( engine.getType() == EngineType.XL ){
            LoadoutPart lt = loadoutPart.getLoadout().getPart(Part.LeftTorso);
            LoadoutPart rt = loadoutPart.getLoadout().getPart(Part.RightTorso);
            lt.removeItem(LoadoutPart.ENGINE_INTERNAL);
            rt.removeItem(LoadoutPart.ENGINE_INTERNAL);
            xBar.post(new Message(lt, Type.ItemRemoved));
            xBar.post(new Message(rt, Type.ItemRemoved));
         }

         int engineHsLeft = loadoutPart.getNumEngineHeatsinks();
         while( engineHsLeft > 0 ){
            engineHsLeft--;
            numEngineHS++;
            if( loadoutPart.getLoadout().getUpgrades().hasDoubleHeatSinks() )
               loadoutPart.removeItem(ItemDB.DHS);
            else
               loadoutPart.removeItem(ItemDB.SHS);
         }
      }
      loadoutPart.removeItem(item);
      xBar.post(new Message(loadoutPart, Type.ItemRemoved));
   }

   /**
    * Adds an item without checks. Will add numEngineHS heat sinks if the item is an engine.
    */
   protected void addItem(){
      if( item instanceof Engine ){
         Engine engine = (Engine)item;
         if( engine.getType() == EngineType.XL ){
            LoadoutPart lt = loadoutPart.getLoadout().getPart(Part.LeftTorso);
            LoadoutPart rt = loadoutPart.getLoadout().getPart(Part.RightTorso);
            lt.addItem(LoadoutPart.ENGINE_INTERNAL);
            rt.addItem(LoadoutPart.ENGINE_INTERNAL);
            xBar.post(new Message(lt, Type.ItemAdded));
            xBar.post(new Message(rt, Type.ItemAdded));
         }
         while( numEngineHS > 0 ){
            numEngineHS--;
            if( loadoutPart.getLoadout().getUpgrades().hasDoubleHeatSinks() )
               loadoutPart.addItem(ItemDB.DHS);
            else
               loadoutPart.addItem(ItemDB.SHS);
         }
      }
      loadoutPart.addItem(item);
      xBar.post(new Message(loadoutPart, Type.ItemAdded));
   }
}

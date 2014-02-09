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
package lisong_mechlab.model.loadout.part;

import lisong_mechlab.model.chassi.Part;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message;
import lisong_mechlab.model.loadout.part.LoadoutPart.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * A helper class for implementing {@link Operation}s that affect items on a {@link LoadoutPart}.
 * 
 * @author Li Song
 */
abstract class ItemOperation extends Operation{
   private int                   numEngineHS = 0;
   protected final LoadoutPart   loadoutPart;
   private transient MessageXBar xBar;

   /**
    * Creates a new {@link ItemOperation}. The deriving classes shall throw if the the operation with the given item
    * would violate the {@link Loadout} or {@link LoadoutPart} invariant.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to send messages to when changes occur.
    * @param aLoadoutPart
    *           The {@link LoadoutPart} that this operation will affect.
    */
   ItemOperation(MessageXBar anXBar, LoadoutPart aLoadoutPart){
      loadoutPart = aLoadoutPart;
      xBar = anXBar;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((loadoutPart == null) ? 0 : loadoutPart.hashCode());
      result = prime * result + numEngineHS;
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if(!(obj instanceof ItemOperation))
         return false;
      
      ItemOperation other = (ItemOperation)obj;
      return loadoutPart == other.loadoutPart;
   }

   /**
    * Removes an item without checks. Will count up the numEngineHS variable to the number of heat sinks removed.
    * 
    * @param anItem
    *           The item to remove.
    */
   protected void removeItem(Item anItem){
      if( anItem instanceof Engine ){
         Engine engine = (Engine)anItem;
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
            loadoutPart.removeItem(loadoutPart.getLoadout().getUpgrades().getHeatSink().getHeatSinkType());
         }
      }
      loadoutPart.removeItem(anItem);
      xBar.post(new Message(loadoutPart, Type.ItemRemoved));
   }

   /**
    * Adds an item without checks. Will add numEngineHS heat sinks if the item is an engine.
    * 
    * @param anItem
    *           The item to add.
    */
   protected void addItem(Item anItem){
      if( anItem instanceof Engine ){
         Engine engine = (Engine)anItem;
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
            loadoutPart.addItem(loadoutPart.getLoadout().getUpgrades().getHeatSink().getHeatSinkType());
         }
      }
      loadoutPart.addItem(anItem);
      xBar.post(new Message(loadoutPart, Type.ItemAdded));
   }
}

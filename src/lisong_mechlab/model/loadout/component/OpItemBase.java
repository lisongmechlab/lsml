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

import lisong_mechlab.model.NotificationMessage;
import lisong_mechlab.model.NotificationMessage.Severity;
import lisong_mechlab.model.chassi.Location;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.item.EngineType;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message;
import lisong_mechlab.model.loadout.component.ConfiguredComponent.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * A helper class for implementing {@link Operation}s that affect items on a {@link ConfiguredComponent}.
 * 
 * @author Li Song
 */
abstract class OpItemBase extends Operation{
   private int                         numEngineHS = 0;
   protected final ConfiguredComponent component;
   private transient MessageXBar       xBar;

   /**
    * Creates a new {@link OpItemBase}. The deriving classes shall throw if the the operation with the given item would
    * violate the {@link Loadout} or {@link ConfiguredComponent} invariant.
    * 
    * @param anXBar
    *           The {@link MessageXBar} to send messages to when changes occur.
    * @param aLoadoutPart
    *           The {@link ConfiguredComponent} that this operation will affect.
    */
   OpItemBase(MessageXBar anXBar, ConfiguredComponent aLoadoutPart){
      component = aLoadoutPart;
      xBar = anXBar;
   }

   @Override
   public int hashCode(){
      final int prime = 31;
      int result = 1;
      result = prime * result + ((component == null) ? 0 : component.hashCode());
      result = prime * result + numEngineHS;
      return result;
   }

   @Override
   public boolean equals(Object obj){
      if( !(obj instanceof OpItemBase) )
         return false;

      OpItemBase other = (OpItemBase)obj;
      return component == other.component;
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
            ConfiguredComponent lt = component.getLoadout().getPart(Location.LeftTorso);
            ConfiguredComponent rt = component.getLoadout().getPart(Location.RightTorso);
            lt.removeItem(ConfiguredComponent.ENGINE_INTERNAL);
            rt.removeItem(ConfiguredComponent.ENGINE_INTERNAL);
            if( xBar != null ){
               xBar.post(new Message(lt, Type.ItemRemoved));
               xBar.post(new Message(rt, Type.ItemRemoved));
            }
         }

         int engineHsLeft = component.getNumEngineHeatsinks();
         while( engineHsLeft > 0 ){
            engineHsLeft--;
            numEngineHS++;
            component.removeItem(component.getLoadout().getUpgrades().getHeatSink().getHeatSinkType());
         }
      }
      component.removeItem(anItem);
      if( xBar != null ){
         xBar.post(new Message(component, Type.ItemRemoved));
      }
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
            ConfiguredComponent lt = component.getLoadout().getPart(Location.LeftTorso);
            ConfiguredComponent rt = component.getLoadout().getPart(Location.RightTorso);
            lt.addItem(ConfiguredComponent.ENGINE_INTERNAL);
            rt.addItem(ConfiguredComponent.ENGINE_INTERNAL);
            if( xBar != null ){
               xBar.post(new Message(lt, Type.ItemAdded));
               xBar.post(new Message(rt, Type.ItemAdded));
            }
         }
         while( numEngineHS > 0 ){
            numEngineHS--;
            component.addItem(component.getLoadout().getUpgrades().getHeatSink().getHeatSinkType());
         }
      }

      Engine engine = component.getLoadout().getEngine();
      if( anItem == ItemDB.CASE && engine != null && engine.getType() == EngineType.XL && xBar != null ){
         xBar.post(new NotificationMessage(Severity.WARNING, component.getLoadout(), "C.A.S.E. together with XL engine has no effect."));
      }
      component.addItem(anItem);
      if( xBar != null ){
         xBar.post(new Message(component, Type.ItemAdded));
      }
   }
}

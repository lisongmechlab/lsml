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
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Internal;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.item.ItemDB;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message;
import lisong_mechlab.model.loadout.component.ConfiguredComponentBase.Message.Type;
import lisong_mechlab.util.MessageXBar;
import lisong_mechlab.util.OperationStack.Operation;

/**
 * A helper class for implementing {@link Operation}s that affect items on a {@link ConfiguredComponentBase}.
 * 
 * @author Li Song
 */
abstract class OpItemBase extends Operation{
   private int                         numEngineHS = 0;
   private final MessageXBar           xBar;
   protected final ConfiguredComponentBase component;
   protected final LoadoutBase<?>   loadout;
   protected final Item                item;

   /**
    * Creates a new {@link OpItemBase}. The deriving classes shall throw if the the operation with the given item would
    * violate the {@link LoadoutStandard} or {@link ConfiguredComponentBase} invariant.
    * 
    * @param aXBar
    *           The {@link MessageXBar} to send messages to when changes occur.
    * @param aLoadout
    *           The {@link LoadoutBase} to operate on.
    * @param aComponent
    *           The {@link ConfiguredComponentBase} that this operation will affect.
    * @param aItem
    *           The {@link Item} to add or remove.
    */
   protected OpItemBase(MessageXBar aXBar, LoadoutBase<?> aLoadout, ConfiguredComponentBase aComponent, Item aItem){
      if( aItem instanceof Internal )
         throw new IllegalArgumentException("Can't add/remove internals to/from a loadout!");

      loadout = aLoadout;
      component = aComponent;
      xBar = aXBar;
      item = aItem;
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
   public boolean equals(Object aObject){
      if( !(aObject instanceof OpItemBase) )
         return false;

      OpItemBase other = (OpItemBase)aObject;
      return component == other.component;
   }

   /**
    * Removes an item without checks. Will count up the numEngineHS variable to the number of heat sinks removed.
    * 
    * @param aItem
    *           The item to remove.
    */
   protected void removeItem(Item aItem){
      if( !component.canRemoveItem(aItem) )
         throw new IllegalArgumentException("Can not remove item: " + aItem + " from " + component);

      if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;
         if( engine.getType() == EngineType.XL ){
            ConfiguredComponentBase lt = loadout.getComponent(Location.LeftTorso);
            ConfiguredComponentBase rt = loadout.getComponent(Location.RightTorso);

            Internal xlSide = engine.isClan() ? ConfiguredComponentBase.ENGINE_INTERNAL_CLAN : ConfiguredComponentBase.ENGINE_INTERNAL;
            lt.removeItem(xlSide);
            rt.removeItem(xlSide);
            if( xBar != null ){
               xBar.post(new Message(lt, Type.ItemRemoved));
               xBar.post(new Message(rt, Type.ItemRemoved));
            }
         }

         int engineHsLeft = component.getEngineHeatsinks();
         HeatSink heatSinkType = loadout.getUpgrades().getHeatSink().getHeatSinkType();
         while( engineHsLeft > 0 ){
            engineHsLeft--;
            numEngineHS++;
            component.removeItem(heatSinkType);
         }
      }
      component.removeItem(aItem);
      if( xBar != null ){
         xBar.post(new Message(component, Type.ItemRemoved));
      }
   }

   /**
    * Adds an item without checks. Will add numEngineHS heat sinks if the item is an engine.
    * 
    * @param aItem
    *           The item to add.
    */
   protected void addItem(Item aItem){
      if( aItem instanceof Engine ){
         Engine engine = (Engine)aItem;
         if( engine.getType() == EngineType.XL ){
            ConfiguredComponentBase lt = loadout.getComponent(Location.LeftTorso);
            ConfiguredComponentBase rt = loadout.getComponent(Location.RightTorso);

            Internal xlSide = engine.isClan() ? ConfiguredComponentBase.ENGINE_INTERNAL_CLAN : ConfiguredComponentBase.ENGINE_INTERNAL;
            lt.addItem(xlSide);
            rt.addItem(xlSide);
            if( xBar != null ){
               xBar.post(new Message(lt, Type.ItemAdded));
               xBar.post(new Message(rt, Type.ItemAdded));
            }
         }
         while( numEngineHS > 0 ){
            numEngineHS--;
            component.addItem(loadout.getUpgrades().getHeatSink().getHeatSinkType());
         }
      }

      Engine engine = loadout.getEngine();
      if( aItem == ItemDB.CASE && engine != null && engine.getType() == EngineType.XL && xBar != null ){
         xBar.post(new NotificationMessage(Severity.WARNING, loadout, "C.A.S.E. together with XL engine has no effect."));
      }
      component.addItem(aItem);
      if( xBar != null ){
         xBar.post(new Message(component, Type.ItemAdded));
      }
   }
}

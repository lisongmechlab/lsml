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
package lisong_mechlab.model.loadout.component;

import java.util.Collection;

import lisong_mechlab.model.chassi.ComponentStandard;
import lisong_mechlab.model.chassi.HardPoint;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.model.item.HeatSink;
import lisong_mechlab.model.item.Item;
import lisong_mechlab.model.loadout.LoadoutStandard;

/**
 * This class implements {@link ConfiguredComponentBase} for {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class ConfiguredComponentStandard extends ConfiguredComponentBase{

   public ConfiguredComponentStandard(ComponentStandard aInternalPart, boolean aAutoArmor){
      super(aInternalPart, aAutoArmor);
   }
   
   public ConfiguredComponentStandard(ConfiguredComponentStandard aComponent){
      super(aComponent);
   }

   @Override
   public boolean canAddItem(Item aItem){
      if( aItem instanceof HeatSink && getEngineHeatsinks() < getEngineHeatsinksMax() ){
         return true;
      }
      return super.canAddItem(aItem);
   }

   @Override
   public int getHardPointCount(HardPointType aHardpointType){
      return getInternalComponent().getHardPointCount(aHardpointType);
   }

   @Override
   public Collection<HardPoint> getHardPoints(){
      return getInternalComponent().getHardPoints();
   }

   @Override
   public Collection<Item> getItemsFixed(){
      return getInternalComponent().getFixedItems();
   }
   
   @Override
   public ComponentStandard getInternalComponent(){
      return (ComponentStandard)super.getInternalComponent();
   }

   @Override
   public boolean hasMissileBayDoors(){
      return getInternalComponent().hasMissileBayDoors();
   }
}

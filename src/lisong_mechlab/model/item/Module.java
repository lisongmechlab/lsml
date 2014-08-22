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
package lisong_mechlab.model.item;

import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;

/**
 * A refinement of {@link Item} for modules.
 * 
 * @author Emily Björk
 */
public class Module extends Item{

   public Module(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, HardPointType aHardpointType, int aHP,
                 Faction aFaction){
      super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardpointType, aHP, aFaction);
   }

   public Module(String aNameTag, String aDesc, int aSlots, int aHealth, Faction aFaction){
      super(aNameTag, aDesc, aSlots, aHealth, aFaction);
   }

   public Module(ItemStatsModule aModule){
      super(aModule, HardPointType.NONE, aModule.ModuleStats.slots, aModule.ModuleStats.tons, aModule.ModuleStats.health);
   }

   public Module(ItemStatsModule aModule, HardPointType hardpoint){
      super(aModule, hardpoint, aModule.ModuleStats.slots, aModule.ModuleStats.tons, aModule.ModuleStats.health);
   }
}

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

import lisong_mechlab.model.chassi.HardpointType;
import lisong_mechlab.model.loadout.Loadout;
import lisong_mechlab.model.loadout.Upgrades;
import lisong_mechlab.model.mwo_parsing.helpers.ItemStatsModule;

public class Engine extends HeatSource{
   public final static double ENGINE_HEAT_FULL_THROTTLE = 0.2;
   public final static double ENGINE_HEAT_66_THROTTLE   = 0.1;

   protected final int        rating;
   protected final EngineType type;
   final private int          internalHs;
   final private int          heatsinkslots;

   public Engine(ItemStatsModule aStatsModule){
      super(aStatsModule, HardpointType.NONE, 6, aStatsModule.EngineStats.weight, ENGINE_HEAT_FULL_THROTTLE, aStatsModule.EngineStats.health);
      int hs = aStatsModule.EngineStats.heatsinks;
      internalHs = Math.min(10, hs);
      heatsinkslots = hs - internalHs;
      type = (aStatsModule.EngineStats.slots == 12) ? (EngineType.XL) : (EngineType.STD);
      rating = aStatsModule.EngineStats.rating;
   }

   public EngineType getType(){
      return type;
   }

   public int getRating(){
      return rating;
   }

   public int getNumInternalHeatsinks(){
      return internalHs;
   }

   public int getNumHeatsinkSlots(){
      return heatsinkslots;
   }

   @Override
   public String getShortName(Upgrades anUpgrades){
      String name = getName(anUpgrades);
      name = name.replace("ENGINE ", "");
      return name;
   }

   @Override
   public boolean isEquippableOn(Loadout aLoadout){
      return aLoadout.getChassi().getEngineMax() >= rating && aLoadout.getChassi().getEngineMin() <= rating;
   }
}

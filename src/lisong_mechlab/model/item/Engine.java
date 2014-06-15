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

import lisong_mechlab.model.Faction;
import lisong_mechlab.model.chassi.HardPointType;
import lisong_mechlab.mwo_data.helpers.ItemStatsModule;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class Engine extends HeatSource{
   public final static double ENGINE_HEAT_FULL_THROTTLE = 0.2;
   public final static double ENGINE_HEAT_66_THROTTLE   = 0.1;

   @XStreamAsAttribute
   protected final int        rating;
   @XStreamAsAttribute
   protected final EngineType type;
   @XStreamAsAttribute
   final private int          internalHs;
   @XStreamAsAttribute
   final private int          heatSinkSlots;

   public Engine(String aName, String aDesc, String aMwoName, int aMwoId, int aSlots, double aTons, HardPointType aHardPointType, int aHP,
                 Faction aFaction, int aRating, EngineType aType, int aInternalHS, int aHSSlots){
      super(aName, aDesc, aMwoName, aMwoId, aSlots, aTons, aHardPointType, aHP, aFaction, ENGINE_HEAT_FULL_THROTTLE);
      rating = aRating;
      type = aType;
      internalHs = aInternalHS;
      heatSinkSlots = aHSSlots;
   }

   public Engine(ItemStatsModule aStatsModule){
      super(aStatsModule, HardPointType.NONE, 6, aStatsModule.EngineStats.weight, ENGINE_HEAT_FULL_THROTTLE, aStatsModule.EngineStats.health);
      int hs = aStatsModule.EngineStats.heatsinks;
      internalHs = Math.min(10, hs);
      heatSinkSlots = hs - internalHs;
      type = (getName().toLowerCase().contains("xl")) ? (EngineType.XL) : (EngineType.STD);
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
      return heatSinkSlots;
   }

   @Override
   public String getShortName(){
      String name = getName();
      name = name.replace("ENGINE ", "");
      return name;
   }
}

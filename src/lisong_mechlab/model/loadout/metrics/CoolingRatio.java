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
package lisong_mechlab.model.loadout.metrics;

import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the effectiveness of the {@link Loadout}'s cooling. A ratio of 0.0 means no heat is
 * dissipated. A ratio of 1.0 means all generated heat is dissipated. A ratio of > 1.0 means the mech has too much
 * cooling.
 * 
 * @author Emily Björk
 */
public class CoolingRatio implements Metric{
   private final HeatDissipation dissipation;
   private final HeatGeneration  generation;

   public CoolingRatio(final HeatDissipation aDissipation, final HeatGeneration aHeatGeneration){
      dissipation = aDissipation;
      generation = aHeatGeneration;
   }

   @Override
   public double calculate(){
      final double generatedHeat = generation.calculate();
      if( generatedHeat <= 0 ){
         return 1.0;
      }
      return dissipation.calculate() / generatedHeat;
   }
}

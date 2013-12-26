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

/**
 * This {@link Metric} calculates the number of seconds a mech can shoot all weapons before it over heats.
 * 
 * @author Emily Björk
 */
public class TimeToOverHeat implements Metric{
   private final HeatCapacity    capacity;
   private final HeatDissipation dissipation;
   private final HeatGeneration  generation;

   public TimeToOverHeat(final HeatCapacity aCapacity, final HeatDissipation aDissipation, final HeatGeneration aHeatGeneration){
      capacity = aCapacity;
      dissipation = aDissipation;
      generation = aHeatGeneration;
   }

   @Override
   public double calculate(){
      final double heatDifferential = generation.calculate() - dissipation.calculate();
      final double heatCapacity = capacity.calculate();
      if( heatDifferential <= 0 || heatCapacity / heatDifferential >= 15 * 60 ){ // 15min = infinity in MWO
         return Double.POSITIVE_INFINITY;
      }
      return heatCapacity / heatDifferential;
   }

}

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
import lisong_mechlab.util.WeaponRanges;

/**
 * This class is a refinement of {@link Metric} to include a notion that the metric has a dependency on range to target.
 * 
 * @author Emily Björk
 */
public abstract class RangeMetric implements Metric{
   protected double        range      = -1;
   protected boolean       fixedRange = false;
   protected final Loadout loadout;

   public RangeMetric(Loadout aLoadout){
      loadout = aLoadout;
   }

   /**
    * Changes the range for which the damage is calculated. A value of 0 or less will result in the range with maximum
    * damage always being selected.
    * 
    * @param aRange
    *           The range to calculate the damage at.
    */
   public void changeRange(double aRange){
      fixedRange = aRange > 0;
      range = aRange;
   }

   /**
    * @return The range that the result of the last call to calculate() is for.
    */
   public double getRange(){
      return range;
   }

   @Override
   public double calculate(){
      if( fixedRange )
         return calculate(range);

      double max = Double.NEGATIVE_INFINITY;
      for(Double r : WeaponRanges.getRanges(loadout)){
         double dps = calculate(r);
         if( dps >= max ){
            max = dps;
            range = r;
         }
      }
      return max;
   }

   public abstract double calculate(double aRange);
}

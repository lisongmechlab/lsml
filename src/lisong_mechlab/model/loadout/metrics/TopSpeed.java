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

import lisong_mechlab.model.chassi.Chassi;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the maximal speed the loadout can have, taking speed tweak into account.
 * 
 * @author Emily Björk
 */
public class TopSpeed implements Metric{
   private final Loadout loadout;

   public TopSpeed(final Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      Engine engine = loadout.getEngine();
      if( null == engine )
         return 0;
      return calculate(engine.getRating(), loadout.getChassi(), loadout.getEfficiencies().getSpeedModifier());
   }

   /**
    * Performs the actual calculation. This has been extracted because there are situations where the maximal speed is
    * needed without having a {@link Loadout} at hand.
    * 
    * @param aRating
    *           The engine rating.
    * @param aChassi
    *           The chassi the speed is for (determines speed factor).
    * @param aModifier
    *           A modifier to use, 1.0 for normal and 1.1 for speed tweak.
    * @return The speed in [km/h].
    */
   static public double calculate(final int aRating, final Chassi aChassi, final double aModifier){
      return aChassi.getSpeedFactor() * aRating / aChassi.getMassMax() * aModifier;
   }
}

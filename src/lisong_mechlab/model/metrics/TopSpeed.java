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
package lisong_mechlab.model.metrics;

import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.LoadoutBase;
import lisong_mechlab.model.loadout.LoadoutStandard;

/**
 * This {@link Metric} calculates the maximal speed the loadout can have, taking speed tweak into account.
 * 
 * @author Li Song
 */
public class TopSpeed implements Metric{
   private final LoadoutBase<?> loadout;

   public TopSpeed(final LoadoutBase<?> aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      Engine engine = loadout.getEngine();
      if( null == engine )
         return 0;
      return calculate(engine.getRating(), loadout.getMovementProfile(), loadout.getChassis().getMassMax(), loadout.getEfficiencies()
                                                                                                                   .getSpeedModifier());
   }

   /**
    * Performs the actual calculation. This has been extracted because there are situations where the maximal speed is
    * needed without having a {@link LoadoutStandard} at hand.
    * 
    * @param aRating
    *           The engine rating.
    * @param aMovementProfile
    *           The movement profile to calculate the speed with.
    * @param aMaxMass
    *           The mass of the chassis to calculate for.
    * @param aModifier
    *           A modifier to use, 1.0 for normal and 1.1 for speed tweak.
    * @return The speed in [km/h].
    */
   static public double calculate(final int aRating, final MovementProfile aMovementProfile, final double aMaxMass, final double aModifier){
      return aMovementProfile.getMaxMovementSpeed() * aRating / aMaxMass * aModifier;
   }
}

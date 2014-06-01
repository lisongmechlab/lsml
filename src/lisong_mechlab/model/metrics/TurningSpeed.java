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

package lisong_mechlab.model.metrics;

import lisong_mechlab.model.chassi.MovementProfile;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.LoadoutBase;

/**
 * This {@link Metric} calculates how fast a mech will turn (degrees per second).
 * 
 * @author Emily Björk
 */
public class TurningSpeed implements Metric{

   private final LoadoutBase<?> loadout;

   public TurningSpeed(LoadoutBase<?> aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      Engine engine = loadout.getEngine();
      if( engine == null )
         return 0.0;
      return getTurnRateAtThrottle(0.0, engine.getRating()) * loadout.getEfficiencies().getTurnSpeedModifier();
   }
   
   public double getTurnRateAtThrottle(double aThrottle, int aEngineRating){
      final double k = (double)aEngineRating / loadout.getChassis().getMassMax() * 180.0 / Math.PI;
      
      MovementProfile mp = loadout.getMovementProfile();
      
      if( aThrottle <= mp.getTurnLerpLowSpeed() ){
         return k * mp.getTurnLerpLowRate();
      }
      else if( aThrottle <= mp.getTurnLerpMidSpeed() ){
         final double f = (aThrottle - mp.getTurnLerpLowSpeed()) / (mp.getTurnLerpMidSpeed() - mp.getTurnLerpLowSpeed());
         return k * (mp.getTurnLerpLowRate() + (mp.getTurnLerpMidRate() - mp.getTurnLerpLowRate()) * f);
      }
      else if( aThrottle < mp.getTurnLerpHighSpeed() ){
         final double f = (aThrottle - mp.getTurnLerpMidSpeed()) / (mp.getTurnLerpHighSpeed() - mp.getTurnLerpMidSpeed());
         return k * (mp.getTurnLerpMidRate() + (mp.getTurnLerpHighRate() - mp.getTurnLerpMidRate()) * f);
      }
      else{
         return k * mp.getTurnLerpHighRate();
      }
   }
}

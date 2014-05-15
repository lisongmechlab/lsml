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

import lisong_mechlab.model.chassi.ChassisBase;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates how fast a mech will turn (degrees per second).
 * 
 * @author Li Song
 */
public class TurningSpeed implements Metric{

   private final Loadout loadout;

   public TurningSpeed(Loadout aLoadout){
      loadout = aLoadout;
   }

   @Override
   public double calculate(){
      ChassisBase<?> chassi = loadout.getChassis();
      Engine engine = loadout.getEngine();
      if( engine == null )
         return 0.0;
      return loadout.getEfficiencies().getTurnSpeedModifier() * 360.0 / 31.4 * loadout.getEngine().getRating() / chassi.getMassMax();
   }
}

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

import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.loadout.Loadout;

/**
 * This {@link Metric} calculates the heat dissipation for a {@link Loadout}.
 * 
 * @author Li Song
 */
public class HeatDissipation implements Metric{
   private final Loadout loadout;
   private Environment   environment;

   public HeatDissipation(final Loadout aLoadout, final Environment anEnvironment){
      loadout = aLoadout;
      environment = anEnvironment;
   }

   @Override
   public double calculate(){
      double ans = 0;
      int enginehs = 0;
      if( loadout.getEngine() != null ){
         enginehs = loadout.getEngine().getNumInternalHeatsinks();
      }

      // Engine internal HS count as true doubles
      ans += enginehs * (loadout.getUpgrades().getHeatSink().isDouble() ? 0.2 : 0.1);
      ans += (loadout.getHeatsinksCount() - enginehs) * loadout.getUpgrades().getHeatSink().getHeatSinkType().getDissipation();
      ans *= loadout.getEfficiencies().getHeatDissipationModifier();
      if( environment != null ){
         ans -= environment.getHeat();
      }
      return ans;
   }

   public void changeEnvironment(Environment anEnvironment){
      environment = anEnvironment;
   }
}

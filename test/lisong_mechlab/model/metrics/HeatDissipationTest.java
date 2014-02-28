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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.environment.Environment;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;
import lisong_mechlab.model.upgrades.HeatsinkUpgrade;
import lisong_mechlab.model.upgrades.UpgradeDB;

import org.junit.Test;

/**
 * Test suite for {@link HeatDissipation}.
 * 
 * @author Li Song
 */
public class HeatDissipationTest{
   private final MockLoadoutContainer mlc = new MockLoadoutContainer();
   private HeatDissipation            cut = new HeatDissipation(mlc.loadout, null);

   /**
    * The heat dissipation of a 'mech is dependent on the heat sink types. > For single heat sinks it is simply the
    * number of heat sinks multiplied by any modifier from the efficiencies. > For double heat sinks it each engine
    * internal heat sink counts as 0.2 and any other heat sinks count as 0.14.
    */
   @Test
   public void testCalculate(){
      HeatsinkUpgrade hs = UpgradeDB.DOUBLE_HEATSINKS;
      final double dissipationFactor = 1.3;
      final int externalHs = 5;
      final int internalHs = 9;
      final double internalHsDissipation = 0.2;
      final double externalHsDissipation = hs.getHeatSinkType().getDissipation();

      Engine engine = mock(Engine.class);
      when(engine.getNumInternalHeatsinks()).thenReturn(internalHs);
      when(mlc.efficiencies.getHeatDissipationModifier()).thenReturn(dissipationFactor);
      when(mlc.upgrades.getHeatSink()).thenReturn(hs);
      when(mlc.loadout.getEngine()).thenReturn(engine);
      when(mlc.loadout.getHeatsinksCount()).thenReturn(externalHs + internalHs);

      double expectedDissipation = (internalHs * internalHsDissipation + externalHs * externalHsDissipation) * dissipationFactor;
      assertEquals(expectedDissipation, cut.calculate(), Math.ulp(expectedDissipation) * 4);
   }

   @Test
   public void testCalculateEnvironment(){
      Environment environment = mock(Environment.class);
      final double environmentHeat = 0.3;

      when(mlc.upgrades.getHeatSink()).thenReturn(UpgradeDB.STANDARD_HEATSINKS);
      when(mlc.efficiencies.getHeatDissipationModifier()).thenReturn(1.0);
      when(mlc.loadout.getHeatsinksCount()).thenReturn(10);
      when(environment.getHeat()).thenReturn(environmentHeat);

      cut.changeEnvironment(environment);

      double expectedDissipation = 1.0 - environmentHeat;
      assertEquals(expectedDissipation, cut.calculate(), Math.ulp(expectedDissipation) * 4);
   }
}

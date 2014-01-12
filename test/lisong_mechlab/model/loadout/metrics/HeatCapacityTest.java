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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lisong_mechlab.model.helpers.MockLoadoutContainer;
import lisong_mechlab.model.item.Engine;

import org.junit.Test;

/**
 * Test suite for {@link HeatCapacity}
 * 
 * @author Emily Björk
 */
public class HeatCapacityTest{
   final private MockLoadoutContainer mlc = new MockLoadoutContainer();
   final private HeatCapacity         cut = new HeatCapacity(mlc.loadout);

   /**
    * Each 'mech has a base heat capacity of 30 heat. Each single heat sink adds 1 capacity. Each double heat sink adds
    * 1.4 capacity. Except for the ones counted as engine internal heat sinks which count as 2.0 (those in engine slots
    * count as 1.4 still). These values are also affected by the efficiency modifier.
    */
   @Test
   public void testCalculate(){
      final boolean doubleHs = true;
      final double capacityFactor = 1.3;
      final int numExternalHs = 5;
      final int numInternalHs = 9;
      final double basecapacity = 30;
      final double internalHsCapacity = 2.0;
      final double externalHsCapacity = 1.4;

      Engine engine = mock(Engine.class);
      when(engine.getNumInternalHeatsinks()).thenReturn(numInternalHs);
      when(mlc.efficiencies.getHeatCapacityModifier()).thenReturn(capacityFactor);
      when(mlc.upgrades.hasDoubleHeatSinks()).thenReturn(doubleHs);
      when(mlc.loadout.getEngine()).thenReturn(engine);
      when(mlc.loadout.getHeatsinksCount()).thenReturn(numExternalHs + numInternalHs);

      double expectedCapacity = (basecapacity + numInternalHs * internalHsCapacity + numExternalHs * externalHsCapacity) * capacityFactor;
      assertEquals(expectedCapacity, cut.calculate(), 0.0);
   }

}

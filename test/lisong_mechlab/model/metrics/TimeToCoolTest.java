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

import static org.junit.Assert.*;
import lisong_mechlab.model.item.Engine;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test suite for {@link TimeToCool} {@link Metric}.
 * 
 * @author Emily Björk
 */
public class TimeToCoolTest{

   @Test
   public void testCalculate(){
      HeatDissipation heatDissipation = Mockito.mock(HeatDissipation.class);
      HeatCapacity heatCapacity = Mockito.mock(HeatCapacity.class);

      double capacity = 60;
      double dissipation = 2.4;
      Mockito.when(heatDissipation.calculate()).thenReturn(dissipation);
      Mockito.when(heatCapacity.calculate()).thenReturn(capacity);

      TimeToCool cut = new TimeToCool(heatCapacity, heatDissipation);

      assertEquals(capacity / (dissipation - Engine.ENGINE_HEAT_FULL_THROTTLE), cut.calculate(), 0.0);
   }
}

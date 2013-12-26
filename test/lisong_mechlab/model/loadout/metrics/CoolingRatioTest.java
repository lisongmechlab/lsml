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
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test suite for {@link CoolingRatio}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class CoolingRatioTest{
   @Mock
   private HeatDissipation dissipation;
   @Mock
   private HeatGeneration  heatGeneration;
   @InjectMocks
   private CoolingRatio    cut;

   @Test
   public void testCalculate() throws Exception{
      double heat = 10;
      double cooling = 5;
      when(heatGeneration.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      assertEquals(cooling / heat, cut.calculate(), 0);
   }

   @Test
   public void testCalculate_noHeat() throws Exception{
      double heat = 0;
      double cooling = 5;

      when(heatGeneration.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      assertEquals(1.0, cut.calculate(), 0);
   }
}

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
 * A test suite for {@link TimeToOverHeat}.
 * 
 * @author Emily Björk
 */
@RunWith(MockitoJUnitRunner.class)
public class TimeToOverHeatTest{
   @Mock
   private HeatCapacity    capacity;
   @Mock
   private HeatDissipation dissipation;
   @Mock
   private HeatGeneration  generation;

   @InjectMocks
   private TimeToOverHeat  cut;

   /**
    * 15 minutes and above is rounded up to infinity. Matches are only 15 minutes :)
    */
   @Test
   public void testGetTimeToOverHeat_15minutes(){
      final double heat = 10;
      final double cooling = heat - 1;
      final double ccapacity = 15 * 60;
      when(generation.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      when(capacity.calculate()).thenReturn(ccapacity);

      assertEquals(Double.POSITIVE_INFINITY, cut.calculate(), 0);
   }

   /**
    * If a mech generates 10 heat per second and can dissipate 5, then the mech will over heat after the differential
    * has filled the heat capacity: capacity / (generation - dissipation)
    */
   @Test
   public void testGetTimeToOverHeat(){
      double heat = 10;
      double cooling = 5;
      double ccapacity = 60;
      when(generation.calculate()).thenReturn(heat);
      when(dissipation.calculate()).thenReturn(cooling);
      when(capacity.calculate()).thenReturn(ccapacity);
      assertEquals(ccapacity / (heat - cooling), cut.calculate(), 0);
   }
}

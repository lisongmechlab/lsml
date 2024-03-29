/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.metrics;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test suite for {@link CoolingRatio}.
 *
 * @author Li Song
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class CoolingRatioTest {
  @InjectMocks private CoolingRatio cut;
  @Mock private HeatDissipation dissipation;
  @Mock private HeatGeneration heatGeneration;

  @Test
  public void testCalculate() {
    final double heat = 10;
    final double cooling = 5;
    when(heatGeneration.calculate()).thenReturn(heat);
    when(dissipation.calculate()).thenReturn(cooling);
    assertEquals(cooling / heat, cut.calculate(), 0);
  }

  @Test
  public void testCalculate_noHeat() {
    final double heat = 0;
    final double cooling = 5;

    when(heatGeneration.calculate()).thenReturn(heat);
    when(dissipation.calculate()).thenReturn(cooling);
    assertEquals(1.0, cut.calculate(), 0);
  }
}

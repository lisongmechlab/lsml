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
package org.lisoft.lsml.model.metrics.helpers;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * Test suite for {@link TruncatedSignal}
 *
 * @author Li Song
 */
public class TruncatedSignalTest extends TestCase {

  @Test
  public void testTruncateAtOnePeriod() {
    TruncatedSignal cut = new TruncatedSignal(new IntegratedImpulseTrain(4, 5), 4);
    assertEquals(5.0, cut.integrateFromZeroTo(0.0), 0.0);
    assertEquals(10.0, cut.integrateFromZeroTo(4.0), 0.0);
  }

  @Test
  public void testTruncateAtZero() {
    TruncatedSignal cut = new TruncatedSignal(new IntegratedImpulseTrain(4, 5), 0);
    assertEquals(5.0, cut.integrateFromZeroTo(0.0), 0.0);
  }

  @Test
  public void testTruncateJustBeforeOnePeriod() {
    final double p = 4.0;
    final double t = p - 1E-9;
    TruncatedSignal cut = new TruncatedSignal(new IntegratedImpulseTrain(p, 5), t);
    assertEquals(5.0, cut.integrateFromZeroTo(0.0), 0.0);
    assertEquals(5.0, cut.integrateFromZeroTo(t), 0.0);
    assertEquals(5.0, cut.integrateFromZeroTo(p), 0.0);
  }
}

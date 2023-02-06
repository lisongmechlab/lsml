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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Test suite for IntegratedImpulseTrain */
public class IntegratedImpulseTrainTest {

  @Test
  public void testAtZero() {
    IntegratedImpulseTrain cut = new IntegratedImpulseTrain(10, 5);
    assertEquals(5, cut.integrateFromZeroTo(0), 0.0);
  }

  @Test
  public void testInfinitePeriod() {
    IntegratedImpulseTrain cut = new IntegratedImpulseTrain(Double.POSITIVE_INFINITY, 5);
    assertEquals(5, cut.integrateFromZeroTo(0), 0.0);
    assertEquals(5, cut.integrateFromZeroTo(Double.MAX_VALUE), 0.0);
  }

  @Test
  public void testJustBeforeOnePeriod() {
    IntegratedImpulseTrain cut = new IntegratedImpulseTrain(10, 5);
    assertEquals(5, cut.integrateFromZeroTo(Math.nextDown(10.0)), 0.0);
  }

  @Test
  public void testMidPeriod() {
    IntegratedImpulseTrain cut = new IntegratedImpulseTrain(10, 5);
    assertEquals(5, cut.integrateFromZeroTo(5), 0.0);
  }

  @Test
  public void testOnePeriod() {
    IntegratedImpulseTrain cut = new IntegratedImpulseTrain(10, 5);
    assertEquals(10, cut.integrateFromZeroTo(10), 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroPeriod() {
    new IntegratedImpulseTrain(0, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePeriod() {
    new IntegratedImpulseTrain(-0.1, 5);
  }
}

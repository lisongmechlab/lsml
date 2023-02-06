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

/**
 * This class implements the integral of a impulse train (also known as a Dirac comb).
 *
 * <p>Note that the first impulse occurs at time t=0. Thus after k*period seconds, there will have
 * been k+1 impulses!
 *
 * @author Li Song
 */
public class IntegratedImpulseTrain implements IntegratedSignal {
  private final double amplitude;
  private final double period;

  public IntegratedImpulseTrain(double aPeriod, double aAmplitude) {
    if (aPeriod <= 0.0) {
      throw new IllegalArgumentException(
          "Cannot create impulse train with period of zero or less!");
    }
    period = aPeriod;
    amplitude = aAmplitude;
  }

  @Override
  public double integrateFromZeroTo(double aTime) {
    final double impulses = Math.floor(aTime / period + 1);
    return impulses * amplitude;
  }
}

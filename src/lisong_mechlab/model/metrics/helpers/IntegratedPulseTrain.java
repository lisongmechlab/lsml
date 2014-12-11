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
package lisong_mechlab.model.metrics.helpers;

/**
 * This class implements the integral of a pulse wave (or pulse train).
 * 
 * @author Li Song
 */
public class IntegratedPulseTrain implements IntegratedSignal {
    private final double period;
    private final double pulseWidth;
    private final double amplitude;

    public IntegratedPulseTrain(double aPeriod, double aPulseWidth, double aAmplitude) {
        period = aPeriod;
        pulseWidth = aPulseWidth;
        amplitude = aAmplitude;
    }

    @Override
    public double integrateFromZeroTo(double aTime) {
        int periods = (int) (aTime / period);
        double sum = amplitude == 0.0 ? 0 : periods * pulseWidth * amplitude; // Whole periods this far
        double partialTime = Math.min(aTime - periods * period, pulseWidth);
        return sum + partialTime * amplitude;
    }
}

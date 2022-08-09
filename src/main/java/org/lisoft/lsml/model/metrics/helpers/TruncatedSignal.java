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
package org.lisoft.lsml.model.metrics.helpers;

/**
 * This class takes an existing signal and truncates it at a specific time.
 * I.e. the integral stops changing.
 *
 * @author Li Song
 */
public class TruncatedSignal implements IntegratedSignal {
    private final IntegratedSignal signal;
    private final double truncationTime;

    public TruncatedSignal(IntegratedSignal aSignal, double aTruncationTime) {
        signal = aSignal;
        truncationTime = aTruncationTime;
    }

    @Override
    public double integrateFromZeroTo(double aTime) {
        if (aTime >= truncationTime) {
            return signal.integrateFromZeroTo(truncationTime);
        }
        return signal.integrateFromZeroTo(aTime);
    }
}

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
 * This class implements an integrated constant (which is a linear function when integrated).
 *
 * @author Li Song
 */
public class IntegratedConstantSignal implements IntegratedSignal {
    private final double constant;

    public IntegratedConstantSignal(double aConstant) {this.constant = aConstant;}

    @Override
    public double integrateFromZeroTo(double aTime) {
        return constant * aTime;
    }
}

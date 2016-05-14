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
package org.lisoft.lsml.model.metrics;

/**
 * Extension of the {@link Metric} interface to support range dependency.
 *
 * @author Li Song
 */
public interface RangeMetric extends Metric {

    double calculate(double aRange);

    /**
     * @return The range which {@link #calculate()} used for it's result. If the range has been set to a value larger
     *         than or equal to 0.0 then that value is returned, otherwise the metric calculated using the optimal range
     *         and that range is returned.
     */
    double getCurrentRange();

    /**
     * @return The range that the result of the last call to calculate() is for.
     */
    double getRange();

    /**
     * Changes the range for which the damage is calculated. A value of 0 or less will result in the range with maximum
     * damage always being selected.
     *
     * @param aRange
     *            The range to calculate the damage at.
     */
    void setRange(double aRange);

}
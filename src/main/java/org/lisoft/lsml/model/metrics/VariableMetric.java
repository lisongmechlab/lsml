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

import org.lisoft.lsml.model.loadout.Loadout;

import java.util.List;

/**
 * A base class for all metrics that are time dependent. A metric is a derived quantity that is calculated from a
 * {@link Loadout}.
 *
 * @author Li Song
 */
public interface VariableMetric {

    /**
     * Calculates the value of the metric. May employ caching but the caching must be transparent.
     *
     * @param aValue The value to calculate the metric for.
     * @return The value of the metric.
     */
    double calculate(double aValue);

    /**
     * @return The human readable name of the input value.
     */
    String getArgumentName();

    /**
     * @return A {@link List} of values which if evaluated are usable for plotting the metric.
     */
    List<Double> getArgumentValues();

    /**
     * @return The human readable name of the calculated metric.
     */
    String getMetricName();
}

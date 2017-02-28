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
package org.lisoft.lsml.model.metrics;

import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.WeaponRanges;

/**
 * This class is a refinement of {@link Metric} to include a notion that the metric has a dependency on range to target
 * and time.
 *
 * @author Emily Björk
 */
public abstract class RangeTimeMetric implements RangeMetric {
    private double range = -1;
    protected double time = 0;
    private double lastRange = -1;
    private boolean fixedRange = false;
    protected final Loadout loadout;

    public RangeTimeMetric(Loadout aLoadout) {
        loadout = aLoadout;
    }

    /**
     * Will handle calculation of the metric with the current values for range and time. If range is set to below 0, the
     * metric will be evaluated in all range points returned by {@link WeaponRanges#getRanges(Loadout)} and the maximum
     * value (with ties breaking to larger ranges) be returned.
     *
     * @see org.lisoft.lsml.model.metrics.Metric#calculate()
     */
    @Override
    public final double calculate() {
        if (fixedRange) {
            lastRange = range;
            return calculate(range, time);
        }

        double max = Double.NEGATIVE_INFINITY;
        for (final Double r : WeaponRanges.getRanges(loadout)) {
            final double value = calculate(r, time);
            if (value >= max) {
                max = value;
                lastRange = r;
            }
        }
        return max;
    }

    /**
     * The {@link #calculate()} method will defer to this method for performing the actual calculations. This method
     * must not use the time returned by {@link #getTime()} or range returned by {@link #getUserRange()}; Doing so will
     * result in erroneous results.
     *
     * @param aRange
     *            The range to calculate for.
     * @param aTime
     *            The time to calculate for.
     * @return The value of the metric for the above parameters.
     */
    public abstract double calculate(double aRange, double aTime);

    /**
     * Changes the time point for the metric. The start of time is defined as time = 0.
     *
     * @param aTime
     *            The new time to set.
     */
    public void changeTime(double aTime) {
        time = aTime;
    }

    @Override
    public double getDisplayRange() {
        return lastRange;
    }

    /**
     * @return The range that the result of the last call to calculate() is for.
     */
    @Override
    public double getUserRange() {
        return range;
    }

    /**
     * @return The currently selected time for this metric.
     */
    public double getTime() {
        return time;
    }

    /**
     * Changes the range for which the metric is calculated. A value of 0 or less will result in the "optimal" range (in
     * a relevant sense) being selected.
     *
     * @param aRange
     *            The range to calculate the metric at.
     */
    @Override
    public void setUserRange(double aRange) {
        fixedRange = aRange > 0;
        range = aRange;
    }
}

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
import org.lisoft.lsml.util.WeaponRanges;

/**
 * This class is a refinement of {@link Metric} to include a notion that the metric has a dependency on range to target.
 *
 * @author Li Song
 */
public abstract class AbstractRangeMetric implements RangeMetric {
    protected double range = -1;
    protected boolean fixedRange = false;
    protected double lastRange = -1;
    protected final Loadout loadout;

    public AbstractRangeMetric(Loadout aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public double calculate() {
        if (fixedRange) {
            lastRange = range;
            return calculate(range);
        }

        double max = Double.NEGATIVE_INFINITY;
        for (final Double r : WeaponRanges.getRanges(loadout)) {
            if (r < 0) {
                continue;
            }
            final double value = calculate(r);
            if (value >= max) {
                max = value;
                lastRange = r;
            }
        }
        return max;
    }

    @Override
    public abstract double calculate(double aRange);

    @Override
    public double getCurrentRange() {
        return lastRange;
    }

    @Override
    public double getRange() {
        return range;
    }

    @Override
    public void setRange(double aRange) {
        fixedRange = aRange > 0;
        range = aRange;
    }

    protected void checkRange(double aRange) {
        if (aRange < 0.0) {
            throw new IllegalArgumentException("Range must be larger than or equal to 0.0m!");
        }
    }
}

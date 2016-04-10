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

import org.lisoft.lsml.model.loadout.LoadoutStandard;

/**
 * This class calculates the time a mech can keep firing all weapons before it over heats.
 * 
 * @author Emily Björk
 */
public class AlphaTimeToOverHeat implements Metric {
    private final HeatDissipation heatDissipation;
    private final HeatOverTime heatOverTime;
    private final HeatCapacity heatCapacity;
    static private final double MAX_TIME = 15 * 60;
    static private final double TIME_STEP = 0.1; // 9k iterations at worst

    /**
     * Creates a new {@link Metric}.
     * 
     * @param aHeatCapacity
     *            The {@link HeatCapacity} for the {@link LoadoutStandard}.
     * @param aHeatOverTime
     *            The {@link HeatOverTime} for the {@link LoadoutStandard}.
     * @param aHeatDissipation
     *            The {@link HeatDissipation} for the {@link LoadoutStandard}.
     */
    public AlphaTimeToOverHeat(HeatCapacity aHeatCapacity, HeatOverTime aHeatOverTime,
            HeatDissipation aHeatDissipation) {
        heatOverTime = aHeatOverTime;
        heatCapacity = aHeatCapacity;
        heatDissipation = aHeatDissipation;
    }

    @Override
    public double calculate() {
        double heat = 0;
        double time = 0;
        double lastHeat = 0;
        final double dissipated = heatDissipation.calculate() * TIME_STEP;

        final double capacity = heatCapacity.calculate();
        // First order linear ODE, using Euler's method.
        while (time < MAX_TIME) {
            final double currentHeat = heatOverTime.calculate(time);
            final double generated = currentHeat - lastHeat;
            heat += generated - dissipated;
            heat = Math.max(0, heat);
            time += TIME_STEP;
            lastHeat = currentHeat;
            if (heat >= capacity) {
                return time;
            }
        }
        return Double.POSITIVE_INFINITY;
    }
}

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

/**
 * This metric computes the amount of heat generated from a burst of the set time.
 *
 * @author Emily Björk
 */
public class BurstHeat implements Metric {

    private final BurstDamageOverTime burst;
    private final HeatOverTime heat;

    /**
     * @param aBurstDamageOverTime
     *            A {@link BurstDamageOverTime} to get the burst duration from.
     * @param aHeatOverTime
     *            A {@link HeatOverTime} to compute the heat generated from.
     *
     */
    public BurstHeat(BurstDamageOverTime aBurstDamageOverTime, HeatOverTime aHeatOverTime) {
        burst = aBurstDamageOverTime;
        heat = aHeatOverTime;
    }

    @Override
    public double calculate() {
        return heat.calculate(burst.getTime());
    }
}

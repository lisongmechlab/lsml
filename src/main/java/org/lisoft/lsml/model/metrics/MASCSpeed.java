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

import org.lisoft.lsml.model.item.MASC;
import org.lisoft.lsml.model.loadout.Loadout;

/**
 * This class calculates the speed of a loadout with MASC activated.
 *
 * @author Emily Björk
 *
 */
public class MASCSpeed implements Metric {

    private final Loadout loadout;
    private final TopSpeed topSpeed;

    /**
     * Creates a new {@link MASCSpeed} that will calculate the speed with MASC active for the given loadout.
     *
     * @param aLoadout
     *            The loadout to calculate for.
     * @param aTopSpeed
     *            The top speed metric to use for calculating the base speed.
     */
    public MASCSpeed(Loadout aLoadout, TopSpeed aTopSpeed) {
        loadout = aLoadout;
        topSpeed = aTopSpeed;
    }

    @Override
    public double calculate() {
        for (final MASC masc : loadout.items(MASC.class)) {
            // There can only be one.
            return topSpeed.calculate() * (1.0 + masc.getSpeedBoost());
        }
        return Double.NaN;
    }

}

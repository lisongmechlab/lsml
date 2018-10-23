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

import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.*;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.modifiers.*;

/**
 * This {@link Metric} calculates the total heat capacity of a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class HeatCapacity implements Metric {
    /**
     * This is a constant that is used to convert from dissipation values to capacity values unless specific capacity
     * values are given.
     */
    private static final int DISSIPATION_2_CAPACITY = 10;
    private static final double BASE_HEAT_CAPACITY = 50; // 30 Base + 20 from mandatory heat sinks
    private static final Attribute BASE_HEAT = new Attribute(BASE_HEAT_CAPACITY, ModifierDescription.SEL_HEAT_LIMIT);
    private final Loadout loadout;
    private Environment environment;

    public HeatCapacity(final Loadout aLoadout, Environment aEnvironment) {
        loadout = aLoadout;
        environment = aEnvironment;
    }

    @Override
    public double calculate() {
        final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
        final double hsCapacity = Math.max(0, (loadout.getHeatsinksCount() - 10) * hs.getCapacity());
        final double throttleCapacity = -DISSIPATION_2_CAPACITY * Engine.ENGINE_HEAT_FULL_THROTTLE;
        final double envCapacity = -DISSIPATION_2_CAPACITY * environment.getHeat(loadout.getAllModifiers());
        final double ans = BASE_HEAT.value(loadout.getAllModifiers()) + hsCapacity + throttleCapacity + envCapacity;
        return ans;
    }

    public void changeEnvironment(Environment anEnvironment) {
        environment = anEnvironment;
    }
}

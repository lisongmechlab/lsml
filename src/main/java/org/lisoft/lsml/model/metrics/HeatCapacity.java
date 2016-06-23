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

import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

/**
 * This {@link Metric} calculates the total heat capacity of a {@link LoadoutStandard}.
 *
 * @author Emily Björk
 */
public class HeatCapacity implements Metric {
    /**
     * This is a constant that is used to convert from dissipation values to capacity values unless specific capacity
     * values are given.
     */
    private static final int DISSIPATION_2_CAPACITY = 10;
    private static final double BASE_HEAT_CAPACITY = 30;
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
        final Engine engine = loadout.getEngine();
        final int engineHs = engine != null ? engine.getNumInternalHeatsinks() : 0;
        final double internalHsCapacity = DISSIPATION_2_CAPACITY * engineHs * hs.getEngineDissipation();
        final double throttleCapacity = -DISSIPATION_2_CAPACITY * Engine.ENGINE_HEAT_FULL_THROTTLE;
        final double envCapacity = -DISSIPATION_2_CAPACITY * environment.getHeat(loadout.getModifiers());
        final double externalHsCapacity = (loadout.getHeatsinksCount() - engineHs) * hs.getCapacity();
        final double ans = BASE_HEAT.value(loadout.getModifiers()) + internalHsCapacity + externalHsCapacity
                + throttleCapacity + envCapacity;
        return ans;
    }

    public void changeEnvironment(Environment anEnvironment) {
        environment = anEnvironment;
    }
}

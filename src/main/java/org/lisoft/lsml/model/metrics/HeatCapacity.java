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

import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

/**
 * This {@link Metric} calculates the total heat capacity of a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class HeatCapacity implements Metric {
    private final Loadout loadout;
    private static final double MECH_BASE_HEAT_CAPACITY = 30;

    public HeatCapacity(final Loadout aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public double calculate() {
        HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();
        final double ans = MECH_BASE_HEAT_CAPACITY + loadout.getHeatsinksCount() * hs.getCapacity();
        final Attribute heatLimit = new Attribute(ans, ModifierDescription.SEL_HEAT_LIMIT);
        return heatLimit.value(loadout.getModifiers());
    }
}

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
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

import java.util.Collection;

import static org.lisoft.lsml.model.metrics.HeatCapacity.MANDATORY_ENGINE_HEAT_SINKS;

/**
 * This {@link Metric} calculates the heat dissipation for a {@link LoadoutStandard}.
 *
 * @author Li Song
 */
public class HeatDissipation implements Metric {
    private final Loadout loadout;
    private Environment environment;

    public HeatDissipation(final Loadout aLoadout, final Environment aEnvironment) {
        loadout = aLoadout;
        environment = aEnvironment;
    }

    @Override
    public double calculate() {
        final Collection<Modifier> modifiers = loadout.getAllModifiers();
        final HeatSink protoHeatSink = loadout.getUpgrades().getHeatSink().getHeatSinkType();

        // At the time of writing, 2022-01-30, there exists no heat sink for which the internal/external dissipation
        // differs, so it's currently impossible to determine whether the internal dissipation attribute (which is
        // present in the game data files) applies to the mandatory 10 heat sinks like it does for heat capacity
        // or if it only applies to the actual number of heat sinks in the engine. At the very least, even if the
        // devs decide to differentiate the internal/external dissipation numbers this would at worst be incorrect for
        // engine sizes < 250 which aren't that common.
        final int externalHeatSinks = Math.max(0, loadout.getTotalHeatSinksCount() - MANDATORY_ENGINE_HEAT_SINKS);
        final int internalHeatSinks = loadout.getTotalHeatSinksCount() - externalHeatSinks;
        final double engineDissipation = internalHeatSinks * protoHeatSink.getEngineDissipation();
        final double externalDissipation = externalHeatSinks * protoHeatSink.getDissipation();
        final double totalDissipation = engineDissipation + externalDissipation;

        // TODO: Verify if heat dissipation quirks applies to total dissipation or just engine internal dissipation.
        final Attribute heatDissipation = new Attribute(totalDissipation, ModifierDescription.SEL_HEAT_DISSIPATION);

        final double environmentDissipation = (environment != null) ? environment.getHeat(modifiers) : 0;
        return heatDissipation.value(modifiers) - environmentDissipation;
    }

    public void changeEnvironment(Environment anEnvironment) {
        environment = anEnvironment;
    }
}

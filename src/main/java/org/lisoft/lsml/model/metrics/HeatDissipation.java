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

import java.util.Collection;

import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.model.modifiers.ModifierDescription;

/**
 * This {@link Metric} calculates the heat dissipation for a {@link LoadoutStandard}.
 * 
 * @author Emily Björk
 */
public class HeatDissipation implements Metric {
    private final Loadout loadout;
    private Environment   environment;

    public HeatDissipation(final Loadout aLoadout, final Environment aEnvironment) {
        loadout = aLoadout;
        environment = aEnvironment;
    }

    @Override
    public double calculate() {
        final Collection<Modifier> modifiers = loadout.getModifiers();
        final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();

        final int internalHs = (loadout.getEngine() == null) ? 0 : loadout.getEngine().getNumInternalHeatsinks();
        final int externalHs = loadout.getHeatsinksCount() - internalHs;
        final double ans = internalHs * hs.getEngineDissipation() + externalHs * hs.getDissipation();

        final Attribute heatDissipation = new Attribute(ans, ModifierDescription.SEL_HEAT_DISSIPATION);
        final double externalHeat = (environment != null) ? environment.getHeat(modifiers) : 0;

        return heatDissipation.value(modifiers) - externalHeat;
    }

    public void changeEnvironment(Environment anEnvironment) {
        environment = anEnvironment;
    }
}

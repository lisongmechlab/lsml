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

import java.util.Collection;

import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.item.Engine;
import org.lisoft.lsml.model.item.HeatSink;
import org.lisoft.lsml.model.loadout.*;
import org.lisoft.lsml.model.modifiers.*;

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
        final HeatSink hs = loadout.getUpgrades().getHeatSink().getHeatSinkType();

        final Engine engine = loadout.getEngine();
        final double engineDissipation;
        if(null!=engine){
            engineDissipation = engine.getNumInternalHeatsinks() * hs.getEngineDissipation();
        }else{
            engineDissipation = 0.0;
        }

        final double externalDissipation = loadout.getExternalHeatSinksCount() * hs.getDissipation();
        final double totalDissipation = engineDissipation + externalDissipation;
        final Attribute heatDissipation = new Attribute(totalDissipation, ModifierDescription.SEL_HEAT_DISSIPATION);

        final double environmentDissipation = (environment != null) ? environment.getHeat(modifiers) : 0;
        return heatDissipation.value(modifiers) - environmentDissipation;
    }

    public void changeEnvironment(Environment anEnvironment) {
        environment = anEnvironment;
    }
}

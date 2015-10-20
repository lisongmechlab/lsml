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

import org.lisoft.lsml.model.datacache.ModifiersDB;
import org.lisoft.lsml.model.environment.Environment;
import org.lisoft.lsml.model.loadout.LoadoutBase;
import org.lisoft.lsml.model.loadout.LoadoutStandard;
import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This {@link Metric} calculates the heat dissipation for a {@link LoadoutStandard}.
 * 
 * @author Li Song
 */
public class HeatDissipation implements Metric {
    private final LoadoutBase<?> loadout;
    private Environment          environment;

    public HeatDissipation(final LoadoutBase<?> aLoadout, final Environment aEnvironment) {
        loadout = aLoadout;
        environment = aEnvironment;
    }

    @Override
    public double calculate() {
        Collection<Modifier> modifiers = loadout.getModifiers();

        double ans = 0;
        int enginehs = 0;
        if (loadout.getEngine() != null) {
            enginehs = loadout.getEngine().getNumInternalHeatsinks();
        }

        final double dissipation = loadout.getUpgrades().getHeatSink().getHeatSinkType().getDissipation();

        // Engine internal HS count as true doubles
        ans += enginehs * (loadout.getUpgrades().getHeatSink().isDouble() ? 0.2 : 0.1);
        ans += (loadout.getHeatsinksCount() - enginehs) * dissipation;

        final Attribute heatDissipation = new Attribute(ans, ModifiersDB.SEL_HEAT_DISSIPATION);
        final double externalHeat = (environment != null) ? environment.getHeat(modifiers) : 0;

        return heatDissipation.value(modifiers) - externalHeat;
    }

    public void changeEnvironment(Environment anEnvironment) {
        environment = anEnvironment;
    }
}

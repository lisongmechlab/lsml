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

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.helpers.IntegratedConstantSignal;
import org.lisoft.lsml.model.metrics.helpers.IntegratedImpulseTrain;
import org.lisoft.lsml.model.metrics.helpers.IntegratedSignal;
import org.lisoft.lsml.model.metrics.helpers.TruncatedSignal;
import org.lisoft.lsml.model.modifiers.Modifier;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Computes the highest percentage of the heat capacity that will be consumed during an alpha strike,
 * taking heat sinking and server tick into account. Includes engine heat.
 *
 * @author Li Song
 */
public class AlphaHeatPercent implements Metric {
    private static final double EPSILON = 1E-6;
    private static final double MATCH_LENGTH_SECONDS = 15 * 60;
    private final GhostHeat ghostHeat;
    private final int group;
    private final HeatCapacity heatCapacity;
    private final HeatDissipation heatDissipation;
    private final Loadout loadout;

    public AlphaHeatPercent(GhostHeat aGhostHeat, HeatDissipation aHeatDissipation, HeatCapacity aHeatCapacity,
                            Loadout aLoadout) {
        this(aGhostHeat, aHeatDissipation, aHeatCapacity, aLoadout, -1);
    }

    public AlphaHeatPercent(GhostHeat aGhostHeat, HeatDissipation aHeatDissipation, HeatCapacity aHeatCapacity,
                            Loadout aLoadout, int aWeaponGroup) {
        ghostHeat = aGhostHeat;
        heatDissipation = aHeatDissipation;
        heatCapacity = aHeatCapacity;
        loadout = aLoadout;
        group = aWeaponGroup;
    }

    @Override
    public double calculate() {
        final double capacity = heatCapacity.calculate();
        final Collection<Modifier> modifiers = loadout.getAllModifiers();
        final Collection<IntegratedSignal> heatSignals = new ArrayList<>();
        heatSignals.add(new IntegratedImpulseTrain(Double.POSITIVE_INFINITY, ghostHeat.calculate()));
        heatSignals.add(new IntegratedConstantSignal(-heatDissipation.calculate()));

        if (loadout.getEngine() != null) {
            heatSignals.add(loadout.getEngine().getExpectedHeatSignal(modifiers));
        }

        final Iterable<Weapon> weaponsInGroup;
        if (group >= 0) {
            weaponsInGroup = loadout.getWeaponGroups().getWeapons(group, loadout);
        } else {
            weaponsInGroup = loadout.items(Weapon.class);
        }

        final double tickDuration = 0.1;
        double maxPeriod = 0.0;
        for (Weapon weapon : weaponsInGroup) {
            double firingPeriod = weapon.getRawFiringPeriod(modifiers) - EPSILON;
            if (firingPeriod > MATCH_LENGTH_SECONDS) {
                firingPeriod = tickDuration - EPSILON;
            }
            maxPeriod = Math.max(maxPeriod, firingPeriod);
            if (weapon.isOffensive()) {
                heatSignals.add(new TruncatedSignal(weapon.getExpectedHeatSignal(modifiers), firingPeriod));
            }
        }

        double maxHeat = 0.0;
        final int numTicks = (int) Math.ceil(maxPeriod / tickDuration);
        for (int tick = 0; tick <= numTicks; tick++) {
            double t = tickDuration * tick;
            // This linear time scan isn't the prettiest, but it does the job in a few hundred
            // iterations at worst. The performance can be improved by restricting maxPeriod more
            // or by applying calculus and suitable API changes in the integrated signals class.

            double signalSum = 0.0;
            for (IntegratedSignal signal : heatSignals) {
                signalSum += signal.integrateFromZeroTo(t);
            }
            maxHeat = Math.max(maxHeat, signalSum);
        }
        return maxHeat / capacity;
    }
}

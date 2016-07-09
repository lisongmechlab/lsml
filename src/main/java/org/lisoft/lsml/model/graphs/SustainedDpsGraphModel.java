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
package org.lisoft.lsml.model.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.lisoft.lsml.model.item.ItemComparator;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.MaxSustainedDPS;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.util.WeaponRanges;

/**
 * This class is used as a model for graphs showing the maximal sustained DPS of a {@link Loadout}.
 *
 * @author Li Song
 *
 */
public class SustainedDpsGraphModel implements DamageGraphModel {
    private final MaxSustainedDPS sustainedDPS;
    private final Loadout loadout;

    /**
     * Creates a new model.
     *
     * @param aSustainedDPS
     *            The {@link MaxSustainedDPS} object to use in calculating this model's data.
     * @param aLoadout
     *            The {@link Loadout} to calculate for.
     */
    public SustainedDpsGraphModel(MaxSustainedDPS aSustainedDPS, Loadout aLoadout) {
        sustainedDPS = aSustainedDPS;
        loadout = aLoadout;
    }

    @Override
    public SortedMap<Weapon, List<Pair<Double, Double>>> getData() {
        final Collection<Modifier> modifiers = loadout.getModifiers();
        final SortedMap<Weapon, List<Pair<Double, Double>>> data = new TreeMap<>(ItemComparator.byRange(modifiers));

        for (final double range : WeaponRanges.getRanges(loadout)) {
            final Set<Entry<Weapon, Double>> damageDistributio = sustainedDPS.getWeaponRatios(range).entrySet();
            for (final Map.Entry<Weapon, Double> entry : damageDistributio) {
                final Weapon weapon = entry.getKey();
                final double ratio = entry.getValue();
                final double dps = weapon.getStat("d/s", modifiers);
                final double rangeEff = weapon.getRangeEffectivity(range, modifiers);

                data.computeIfAbsent(weapon, aWeapon -> new ArrayList<>())
                        .add(new Pair<>(range, dps * ratio * rangeEff));
            }
        }
        return data;
    }

    @Override
    public String getTitle() {
        return "Sustained DPS";
    }

    @Override
    public String getXAxisLabel() {
        return "Range [m]";
    }

    @Override
    public String getYAxisLabel() {
        return "DPS";
    }
}
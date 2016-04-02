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
package org.lisoft.lsml.model.graphs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.modifiers.Modifier;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.util.WeaponRanges;

/**
 * This class is used as a model for the maximal DPS of a {@link Loadout} as a graph.
 * 
 * @author Emily Björk
 *
 */
public class MaxDpsGraphModel implements DamageGraphModel {
    private final Loadout loadout;

    /**
     * Creates a new model.
     * 
     * @param aLoadout
     *            The loadout to calculate for.
     */
    public MaxDpsGraphModel(Loadout aLoadout) {
        loadout = aLoadout;
    }

    @Override
    public SortedMap<Weapon, List<Pair<Double, Double>>> getData() {
        final Collection<Modifier> modifiers = loadout.getModifiers();

        // Figure out how many of each weapon
        SortedMap<Weapon, Integer> multiplicity = new TreeMap<Weapon, Integer>(Weapon.RANGE_WEAPON_ORDERING);
        for (Weapon weapon : loadout.items(Weapon.class)) {
            if (!weapon.isOffensive())
                continue;
            if (!multiplicity.containsKey(weapon)) {
                multiplicity.put(weapon, 0);
            }
            int v = multiplicity.get(weapon);
            multiplicity.put(weapon, v + 1);
        }

        // Result container
        SortedMap<Weapon, List<Pair<Double, Double>>> result = new TreeMap<Weapon, List<Pair<Double, Double>>>(
                Weapon.RANGE_WEAPON_ORDERING);

        // Calculate the DPS
        Double[] ranges = WeaponRanges.getRanges(loadout);
        for (Map.Entry<Weapon, Integer> uniqueWeaponMultiplicity : multiplicity.entrySet()) {
            Weapon weapon = uniqueWeaponMultiplicity.getKey();
            int mult = uniqueWeaponMultiplicity.getValue();

            List<Pair<Double, Double>> series = new ArrayList<>();
            for (double range : ranges) {
                final double dps = weapon.getStat("d/s", modifiers);
                final double rangeEff = weapon.getRangeEffectivity(range, modifiers);
                series.add(new Pair<Double, Double>(range, dps * rangeEff * mult));
            }
            result.put(weapon, series);
        }
        return result;
    }

    @Override
    public String getXAxisLabel() {
        return "Range [m]";
    }

    @Override
    public String getYAxisLabel() {
        return "DPS";
    }

    @Override
    public String getTitle() {
        return "Maximal DPS";
    }
}
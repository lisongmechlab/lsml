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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.loadout.LoadoutMetrics;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.util.WeaponRanges;

/**
 * This class is used as a model for displaying graphs showing the alpha strike damage of a 'Mech.
 * 
 * @author Li Song
 *
 */
public class AlphaStrikeGraphModel implements DamageGraphModel {
    private final LoadoutMetrics metrics;
    private final Loadout        loadout;

    /**
     * Creates a new model.
     * 
     * @param aMetrics
     *            The {@link LoadoutMetrics} object to use in calculating this model's data.
     * @param aLoadout
     *            The loadout to calculate for.
     */
    public AlphaStrikeGraphModel(LoadoutMetrics aMetrics, Loadout aLoadout) {
        metrics = aMetrics;
        loadout = aLoadout;
    }

    @Override
    public SortedMap<Weapon, List<Pair<Double, Double>>> getData() {
        SortedMap<Weapon, List<Pair<Double, Double>>> data = new TreeMap<Weapon, List<Pair<Double, Double>>>(
                Weapon.RANGE_WEAPON_ORDERING);

        Double[] ranges = WeaponRanges.getRanges(loadout);
        for (double range : ranges) {
            Set<Entry<Weapon, Double>> dist = metrics.alphaStrike.getWeaponRatios(range).entrySet();
            for (Map.Entry<Weapon, Double> entry : dist) {
                final Weapon weapon = entry.getKey();
                if (!data.containsKey(weapon)) {
                    data.put(weapon, new ArrayList<Pair<Double, Double>>());
                }
                data.get(weapon).add(new Pair<Double, Double>(range, entry.getValue()));
            }
        }
        return data;
    }

    @Override
    public String getXAxisLabel() {
        return "Range [m]";
    }

    @Override
    public String getYAxisLabel() {
        return "Damage";
    }

    @Override
    public String getTitle() {
        return "Alpha Strike Damage";
    }
}
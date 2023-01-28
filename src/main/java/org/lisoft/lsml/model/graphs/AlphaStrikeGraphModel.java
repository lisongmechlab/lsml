/*
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013-2023  Li Song
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
package org.lisoft.lsml.model.graphs;

import java.util.*;
import java.util.Map.Entry;
import org.lisoft.lsml.model.item.ItemComparator;
import org.lisoft.lsml.model.item.Weapon;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.model.metrics.AlphaStrike;
import org.lisoft.lsml.util.Pair;
import org.lisoft.lsml.util.WeaponRanges;

/**
 * This class is used as a model for displaying graphs showing the alpha strike damage of a 'Mech.
 *
 * @author Li Song
 */
public class AlphaStrikeGraphModel implements DamageGraphModel {
  private final AlphaStrike alphaStrikeMetric;
  private final Loadout loadout;

  /**
   * Creates a new model.
   *
   * @param aAlphaStrikeMetric The {@link AlphaStrike} object to use in calculating this model's
   *     data.
   * @param aLoadout The loadout to calculate for.
   */
  public AlphaStrikeGraphModel(AlphaStrike aAlphaStrikeMetric, Loadout aLoadout) {
    alphaStrikeMetric = aAlphaStrikeMetric;
    loadout = aLoadout;
  }

  @Override
  public SortedMap<Weapon, List<Pair<Double, Double>>> getData() {
    final SortedMap<Weapon, List<Pair<Double, Double>>> data =
        new TreeMap<>(ItemComparator.byRange(loadout.getAllModifiers()));

    for (final double range : WeaponRanges.getRanges(loadout)) {
      final Set<Entry<Weapon, Double>> dist =
          alphaStrikeMetric.getWeaponDamageContribution(range).entrySet();
      for (final Map.Entry<Weapon, Double> entry : dist) {
        final Weapon weapon = entry.getKey();
        data.computeIfAbsent(weapon, (aWeapon) -> new ArrayList<>())
            .add(new Pair<>(range, entry.getValue()));
      }
    }
    return data;
  }

  @Override
  public String getTitle() {
    return "Alpha Strike Damage";
  }

  @Override
  public String getXAxisLabel() {
    return "Range [m]";
  }

  @Override
  public String getYAxisLabel() {
    return "Damage";
  }
}

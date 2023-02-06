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
package org.lisoft.lsml.util;

import java.util.*;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.equipment.Weapon;
import org.lisoft.lsml.mwo_data.modifiers.Modifier;

/**
 * This class will calculate the set of ranges at which weapons change damage. In essence, it
 * calculates the ordered union of the zero, min, long and max ranges for all given weapons.
 *
 * @author Li Song
 */
public class WeaponRanges {

  public static List<Double> getRanges(
      Collection<Weapon> aWeaponCollection, Collection<Modifier> aModifiers) {
    final SortedSet<Double> ans = new TreeSet<>();

    ans.add(0.0);
    for (final Weapon weapon : aWeaponCollection) {
      if (!weapon.isOffensive()) {
        continue;
      }
      ans.addAll(weapon.getRangeProfile().getPolygonTrainRanges(10, aModifiers));
    }
    return new ArrayList<>(ans);
  }

  public static List<Double> getRanges(Loadout aLoadout) {
    final List<Weapon> weapons = new ArrayList<>();
    aLoadout.items(Weapon.class).forEach(weapons::add);
    return getRanges(weapons, aLoadout.getAllModifiers());
  }
}

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
package org.lisoft.lsml.view_fx.util;

import java.util.Collection;
import java.util.stream.Collectors;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.mwo_data.modifiers.AffectsWeaponPredicate;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class represents a loadout that is displayed in various tables. It includes a pre-computed
 * and pre-filtered modifier list for performance reasons.
 *
 * @author Li Song
 */
public class DisplayLoadout {
  public final Collection<Modifier> filteredModifiers;
  public final Loadout loadout;
  public final Collection<Modifier> rawModifiers;

  public DisplayLoadout(Loadout aLoadout) {
    loadout = aLoadout;
    rawModifiers = aLoadout.getAllModifiers();
    filteredModifiers =
        rawModifiers.stream().filter(new AffectsWeaponPredicate()).collect(Collectors.toList());
  }

  /**
   * @return the loadout
   */
  public Loadout getLoadout() {
    return loadout;
  }
}

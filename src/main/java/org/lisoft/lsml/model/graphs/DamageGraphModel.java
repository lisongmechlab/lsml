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

import java.util.List;
import java.util.SortedMap;
import org.lisoft.lsml.util.Pair;
import org.lisoft.mwo_data.equipment.Weapon;

/**
 * This interface models a model as a part of the MVC design for showing graphs.
 *
 * @author Li Song
 */
public interface DamageGraphModel {
  /**
   * @return A map of data mapping each weapon to a list of xy pairs.
   */
  SortedMap<Weapon, List<Pair<Double, Double>>> getData();

  String getTitle();

  String getXAxisLabel();

  String getYAxisLabel();
}

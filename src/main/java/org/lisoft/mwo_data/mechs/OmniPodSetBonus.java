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
package org.lisoft.mwo_data.mechs;

import java.util.Collection;
import java.util.Collections;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This class models a set of {@link OmniPod}s in order to provide the set bonus when a number of
 * pods of a set are equipped.
 *
 * @author Li Song
 */
public class OmniPodSetBonus {
  private final int minPieces;
  private final Collection<Modifier> modifiers;

  public OmniPodSetBonus(int aMinPieces, Collection<Modifier> aModifiers) {
    minPieces = aMinPieces;
    modifiers = aModifiers;
  }

  public Collection<Modifier> getModifiers() {
    return Collections.unmodifiableCollection(modifiers);
  }

  /**
   * @return The minimum number of pieces of the set that have to be present for the bonus to be
   *     applied.
   */
  public int getMinPieces() {
    return minPieces;
  }
}

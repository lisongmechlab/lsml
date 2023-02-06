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
package org.lisoft.lsml.mwo_data.modifiers;

/**
 * Values can be categorised based on how the affect the subjective performance of a mech.
 *
 * <p>There are three classes:
 *
 * <ul>
 *   <li>Positive Good: A positive value on the quirk is desirable for the pilot.
 *   <li>Negative Good: A negative value on the quirk is desirable for the pilot.
 *   <li>Indeterminate: Value isn't unanimously desirable. For example heat transfer quirk is good
 *       for cold maps but bad on hot maps, so it's indeterminate.
 * </ul>
 *
 * @author Li Song
 */
public enum ModifierType {
  INDETERMINATE,
  NEGATIVE_GOOD,
  POSITIVE_GOOD
}

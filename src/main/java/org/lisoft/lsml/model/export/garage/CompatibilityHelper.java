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
package org.lisoft.lsml.model.export.garage;

import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.GuidanceUpgrade;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.MissileWeapon;

/**
 * This class helps dealing with compatibility issues that arise along the way.
 *
 * @author Li Song
 */
public class CompatibilityHelper {

  /**
   * February 4th patch introduced new weapon IDs for Artemis enabled missile launchers. This
   * function canonicalizes old missile launchers to the new types if applicable.
   *
   * @param aItem The item to fix.
   * @param aGuidanceType The current {@link GuidanceUpgrade}.
   * @return A canonised item.
   */
  public static Item fixArtemis(final Item aItem, GuidanceUpgrade aGuidanceType) {
    Item ans = aItem;
    if (aItem instanceof final MissileWeapon weapon) {
      ans = aGuidanceType.upgrade(weapon);
    } else if (aItem instanceof final Ammunition ammunition) {
      ans = aGuidanceType.upgrade(ammunition);
    }
    return ans;
  }
}

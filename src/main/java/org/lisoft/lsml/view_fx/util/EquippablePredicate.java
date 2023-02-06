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

import java.util.function.Predicate;
import javafx.scene.control.TreeItem;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.mwo_data.equipment.AmmoWeapon;
import org.lisoft.lsml.mwo_data.equipment.Ammunition;
import org.lisoft.lsml.mwo_data.equipment.Item;
import org.lisoft.lsml.mwo_data.equipment.MwoObject;
import org.lisoft.lsml.mwo_data.mechs.Chassis;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;

/**
 * This predicate is used for hiding items from the equipment list that are of no interest to the
 * user at the moment.
 *
 * @author Li Song
 */
public class EquippablePredicate implements Predicate<TreeItem<Object>> {
  private final Loadout loadout;

  /**
   * Creates a new predicate instance.
   *
   * @param aLoadout The {@link Loadout} to create the predicate for.
   */
  public EquippablePredicate(Loadout aLoadout) {
    loadout = aLoadout;
  }

  @Override
  public boolean test(TreeItem<Object> aTreeItem) {
    if (!aTreeItem.getChildren().isEmpty() && !aTreeItem.isLeaf()) {
      return true; // Show non empty categories
    }

    final Object object = aTreeItem.getValue();
    if (object instanceof MwoObject) {
      final MwoObject equipment = (MwoObject) aTreeItem.getValue();
      final Chassis chassis = loadout.getChassis();

      if (!equipment.getFaction().isCompatible(chassis.getFaction())) {
        return false;
      }

      if (equipment instanceof Item) {
        final Item item = (Item) equipment;

        if (!chassis.isAllowed(item)) {
          return false;
        }

        if (!item.isCompatible(loadout.getUpgrades())) {
          return false;
        }

        if (item instanceof Ammunition) {
          return hasRelevantWeaponFor((Ammunition) item);
        }

        final HardPointType hardPoint = item.getHardpointType();
        return hardPoint == HardPointType.NONE || loadout.getHardpointsCount(hardPoint) >= 1;
      }
      return true;
    }
    return false;
  }

  private boolean hasRelevantWeaponFor(final Ammunition ammunition) {
    for (final AmmoWeapon weapon : loadout.items(AmmoWeapon.class)) {
      if (weapon.isCompatibleAmmo(ammunition)) {
        return true;
      }
    }

    for (final Ammunition otherAmmo : loadout.items(Ammunition.class)) {
      if (otherAmmo == ammunition) {
        return true;
      }
    }
    return false;
  }
}

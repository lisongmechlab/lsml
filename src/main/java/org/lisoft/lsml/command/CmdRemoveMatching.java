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
package org.lisoft.lsml.command;

import java.util.function.Predicate;
import org.lisoft.lsml.messages.MessageDelivery;
import org.lisoft.lsml.model.loadout.ConfiguredComponent;
import org.lisoft.lsml.model.loadout.Loadout;
import org.lisoft.lsml.util.CommandStack.Command;
import org.lisoft.lsml.util.CommandStack.CompositeCommand;
import org.lisoft.mwo_data.equipment.AmmoWeapon;
import org.lisoft.mwo_data.equipment.Ammunition;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.Weapon;

/**
 * This class removes all items matching a given predicate.
 *
 * @author Li Song
 */
public class CmdRemoveMatching extends CompositeCommand {

  private final Loadout loadout;
  private final Predicate<Item> predicate;

  public CmdRemoveMatching(
      String aDescription,
      MessageDelivery aMessageTarget,
      Loadout aLoadout,
      Predicate<Item> aPredicate) {
    super(aDescription, aMessageTarget);
    predicate = aPredicate;
    loadout = aLoadout;
  }

  public static Command removeWeaponSystem(
      MessageDelivery aMessageTarget, Loadout aLoadout, Weapon aWeapon) {
    if (aWeapon instanceof final AmmoWeapon ammoWeapon) {
      if (!ammoWeapon.hasBuiltInAmmo()) {
        final Ammunition ammo = ammoWeapon.getAmmoType();
        final Ammunition ammoHalf = ammoWeapon.getAmmoHalfType();
        return new CmdRemoveMatching(
            "remove all " + aWeapon.getName() + " and ammo",
            aMessageTarget,
            aLoadout,
            aItem -> aItem == aWeapon || aItem == ammo || aItem == ammoHalf);
      }
    }
    return new CmdRemoveMatching(
        "remove all " + aWeapon.getName(), aMessageTarget, aLoadout, aItem -> aItem == aWeapon);
  }

  @Override
  protected void buildCommand() {
    for (final ConfiguredComponent confComp : loadout.getComponents()) {
      for (final Item equippedItem : confComp.getItemsEquipped()) {
        if (predicate.test(equippedItem)) {
          addOp(new CmdRemoveItem(messageBuffer, loadout, confComp, equippedItem));
        }
      }
    }
  }
}

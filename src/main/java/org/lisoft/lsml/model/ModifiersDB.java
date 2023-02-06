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
package org.lisoft.lsml.model;

import java.util.*;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.mwo_data.Database;
import org.lisoft.mwo_data.equipment.NoSuchItemException;
import org.lisoft.mwo_data.equipment.Weapon;
import org.lisoft.mwo_data.modifiers.ModifierDescription;
import org.lisoft.mwo_data.modifiers.ModifierType;
import org.lisoft.mwo_data.modifiers.Operation;

/**
 * A database of all the quirks in the game.
 *
 * @author Li Song
 */
public class ModifiersDB {
  public static final ModifierDescription HEAT_MOVEMENT_DESC;
  private static final Map<String, ModifierDescription> mwoName2modifier;

  /*
   A decision has been made to rely on static initializers for *DB classes. The motivation is that all items are
   immutable, and this is the only way that allows providing global item constants such as ItemDB.AMS.
  */
  static {
    final Database database = LiSongMechLab.getDatabase();

    mwoName2modifier = new HashMap<>();
    final Collection<ModifierDescription> modifiers = database.getModifierDescriptions().values();
    for (final ModifierDescription description : modifiers) {
      mwoName2modifier.put(canonicalize(description.getKey()), description);
    }

    //noinspection SpellCheckingInspection
    HEAT_MOVEMENT_DESC =
        new ModifierDescription(
            "ENGINE HEAT",
            "movementheat_multiplier",
            Operation.MUL,
            ModifierDescription.SEL_HEAT_MOVEMENT,
            null,
            ModifierType.NEGATIVE_GOOD);
  }

  public static Collection<String> getAllSelectors(Class<? extends Weapon> aClass) {
    final Set<String> ans = new HashSet<>();
    for (final Weapon w : ItemDB.lookup(aClass)) {
      ans.addAll(w.getAliases());
    }
    ans.addAll(ModifierDescription.SEL_HEAT_DISSIPATION);
    ans.addAll(ModifierDescription.SEL_HEAT_LIMIT);
    return ans;
  }

  public static Collection<String> getAllWeaponSelectors() {
    final Set<String> ans = new HashSet<>();
    for (final Weapon w : ItemDB.lookup(Weapon.class)) {
      ans.addAll(w.getAliases());
    }
    ans.addAll(ModifierDescription.SEL_HEAT_DISSIPATION);
    ans.addAll(ModifierDescription.SEL_HEAT_LIMIT);
    ans.addAll(ModifierDescription.SEL_HEAT_EXTERNAL_TRANSFER);
    return ans;
  }

  /**
   * Looks up a {@link ModifierDescription} by a MWO key.
   *
   * @param aKey The lookup key.
   * @return A {@link ModifierDescription}.
   * @throws NoSuchItemException if no {@link ModifierDescription} was found with that key.
   */
  public static ModifierDescription lookup(String aKey) throws NoSuchItemException {
    final ModifierDescription description = mwoName2modifier.get(canonicalize(aKey));
    if (description == null) {
      throw new NoSuchItemException("Unknown key!");
    }
    return description;
  }

  /**
   * Canonizes a string for lookup in the maps.
   *
   * @param aName The string to canonicalize.
   * @return A canonized {@link String}.
   */
  private static String canonicalize(String aName) {
    return aName.toLowerCase();
  }
}

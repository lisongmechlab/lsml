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
import org.lisoft.mwo_data.equipment.Consumable;
import org.lisoft.mwo_data.equipment.Item;
import org.lisoft.mwo_data.equipment.NoSuchItemException;

/**
 * This class acts as a database of all the consumable modules that are parsed.
 *
 * <p>XXX: Consider merging {@link Consumable} into {@link Item}.
 *
 * @author Li Song
 */
public class ConsumableDB {
  private static final Map<Integer, Consumable> mwoIndex2module;
  private static final Map<String, Consumable> name2module;

  /*
   * A decision has been made to rely on static initializers for *DB classes. The motivation is that
   * all items are immutable, and this is the only way that allows providing global item constants
   * such as ItemDB.AMS.
   */
  static {
    final Database database = LiSongMechLab.getDatabase();
    mwoIndex2module = new HashMap<>();
    name2module = new HashMap<>();

    for (final Consumable module : database.getPilotModules()) {
      mwoIndex2module.put(module.getId(), module);
      name2module.put(module.getName(), module);
    }
  }

  public static List<Consumable> lookup(Class<? extends Consumable> aClass) {
    final List<Consumable> ans = new ArrayList<>();
    for (final Consumable module : mwoIndex2module.values()) {
      if (aClass.isAssignableFrom(module.getClass())) {
        ans.add(module);
      }
    }
    return ans;
  }

  public static Collection<Consumable> lookup(Consumable.ConsumableType aType) {
    final List<Consumable> ans = new ArrayList<>();
    for (final Consumable consumable : mwoIndex2module.values()) {
      if (consumable.getType() == aType) {
        ans.add(consumable);
      }
    }
    return ans;
  }

  public static Consumable lookup(int aId) throws NoSuchItemException {
    final Consumable module = mwoIndex2module.get(aId);
    if (null == module) {
      throw new NoSuchItemException("No module found with ID: " + aId);
    }
    return module;
  }

  /**
   * Looks up a pilot module by string name.
   *
   * @param aName The name of the module to lookup.
   * @return A {@link Consumable} by the given name.
   * @throws NoSuchItemException if no {@link Consumable} could be found with the given name.
   */
  public static Consumable lookup(String aName) throws NoSuchItemException {
    final Consumable module = name2module.get(aName);
    if (module == null) {
      throw new NoSuchItemException("No module by name: " + aName);
    }
    return module;
  }
}

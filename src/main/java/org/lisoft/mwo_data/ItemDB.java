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
package org.lisoft.mwo_data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lisoft.lsml.view_fx.LiSongMechLab;
import org.lisoft.mwo_data.equipment.*;
import org.lisoft.mwo_data.mechs.HardPointType;

/**
 * This class is a database of all {@link Item}s. One can look up by MWO id, textual name and MWO
 * string name of the item.
 *
 * @author Li Song
 */
public class ItemDB {
  public static final AmmoWeapon AMS;
  public static final Item BAP;
  public static final Item CASE;
  public static final AmmoWeapon C_AMS;
  public static final HeatSink DHS;
  public static final Internal DYN_ARMOUR;
  public static final Internal DYN_STRUCT;
  public static final Item ECM;
  public static final Internal FIX_ARMOUR;
  public static final Internal FIX_STRUCT;
  public static final Internal HA;
  public static final int HA_ID = 1911; // HandActuator
  public static final Internal LAA;
  public static final int LAA_ID = 1910; // LowerArmActuator
  public static final HeatSink SHS;
  public static final Internal UAA;
  public static final int UAA_ID = 1909; // UpperArmActuator
  private static final Map<String, Item> localizedName2item;
  private static final Map<Integer, Item> mwoIndex2item;
  private static final Map<String, Item> mwoName2item;

  /*
   * A decision has been made to rely on static initializers for *DB classes. The motivation is that
   * all items are immutable, and this is the only way that allows providing global item constants
   * such as ItemDB.AMS.
   */
  static {
    final Database database = LiSongMechLab.getDatabase();

    mwoName2item = new HashMap<>();
    localizedName2item = new HashMap<>();
    mwoIndex2item = new HashMap<>();

    for (final Item item : database.getItems()) {
      put(item);
    }

    // Initialize special items
    try {
      C_AMS = (AmmoWeapon) lookup("C-AMS");

      AMS = (AmmoWeapon) lookup("AMS");
      SHS = (HeatSink) lookup("STD HEAT SINK");
      DHS = (HeatSink) lookup("DOUBLE HEAT SINK");
      ECM = lookup("GUARDIAN ECM");
      BAP = lookup("BEAGLE ACTIVE PROBE");
      CASE = lookup("C.A.S.E.");

      UAA = (Internal) lookup(UAA_ID);
      LAA = (Internal) lookup(LAA_ID);
      HA = (Internal) lookup(HA_ID);

      DYN_ARMOUR =
          new Internal("DYNAMIC ARMOUR", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
      DYN_STRUCT =
          new Internal(
              "DYNAMIC STRUCTURE", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
      FIX_ARMOUR =
          new Internal("FIXED ARMOUR", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
      FIX_STRUCT =
          new Internal("FIXED STRUCTURE", null, null, 0, 1, 0, HardPointType.NONE, 0, Faction.ANY);
    } catch (final NoSuchItemException e) {
      throw new RuntimeException(e);
    }
  }

  public static Engine getEngine(int aRating, Engine.EngineType aType, Faction aFaction)
      throws NoSuchItemException {
    final StringBuilder sb = new StringBuilder();
    if (aType == Engine.EngineType.XL && aFaction == Faction.CLAN) {
      sb.append("CLAN ");
    }

    if (aType == Engine.EngineType.LE) {
      sb.append("LIGHT");
    } else {
      sb.append(aType.name());
    }

    sb.append(" ENGINE ").append(aRating);
    return (Engine) lookup(sb.toString());
  }

  @SuppressWarnings("unchecked")
  // It is checked...
  public static <T extends Item> List<T> lookup(Class<T> aClass) {
    final List<T> ans = new ArrayList<>();
    for (final Item it : mwoIndex2item.values()) {
      if (aClass.isInstance(it)) {
        ans.add((T) it);
      }
    }
    return ans;
  }

  public static Item lookup(int aMwoIndex) throws NoSuchItemException {
    if (!mwoIndex2item.containsKey(aMwoIndex)) {
      throw new NoSuchItemException("No item with ID: " + aMwoIndex);
    }
    return mwoIndex2item.get(aMwoIndex);
  }

  public static Item lookup(final String aItemName) throws NoSuchItemException {
    final String key = canonize(aItemName);
    if (!localizedName2item.containsKey(key)) {
      if (!mwoName2item.containsKey(key)) {
        throw new NoSuchItemException("No item with name:" + aItemName);
      }
      return mwoName2item.get(key);
    }
    return localizedName2item.get(key);
  }

  private static String canonize(String aString) {
    return aString.toLowerCase();
  }

  private static void put(Item aItem) {
    mwoName2item.put(canonize(aItem.getKey()), aItem);
    localizedName2item.put(canonize(aItem.getName()), aItem);
    localizedName2item.put(canonize(aItem.getShortName()), aItem);
    if (aItem.getId() >= 0) {
      mwoIndex2item.put(aItem.getId(), aItem);
    }
  }
}

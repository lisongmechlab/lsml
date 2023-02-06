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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Enumerates all possible hard point types.
 *
 * @author Li Song
 */
@XStreamAlias("Type")
public enum HardPointType {
  ENERGY("E"),
  BALLISTIC("B"),
  MISSILE("M"),
  AMS("AMS"),
  ECM("ECM"),
  NONE("");

  private final String shortName;

  HardPointType(String aShortName) {
    shortName = aShortName;
  }

  public static HardPointType fromMwoType(String type) {
    return switch (type) {
      case "Energy" -> HardPointType.ENERGY;
      case "AMS" -> HardPointType.AMS;
      case "Ballistic" -> HardPointType.BALLISTIC;
      case "Missile" -> HardPointType.MISSILE;
      default -> throw new RuntimeException("Unknown hardpoint type!");
    };
  }

  public static HardPointType fromMwoType(int type) {
    return switch (type) {
      case 1 -> HardPointType.ENERGY;
      case 4 -> HardPointType.AMS;
      case 0 -> HardPointType.BALLISTIC;
      case 2 -> HardPointType.MISSILE;
      default -> throw new RuntimeException("Unknown hardpoint type!");
    };
  }

  public String shortName() {
    return shortName;
  }
}

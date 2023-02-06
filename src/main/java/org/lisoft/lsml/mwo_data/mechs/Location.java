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
package org.lisoft.lsml.mwo_data.mechs;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.List;

/**
 * Enumerates all possible locations for components.
 *
 * @author Li Song
 */
public enum Location {
  Head("Head", "head", "HD"), //
  LeftArm("Left Arm", "left_arm", "LA"), //
  LeftLeg("Left Leg", "left_leg", "LL"), //
  LeftTorso("Left Torso", "left_torso", "LT", true), //
  CenterTorso("Center Torso", "centre_torso", "CT", true), //
  RightTorso("Right Torso", "right_torso", "RT", true), //
  RightLeg("Right Leg", "right_leg", "RL"), //
  RightArm("Right Arm", "right_arm", "RA");

  public static final List<Location> MWO_EXPORT_ORDER =
      unmodifiableList(
          Arrays.asList(
              CenterTorso, RightTorso, LeftTorso, LeftArm, RightArm, LeftLeg, RightLeg, Head));
  public static final List<Location> RIGHT_TO_LEFT =
      unmodifiableList(
          Arrays.asList(
              RightArm, RightTorso, RightLeg, Head, CenterTorso, LeftTorso, LeftLeg, LeftArm));
  private final String longName;
  private final String mwoName;
  private final String mwoNameRear;
  private final String shortName;
  private final boolean twosided;

  Location(String aLongName, String aMwoName, String aShortName) {
    this(aLongName, aMwoName, aShortName, false);
  }

  Location(String aLongName, String aMwoName, String aShortName, boolean aTwosided) {
    longName = aLongName;
    shortName = aShortName;
    twosided = aTwosided;
    mwoName = aMwoName;
    mwoNameRear = mwoName + "_rear";
  }

  public static Location fromMwoName(String componentName) {
    for (final Location part : Location.values()) {
      if (part.mwoName.equals(componentName) || part.mwoNameRear.equals(componentName)) {
        return part;
      }
    }
    throw new RuntimeException("Unknown component in mech chassi! " + componentName);
  }

  public static boolean isRear(String aName) {
    return aName.endsWith("_rear");
  }

  public boolean isSideTorso() {
    return this == RightTorso || this == LeftTorso;
  }

  public boolean isTwoSided() {
    return twosided;
  }

  public String longName() {
    return longName;
  }

  public Location oppositeSide() {
    switch (this) {
      case LeftArm:
        return RightArm;
      case LeftLeg:
        return RightLeg;
      case LeftTorso:
        return RightTorso;
      case RightArm:
        return LeftArm;
      case RightLeg:
        return LeftLeg;
      case RightTorso:
        return LeftTorso;
      case CenterTorso: // Fall-through
      case Head: // Fall-through
      default:
        return null;
    }
  }

  public String shortName() {
    return shortName;
  }

  public String toMwoName() {
    return mwoName;
  }

  public String toMwoRearName() {
    return mwoNameRear;
  }
}

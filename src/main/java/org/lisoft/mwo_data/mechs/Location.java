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
      List.of(CenterTorso, RightTorso, LeftTorso, LeftArm, RightArm, LeftLeg, RightLeg, Head);
  public static final List<Location> RIGHT_TO_LEFT =
      List.of(RightArm, RightTorso, RightLeg, Head, CenterTorso, LeftTorso, LeftLeg, LeftArm);
  private final String longName;
  private final String mwoName;
  private final String mwoNameRear;
  private final String shortName;
  private final boolean twoSided;

  Location(String aLongName, String aMwoName, String aShortName) {
    this(aLongName, aMwoName, aShortName, false);
  }

  Location(String aLongName, String aMwoName, String aShortName, boolean aTwoSided) {
    longName = aLongName;
    shortName = aShortName;
    twoSided = aTwoSided;
    mwoName = aMwoName;
    mwoNameRear = mwoName + "_rear";
  }

  public static Location fromMwoName(String componentName) {
    for (final Location part : Location.values()) {
      if (part.mwoName.equals(componentName) || part.mwoNameRear.equals(componentName)) {
        return part;
      }
    }
    throw new RuntimeException("Unknown component in mech chassis! " + componentName);
  }

  public static boolean isRear(String aName) {
    return aName.endsWith("_rear");
  }

  public boolean isSideTorso() {
    return this == RightTorso || this == LeftTorso;
  }

  /**
   * A location is considered two-sided when there can be armour on the front and the back of the
   * location. This is true for CT, LT and RT.
   *
   * @return true if the location is two-sided.
   */
  public boolean isTwoSided() {
    return twoSided;
  }

  public String longName() {
    return longName;
  }

  /**
   * For locations that are not {@link #Head} or {@link #CenterTorso}, provide the mirror side. E.g.
   * for LT, return RT.
   *
   * @return The mirror, or opposite location of this {@link Location}.
   */
  public Location otherSide() {
    return switch (this) {
      case LeftArm -> RightArm;
      case LeftLeg -> RightLeg;
      case LeftTorso -> RightTorso;
      case RightArm -> LeftArm;
      case RightLeg -> LeftLeg;
      case RightTorso -> LeftTorso;
      case CenterTorso, Head -> throw new IllegalArgumentException(
          this.toMwoName() + " has no opposite side!");
    };
  }

  public String shortName() {
    return shortName;
  }

  public String toMwoName() {
    return mwoName;
  }
}

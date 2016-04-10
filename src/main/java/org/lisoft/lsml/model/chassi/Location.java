/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Li Song
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
//@formatter:on
package org.lisoft.lsml.model.chassi;

/**
 * Enumerates all possible locations for components.
 * 
 * @author Li Song
 */
public enum Location {
    Head("Head", "head", "HD"), LeftArm("Left Arm", "left_arm", "LA"), LeftLeg("Left Leg", "left_leg", "LL"), LeftTorso(
            "Left Torso", "left_torso", "LT",
            true), CenterTorso("Center Torso", "centre_torso", "CT", true), RightTorso("Right Torso", "right_torso",
                    "RT", true), RightLeg("Right Leg", "right_leg", "RL"), RightArm("Right Arm", "right_arm", "RA");

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

    private final String mwoName;
    private final String mwoNameRear;
    private final String shortName;
    private final String longName;
    private final boolean twosided;

    public String longName() {
        return longName;
    }

    public String shortName() {
        return shortName;
    }

    public boolean isTwoSided() {
        return twosided;
    }

    private final static Location[] right2left = new Location[] { Location.RightArm, Location.RightTorso,
            Location.RightLeg, Location.Head, Location.CenterTorso, Location.LeftTorso, Location.LeftLeg,
            Location.LeftArm };

    public static Location[] right2Left() {
        return right2left;
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
            default:
                return null;
        }
    }

    public static Location fromMwoName(String componentName) {
        for (Location part : Location.values()) {
            if (part.mwoName.equals(componentName) || part.mwoNameRear.equals(componentName)) {
                return part;
            }
        }
        throw new RuntimeException("Unknown component in mech chassi! " + componentName);
    }

    public boolean isSideTorso() {
        return this == RightTorso || this == LeftTorso;
    }

    public static boolean isRear(String aName) {
        return aName.endsWith("_rear");
    }

    public String toMwoName() {
        return mwoName;
    }

    public String toMwoRearName() {
        return mwoNameRear;
    }
}

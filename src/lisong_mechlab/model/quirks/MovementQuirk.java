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
package lisong_mechlab.model.quirks;

import lisong_mechlab.model.chassi.MovementModifier;
import lisong_mechlab.model.quirks.Quirks.QuirkBenefit;

/**
 * A base class for quirks that affect movement.
 * 
 * @author Li Song
 *
 */
public class MovementQuirk extends Quirk implements MovementModifier {
    public static enum Affects {
        TorsoAngleYawAdd, TorsoSpeedYawMult, TorsoAnglePitchAdd, TorsoSpeedPitchMult,

        ArmAngleYawAdd, ArmSpeedYawMult, ArmAnglePitchAdd, ArmSpeedPitchMult,

        TurnLerpLowSpeedMult, TurnLerpMidSpeedMult, TurnLerpHighSpeedMult, TurnLerpLowRateMult, TurnLerpMidRateMult, TurnLerpHighRateMult
    }

    private final Affects affects;

    /**
     * Creates a new movement quirk.
     * 
     * @param aName
     *            The human readable name of the quirk.
     * @param aValue
     *            The quirk value.
     * @param aAffects
     *            The attribute that this quirk affects.
     */
    public MovementQuirk(String aName, double aValue, Affects aAffects) {
        super(aName, aValue);
        affects = aAffects;
    }

    @Override
    public double extraTorsoYawMax(double aBase) {
        if (affects == Affects.TorsoAngleYawAdd)
            return value;
        return 0;
    }

    @Override
    public double extraTorsoYawSpeed(double aBase) {
        if (affects == Affects.TorsoSpeedYawMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTorsoPitchMax(double aBase) {
        if (affects == Affects.TorsoAnglePitchAdd)
            return value;
        return 0;
    }

    @Override
    public double extraTorsoPitchSpeed(double aBase) {
        if (affects == Affects.TorsoSpeedPitchMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraArmYawMax(double aBase) {
        if (affects == Affects.ArmAngleYawAdd)
            return value;
        return 0;
    }

    @Override
    public double extraArmYawSpeed(double aBase) {
        if (affects == Affects.ArmSpeedYawMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraArmPitchMax(double aBase) {
        if (affects == Affects.ArmAnglePitchAdd)
            return value;
        return 0;
    }

    @Override
    public double extraArmPitchSpeed(double aBase) {
        if (affects == Affects.ArmSpeedPitchMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTurnLerpLowSpeed(double aBase) {
        if (affects == Affects.TurnLerpLowSpeedMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTurnLerpMidSpeed(double aBase) {
        if (affects == Affects.TurnLerpMidSpeedMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTurnLerpHighSpeed(double aBase) {
        if (affects == Affects.TurnLerpHighSpeedMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTurnLerpLowRate(double aBase) {
        if (affects == Affects.TurnLerpLowRateMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTurnLerpMidRate(double aBase) {
        if (affects == Affects.TurnLerpMidRateMult)
            return value * aBase;
        return 0;
    }

    @Override
    public double extraTurnLerpHighRate(double aBase) {
        if (affects == Affects.TurnLerpHighRateMult)
            return value * aBase;
        return 0;
    }

    @Override
    public QuirkBenefit isPositiveGood() {
        return QuirkBenefit.POSITIVE_GOOD;
    }

    @Override
    protected void writeValue(StringBuilder aSB) {
        if (value > 0) {
            aSB.append("+");
        }
        switch (affects) {
            case ArmAnglePitchAdd:
            case TorsoAnglePitchAdd:
            case ArmAngleYawAdd:
            case TorsoAngleYawAdd:
                aSB.append(FORMAT.format(value));
                break;
            default:
                aSB.append(FORMAT.format(value * 100)).append("%");
        }
    }
}

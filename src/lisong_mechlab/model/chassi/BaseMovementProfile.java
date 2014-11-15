/*
 * @formatter:off
 * Li Song Mechlab - A 'mech building tool for PGI's MechWarrior: Online.
 * Copyright (C) 2013  Emily Björk
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
package lisong_mechlab.model.chassi;

import java.util.Collection;

import lisong_mechlab.model.quirks.Attribute;
import lisong_mechlab.model.quirks.Modifier;
import lisong_mechlab.model.quirks.ModifiersDB;

/**
 * This class contains the movement parameters for a chassis.
 * 
 * @author Emily Björk
 */
public class BaseMovementProfile implements MovementProfile {
    private final Attribute         maxMovementSpeed;
    private final Attribute         torsoTurnSpeedYaw;
    private final Attribute         torsoTurnSpeedPitch;
    private final Attribute         armTurnSpeedYaw;
    private final Attribute         armTurnSpeedPitch;
    private final Attribute         maxTorsoAngleYaw;
    private final Attribute         maxTorsoAnglePitc;
    private final Attribute         maxArmRotationYaw;
    private final Attribute         maxArmRotationPitch;
    private final Attribute         reverseSpeedMultiplier;
    private final Attribute         turnLerpLowSpeed;
    private final Attribute         turnLerpMidSpeed;
    private final Attribute         turnLerpHighSpeed;
    private final Attribute         turnLerpLowRate;
    private final Attribute         turnLerpMidRate;
    private final Attribute         turnLerpHighRate;
    private final MovementArchetype archetype;

    public BaseMovementProfile(double aMaxMovementSpeed, double aReverseSpeedMult, double aTorsoTurnSpeedYaw,
            double aTorsoTurnSpeedPitch, double aArmTurnSpeedYaw, double aArmTurnSpeedPitch, double aMaxTorsoAngleYaw,
            double aMaxTorsoAnglePitch, double aMaxArmRotationYaw, double aMaxArmRotationPitch,
            double aTurnLerpLowSpeed, double aTurnLerpMidSpeed, double aTurnLerpHighSpeed, double aTurnLerpLowRate,
            double aTurnLerpMidRate, double aTurnLerpHighRate, MovementArchetype aMovementArchetype) {
        maxMovementSpeed = new Attribute(aMaxMovementSpeed, ModifiersDB.SEL_MOVEMENT_MAX_SPEED);
        reverseSpeedMultiplier = new Attribute(aReverseSpeedMult, ModifiersDB.SEL_MOVEMENT_REVERSE_MUL);
        
        torsoTurnSpeedYaw = new Attribute(aTorsoTurnSpeedYaw, ModifiersDB.SEL_MOVEMENT_TORSO_SPEED, "yaw");
        torsoTurnSpeedPitch = new Attribute(aTorsoTurnSpeedPitch, ModifiersDB.SEL_MOVEMENT_TORSO_SPEED, "pitch");
        armTurnSpeedYaw = new Attribute(aArmTurnSpeedYaw, ModifiersDB.SEL_MOVEMENT_ARM_SPEED, "yaw");
        armTurnSpeedPitch = new Attribute(aArmTurnSpeedPitch, ModifiersDB.SEL_MOVEMENT_ARM_SPEED, "pitch");
        maxTorsoAngleYaw = new Attribute(aMaxTorsoAngleYaw, ModifiersDB.SEL_MOVEMENT_TORSO_ANGLE, "yaw");
        maxTorsoAnglePitc = new Attribute(aMaxTorsoAnglePitch, ModifiersDB.SEL_MOVEMENT_TORSO_ANGLE, "pitch");
        maxArmRotationYaw = new Attribute(aMaxArmRotationYaw, ModifiersDB.SEL_MOVEMENT_ARM_ANGLE, "yaw");
        maxArmRotationPitch = new Attribute(aMaxArmRotationPitch, ModifiersDB.SEL_MOVEMENT_ARM_ANGLE, "pitch");

        turnLerpLowSpeed = new Attribute(aTurnLerpLowSpeed, ModifiersDB.SEL_MOVEMENT_TURN_SPEED, "lowrate");
        turnLerpMidSpeed = new Attribute(aTurnLerpMidSpeed, ModifiersDB.SEL_MOVEMENT_TURN_SPEED, "midrate");
        turnLerpHighSpeed = new Attribute(aTurnLerpHighSpeed, ModifiersDB.SEL_MOVEMENT_TURN_SPEED, "highrate");
        turnLerpLowRate = new Attribute(aTurnLerpLowRate, ModifiersDB.SEL_MOVEMENT_TURN_RATE, "lowrate");
        turnLerpMidRate = new Attribute(aTurnLerpMidRate, ModifiersDB.SEL_MOVEMENT_TURN_RATE, "midrate");
        turnLerpHighRate = new Attribute(aTurnLerpHighRate, ModifiersDB.SEL_MOVEMENT_TURN_RATE, "highrate");

        archetype = aMovementArchetype;
    }

    @Override
    public MovementArchetype getMovementArchetype() {
        return archetype;
    }

    @Override
    public double getMaxMovementSpeed(Collection<Modifier> aModifiers) {
        return maxMovementSpeed.value(aModifiers);
    }

    @Override
    public double getReverseSpeedMultiplier(Collection<Modifier> aModifiers) {
        return reverseSpeedMultiplier.value(aModifiers);
    }

    @Override
    public double getTorsoYawMax(Collection<Modifier> aModifiers) {
        return maxTorsoAngleYaw.value(aModifiers);
    }

    @Override
    public double getTorsoYawSpeed(Collection<Modifier> aModifiers) {
        return torsoTurnSpeedYaw.value(aModifiers);
    }

    @Override
    public double getTorsoPitchMax(Collection<Modifier> aModifiers) {
        return maxTorsoAnglePitc.value(aModifiers);
    }

    @Override
    public double getTorsoPitchSpeed(Collection<Modifier> aModifiers) {
        return torsoTurnSpeedPitch.value(aModifiers);
    }

    @Override
    public double getArmYawMax(Collection<Modifier> aModifiers) {
        return maxArmRotationYaw.value(aModifiers);
    }

    @Override
    public double getArmYawSpeed(Collection<Modifier> aModifiers) {
        return armTurnSpeedYaw.value(aModifiers);
    }

    @Override
    public double getArmPitchMax(Collection<Modifier> aModifiers) {
        return maxArmRotationPitch.value(aModifiers);
    }

    @Override
    public double getArmPitchSpeed(Collection<Modifier> aModifiers) {
        return armTurnSpeedPitch.value(aModifiers);
    }

    @Override
    public double getTurnLerpLowSpeed(Collection<Modifier> aModifiers) {
        return turnLerpLowSpeed.value(aModifiers);
    }

    @Override
    public double getTurnLerpMidSpeed(Collection<Modifier> aModifiers) {
        return turnLerpMidSpeed.value(aModifiers);
    }

    @Override
    public double getTurnLerpHighSpeed(Collection<Modifier> aModifiers) {
        return turnLerpHighSpeed.value(aModifiers);
    }

    @Override
    public double getTurnLerpLowRate(Collection<Modifier> aModifiers) {
        return turnLerpLowRate.value(aModifiers);
    }

    @Override
    public double getTurnLerpMidRate(Collection<Modifier> aModifiers) {
        return turnLerpMidRate.value(aModifiers);
    }

    @Override
    public double getTurnLerpHighRate(Collection<Modifier> aModifiers) {
        return turnLerpHighRate.value(aModifiers);
    }
}

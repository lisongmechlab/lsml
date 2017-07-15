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
package org.lisoft.lsml.model.chassi;

import static org.lisoft.lsml.model.modifiers.ModifierDescription.SEL_MOVEMENT_ARM;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SEL_MOVEMENT_MAX_FWD_SPEED;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SEL_MOVEMENT_MAX_REV_SPEED;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SEL_MOVEMENT_TORSO;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SEL_MOVEMENT_TURN_RATE;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SEL_MOVEMENT_TURN_SPEED;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SPEC_MOVEMENT_PITCHANGLE;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SPEC_MOVEMENT_PITCHSPEED;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SPEC_MOVEMENT_YAWANGLE;
import static org.lisoft.lsml.model.modifiers.ModifierDescription.SPEC_MOVEMENT_YAWSPEED;

import java.util.Collection;

import org.lisoft.lsml.model.modifiers.Attribute;
import org.lisoft.lsml.model.modifiers.Modifier;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class contains the movement parameters for a chassis.
 *
 * @author Emily Björk
 */
public class BaseMovementProfile implements MovementProfile {
    private final Attribute maxMovementSpeed;
    private final Attribute torsoTurnSpeedYaw;
    private final Attribute torsoTurnSpeedPitch;
    private final Attribute armTurnSpeedYaw;
    private final Attribute armTurnSpeedPitch;
    private final Attribute maxTorsoAngleYaw;
    private final Attribute maxTorsoAnglePitch;
    private final Attribute maxArmRotationYaw;
    private final Attribute maxArmRotationPitch;
    private final Attribute reverseSpeedMultiplier;
    private final Attribute turnLerpLowSpeed;
    private final Attribute turnLerpMidSpeed;
    private final Attribute turnLerpHighSpeed;
    private final Attribute turnLerpLowRate;
    private final Attribute turnLerpMidRate;
    private final Attribute turnLerpHighRate;
    @XStreamAsAttribute
    private final MovementArchetype archetype;

    public BaseMovementProfile(double aMaxMovementSpeed, double aReverseSpeedMult, double aTorsoTurnSpeedYaw,
            double aTorsoTurnSpeedPitch, double aArmTurnSpeedYaw, double aArmTurnSpeedPitch, double aMaxTorsoAngleYaw,
            double aMaxTorsoAnglePitch, double aMaxArmRotationYaw, double aMaxArmRotationPitch,
            double aTurnLerpLowSpeed, double aTurnLerpMidSpeed, double aTurnLerpHighSpeed, double aTurnLerpLowRate,
            double aTurnLerpMidRate, double aTurnLerpHighRate, MovementArchetype aMovementArchetype) {
        maxMovementSpeed = new Attribute(aMaxMovementSpeed, SEL_MOVEMENT_MAX_FWD_SPEED);
        reverseSpeedMultiplier = new Attribute(aReverseSpeedMult, SEL_MOVEMENT_MAX_REV_SPEED);

        torsoTurnSpeedYaw = new Attribute(aTorsoTurnSpeedYaw, SEL_MOVEMENT_TORSO, SPEC_MOVEMENT_YAWSPEED);
        torsoTurnSpeedPitch = new Attribute(aTorsoTurnSpeedPitch, SEL_MOVEMENT_TORSO, SPEC_MOVEMENT_PITCHSPEED);
        maxTorsoAngleYaw = new Attribute(aMaxTorsoAngleYaw, SEL_MOVEMENT_TORSO, SPEC_MOVEMENT_YAWANGLE);
        maxTorsoAnglePitch = new Attribute(aMaxTorsoAnglePitch, SEL_MOVEMENT_TORSO, SPEC_MOVEMENT_PITCHANGLE);

        armTurnSpeedYaw = new Attribute(aArmTurnSpeedYaw, SEL_MOVEMENT_ARM, SPEC_MOVEMENT_YAWSPEED);
        armTurnSpeedPitch = new Attribute(aArmTurnSpeedPitch, SEL_MOVEMENT_ARM, SPEC_MOVEMENT_PITCHSPEED);
        maxArmRotationYaw = new Attribute(aMaxArmRotationYaw, SEL_MOVEMENT_ARM, SPEC_MOVEMENT_YAWANGLE);
        maxArmRotationPitch = new Attribute(aMaxArmRotationPitch, SEL_MOVEMENT_ARM, SPEC_MOVEMENT_PITCHANGLE);

        turnLerpLowSpeed = new Attribute(aTurnLerpLowSpeed, SEL_MOVEMENT_TURN_SPEED, "lowrate");
        turnLerpMidSpeed = new Attribute(aTurnLerpMidSpeed, SEL_MOVEMENT_TURN_SPEED, "midrate");
        turnLerpHighSpeed = new Attribute(aTurnLerpHighSpeed, SEL_MOVEMENT_TURN_SPEED, "highrate");
        turnLerpLowRate = new Attribute(aTurnLerpLowRate, SEL_MOVEMENT_TURN_RATE, "lowrate");
        turnLerpMidRate = new Attribute(aTurnLerpMidRate, SEL_MOVEMENT_TURN_RATE, "midrate");
        turnLerpHighRate = new Attribute(aTurnLerpHighRate, SEL_MOVEMENT_TURN_RATE, "highrate");

        archetype = aMovementArchetype;
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
    public double getArmYawMax(Collection<Modifier> aModifiers) {
        return maxArmRotationYaw.value(aModifiers);
    }

    @Override
    public double getArmYawSpeed(Collection<Modifier> aModifiers) {
        return armTurnSpeedYaw.value(aModifiers);
    }

    @Override
    public MovementArchetype getMovementArchetype() {
        return archetype;
    }

    @Override
    public double getReverseSpeedMultiplier(Collection<Modifier> aModifiers) {
        return reverseSpeedMultiplier.value(aModifiers);
    }

    @Override
    public double getSpeedFactor(Collection<Modifier> aModifiers) {
        return maxMovementSpeed.value(aModifiers);
    }

    @Override
    public double getTorsoPitchMax(Collection<Modifier> aModifiers) {
        return maxTorsoAnglePitch.value(aModifiers);
    }

    @Override
    public double getTorsoPitchSpeed(Collection<Modifier> aModifiers) {
        return torsoTurnSpeedPitch.value(aModifiers);
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
    public double getTurnLerpHighRate(Collection<Modifier> aModifiers) {
        return turnLerpHighRate.value(aModifiers);
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
    public double getTurnLerpLowSpeed(Collection<Modifier> aModifiers) {
        return turnLerpLowSpeed.value(aModifiers);
    }

    @Override
    public double getTurnLerpMidRate(Collection<Modifier> aModifiers) {
        return turnLerpMidRate.value(aModifiers);
    }

    @Override
    public double getTurnLerpMidSpeed(Collection<Modifier> aModifiers) {
        return turnLerpMidSpeed.value(aModifiers);
    }
}

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

import java.util.Collection;

import org.lisoft.lsml.model.modifiers.Modifier;

/**
 * This interface models the movement profile of any 'mech, clan or inner sphere. The values provided are base values
 * which need to be processed to give human readable quantities. The values match those found in the .mdf files.
 * 
 * @author Li Song
 */
public interface MovementProfile {
    double MAX_LEGGED_SPEED_KPH = 40.0;

    /**
     * The movement archetype determines how a mech behaves when going up a slope.
     * 
     * @return The movement archetype.
     */
    MovementArchetype getMovementArchetype();

    /**
     * This value is used to calculate the top speed of a mech as follows:
     * <p>
     * <code>topSpeed = PSM * K * ER / M</code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K =
     * {@link #getMaxMovementSpeed(Collection)}.
     * 
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * 
     * @return A modifier used to calculate the top speed of a loadout.
     */
    double getMaxMovementSpeed(Collection<Modifier> aModifiers);

    /**
     * This value is used to calculate the top reverse speed of a mech as follows:
     * <p>
     * <code>topReverseSpeed = PSM * topSpeed * R</code>
     * </p>
     * <p>
     * Where R = {@link #getReverseSpeedMultiplier(Collection)}, PSM = Pilot Skill Modifiers and topSpeed is calculated
     * as documented in {@link #getMaxMovementSpeed(Collection)}.
     * 
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return A modifier used to calculate the max reverse speed of a loadout.
     */
    double getReverseSpeedMultiplier(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The maximum, torso yaw angle in degrees.
     */
    double getTorsoYawMax(Collection<Modifier> aModifiers);

    /**
     * This value is used to calculate the top torso yaw speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>yawSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getTorsoYawSpeed(Collection)}
     * .
     * 
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return A modifier used to calculate the max torso yaw speed.
     */
    double getTorsoYawSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The maximum, torso pitch angle in degrees.
     */
    double getTorsoPitchMax(Collection<Modifier> aModifiers);

    /**
     * This value is used to calculate the top torso pitch speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>pitchSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K =
     * {@link #getTorsoPitchSpeed(Collection)}.
     * 
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return A modifier used to calculate the max torso pitch speed.
     */
    double getTorsoPitchSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The maximum, arm yaw angle in degrees. The arm yaw is relative to the torso orientation.
     */
    double getArmYawMax(Collection<Modifier> aModifiers);

    /**
     * This value is used to calculate the top arm yaw speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>yawSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getArmYawSpeed(Collection)}.
     * 
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return A modifier used to calculate the arm yaw speed.
     */
    double getArmYawSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The maximum, arm pitch angle in degrees. The arm pitch is relative to the torso orientation.
     */
    double getArmPitchMax(Collection<Modifier> aModifiers);

    /**
     * This value is used to calculate the top arm pitch speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>pitchSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getArmPitchSpeed(Collection)
     * )}.
     * 
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return A modifier used to calculate the arm pitch speed.
     */
    double getArmPitchSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The speed at which the low turn rate comes into effect.
     */
    double getTurnLerpLowSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The speed at which the middle turn rate comes into effect.
     */
    double getTurnLerpMidSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The speed at which the high turn rate comes into effect.
     */
    double getTurnLerpHighSpeed(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The rate at which the mech turns when it's moving at a low speed.
     */
    double getTurnLerpLowRate(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The rate at which the mech turns when it's moving at a middle speed.
     */
    double getTurnLerpMidRate(Collection<Modifier> aModifiers);

    /**
     * @param aModifiers
     *            A set of modifiers to apply to the base value.
     * @return The rate at which the mech turns when it's moving at a high speed.
     */
    double getTurnLerpHighRate(Collection<Modifier> aModifiers);

}

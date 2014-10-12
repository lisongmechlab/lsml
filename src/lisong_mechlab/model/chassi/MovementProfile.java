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

/**
 * This interface models the movement profile of any 'mech, clan or inner sphere. The values provided are base values
 * which need to be processed to give human readable quantities. The values match those found in the .mdf files.
 * 
 * @author Emily Björk
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
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getMaxMovementSpeed()}.
     * 
     * @return A modifier used to calculate the top speed of a loadout.
     */
    double getMaxMovementSpeed();

    /**
     * This value is used to calculate the top reverse speed of a mech as follows:
     * <p>
     * <code>topReverseSpeed = PSM * topSpeed * R</code>
     * </p>
     * <p>
     * Where R = {@link #getReverseSpeedMultiplier()}, PSM = Pilot Skill Modifiers and topSpeed is calculated as
     * documented in {@link #getMaxMovementSpeed()}.
     * 
     * @return A modifier used to calculate the max reverse speed of a loadout.
     */
    double getReverseSpeedMultiplier();

    /**
     * @return The maximum raw (unmodified), torso yaw angle in degrees.
     */
    double getTorsoYawMax();

    /**
     * This value is used to calculate the top torso yaw speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>yawSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getTorsoYawSpeed()}.
     * 
     * @return A modifier used to calculate the max torso yaw speed.
     */
    double getTorsoYawSpeed();

    /**
     * @return The maximum raw (unmodified), torso pitch angle in degrees.
     */
    double getTorsoPitchMax();

    /**
     * This value is used to calculate the top torso pitch speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>pitchSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getTorsoPitchSpeed()}.
     * 
     * @return A modifier used to calculate the max torso pitch speed.
     */
    double getTorsoPitchSpeed();

    /**
     * @return The maximum raw (unmodified), arm yaw angle in degrees. The arm yaw is relative to the torso orientation.
     */
    double getArmYawMax();

    /**
     * This value is used to calculate the top arm yaw speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>yawSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getArmYawSpeed()}.
     * 
     * @return A modifier used to calculate the arm yaw speed.
     */
    double getArmYawSpeed();

    /**
     * @return The maximum raw (unmodified), arm pitch angle in degrees. The arm pitch is relative to the torso
     *         orientation.
     */
    double getArmPitchMax();

    /**
     * This value is used to calculate the top arm pitch speed (in degrees/second) of a mech as follows:
     * <p>
     * <code>pitchSpeedDegS = PSM * ER / M * K </code>
     * </p>
     * <p>
     * Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = {@link #getArmPitchSpeed()}.
     * 
     * @return A modifier used to calculate the arm pitch speed.
     */
    double getArmPitchSpeed();

    /**
     * @return The speed at which the low turn rate comes into effect.
     */
    double getTurnLerpLowSpeed();

    /**
     * @return The speed at which the middle turn rate comes into effect.
     */
    double getTurnLerpMidSpeed();

    /**
     * @return The speed at which the high turn rate comes into effect.
     */
    double getTurnLerpHighSpeed();

    /**
     * @return The rate at which the mech turns when it's moving at a low speed.
     */
    double getTurnLerpLowRate();

    /**
     * @return The rate at which the mech turns when it's moving at a middle speed.
     */
    double getTurnLerpMidRate();

    /**
     * @return The rate at which the mech turns when it's moving at a high speed.
     */
    double getTurnLerpHighRate();

}

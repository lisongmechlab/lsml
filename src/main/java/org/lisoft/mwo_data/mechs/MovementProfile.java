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

import java.util.Collection;
import org.lisoft.mwo_data.modifiers.Modifier;

/**
 * This interface models the movement profile of any Mech, clan or inner sphere. The values provided
 * are base values which need to be processed to give human-readable quantities. The values match
 * those found in the .mdf files.
 *
 * @author Li Song
 */
public interface MovementProfile {

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The maximum, arm pitch angle in degrees. The arm pitch is relative to the torso
   *     orientation.
   */
  double getArmPitchMax(Collection<Modifier> aModifiers);

  /**
   * This value is used to calculate the top arm pitch speed (in degrees/second) of a mech as
   * follows:
   *
   * <p><code>pitchSpeedDegS = PSM * ER / M * K </code>
   *
   * <p>Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = armPitchSpeed.
   *
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return A modifier used to calculate the arm pitch speed.
   */
  double getArmPitchSpeed(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The maximum, arm yaw angle in degrees. The arm yaw is relative to the torso
   *     orientation.
   */
  double getArmYawMax(Collection<Modifier> aModifiers);

  /**
   * This value is used to calculate the top arm yaw speed (in degrees/second) of a mech as follows:
   *
   * <p><code>yawSpeedDegS = PSM * ER / M * K </code>
   *
   * <p>Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = armYawSpeed.
   *
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return A modifier used to calculate the arm yaw speed.
   */
  double getArmYawSpeed(Collection<Modifier> aModifiers);

  /**
   * The movement archetype determines how a mech behaves when going up a slope.
   *
   * @return The movement archetype.
   */
  MovementArchetype getMovementArchetype();

  /**
   * This value is used to calculate the top reverse speed of a mech as follows:
   *
   * <p><code>topReverseSpeed = PSM * topSpeed * R</code>
   *
   * <p>Where R = ReverseSpeedMultiplier, PSM = Pilot Skill Modifiers and topSpeed is calculated as
   * documented in {@link #getSpeedFactor(Collection)}.
   *
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return A modifier used to calculate the max reverse speed of a loadout.
   */
  double getReverseSpeedMultiplier(Collection<Modifier> aModifiers);

  /**
   * This value is used to calculate the top speed of a mech as follows:
   *
   * <p><code>topSpeed = PSM * K * ER / M</code>
   *
   * <p>Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = SpeedFactor.
   *
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return A modifier used to calculate the top speed of a loadout.
   */
  double getSpeedFactor(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The maximum, torso pitch angle in degrees.
   */
  double getTorsoPitchMax(Collection<Modifier> aModifiers);

  /**
   * This value is used to calculate the top torso pitch speed (in degrees/second) of a mech as
   * follows:
   *
   * <p><code>pitchSpeedDegS = PSM * ER / M * K </code>
   *
   * <p>Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = torsoPitchSpeed.
   *
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return A modifier used to calculate the max torso pitch speed.
   */
  double getTorsoPitchSpeed(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The maximum, torso yaw angle in degrees.
   */
  double getTorsoYawMax(Collection<Modifier> aModifiers);

  /**
   * This value is used to calculate the top torso yaw speed (in degrees/second) of a mech as
   * follows:
   *
   * <p><code>yawSpeedDegS = PSM * ER / M * K </code>
   *
   * <p>Where ER = Engine Rating, M = Max Tons, PSM = Pilot Skill Modifiers and K = torsoYawSpeed.
   *
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return A modifier used to calculate the max torso yaw speed.
   */
  double getTorsoYawSpeed(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The rate at which the mech turns when it's moving at a high speed.
   */
  double getTurnLerpHighRate(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The speed at which the high turn rate comes into effect.
   */
  double getTurnLerpHighSpeed(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The rate at which the mech turns when it's moving at a low speed.
   */
  double getTurnLerpLowRate(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The speed at which the low turn rate comes into effect.
   */
  double getTurnLerpLowSpeed(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The rate at which the mech turns when it's moving at a middle speed.
   */
  double getTurnLerpMidRate(Collection<Modifier> aModifiers);

  /**
   * @param aModifiers A set of modifiers to apply to the base value.
   * @return The speed at which the middle turn rate comes into effect.
   */
  double getTurnLerpMidSpeed(Collection<Modifier> aModifiers);

  /**
   * The different movement archetypes.
   *
   * <p><a
   * href="http://mwomercs.com/forums/topic/124437-new-battlemech-movement-behavior/">Reference</a>
   *
   * @author Li Song
   */
  enum MovementArchetype {
    Tiny(40.0),
    Small(35.0),
    Medium(30.0),
    Large(25.0),
    Huge(20.0);

    private final double slowDownDeg;

    MovementArchetype(double aSlowDownAngle) {
      slowDownDeg = aSlowDownAngle;
    }

    /**
     * @return The maximal slope angle (in degrees) this archetype can climb.
     */
    public static double getMaxSlope() {
      return 45.0;
    }

    /**
     * Calculates the slow-down factor at any angle for this movement archetype.
     *
     * @param aAngle The angle (in degrees) at which to get the slow-down.
     * @return A factor where 1.0 means full speed and 0.0 means standstill.
     */
    public double getSlowDownFactor(double aAngle) {
      if (aAngle < slowDownDeg) {
        return 1.0;
      } else if (aAngle > getMaxSlope()) {
        return 0.0;
      } else {
        return (getMaxSlope() - aAngle) / (getMaxSlope() - getSlowDownSlope());
      }
    }

    /**
     * @return The maximal slope angle (in degrees) after which the mech starts to slow down.
     */
    public double getSlowDownSlope() {
      return slowDownDeg;
    }
  }
}

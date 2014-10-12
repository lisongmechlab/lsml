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
package lisong_mechlab.model.chassi;

/**
 * This interface models objects that can affect a mech's movement.
 * 
 * @author Li Song
 */
public interface MovementModifier {

    /**
     * Determines how much should be added to the torso max yaw angle.
     * 
     * @param aBase
     *            The base value of the torso yaw angle.
     * @see lisong_mechlab.model.chassi.MovementProfile#getTorsoYawMax()
     * @return The additive bonus to yaw angle.
     */
    double extraTorsoYawMax(double aBase);

    /**
     * Determines how much should be added to the torso yaw speed.
     * 
     * @param aBase
     *            The base value of the torso yaw speed.
     * @see lisong_mechlab.model.chassi.MovementProfile#getTorsoYawSpeed()
     * @return The additive bonus to yaw speed.
     */
    double extraTorsoYawSpeed(double aBase);

    /**
     * Determines how much should be added to the torso max pitch angle.
     * 
     * @param aBase
     *            The base value of the torso pitch angle.
     * @see lisong_mechlab.model.chassi.MovementProfile#getTorsoPitchMax()
     * @return The additive bonus to pitch angle.
     */
    double extraTorsoPitchMax(double aBase);

    /**
     * Determines how much should be added to the torso pitch speed.
     * 
     * @param aBase
     *            The base value of the torso pitch speed.
     * @see lisong_mechlab.model.chassi.MovementProfile#getTorsoPitchSpeed()
     * @return The additive bonus to pitch speed.
     */
    double extraTorsoPitchSpeed(double aBase);

    /**
     * Determines how much should be added to the arm max yaw angle.
     * 
     * @param aBase
     *            The base value of the arm yaw angle.
     * @see lisong_mechlab.model.chassi.MovementProfile#getArmYawMax()
     * @return The additive bonus to yaw angle.
     */
    double extraArmYawMax(double aBase);

    /**
     * Determines how much should be added to the arm max yaw speed.
     * 
     * @param aBase
     *            The base value of the arm yaw speed.
     * @see lisong_mechlab.model.chassi.MovementProfile#getArmYawSpeed()
     * @return The additive bonus to yaw speed.
     */
    double extraArmYawSpeed(double aBase);

    /**
     * Determines how much should be added to the arm max pitch angle.
     * 
     * @param aBase
     *            The base value of the arm pitch angle.
     * @see lisong_mechlab.model.chassi.MovementProfile#getArmPitchMax()
     * @return The additive bonus to pitch angle.
     */
    double extraArmPitchMax(double aBase);

    /**
     * Determines how much should be added to the arm max pitch speed.
     * 
     * @param aBase
     *            The base value of the arm pitch speed.
     * @see lisong_mechlab.model.chassi.MovementProfile#getArmPitchSpeed()
     * @return The additive bonus to pitch speed.
     */
    double extraArmPitchSpeed(double aBase);

    /**
     * Determines how much should be added to the attribute.
     * 
     * @param aBase
     *            The base value of the attribute.
     * @return The additive bonus to the attribute.
     */
    double extraTurnLerpLowSpeed(double aBase);

    /**
     * Determines how much should be added to the attribute.
     * 
     * @param aBase
     *            The base value of the attribute.
     * @return The additive bonus to the attribute.
     */
    double extraTurnLerpMidSpeed(double aBase);

    /**
     * Determines how much should be added to the attribute.
     * 
     * @param aBase
     *            The base value of the attribute.
     * @return The additive bonus to the attribute.
     */
    double extraTurnLerpHighSpeed(double aBase);

    /**
     * Determines how much should be added to the attribute.
     * 
     * @param aBase
     *            The base value of the attribute.
     * @return The additive bonus to the attribute.
     */
    double extraTurnLerpLowRate(double aBase);

    /**
     * Determines how much should be added to the attribute.
     * 
     * @param aBase
     *            The base value of the attribute.
     * @return The additive bonus to the attribute.
     */
    double extraTurnLerpMidRate(double aBase);

    /**
     * Determines how much should be added to the attribute.
     * 
     * @param aBase
     *            The base value of the attribute.
     * @return The additive bonus to the attribute.
     */
    double extraTurnLerpHighRate(double aBase);
}

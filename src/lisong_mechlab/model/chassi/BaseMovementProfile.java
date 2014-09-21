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

import lisong_mechlab.mwo_data.helpers.MdfMovementTuning;

/**
 * This class contains the static movement parameters for a chassis.
 * 
 * @author Emily Björk
 */
public class BaseMovementProfile implements MovementProfile {
	private final MdfMovementTuning	mdf;		// TODO: Do not use the parsing classes, even though this is essentially
												// a
												// wrapper.
	private final MovementArchetype	archetype;

	public BaseMovementProfile(MdfMovementTuning aMdf) {
		mdf = aMdf;
		archetype = MovementArchetype.valueOf(mdf.MovementArchetype);
	}

	@Override
	public MovementArchetype getMovementArchetype() {
		return archetype;
	}

	@Override
	public double getMaxMovementSpeed() {
		return mdf.MaxMovementSpeed;
	}

	@Override
	public double getReverseSpeedMultiplier() {
		return mdf.ReverseSpeedMultiplier;
	}

	@Override
	public double getTorsoYawMax() {
		return mdf.MaxTorsoAngleYaw;
	}

	@Override
	public double getTorsoYawSpeed() {
		return mdf.TorsoTurnSpeedYaw;
	}

	@Override
	public double getTorsoPitchMax() {
		return mdf.MaxTorsoAnglePitch;
	}

	@Override
	public double getTorsoPitchSpeed() {
		return mdf.TorsoTurnSpeedPitch;
	}

	@Override
	public double getArmYawMax() {
		return mdf.MaxArmRotationYaw;
	}

	@Override
	public double getArmYawSpeed() {
		return mdf.ArmTurnSpeedYaw;
	}

	@Override
	public double getArmPitchMax() {
		return mdf.MaxArmRotationPitch;
	}

	@Override
	public double getArmPitchSpeed() {
		return mdf.ArmTurnSpeedPitch;
	}

	@Override
	public double getTurnLerpLowSpeed() {
		return mdf.TurnLerpLowSpeed;
	}

	@Override
	public double getTurnLerpMidSpeed() {
		return mdf.TurnLerpMidSpeed;
	}

	@Override
	public double getTurnLerpHighSpeed() {
		return mdf.TurnLerpHighSpeed;
	}

	@Override
	public double getTurnLerpLowRate() {
		return mdf.TurnLerpLowRate;
	}

	@Override
	public double getTurnLerpMidRate() {
		return mdf.TurnLerpMidRate;
	}

	@Override
	public double getTurnLerpHighRate() {
		return mdf.TurnLerpHighRate;
	}
}

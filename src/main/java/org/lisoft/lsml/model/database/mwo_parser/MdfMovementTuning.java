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
package org.lisoft.lsml.model.database.mwo_parser;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.lsml.model.chassi.BaseMovementProfile;

public class MdfMovementTuning {
  @XStreamAsAttribute public double ArmTurnSpeedPitch;
  @XStreamAsAttribute public double ArmTurnSpeedYaw;
  @XStreamAsAttribute public double MaxArmRotationPitch;
  @XStreamAsAttribute public double MaxArmRotationYaw;
  @XStreamAsAttribute public double MaxMovementSpeed;
  @XStreamAsAttribute public double MaxTorsoAnglePitch;
  @XStreamAsAttribute public double MaxTorsoAngleYaw;
  @XStreamAsAttribute public String MovementArchetype = "Huge";
  @XStreamAsAttribute public double ReverseSpeedMultiplier;
  @XStreamAsAttribute public double TorsoTurnSpeedPitch;
  @XStreamAsAttribute public double TorsoTurnSpeedYaw;
  @XStreamAsAttribute public double TurnLerpHighRate;
  @XStreamAsAttribute public double TurnLerpHighSpeed;
  @XStreamAsAttribute public String TurnLerpLowRate;
  @XStreamAsAttribute public double TurnLerpLowSpeed;
  @XStreamAsAttribute public double TurnLerpMidRate;
  @XStreamAsAttribute public double TurnLerpMidSpeed;

  public BaseMovementProfile asMovementProfile() {
    double TurnLerpLowRateFixForBug747 = Double.parseDouble(TurnLerpLowRate.replace("..", "."));
    return new BaseMovementProfile(
        MaxMovementSpeed,
        ReverseSpeedMultiplier,
        TorsoTurnSpeedYaw,
        TorsoTurnSpeedPitch,
        ArmTurnSpeedYaw,
        ArmTurnSpeedPitch,
        MaxTorsoAngleYaw,
        MaxTorsoAnglePitch,
        MaxArmRotationYaw,
        MaxArmRotationPitch,
        TurnLerpLowSpeed,
        TurnLerpMidSpeed,
        TurnLerpHighSpeed,
        TurnLerpLowRateFixForBug747,
        TurnLerpMidRate,
        TurnLerpHighRate,
        org.lisoft.lsml.model.chassi.MovementArchetype.valueOf(MovementArchetype));
  }
}

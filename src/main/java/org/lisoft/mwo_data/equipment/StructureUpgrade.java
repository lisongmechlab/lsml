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
package org.lisoft.mwo_data.equipment;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.lisoft.mwo_data.Faction;
import org.lisoft.mwo_data.mechs.Chassis;

/**
 * Represents an upgrade to a Mech's internal structure.
 *
 * @author Li Song
 */
public class StructureUpgrade extends Upgrade {
  @XStreamAsAttribute private final int extraSlots;
  @XStreamAsAttribute private final double internalStructurePct;

  public StructureUpgrade(
      String aUiName,
      String aUiDesc,
      String aMwoName,
      int aMwoId,
      Faction aFaction,
      int aExtraSlots,
      double aStructurePct) {
    super(aUiName, aUiDesc, aMwoName, aMwoId, aFaction);
    extraSlots = aExtraSlots;
    internalStructurePct = aStructurePct;
  }

  /**
   * @return The number of dynamic slots that this upgrade requires to be distributed over the
   *     components.
   */
  public int getDynamicSlots() {
    return extraSlots;
  }

  /**
   * Calculates the mass of the internal structure of a mech of the given chassis.
   *
   * @param aChassis The chassis to calculate the internal structure mass for.
   * @return The mass of the internal structure.
   */
  public double getStructureMass(Chassis aChassis) {
    final double ans = aChassis.getMassMax() * internalStructurePct;
    return Math.round(10 * ans / 5) * 0.5;
  }

  @Override
  public UpgradeType getType() {
    return UpgradeType.STRUCTURE;
  }
}

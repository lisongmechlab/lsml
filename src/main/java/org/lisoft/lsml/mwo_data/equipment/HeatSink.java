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
package org.lisoft.lsml.mwo_data.equipment;

import org.lisoft.lsml.mwo_data.Faction;
import org.lisoft.lsml.mwo_data.mechs.HardPointType;
import org.lisoft.lsml.mwo_data.mechs.Upgrades;

public class HeatSink extends Module {
  private final double capacity;
  private final double dissipation;
  private final double engineCapacity;
  private final double engineDissipation;

  public HeatSink(
      String aName,
      String aDesc,
      String aMwoName,
      int aMwoId,
      int aSlots,
      double aTons,
      HardPointType aHardPointType,
      double aHP,
      Faction aFaction,
      double aDissipation,
      double aEngineDissipation,
      double aCapacity,
      double aEngineCapacity) {
    super(
        aName,
        aDesc,
        aMwoName,
        aMwoId,
        aSlots,
        aTons,
        aHardPointType,
        aHP,
        aFaction,
        null,
        null,
        null);
    dissipation = aDissipation;
    engineDissipation = aEngineDissipation;
    capacity = aCapacity;
    engineCapacity = aEngineCapacity;
  }

  /**
   * @return The heat capacity of one heat sink internal to the engine.
   */
  public double getCapacity() {
    return capacity;
  }

  /**
   * @return Heat per second removed from the Mech by one of these heat sinks (external to engine).
   */
  public double getDissipation() {
    return dissipation;
  }

  /**
   * The heat capacity of heat sinks if they are internal to the engine. Does not apply for heat
   * sinks that are in "engine slots" but rather built into the engine.
   *
   * @return The heat capacity of one heat sink internal to the engine.
   */
  public double getEngineCapacity() {
    return engineCapacity;
  }

  /**
   * @return Heat per second removed from the Mech by one of these heat sinks (internal to engine).
   */
  public double getEngineDissipation() {
    return engineDissipation;
  }

  @Override
  public boolean isCompatible(Upgrades aUpgrades) {
    return aUpgrades.getHeatSink().getHeatSinkType() == this;
  }

  public boolean isDouble() {
    return getSlots() > 1; // Account for double precision
  }
}

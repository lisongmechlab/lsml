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
package org.lisoft.lsml.mwo_data.mechs;

import org.lisoft.lsml.mwo_data.equipment.ArmourUpgrade;
import org.lisoft.lsml.mwo_data.equipment.GuidanceUpgrade;
import org.lisoft.lsml.mwo_data.equipment.HeatSinkUpgrade;
import org.lisoft.lsml.mwo_data.equipment.StructureUpgrade;

/**
 * This subclass of {@link Upgrades} can be mutated by setters.
 *
 * @author Li Song
 */
public class UpgradesMutable extends Upgrades {

  /**
   * Creates a new {@link UpgradesMutable}.
   *
   * @param aArmour The initial {@link ArmourUpgrade}.
   * @param aStructure The initial {@link StructureUpgrade}.
   * @param aGuidance The initial {@link GuidanceUpgrade}.
   * @param aHeatSinks The initial {@link HeatSinkUpgrade}.
   */
  public UpgradesMutable(
      ArmourUpgrade aArmour,
      StructureUpgrade aStructure,
      GuidanceUpgrade aGuidance,
      HeatSinkUpgrade aHeatSinks) {
    super(aArmour, aStructure, aGuidance, aHeatSinks);
  }

  /**
   * Copy constructor, performs a deep copy.
   *
   * @param aUpgrades An {@link UpgradesMutable} object to copy.
   */
  public UpgradesMutable(UpgradesMutable aUpgrades) {
    super(aUpgrades);
  }

  /**
   * Changes the armour type.
   *
   * <p>This is package visibility as it is only intended to be modified by the Op* classes.
   *
   * @param aArmourUpgrade The new {@link ArmourUpgrade}.
   */
  public void setArmour(ArmourUpgrade aArmourUpgrade) {
    armourType = aArmourUpgrade;
  }

  /**
   * Changes the heat sink type.
   *
   * <p>This is package visibility as it is only intended to be modified by the Op* classes.
   *
   * @param aHeatsinkUpgrade The new {@link HeatSinkUpgrade}.
   */
  public void setHeatSink(HeatSinkUpgrade aHeatsinkUpgrade) {
    heatSinkType = aHeatsinkUpgrade;
  }

  /**
   * Changes the internal structure type.
   *
   * <p>This is package visibility as it is only intended to be modified by the Op* classes.
   *
   * @param aStructureUpgrade The new {@link StructureUpgrade}.
   */
  public void setStructure(StructureUpgrade aStructureUpgrade) {
    structureType = aStructureUpgrade;
  }
}

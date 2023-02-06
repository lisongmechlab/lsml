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

/**
 * Base class that models modules that can be equipped in module slots on mechs.
 *
 * @author Li Song
 */
public class Consumable extends MwoObject {
  @XStreamAsAttribute private final ConsumableType type;

  /**
   * Creates a new {@link Consumable}.
   *
   * @param aUiName The name that is displayed in the user interface.
   * @param aShortUiName A shorthand name of the object that can be used when space is limited.
   * @param aMwoName The name of the module in the MWO data files.
   * @param aMwoId The ID of the module in the MWO data files.
   * @param aDescription The human-readable description of the module.
   * @param aFaction The required faction for this module.
   * @param aType The type of the consumable.
   */
  public Consumable(
      String aUiName,
      String aShortUiName,
      String aDescription,
      String aMwoName,
      int aMwoId,
      Faction aFaction,
      ConsumableType aType) {
    super(aUiName, aShortUiName, aDescription, aMwoName, aMwoId, aFaction);
    type = aType;
  }

  public ConsumableType getType() {
    return type;
  }

  @Override
  public String toString() {
    return getName();
  }

  /**
   * All the different types of consumables present in MWO.
   *
   * @author Li Song
   */
  public enum ConsumableType {
    STRATEGIC_STRIKE,
    COOLANT_FLUSH,
    UAV,
    UNKNOWN;

    /**
     * Determines the consumable type from the MWO equipType string.
     *
     * @param aEquipType The string from the data file's equipType field.
     * @return A {@link ConsumableType}.
     */
    public static ConsumableType fromMwo(String aEquipType) {
      return switch (aEquipType.toLowerCase()) {
        case "strategicstrike" -> STRATEGIC_STRIKE;
        case "uav" -> UAV;
        case "coolantflush" -> COOLANT_FLUSH;
        default -> UNKNOWN;
      };
    }
  }
}

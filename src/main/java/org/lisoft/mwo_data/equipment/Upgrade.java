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

import org.lisoft.mwo_data.Faction;

/**
 * Base class for all upgrades for Mechs.
 *
 * @author Li Song
 */
public abstract class Upgrade extends MwoObject {
  protected Upgrade(String aUiName, String aUiDesc, String aMwoName, int aMwoId, Faction aFaction) {
    super(aUiName.replace(" TYPE", ""), shorten(aUiName), aUiDesc, aMwoName, aMwoId, aFaction);
  }

  /**
   * @return The {@link UpgradeType} of this upgrade.
   */
  public abstract UpgradeType getType();

  private static String shorten(String aUiName) {
    String shortName = aUiName.replace("CLAN ", "C-");
    shortName = shortName.replace("ENDO-STEEL ", "ES-");
    shortName = shortName.replace("FERRO-FIBROUS ", "FF-");
    shortName = shortName.replace(" TYPE", "");
    return shortName;
  }

  public enum UpgradeType {
    ARMOUR,
    STRUCTURE,
    HEATSINK,
    ARTEMIS;

    public static UpgradeType fromMwo(String aMwoType) {
      if ("ARMOR".equalsIgnoreCase(aMwoType)) {
        return ARMOUR;
      }
      return valueOf(aMwoType.toUpperCase());
    }
  }
}

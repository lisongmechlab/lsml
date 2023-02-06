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

/**
 * Enumerates the different chassis types
 *
 * @author Li Song
 */
public enum ChassisVariant {
  HERO,
  NORMAL,
  SPECIAL;

  public static ChassisVariant fromString(String aChassis, String aVariant) {
    // Special case: PGI introduced some loyalty mech versions of chassis for which
    // there is no normal version. These chassis are counted as hero.
    if (aChassis.contains("EXE-C(L)")
        || aChassis.contains("NVA-D(L)")
        || aChassis.contains("WVR-7D(L)")
        || aChassis.contains("CDA-3F(L)")
        || aChassis.contains("ZEU-9S2(L)")) {
      return HERO;
    }

    if (aVariant == null || aVariant.isEmpty()) {
      // Either normal or special
      if (aChassis.contains("(")) {
        return SPECIAL; // Treat all non-normal and non-heroes as special.
      }
      return NORMAL;
    }

    final String lowerCase = aVariant.toLowerCase();
    if ("hero".equals(lowerCase)) {
      return HERO;
    }
    return ChassisVariant.SPECIAL;
  }

  /**
   * @return <code>true</code> if this is a special variant (not hero or normal mech).
   */
  public boolean isVariation() {
    return !(this == HERO || this == NORMAL);
  }
}

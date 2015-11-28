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
package org.lisoft.lsml.model.chassi;

/**
 * Enumerates the different chassis types
 * 
 * @author Emily Björk
 */
public enum ChassisVariant {
    HERO, NORMAL, SPECIAL;

    public static ChassisVariant fromString(String aChassis, String aVariant) {
        if (aVariant == null || aVariant.isEmpty()) {
            // Either normal or special
            if (aChassis.contains("(")) {
                return SPECIAL; // Treat all non-normal and non-heroes as special.
            }
            return NORMAL;
        }

        String lowerCase = aVariant.toLowerCase();
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

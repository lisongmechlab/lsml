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
 * Enumerates the different chassis types
 * 
 * @author Li Song
 */
public enum ChassisVariant {
    HERO, NORMAL, CHAMPION, SARAH, FOUNDER, PHOENIX;

    public static ChassisVariant fromString(String aChassis, String aVariant) {
        if (null == aVariant)
            return NORMAL;
        
        // Some chassis are marked as champion even though they don't have a base version just to give
        // them a C-bill bonus. We treat these as normal mechs.
        if(!aChassis.contains("(")){
            return NORMAL;
        }
        
        String s = aVariant.toLowerCase();
        for (ChassisVariant variant : values()) {
            if (s.equals(variant.toString().toLowerCase()))
                return variant;
        }
        return NORMAL;
    }

    /**
     * @return <code>true</code> if this is a special variant (not hero or normal mech).
     */
    public boolean isVariation() {
        return !(this == HERO || this == NORMAL);
    }
}
